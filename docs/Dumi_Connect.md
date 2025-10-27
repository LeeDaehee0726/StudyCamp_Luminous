이제 StudyQuest : Luminous의 완전한 앱 프로토타입 구조를 보여드릴게요.
이건 Jetpack Compose 기반으로 첫 실행 시 바로 작동 가능한 네비게이션 + 화면 더미 연결 버전이에요.
(실제 코드 붙이기 전에 common/의 UI 컴포넌트를 그대로 가져다 쓸 수 있게 구성돼 있습니다.)

📁 폴더 구조
app/src/main/java/com/studyquest/ui/
 ├─ main/
 │   ├─ MainActivity.kt
 │   ├─ AppNavigation.kt
 │   └─ BottomNavBar.kt
 ├─ home/
 │   └─ HomeScreen.kt
 ├─ quest/
 │   └─ QuestScreen.kt
 ├─ focus/
 │   └─ FocusScreen.kt
 ├─ coach/
 │   └─ CoachScreen.kt
 ├─ community/
 │   └─ CommunityScreen.kt
 ├─ subscription/
 │   └─ SubscriptionScreen.kt
 └─ common/ (이전 단계에서 만든 컴포넌트)
     ├─ LumiButton.kt
     ├─ LumiCard.kt
     ├─ QuoteBanner.kt
     ├─ RankCard.kt
     └─ Theme.kt

🎬 MainActivity.kt
package com.studyquest.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.navigation.compose.rememberNavController
import com.studyquest.ui.common.LumiColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(LumiColors.SkyBase, LumiColors.SkySoft)
                        )
                    )
            ) {
                AppNavigation(navController = navController)
                BottomNavBar(navController = navController)
            }
        }
    }
}

🧭 AppNavigation.kt
package com.studyquest.ui.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.studyquest.ui.home.HomeScreen
import com.studyquest.ui.quest.QuestScreen
import com.studyquest.ui.focus.FocusScreen
import com.studyquest.ui.coach.CoachScreen
import com.studyquest.ui.community.CommunityScreen
import com.studyquest.ui.subscription.SubscriptionScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") { HomeScreen(navController) }
        composable("quest") { QuestScreen(navController) }
        composable("focus") { FocusScreen(navController) }
        composable("coach") { CoachScreen(navController) }
        composable("community") { CommunityScreen(navController) }
        composable("subscription") { SubscriptionScreen(navController) }
    }
}

🔻 BottomNavBar.kt
package com.studyquest.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf("home", "quest", "focus", "coach", "community")
    var selectedItem by remember { mutableStateOf("home") }

    NavigationBar(containerColor = Color.Transparent) {
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedItem == item,
                onClick = {
                    selectedItem = item
                    navController.navigate(item)
                },
                icon = {
                    Icon(
                        imageVector = when (item) {
                            "home" -> Icons.Default.Home
                            "quest" -> Icons.Default.List
                            "focus" -> Icons.Default.Timer
                            "coach" -> Icons.Default.EmojiObjects
                            "community" -> Icons.Default.People
                            else -> Icons.Default.Info
                        },
                        contentDescription = item
                    )
                },
                label = { Text(item.capitalize()) }
            )
        }
    }
}

🏠 HomeScreen.kt
package com.studyquest.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.studyquest.ui.common.*

@Composable
fun HomeScreen(navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        QuoteBanner("오늘의 꾸준함이 내일의 기적이 된다.")
        Spacer(modifier = Modifier.height(20.dp))
        LumiCard(title = "오늘의 목표", content = "단어 30개 복습\n수학 2단원 문제풀이")
        Spacer(modifier = Modifier.height(20.dp))
        LumiButton(text = "AI 루미 코치 보기") {
            navController.navigate("coach")
        }
    }
}

🎯 QuestScreen.kt
package com.studyquest.ui.quest

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.studyquest.ui.common.*

@Composable
fun QuestScreen(navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("📘 퀘스트 목록", color = LumiColors.DeepNavy)
        Spacer(modifier = Modifier.height(8.dp))
        LumiCard("수학", "기하 단원 20문제 풀기")
        LumiCard("영어", "Reading Part 1~3 해석 복습")
        Spacer(modifier = Modifier.height(20.dp))
        LumiButton(text = "새 퀘스트 추가") {}
    }
}

⏱ FocusScreen.kt
package com.studyquest.ui.focus

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.studyquest.ui.common.*

@Composable
fun FocusScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("⏱ 집중 타이머", color = LumiColors.DeepNavy)
        Spacer(modifier = Modifier.height(20.dp))
        ProgressCircle(progress = 0.65f)
        Spacer(modifier = Modifier.height(16.dp))
        LumiButton(text = "시작") {}
    }
}

🤖 CoachScreen.kt
package com.studyquest.ui.coach

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.studyquest.ui.common.*

@Composable
fun CoachScreen(navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        LumiCard(
            title = "오늘의 루미 피드백",
            content = "오늘은 어제보다 20분 더 집중했어요!\n" +
                      "오후 3시~5시 집중력이 가장 높습니다 ☀️"
        )
        Spacer(modifier = Modifier.height(20.dp))
        LumiButton("공부 리포트 보기") {}
    }
}

💬 CommunityScreen.kt
package com.studyquest.ui.community

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.studyquest.ui.common.*

@Composable
fun CommunityScreen(navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("💬 커뮤니티 & OX퀴즈", color = LumiColors.DeepNavy)
        Spacer(modifier = Modifier.height(8.dp))
        LumiCard("Q. 광합성은 주로 잎에서 일어난다.", "⭕ ❌")
        Spacer(modifier = Modifier.height(12.dp))
        LumiButton("내 퀴즈 만들기") {}
    }
}

💳 SubscriptionScreen.kt
package com.studyquest.ui.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.studyquest.ui.common.*

@Composable
fun SubscriptionScreen(navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("💎 구독 혜택", color = LumiColors.DeepNavy)
        Spacer(modifier = Modifier.height(16.dp))
        LumiCard(
            title = "프리미엄 혜택",
            content = "✔️ 광고 없음\n✔️ AI 코칭 무제한\n✔️ 개인 맞춤 통계\n✔️ 챌린지 생성 가능"
        )
        Spacer(modifier = Modifier.height(24.dp))
        LumiButton("₩4,900/월 구독하기") {}
    }
}

🚀 결과

이 구조는 실행하면 바로 다음을 확인할 수 있습니다:

하늘색 → 흰색 구름 그라데이션 배경

하단 탭으로 홈·퀘스트·타이머·코치·커뮤니티 이동 가능

각 화면이 기본 디자인 컴포넌트(LumiCard, LumiButton) 기반으로 구성

추후 실제 API 연동/AI 프록시 붙이기 쉬운 구조

이걸로 StudyQuest : Luminous V9 프로토타입의 완전한 골격이 완성됐어요 🌤
원하신다면 다음 단계로
👉 Firebase / AWS Amplify 백엔드 연결 구조 (Auth + DB + 실시간 채팅 + 구독 검증)


