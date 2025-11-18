package com.studyquest.app.data.repository

import android.util.Log
import com.studyquest.app.data.local.SecurePreferences
import com.studyquest.app.network.api.AuthApi
import com.studyquest.app.network.model.AuthResponse
import com.studyquest.app.network.model.ErrorResponse
import com.studyquest.app.network.model.LoginRequest
import com.studyquest.app.network.model.RefreshTokenRequest
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import kotlinx.serialization.json.Json
import kotlin.math.max

class AuthRepository(
    private val authApi: AuthApi,
    private val securePreferences: SecurePreferences
) {
    companion object {
        private const val TAG = "AuthRepository"
        private const val TOKEN_EXPIRY_BUFFER_MS = 5_000L

        private val json = Json { ignoreUnknownKeys = true }
    }

    private var sessionRefreshToken: String? = securePreferences.getRefreshToken()

    // HTTP 에러에서 친절한 메시지 추출
    private suspend fun extractErrorMessage(e: Exception): String {
        return when (e) {
            is ResponseException -> {
                try {
                    val errorBody = e.response.body<String>()
                    Log.d(TAG, "에러 응답 본문: $errorBody")
                    val errorResponse = json.decodeFromString<ErrorResponse>(errorBody)
                    // message 필드가 있으면 사용, 없으면 error 필드 사용
                    errorResponse.message?.takeIf { it.isNotBlank() } ?: errorResponse.error
                } catch (parseError: Exception) {
                    Log.e(TAG, "에러 메시지 파싱 실패", parseError)
                    // 파싱 실패 시 에러 메시지에서 JSON의 message 부분만 추출 시도
                    val message = e.message ?: "알 수 없는 오류가 발생했습니다."
                    // "message":"..." 패턴 찾기
                    val messagePattern = """"message"\s*:\s*"([^"]+)"""".toRegex()
                    val match = messagePattern.find(message)
                    match?.groupValues?.getOrNull(1)?.replace("\\n", "\n") ?: "알 수 없는 오류가 발생했습니다."
                }
            }
            else -> e.message ?: "알 수 없는 오류가 발생했습니다."
        }
    }

    // 로그인
    suspend fun login(email: String, password: String, autoLogin: Boolean): Result<AuthResponse> {
        return try {
            Log.d(TAG, "login() 시작 - email: $email, autoLogin: $autoLogin")
            val response = authApi.login(LoginRequest(email, password))
            Log.d(TAG, "login() API 성공 - userId: ${response.userId}")

            securePreferences.setAutoLoginEnabled(autoLogin)
            persistAuthResponse(response)

            Result.success(response)
        } catch (e: Exception) {
            val errorMessage = extractErrorMessage(e)
            Log.e(TAG, "login() 실패 - $errorMessage", e)
            Result.failure(Exception(errorMessage))
        }
    }

    // 토큰 갱신
    suspend fun refreshToken(): Result<AuthResponse> {
        Log.d(TAG, "refreshToken() 시작")
        val refreshToken = resolveRefreshToken()

        if (refreshToken == null) {
            Log.e(TAG, "refreshToken() 실패 - Refresh Token이 없음")
            return Result.failure(Exception("No refresh token found"))
        }

        Log.d(TAG, "Refresh Token 존재 확인 - 길이: ${refreshToken.length}")

        return try {
            Log.d(TAG, "Refresh Token API 호출 중...")
            val response = authApi.refreshToken(RefreshTokenRequest(refreshToken))
            Log.d(TAG, "Refresh Token API 성공")

            persistAuthResponse(response)

            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "refreshToken() 실패 - 저장된 인증 정보 삭제", e)
            // Refresh Token이 만료된 경우 저장된 정보 삭제
            securePreferences.clearAuth()
            sessionRefreshToken = null
            Result.failure(e)
        }
    }

    // 자동 로그인 시도
    suspend fun autoLogin(): Result<Boolean> {
        Log.d(TAG, "========== autoLogin() 시작 ==========")

        val autoLoginEnabled = securePreferences.isAutoLoginEnabled()
        Log.d(TAG, "자동 로그인 설정 여부: $autoLoginEnabled")

        if (!autoLoginEnabled) {
            Log.d(TAG, "자동 로그인이 비활성화되어 있음 - 종료")
            return Result.success(false)
        }

        return try {
            Log.d(TAG, "refreshToken() 호출 시도...")
            val result = refreshToken()

            if (result.isSuccess) {
                Log.d(TAG, "========== autoLogin() 성공 ==========")
                Result.success(true)
            } else {
                Log.e(TAG, "refreshToken() 실패 - autoLogin() 실패")
                Result.success(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "autoLogin() 예외 발생", e)
            Result.success(false)
        }
    }

    // 현재 Access Token 가져오기
    fun getAccessToken(): String? {
        return securePreferences.getAccessToken()
    }

    // 로그아웃
    fun logout() {
        securePreferences.clearAuth()
        sessionRefreshToken = null
    }

    // 로그인 상태 확인
    fun isLoggedIn(): Boolean {
        return securePreferences.getAccessToken() != null
    }

    // 회원가입
    suspend fun signup(
        email: String,
        password: String,
        nickname: String,
        name: String,
        birthdate: String,
        gender: String,
        phone: String,
        address: String,
        mbti: String
    ): Result<String> {
        return try {
            Log.d(TAG, "signup() 시작 - email: $email, nickname: $nickname")
            val request = com.studyquest.app.network.model.SignupRequest(
                email = email,
                password = password,
                nickname = nickname,
                name = name,
                birthdate = birthdate,
                gender = gender,
                phone = phone,
                address = address,
                mbti = mbti
            )
            val response = authApi.signup(request)
            Log.d(TAG, "signup() API 성공 - userId: ${response.userId}")

            Result.success(response.userId)
        } catch (e: Exception) {
            val errorMessage = extractErrorMessage(e)
            Log.e(TAG, "signup() 실패 - $errorMessage", e)
            Result.failure(Exception(errorMessage))
        }
    }

    private fun persistAuthResponse(response: AuthResponse) {
        val expiresAtMillis = calculateExpiryTimestamp(response.expiresIn)
        securePreferences.saveAccessToken(response.accessToken, expiresAtMillis)
        response.userId?.let { securePreferences.saveUserId(it) }

        if (response.refreshToken.isNotBlank()) {
            sessionRefreshToken = response.refreshToken
            securePreferences.saveRefreshToken(response.refreshToken)
        } else if (sessionRefreshToken == null) {
            sessionRefreshToken = securePreferences.getRefreshToken()
        }
    }

    private fun calculateExpiryTimestamp(expiresInSeconds: Int): Long {
        val expiresInMillis = max(0, expiresInSeconds) * 1000L
        val adjusted = max(0L, expiresInMillis - TOKEN_EXPIRY_BUFFER_MS)
        return System.currentTimeMillis() + adjusted
    }

    private fun resolveRefreshToken(): String? {
        if (sessionRefreshToken.isNullOrBlank()) {
            sessionRefreshToken = securePreferences.getRefreshToken()
        }
        return sessionRefreshToken
    }

}
