package com.studyquest.app.ui.coach

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Composable
fun CoachScreen() {
    val records = rememberFocusRecords()
    var timerState by rememberSaveable { mutableStateOf(FocusTimerState.Idle) }
    var sessionAnchor by rememberSaveable { mutableStateOf<Long?>(null) }
    var sessionStartInstant by rememberSaveable { mutableStateOf<Long?>(null) }
    var accumulatedMillis by rememberSaveable { mutableStateOf(0L) }
    var displayMillis by rememberSaveable { mutableStateOf(0L) }
    var startPage by rememberSaveable { mutableStateOf<Int?>(null) }
    var pageDialogState by remember { mutableStateOf<FocusPageDialogState?>(null) }

    val tickerRunning = timerState == FocusTimerState.Running && sessionAnchor != null
    LaunchedEffect(tickerRunning, sessionAnchor, accumulatedMillis) {
        if (!tickerRunning) return@LaunchedEffect
        while (tickerRunning) {
            val now = Clock.System.now().toEpochMilliseconds()
            val segment = now - (sessionAnchor ?: now)
            displayMillis = accumulatedMillis + segment
            delay(16L)
        }
    }

    val today = remember { currentDate() }
    val totalMinutes = records.sumOf { (it.durationMillis / 60000.0).toInt() }
    val title = remember(totalMinutes) { resolveTitle(totalMinutes) }
    val quote = remember(today) { dailyQuoteFor(today) }
    val atmospherePresets = remember { focusAtmospherePresets }
    val timerText = remember(displayMillis) { formatTimer(displayMillis) }

    fun openStartPageDialog(onAfter: (() -> Unit)? = null) {
        pageDialogState = FocusPageDialogState(
            title = "시작 페이지 입력",
            label = "오늘 시작 페이지",
            confirmLabel = "확인",
            initialValue = startPage,
            onConfirm = {
                startPage = it
                onAfter?.invoke()
            }
        )
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(statusBarPadding)
            .imePadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { FocusHeader(title = title, totalMinutes = totalMinutes, quote = quote) }
            item {
                FocusTimerCard(
                    timerText = timerText,
                    startPage = startPage,
                    timerState = timerState,
                    onStartPageClick = { openStartPageDialog() },
                    onStart = {
                        val now = Clock.System.now().toEpochMilliseconds()
                        sessionStartInstant = now
                        sessionAnchor = now
                        accumulatedMillis = 0L
                        displayMillis = 0L
                        timerState = FocusTimerState.Running
                        if (startPage == null) {
                            openStartPageDialog()
                        }
                    },
                    onPauseResume = {
                        if (timerState == FocusTimerState.Running) {
                            val now = Clock.System.now().toEpochMilliseconds()
                            accumulatedMillis += now - (sessionAnchor ?: now)
                            sessionAnchor = null
                            displayMillis = accumulatedMillis
                            timerState = FocusTimerState.Paused
                        } else if (timerState == FocusTimerState.Paused) {
                            sessionAnchor = Clock.System.now().toEpochMilliseconds()
                            timerState = FocusTimerState.Running
                        }
                    },
                    onStop = {
                        if (sessionStartInstant == null) return@FocusTimerCard
                        fun openEndDialog() {
                            pageDialogState = FocusPageDialogState(
                                title = "종료 페이지 입력",
                                label = "마친 페이지",
                                confirmLabel = "기록",
                                onConfirm = { endPage ->
                                    val now = Clock.System.now().toEpochMilliseconds()
                                    val totalMillis = if (timerState == FocusTimerState.Running && sessionAnchor != null) {
                                        accumulatedMillis + (now - sessionAnchor!!)
                                    } else {
                                        accumulatedMillis
                                    }
                                    val startInstant = sessionStartInstant!!
                                    val zone = TimeZone.currentSystemDefault()
                                    val startDateTime = Instant.fromEpochMilliseconds(startInstant).toLocalDateTime(zone)
                                    val endDateTime = Instant.fromEpochMilliseconds(now).toLocalDateTime(zone)
                                    records.add(
                                        0,
                                        FocusRecord(
                                            id = newRecordId(),
                                            date = startDateTime.date,
                                            startTime = startDateTime.time,
                                            endTime = endDateTime.time,
                                            durationMillis = totalMillis,
                                            startPage = startPage ?: endPage,
                                            endPage = endPage,
                                            topic = "스터디"
                                        )
                                    )
                                    timerState = FocusTimerState.Idle
                                    sessionAnchor = null
                                    sessionStartInstant = null
                                    accumulatedMillis = 0L
                                    displayMillis = 0L
                                    startPage = null
                                }
                            )
                        }
                        if (startPage == null) {
                            openStartPageDialog { openEndDialog() }
                        } else {
                            openEndDialog()
                        }
                    }
                )
            }
            item {
                FocusRecordList(
                    records = records,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp)
                )
            }
            item {
                FocusAtmospherePanel(atmospheres = atmospherePresets)
            }
        }

        pageDialogState?.let { dialog ->
            FocusPageDialog(
                state = dialog,
                onDismiss = { pageDialogState = null }
            )
        }
    }
}

@Composable
private fun FocusHeader(
    title: String,
    totalMinutes: Int,
    quote: FocusQuote,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
        )
        Text(
            text = "오늘 집중 ${totalMinutes}분 달성 중",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "오늘의 명언",
                    style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = quote.text,
                    style = MaterialTheme.typography.bodyLarge.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                )
                Text(
                    text = "— ${quote.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FocusTimerCard(
    timerText: String,
    startPage: Int?,
    timerState: FocusTimerState,
    onStartPageClick: () -> Unit,
    onStart: () -> Unit,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "집중 타이머", style = MaterialTheme.typography.titleMedium)
            Text(
                text = timerText,
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                textAlign = TextAlign.Center
            )
            TextButton(onClick = onStartPageClick) {
                Text(
                    text = if (startPage != null) "시작 페이지 $startPage" else "시작 페이지 설정",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = onStart,
                    modifier = Modifier.weight(1f),
                    enabled = timerState == FocusTimerState.Idle
                ) {
                    Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = "타이머 시작")
                }
                Button(
                    onClick = onPauseResume,
                    modifier = Modifier.weight(1f),
                    enabled = timerState != FocusTimerState.Idle
                ) {
                    Icon(
                        imageVector = if (timerState == FocusTimerState.Running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (timerState == FocusTimerState.Running) "타이머 멈춤" else "타이머 재개"
                    )
                }
                OutlinedButton(
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                    enabled = timerState != FocusTimerState.Idle
                ) {
                    Icon(imageVector = Icons.Rounded.Stop, contentDescription = "타이머 종료")
                }
            }
        }
    }
}

@Composable
private fun FocusRecordList(records: List<FocusRecord>, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier, shape = RoundedCornerShape(20.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(text = "공부 기록", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            if (records.isEmpty()) {
                Text(
                    text = "아직 기록이 없어요. 첫 집중을 시작해볼까요?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .heightIn(max = 240.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    records.forEach { record ->
                        FocusRecordItem(record)
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusRecordItem(record: FocusRecord) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = formatRecordDate(record.date),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "${record.topic} ${formatTime(record.startTime)}~${formatTime(record.endTime)} / ${formatDurationLabel(record.durationMillis)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "${record.endPage - record.startPage + 1} page (${record.startPage} → ${record.endPage})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FocusAtmospherePanel(atmospheres: List<FocusAtmosphere>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Text(text = "힐링 무드", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            atmospheres.forEachIndexed { index, atmosphere ->
                val alpha by animateFloatAsState(targetValue = 1f - index * 0.1f, label = "atmosphereAlpha")
                Surface(
                    tonalElevation = 0.dp,
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = alpha)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = atmosphere.icon,
                            contentDescription = null,
                            tint = atmosphere.tint,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(text = atmosphere.title, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = atmosphere.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusPageDialog(
    state: FocusPageDialogState,
    onDismiss: () -> Unit,
) {
    var value by remember(state) { mutableStateOf(state.initialValue?.toString().orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = state.title) },
        text = {
                Column(
                    modifier = Modifier.imePadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                Text(text = state.label)
                OutlinedTextField(
                    value = value,
                    onValueChange = { candidate -> value = candidate.filter { it.isDigit() } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    placeholder = { Text("페이지 숫자") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val page = value.toIntOrNull()
                    if (page != null) {
                        state.onConfirm(page)
                        onDismiss()
                    }
                }
            ) { Text(state.confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

@Composable
private fun rememberFocusRecords(): SnapshotStateList<FocusRecord> {
    return rememberSaveable(
        saver = listSaver(
            save = { list -> list.map { it.toSnapshot() } },
            restore = { snapshots ->
                mutableStateListOf<FocusRecord>().apply {
                    addAll(snapshots.map { it.toRecord() })
                }
            }
        )
    ) { mutableStateListOf() }
}

private data class FocusRecordSnapshot(
    val id: String,
    val dateIso: String,
    val startTime: String,
    val endTime: String,
    val durationMillis: Long,
    val startPage: Int,
    val endPage: Int,
    val topic: String,
)

private fun FocusRecord.toSnapshot(): FocusRecordSnapshot =
    FocusRecordSnapshot(
        id = id,
        dateIso = date.toString(),
        startTime = startTime.toString(),
        endTime = endTime.toString(),
        durationMillis = durationMillis,
        startPage = startPage,
        endPage = endPage,
        topic = topic
    )

private fun FocusRecordSnapshot.toRecord(): FocusRecord =
    FocusRecord(
        id = id,
        date = LocalDate.parse(dateIso),
        startTime = LocalTime.parse(startTime),
        endTime = LocalTime.parse(endTime),
        durationMillis = durationMillis,
        startPage = startPage,
        endPage = endPage,
        topic = topic
    )

private data class FocusRecord(
    val id: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val durationMillis: Long,
    val startPage: Int,
    val endPage: Int,
    val topic: String,
)

private enum class FocusTimerState { Idle, Running, Paused }

private data class FocusPageDialogState(
    val title: String,
    val label: String,
    val confirmLabel: String,
    val initialValue: Int? = null,
    val onConfirm: (Int) -> Unit,
)

private data class FocusQuote(
    val text: String,
    val author: String,
)

private val focusQuotes = listOf(
    FocusQuote("완벽한 순간을 기다리지 말고, 지금의 순간을 완벽하게 만들자.", "익명"),
    FocusQuote("꾸준함은 재능을 이기는 유일한 방법이다.", "익명"),
    FocusQuote("오늘의 한 걸음이 내일의 자신감을 만든다.", "익명"),
    FocusQuote("오래 집중할수록 목표는 가까워진다.", "익명"),
    FocusQuote("멈추지 않는 사람에게 불가능은 없다.", "찰스 킨슬리"),
    FocusQuote("천재와 노력의 차이는 습관이다.", "아리스토텔레스")
)

private data class FocusAtmosphere(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: Color,
)

private val focusAtmospherePresets = listOf(
    FocusAtmosphere(
        title = "잔잔한 파도",
        description = "느릿한 파도 소리를 떠올리며 호흡을 정리해요.",
        icon = Icons.Rounded.WaterDrop,
        tint = Color(0xFF2196F3)
    ),
    FocusAtmosphere(
        title = "차분한 바람",
        description = "창가로 들어오는 시원한 바람처럼 마음을 식혀요.",
        icon = Icons.Rounded.Air,
        tint = Color(0xFF26A69A)
    ),
    FocusAtmosphere(
        title = "포근한 구름",
        description = "구름 위에 앉아 쉬듯이 어깨의 긴장을 풀어보세요.",
        icon = Icons.Rounded.Cloud,
        tint = Color(0xFF90A4AE)
    ),
    FocusAtmosphere(
        title = "번쩍이는 몰입",
        description = "번개처럼 빠르게 목표를 향해 돌진!",
        icon = Icons.Rounded.Bolt,
        tint = Color(0xFFFFA000)
    )
)

private fun resolveTitle(totalMinutes: Int): String = when {
    totalMinutes >= 600 -> "루미너스 집중 마스터"
    totalMinutes >= 300 -> "빛나는 몰입 선배"
    totalMinutes >= 120 -> "꾸준함의 장인"
    totalMinutes >= 60 -> "집중 성장 중"
    else -> "오늘의 첫 걸음을 응원해요"
}

private fun dailyQuoteFor(date: LocalDate): FocusQuote {
    val index = abs(date.toEpochDay()).mod(focusQuotes.size)
    return focusQuotes[index]
}

private fun formatTimer(millis: Long): String {
    val duration = millis.milliseconds
    val hours = duration.inWholeHours
    val minutes = (duration - hours.hours).inWholeMinutes
    val seconds = (duration - hours.hours - minutes.minutes).inWholeSeconds
    val centiseconds = (millis % 1000) / 10
    return String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, centiseconds)
}

private fun formatDurationLabel(millis: Long): String {
    val duration = millis.milliseconds
    val hours = duration.inWholeHours
    val minutes = ((duration - hours.hours).inWholeMinutes)
    val parts = buildString {
        if (hours > 0) append("${hours}시간 ")
        append("${minutes}분")
    }
    return parts
}

private fun formatTime(time: LocalTime): String =
    String.format("%02d:%02d", time.hour, time.minute)

private fun formatRecordDate(date: LocalDate): String =
    "%04d.%02d.%02d".format(date.year, date.monthNumber, date.dayOfMonth)

private fun currentDate(): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

private fun LocalDate.toEpochDay(): Int =
    toEpochDays().toInt()

private fun newRecordId(): String =
    "focus-${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt(0, Int.MAX_VALUE)}"
