package com.studyquest.app.ui.auth

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.studyquest.app.StudyQuestApplication
import kotlinx.coroutines.launch

/**
 * Android 전용 LoginScreen wrapper
 */
@Composable
fun AndroidLoginScreen(
    onLoginSuccess: () -> Unit,
    onSignupClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val application = remember {
        context.applicationContext as? StudyQuestApplication
            ?: throw IllegalStateException("StudyQuestApplication not initialized")
    }
    val authRepository = remember { application.authRepository }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LoginScreen(
        onLoginClick = { email, password, autoLogin ->
            scope.launch {
                Log.d("AndroidLoginScreen", "로그인 시도 - email: $email, autoLogin: $autoLogin")
                isLoading = true
                errorMessage = null

                val result = authRepository.login(email, password, autoLogin)
                result.onSuccess {
                    Log.d("AndroidLoginScreen", "✅ 로그인 성공!")
                    isLoading = false
                    onLoginSuccess()
                }.onFailure { error ->
                    Log.e("AndroidLoginScreen", "❌ 로그인 실패: ${error.message}")
                    // JSON에서 message 필드만 추출
                    val rawMessage = error.message ?: "로그인에 실패했습니다."
                    val messagePattern = """"message"\s*:\s*"([^"]+)"""".toRegex()
                    val match = messagePattern.find(rawMessage)
                    errorMessage = match?.groupValues?.getOrNull(1)?.replace("\\n", "\n") ?: rawMessage
                    isLoading = false
                }
            }
        },
        onSignupClick = onSignupClick,
        errorMessage = errorMessage,
        isLoading = isLoading
    )
}

/**
 * Android 전용 SignupScreen wrapper
 */
@Composable
fun AndroidSignupScreen(
    onSignupSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val application = remember {
        context.applicationContext as? StudyQuestApplication
            ?: throw IllegalStateException("StudyQuestApplication not initialized")
    }
    val authRepository = remember { application.authRepository }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    SignupScreen(
        onSignupClick = { email, password, nickname, name, birthdate, gender, phone, address, mbti ->
            scope.launch {
                Log.d("AndroidSignupScreen", "회원가입 시도 - email: $email, nickname: $nickname")
                isLoading = true
                errorMessage = null

                val result = authRepository.signup(
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

                result.onSuccess { userId ->
                    Log.d("AndroidSignupScreen", "✅ 회원가입 성공! userId: $userId")

                    // 회원가입 성공 후 자동 로그인 시도
                    Log.d("AndroidSignupScreen", "자동 로그인 시도 중...")
                    val loginResult = authRepository.login(email, password, autoLogin = true)

                    loginResult.onSuccess {
                        Log.d("AndroidSignupScreen", "✅ 자동 로그인 성공!")
                        isLoading = false
                        onSignupSuccess()
                    }.onFailure { loginError ->
                        Log.e("AndroidSignupScreen", "❌ 자동 로그인 실패: ${loginError.message}")
                        // 자동 로그인 실패해도 회원가입은 성공했으므로 다음 화면으로 이동
                        isLoading = false
                        onSignupSuccess()
                    }
                }.onFailure { error ->
                    Log.e("AndroidSignupScreen", "❌ 회원가입 실패: ${error.message}")
                    // JSON에서 message 필드만 추출
                    val rawMessage = error.message ?: "회원가입에 실패했습니다."
                    val messagePattern = """"message"\s*:\s*"([^"]+)"""".toRegex()
                    val match = messagePattern.find(rawMessage)
                    errorMessage = match?.groupValues?.getOrNull(1)?.replace("\\n", "\n") ?: rawMessage
                    isLoading = false
                }
            }
        },
        onBackClick = onBackClick,
        errorMessage = errorMessage,
        isLoading = isLoading
    )
}
