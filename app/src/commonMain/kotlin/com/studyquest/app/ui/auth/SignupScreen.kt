package com.studyquest.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignupScreen(
    onSignupClick: (
        email: String,
        password: String,
        nickname: String,
        name: String,
        birthdate: String,
        gender: String,
        phone: String,
        address: String,
        mbti: String
    ) -> Unit,
    onBackClick: () -> Unit,
    errorMessage: String? = null,
    isLoading: Boolean = false
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    // 생년월일 분리 필드
    var birthYear by remember { mutableStateOf("") }
    var birthMonth by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }

    var selectedGender by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // 주소 간소화
    var addressCity by remember { mutableStateOf("") }
    var addressDistrict by remember { mutableStateOf("") }

    var mbti by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordConfirmVisible by remember { mutableStateOf(false) }
    var privacyPolicyAgreed by remember { mutableStateOf(false) }

    // Sky 테마 그라데이션
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFB2E4FA),
            Color(0xFFD4F1FF),
            Color(0xFFEAF8FF)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 헤더
            Text(
                text = "회원가입",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A5F)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Study Camp와 함께 학습을 시작하세요",
                fontSize = 14.sp,
                color = Color(0xFF1E3A5F).copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 회원가입 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.75f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // 필수 정보
                    Text(
                        text = "필수 정보",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0288D1)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 이메일
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("이메일") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, null, tint = Color(0xFF6DD5FA))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6DD5FA),
                            unfocusedBorderColor = Color(0xFFB2E4FA),
                            focusedLabelColor = Color(0xFF6DD5FA),
                            cursorColor = Color(0xFF6DD5FA)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 비밀번호
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("비밀번호") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null, tint = Color(0xFF6DD5FA))
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    null,
                                    tint = Color(0xFF6DD5FA)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6DD5FA),
                            unfocusedBorderColor = Color(0xFFB2E4FA),
                            focusedLabelColor = Color(0xFF6DD5FA),
                            cursorColor = Color(0xFF6DD5FA)
                        )
                    )

                    // 비밀번호 규칙 안내
                    if (password.isNotBlank()) {
                        val hasMinLength = password.length >= 8
                        val hasUpperCase = password.any { it.isUpperCase() }
                        val hasLowerCase = password.any { it.isLowerCase() }
                        val hasDigit = password.any { it.isDigit() }
                        val hasSpecial = password.any { !it.isLetterOrDigit() }

                        Column(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
                            PasswordRequirement("최소 8자 이상", hasMinLength)
                            PasswordRequirement("대문자 포함", hasUpperCase)
                            PasswordRequirement("소문자 포함", hasLowerCase)
                            PasswordRequirement("숫자 포함", hasDigit)
                            PasswordRequirement("특수문자 포함", hasSpecial)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 비밀번호 확인
                    OutlinedTextField(
                        value = passwordConfirm,
                        onValueChange = { passwordConfirm = it },
                        label = { Text("비밀번호 확인") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null, tint = Color(0xFF6DD5FA))
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordConfirmVisible = !passwordConfirmVisible }) {
                                Icon(
                                    if (passwordConfirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    null,
                                    tint = Color(0xFF6DD5FA)
                                )
                            }
                        },
                        visualTransformation = if (passwordConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        isError = password.isNotBlank() && passwordConfirm.isNotBlank() && password != passwordConfirm,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6DD5FA),
                            unfocusedBorderColor = Color(0xFFB2E4FA),
                            focusedLabelColor = Color(0xFF6DD5FA),
                            cursorColor = Color(0xFF6DD5FA)
                        )
                    )
                    if (password.isNotBlank() && passwordConfirm.isNotBlank() && password != passwordConfirm) {
                        Text(
                            text = "비밀번호가 일치하지 않습니다",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 닉네임
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("닉네임") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, null, tint = Color(0xFF6DD5FA))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6DD5FA),
                            unfocusedBorderColor = Color(0xFFB2E4FA),
                            focusedLabelColor = Color(0xFF6DD5FA),
                            cursorColor = Color(0xFF6DD5FA)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 이름
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("이름") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, null, tint = Color(0xFF6DD5FA))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6DD5FA),
                            unfocusedBorderColor = Color(0xFFB2E4FA),
                            focusedLabelColor = Color(0xFF6DD5FA),
                            cursorColor = Color(0xFF6DD5FA)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 생년월일 (3개 필드)
                    Text(
                        text = "생년월일",
                        fontSize = 14.sp,
                        color = Color(0xFF1E3A5F).copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 연도 (YYYY)
                        OutlinedTextField(
                            value = birthYear,
                            onValueChange = {
                                if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                    birthYear = it
                                }
                            },
                            modifier = Modifier.weight(2f),
                            singleLine = true,
                            placeholder = { Text("YYYY", color = Color(0xFF1E3A5F).copy(alpha = 0.3f)) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6DD5FA),
                                unfocusedBorderColor = Color(0xFFB2E4FA),
                                cursorColor = Color(0xFF6DD5FA)
                            )
                        )

                        Text("-", fontSize = 20.sp, color = Color(0xFF1E3A5F))

                        // 월 (MM)
                        OutlinedTextField(
                            value = birthMonth,
                            onValueChange = {
                                if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                                    birthMonth = it
                                }
                            },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true,
                            placeholder = { Text("MM", color = Color(0xFF1E3A5F).copy(alpha = 0.3f)) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6DD5FA),
                                unfocusedBorderColor = Color(0xFFB2E4FA),
                                cursorColor = Color(0xFF6DD5FA)
                            )
                        )

                        Text("-", fontSize = 20.sp, color = Color(0xFF1E3A5F))

                        // 일 (DD)
                        OutlinedTextField(
                            value = birthDay,
                            onValueChange = {
                                if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                                    birthDay = it
                                }
                            },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true,
                            placeholder = { Text("DD", color = Color(0xFF1E3A5F).copy(alpha = 0.3f)) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6DD5FA),
                                unfocusedBorderColor = Color(0xFFB2E4FA),
                                cursorColor = Color(0xFF6DD5FA)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 성별
                    Text(
                        text = "성별",
                        fontSize = 14.sp,
                        color = Color(0xFF1E3A5F).copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("남성", "여성", "기타").forEach { gender ->
                            FilterChip(
                                selected = selectedGender == gender,
                                onClick = { selectedGender = gender },
                                label = {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(gender)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6DD5FA),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF1E3A5F)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 선택 정보
                    Text(
                        text = "선택 정보 (선택사항)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0288D1)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 전화번호 (필수, 자유 형식)
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("전화번호") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, null, tint = Color(0xFF6DD5FA))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        placeholder = { Text("국가별 형식 자유", color = Color(0xFF1E3A5F).copy(alpha = 0.3f)) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6DD5FA),
                            unfocusedBorderColor = Color(0xFFB2E4FA),
                            focusedLabelColor = Color(0xFF6DD5FA),
                            cursorColor = Color(0xFF6DD5FA)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 주소 - 시/군/구
                    OutlinedTextField(
                        value = addressCity,
                        onValueChange = { addressCity = it },
                        label = { Text("시/군/구 (선택)") },
                        leadingIcon = {
                            Icon(Icons.Default.Home, null, tint = Color(0xFF6DD5FA))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        placeholder = { Text("예: 강남구", color = Color(0xFF1E3A5F).copy(alpha = 0.3f)) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6DD5FA),
                            unfocusedBorderColor = Color(0xFFB2E4FA),
                            focusedLabelColor = Color(0xFF6DD5FA),
                            cursorColor = Color(0xFF6DD5FA)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 주소 - 읍/면/동
                    OutlinedTextField(
                        value = addressDistrict,
                        onValueChange = { addressDistrict = it },
                        label = { Text("읍/면/동 (선택)") },
                        leadingIcon = {
                            Icon(Icons.Default.Home, null, tint = Color(0xFF6DD5FA))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        placeholder = { Text("예: 삼성동", color = Color(0xFF1E3A5F).copy(alpha = 0.3f)) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6DD5FA),
                            unfocusedBorderColor = Color(0xFFB2E4FA),
                            focusedLabelColor = Color(0xFF6DD5FA),
                            cursorColor = Color(0xFF6DD5FA)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // MBTI
                    OutlinedTextField(
                        value = mbti,
                        onValueChange = { mbti = it },
                        label = { Text("MBTI (선택)") },
                        leadingIcon = {
                            Icon(Icons.Default.Star, null, tint = Color(0xFF6DD5FA))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        placeholder = { Text("예: ENFP", color = Color(0xFF1E3A5F).copy(alpha = 0.3f)) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6DD5FA),
                            unfocusedBorderColor = Color(0xFFB2E4FA),
                            focusedLabelColor = Color(0xFF6DD5FA),
                            cursorColor = Color(0xFF6DD5FA)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 개인정보 처리방침 동의
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = privacyPolicyAgreed,
                            onCheckedChange = { privacyPolicyAgreed = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF6DD5FA),
                                uncheckedColor = Color(0xFFB2E4FA),
                                checkmarkColor = Color.White
                            )
                        )

                        val annotatedText = buildAnnotatedString {
                            append("(필수) ")
                            pushStringAnnotation(tag = "privacy", annotation = "https://leedaehee0726.github.io/StudyCamp_Luminous/docs/privacy-policy.html")
                            withStyle(style = SpanStyle(
                                color = Color(0xFF6DD5FA),
                                textDecoration = TextDecoration.Underline
                            )) {
                                append("개인정보 처리방침")
                            }
                            pop()
                            append("에 동의합니다")
                        }

                        ClickableText(
                            text = annotatedText,
                            style = LocalTextStyle.current.copy(
                                fontSize = 14.sp,
                                color = Color(0xFF1E3A5F).copy(alpha = 0.8f)
                            ),
                            onClick = { offset ->
                                annotatedText.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                                    .firstOrNull()?.let { annotation ->
                                        // TODO: 웹뷰로 개인정보 처리방침 페이지 열기
                                        // 현재는 체크박스만 토글
                                        privacyPolicyAgreed = !privacyPolicyAgreed
                                    }
                            }
                        )
                    }

                    // 에러 메시지
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 회원가입 버튼
                    Button(
                        onClick = {
                            val birthdate = "$birthYear-${birthMonth.padStart(2, '0')}-${birthDay.padStart(2, '0')}"
                            val address = if (addressCity.isNotBlank() && addressDistrict.isNotBlank()) {
                                "$addressCity $addressDistrict"
                            } else if (addressCity.isNotBlank()) {
                                addressCity
                            } else if (addressDistrict.isNotBlank()) {
                                addressDistrict
                            } else {
                                ""
                            }

                            // 비밀번호 검증
                            val hasMinLength = password.length >= 8
                            val hasUpperCase = password.any { it.isUpperCase() }
                            val hasLowerCase = password.any { it.isLowerCase() }
                            val hasDigit = password.any { it.isDigit() }
                            val hasSpecial = password.any { !it.isLetterOrDigit() }
                            val isPasswordValid = hasMinLength && hasUpperCase && hasLowerCase && hasDigit && hasSpecial

                            val isValid = email.isNotBlank() &&
                                    password.isNotBlank() &&
                                    password == passwordConfirm &&
                                    isPasswordValid &&
                                    nickname.isNotBlank() &&
                                    name.isNotBlank() &&
                                    birthYear.length == 4 &&
                                    birthMonth.length == 2 &&
                                    birthDay.length == 2 &&
                                    selectedGender.isNotBlank() &&
                                    phone.isNotBlank() &&
                                    privacyPolicyAgreed

                            if (isValid) {
                                onSignupClick(
                                    email,
                                    password,
                                    nickname,
                                    name,
                                    birthdate,
                                    selectedGender,
                                    phone,
                                    address,
                                    mbti.ifBlank { "" }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = run {
                            val hasMinLength = password.length >= 8
                            val hasUpperCase = password.any { it.isUpperCase() }
                            val hasLowerCase = password.any { it.isLowerCase() }
                            val hasDigit = password.any { it.isDigit() }
                            val hasSpecial = password.any { !it.isLetterOrDigit() }
                            val isPasswordValid = hasMinLength && hasUpperCase && hasLowerCase && hasDigit && hasSpecial

                            !isLoading &&
                                    email.isNotBlank() &&
                                    password.isNotBlank() &&
                                    password == passwordConfirm &&
                                    isPasswordValid &&
                                    nickname.isNotBlank() &&
                                    name.isNotBlank() &&
                                    birthYear.length == 4 &&
                                    birthMonth.length == 2 &&
                                    birthDay.length == 2 &&
                                    selectedGender.isNotBlank() &&
                                    phone.isNotBlank() &&
                                    privacyPolicyAgreed
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6DD5FA),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFB2E4FA).copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "회원가입",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 뒤로 가기 버튼
                    TextButton(
                        onClick = onBackClick,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "이미 계정이 있으신가요? 로그인",
                            fontSize = 14.sp,
                            color = Color(0xFF6DD5FA),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PasswordRequirement(text: String, isMet: Boolean) {
    // 충족되지 않은 것만 빨간색으로 표시
    if (!isMet) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color(0xFFE57373),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                color = Color(0xFFE57373)
            )
        }
    }
}
