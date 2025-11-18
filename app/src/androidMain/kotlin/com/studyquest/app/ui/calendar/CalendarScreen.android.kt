package com.studyquest.app.ui.calendar

import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.studyquest.app.ui.theme.localization.StringKey
import com.studyquest.app.ui.theme.localization.stringResource
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.io.Serializable
import java.util.Locale
import java.util.UUID
import kotlin.random.Random
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val monthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M월")
private val goalColors = listOf(
    Color(0xFF6DD5FA),
    Color(0xFF4E5BA6),
    Color(0xFF34C759),
    Color(0xFFF5A623),
    Color(0xFFF06292),
    Color(0xFF9C27B0),
)

private val ColorSaver = Saver<Color, Int>(
    save = { it.value.toInt() },
    restore = { Color(it) }
)

private data class CalendarTask(
    val id: String,
    val goalId: String,
    val goalTitle: String,
    val displayTitle: String,
    val color: Color,
    val pageStart: Int,
    val pageEnd: Int,
    val done: Boolean,
)

private data class GoalSchedule(
    val id: String,
    val goalTitle: String,
    val totalPages: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val repeatCount: Int,
    val color: Color,
    val excludedWeekdays: Set<DayOfWeek>,
    val excludedDates: Set<LocalDate>,
    val joinStudyCamp: Boolean,
    val enableReminder: Boolean,
)

private data class WeekGoalBar(
    val goalId: String,
    val label: String,
    val color: Color,
    val startIndex: Int,
    val endIndex: Int,
) {
    val labelIndex: Int get() = (startIndex + endIndex) / 2
}

private data class CalendarTaskSnapshot(
    val id: String,
    val goalId: String,
    val goalTitle: String,
    val displayTitle: String,
    val colorValue: Long,
    val pageStart: Int,
    val pageEnd: Int,
    val done: Boolean,
) : Serializable

private data class TaskDaySnapshot(
    val date: String,
    val tasks: List<CalendarTaskSnapshot>,
) : Serializable

private data class TaskMapSnapshot(
    val entries: List<TaskDaySnapshot>,
) : Serializable

private data class GoalScheduleSnapshot(
    val id: String,
    val goalTitle: String,
    val totalPages: Int,
    val startDate: String,
    val endDate: String,
    val repeatCount: Int,
    val colorValue: Long,
    val excludedWeekdays: List<Int>,
    val excludedDates: List<String>,
    val joinStudyCamp: Boolean,
    val enableReminder: Boolean,
) : Serializable

private data class GoalScheduleSnapshotList(
    val schedules: List<GoalScheduleSnapshot>,
) : Serializable

private data class CalendarPersistencePayload(
    val tasks: TaskMapSnapshot,
    val schedules: GoalScheduleSnapshotList,
) : Serializable

private const val CALENDAR_STATE_FILE = "calendar_state.bin"

private fun SnapshotStateMap<LocalDate, SnapshotStateList<CalendarTask>>.toSnapshot(): TaskMapSnapshot =
    TaskMapSnapshot(
        entries = entries.map { entry ->
            TaskDaySnapshot(
                date = entry.key.format(dateFormatter),
                tasks = entry.value.map { it.toSnapshot() }
            )
        }
    )

private fun TaskMapSnapshot?.toStateMap(): SnapshotStateMap<LocalDate, SnapshotStateList<CalendarTask>> {
    val restored = mutableStateMapOf<LocalDate, SnapshotStateList<CalendarTask>>()
    this?.entries?.forEach { day ->
        val date = LocalDate.parse(day.date, dateFormatter)
        val list = mutableStateListOf<CalendarTask>()
        list.addAll(day.tasks.map { it.toTask() })
        restored[date] = list
    }
    return restored
}

private fun SnapshotStateMap<String, GoalSchedule>.toSnapshot(): GoalScheduleSnapshotList =
    GoalScheduleSnapshotList(values.map { it.toSnapshot() })

private fun GoalScheduleSnapshotList?.toStateMap(): SnapshotStateMap<String, GoalSchedule> {
    val restored = mutableStateMapOf<String, GoalSchedule>()
    this?.schedules?.forEach { snapshot ->
        restored[snapshot.id] = snapshot.toSchedule()
    }
    return restored
}

private fun CalendarTask.toSnapshot(): CalendarTaskSnapshot =
    CalendarTaskSnapshot(
        id = id,
        goalId = goalId,
        goalTitle = goalTitle,
        displayTitle = displayTitle,
        colorValue = color.value.toLong(),
        pageStart = pageStart,
        pageEnd = pageEnd,
        done = done
    )

private fun CalendarTaskSnapshot.toTask(): CalendarTask =
    CalendarTask(
        id = id,
        goalId = goalId,
        goalTitle = goalTitle,
        displayTitle = displayTitle,
        color = Color(colorValue.toULong()),
        pageStart = pageStart,
        pageEnd = pageEnd,
        done = done
    )

private fun GoalSchedule.toSnapshot(): GoalScheduleSnapshot =
    GoalScheduleSnapshot(
        id = id,
        goalTitle = goalTitle,
        totalPages = totalPages,
        startDate = startDate.format(dateFormatter),
        endDate = endDate.format(dateFormatter),
        repeatCount = repeatCount,
        colorValue = color.value.toLong(),
        excludedWeekdays = excludedWeekdays.map { it.value },
        excludedDates = excludedDates.map { it.format(dateFormatter) },
        joinStudyCamp = joinStudyCamp,
        enableReminder = enableReminder
    )

private fun GoalScheduleSnapshot.toSchedule(): GoalSchedule =
    GoalSchedule(
        id = id,
        goalTitle = goalTitle,
        totalPages = totalPages,
        startDate = LocalDate.parse(startDate, dateFormatter),
        endDate = LocalDate.parse(endDate, dateFormatter),
        repeatCount = repeatCount,
        color = Color(colorValue.toULong()),
        excludedWeekdays = excludedWeekdays.map { DayOfWeek.of(it) }.toSet(),
        excludedDates = excludedDates.map { LocalDate.parse(it, dateFormatter) }.toSet(),
        joinStudyCamp = joinStudyCamp,
        enableReminder = enableReminder
    )

private val TaskMapSaver = Saver<SnapshotStateMap<LocalDate, SnapshotStateList<CalendarTask>>, TaskMapSnapshot>(
    save = { stateMap -> stateMap.toSnapshot() },
    restore = { snapshot -> snapshot.toStateMap() }
)

private val GoalScheduleSaver = Saver<SnapshotStateMap<String, GoalSchedule>, GoalScheduleSnapshotList>(
    save = { stateMap -> stateMap.toSnapshot() },
    restore = { snapshot -> snapshot.toStateMap() }
)

private fun readCalendarState(context: Context): CalendarPersistencePayload? =
    runCatching {
        context.openFileInput(CALENDAR_STATE_FILE).use { fis ->
            ObjectInputStream(fis).use { ois ->
                ois.readObject() as? CalendarPersistencePayload
            }
        }
    }.getOrNull()

private fun writeCalendarState(context: Context, payload: CalendarPersistencePayload) {
    runCatching {
        context.openFileOutput(CALENDAR_STATE_FILE, Context.MODE_PRIVATE).use { fos ->
            ObjectOutputStream(fos).use { oos ->
                oos.writeObject(payload)
                oos.flush()
            }
        }
    }
}
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)
@Suppress("UNUSED_PARAMETER")
@Composable
actual fun CalendarScreen(
    userMotto: String,
    onMottoChange: (String) -> Unit,
) {
    val taskMap: SnapshotStateMap<LocalDate, SnapshotStateList<CalendarTask>> = rememberSaveable(
        saver = TaskMapSaver
    ) {
        mutableStateMapOf<LocalDate, SnapshotStateList<CalendarTask>>()
    }
    val goalSchedules: SnapshotStateMap<String, GoalSchedule> = rememberSaveable(
        saver = GoalScheduleSaver
    ) {
        mutableStateMapOf<String, GoalSchedule>()
    }

    val context = LocalContext.current

    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var fontSize by rememberSaveable { mutableStateOf(16f) }
    var textColor by rememberSaveable(stateSaver = ColorSaver) { mutableStateOf(Color.Black) }
    var showGoalSheet by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var calendarExpanded by rememberSaveable { mutableStateOf(true) }
    var planAdjustTask by remember { mutableStateOf<CalendarTask?>(null) }
    var planAdjustDate by remember { mutableStateOf<LocalDate?>(null) }
    var planAdjustInput by remember { mutableStateOf("") }
    var planAdjustError by remember { mutableStateOf<String?>(null) }
    var deleteDialogTask by remember { mutableStateOf<CalendarTask?>(null) }
    var deleteDialogDate by remember { mutableStateOf<LocalDate?>(null) }
    var editingGoalId by remember { mutableStateOf<String?>(null) }
    var goalTitle by rememberSaveable { mutableStateOf("") }
    var totalPages by rememberSaveable { mutableStateOf("") }
    var startDateInput by rememberSaveable { mutableStateOf("") }
    var endDateInput by rememberSaveable { mutableStateOf("") }
    var repeatCountInput by rememberSaveable { mutableStateOf("3") }
    var joinStudyCamp by rememberSaveable { mutableStateOf(false) }
    var enableReminder by rememberSaveable { mutableStateOf(false) }
    var excludedDatesInput by remember { mutableStateOf("") }
    var showExcludedDatePicker by remember { mutableStateOf(false) }
    var excludedWeekdays by remember { mutableStateOf(setOf<DayOfWeek>()) }
    var selectedColor by remember { mutableStateOf(goalColors.random()) }
    var goalError by remember { mutableStateOf<String?>(null) }

    val gradientBrush = remember {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFB3E5FC), Color(0xFFE1F5FE), Color.White)
        )
    }

    fun persistState() {
        val payload = CalendarPersistencePayload(
            tasks = taskMap.toSnapshot(),
            schedules = goalSchedules.toSnapshot()
        )
        coroutineScope.launch(Dispatchers.IO) {
            writeCalendarState(context, payload)
        }
    }

    LaunchedEffect(context) {
        val payload = withContext(Dispatchers.IO) { readCalendarState(context) }
        payload?.let { saved ->
            val restoredTasks = saved.tasks.toStateMap()
            val restoredSchedules = saved.schedules.toStateMap()
            taskMap.clear()
            restoredTasks.forEach { (date, list) ->
                taskMap[date] = list
            }
            goalSchedules.clear()
            restoredSchedules.forEach { (id, schedule) ->
                goalSchedules[id] = schedule
            }
        }
    }

    fun resetGoalFields(defaultDate: LocalDate = selectedDate) {
        editingGoalId = null
        goalTitle = ""
        totalPages = ""
        startDateInput = defaultDate.format(dateFormatter)
        endDateInput = defaultDate.format(dateFormatter)
        repeatCountInput = "3"
        joinStudyCamp = false
        enableReminder = false
        excludedWeekdays = emptySet()
        excludedDatesInput = ""
        selectedColor = goalColors.random()
        goalError = null
    }

    fun openGoalSheet(goalId: String?) {
        if (goalId == null) {
            resetGoalFields()
        } else {
            val schedule = goalSchedules[goalId] ?: return
            editingGoalId = goalId
            goalTitle = schedule.goalTitle
            totalPages = schedule.totalPages.toString()
            startDateInput = schedule.startDate.format(dateFormatter)
            endDateInput = schedule.endDate.format(dateFormatter)
            repeatCountInput = schedule.repeatCount.toString()
            joinStudyCamp = schedule.joinStudyCamp
            enableReminder = schedule.enableReminder
            excludedWeekdays = schedule.excludedWeekdays
            excludedDatesInput = schedule.excludedDates.joinToString(",") { it.format(dateFormatter) }
            selectedColor = schedule.color
            goalError = null
        }
        showGoalSheet = true
    }

    fun applyGoalSchedule(
        schedule: GoalSchedule,
        startOverride: LocalDate? = null,
        totalPagesOverride: Int? = null,
        startingPage: Int = 1,
        preserveBefore: LocalDate? = null,
    ): String? {
        val totalPages = totalPagesOverride ?: schedule.totalPages
        if (totalPages <= 0) return null

        val startDate = startOverride ?: schedule.startDate
        val endDate = schedule.endDate
        if (startDate.isAfter(endDate)) return null
        val totalSpanDays = ChronoUnit.DAYS.between(startDate, endDate) + 1
        if (totalSpanDays < schedule.repeatCount) {
            return "회독 수가 기간보다 많습니다. 종료 날짜를 늘려주세요."
        }

        taskMap.keys.toList().forEach { date ->
            if (preserveBefore != null && date.isBefore(preserveBefore)) return@forEach
            val list = taskMap[date]
            if (list != null) {
                list.removeAll { it.goalId == schedule.id }
                if (list.isEmpty()) {
                    taskMap.remove(date)
                }
            }
        }

        val cycleRanges = computeCycleRanges(startDate, endDate, schedule.repeatCount)
        if (cycleRanges.isEmpty()) {
            return "학습 가능한 기간이 없습니다. 날짜를 다시 확인해주세요."
        }

        var remaining = totalPages
        var currentPage = startingPage
        for ((cycleIndex, range) in cycleRanges.withIndex()) {
            if (remaining <= 0) break
            val availableDays = generateSequence(range.first) { it.plusDays(1) }
                .takeWhile { !it.isAfter(range.second) }
                .filter { it.dayOfWeek !in schedule.excludedWeekdays && it !in schedule.excludedDates }
                .toList()
            if (availableDays.isEmpty()) continue
            var slotsRemaining = availableDays.size
            availableDays.forEach { targetDate ->
                if (remaining <= 0) return@forEach
                if (slotsRemaining <= 0) return@forEach
                val pagesForDay = ceil(remaining.toDouble() / slotsRemaining).toInt().coerceAtLeast(1)
                val startPage = currentPage
                val endPage = startPage + pagesForDay - 1
                val displayTitle = "${schedule.goalTitle.take(2)} $startPage-$endPage"
                val task = CalendarTask(
                    id = UUID.randomUUID().toString(),
                    goalId = schedule.id,
                    goalTitle = schedule.goalTitle,
                    displayTitle = displayTitle,
                    color = schedule.color,
                    pageStart = startPage,
                    pageEnd = endPage,
                    done = false,
                )
                val list = taskMap.getOrPut(targetDate) { mutableStateListOf<CalendarTask>() }
                list.add(task)
                list.sortBy { it.pageStart }
                currentPage = endPage + 1
                remaining -= pagesForDay
                slotsRemaining--
            }
        }

        return if (remaining > 0) {
            "남은 페이지를 배정할 날짜가 부족합니다."
        } else {
            persistState()
            null
        }
    }

    fun markTaskCompletion(date: LocalDate, taskId: String, completed: Boolean) {
        val list = taskMap[date] ?: return
        val index = list.indexOfFirst { it.id == taskId }
        if (index != -1) {
            list[index] = list[index].copy(done = completed)
            persistState()
        }
    }

    fun deleteGoal(goalId: String) {
        goalSchedules.remove(goalId)
        taskMap.keys.toList().forEach { date ->
            val list = taskMap[date]
            if (list != null) {
                list.removeAll { it.goalId == goalId }
                if (list.isEmpty()) taskMap.remove(date)
            }
        }
        persistState()
    }

    fun showInterstitialAd(onFinished: () -> Unit) {
        // TODO: 실제 AdMob 전면 광고 연동 필요
        onFinished()
    }

    fun requestGoalDeletion(goalId: String) {
        showInterstitialAd {
            deleteGoal(goalId)
        }
    }

    fun requireAd(action: () -> Unit) {
        showInterstitialAd {
            action()
        }
    }

    fun postponeTask(task: CalendarTask, date: LocalDate) {
        val list = taskMap[date] ?: return
        val index = list.indexOfFirst { it.id == task.id }
        if (index == -1) return
        val moved = list.removeAt(index)
        if (list.isEmpty()) taskMap.remove(date)
        val targetDate = date.plusDays(1)
        val targetList = taskMap.getOrPut(targetDate) { mutableStateListOf() }
        targetList.add(moved)
        targetList.sortBy { it.pageStart }
        persistState()
    }

    fun adjustGoalProgress(task: CalendarTask, date: LocalDate, pagesCompleted: Int): Boolean {
        val schedule = goalSchedules[task.goalId] ?: return false
        val sortedTasks = taskMap.entries
            .flatMap { entry ->
                entry.value.filter { it.goalId == task.goalId }
                    .map { entry.key to it }
            }
            .sortedBy { it.first }

        val pagesBefore = sortedTasks
            .filter { it.first.isBefore(date) }
            .sumOf { max(0, it.second.pageEnd - it.second.pageStart + 1) }

        val maxAllowedToday = schedule.totalPages - pagesBefore
        val actualToday = pagesCompleted.coerceIn(0, maxAllowedToday)

        val list = taskMap[date] ?: return false
        val index = list.indexOfFirst { it.id == task.id }
        if (index == -1) return false

        val startPage = list[index].pageStart
        val newEnd = if (actualToday <= 0) startPage - 1 else startPage + actualToday - 1
        val displayTitle = if (actualToday <= 0) {
            "${schedule.goalTitle.take(2)} 0p"
        } else {
            "${schedule.goalTitle.take(2)} $startPage-$newEnd"
        }

        list[index] = list[index].copy(
            pageEnd = newEnd,
            displayTitle = displayTitle,
            done = actualToday > 0
        )

        // remove future tasks
        taskMap.keys.toList().forEach { targetDate ->
            if (targetDate.isAfter(date)) {
                val futureList = taskMap[targetDate]
                if (futureList != null) {
                    futureList.removeAll { it.goalId == task.goalId }
                    if (futureList.isEmpty()) taskMap.remove(targetDate)
                }
            }
        }

        val remainingPages = (schedule.totalPages - (pagesBefore + actualToday)).coerceAtLeast(0)
        val nextStartPage = if (actualToday <= 0) startPage else newEnd + 1
        if (remainingPages <= 0) {
            goalSchedules.remove(task.goalId)
            return true
        }

        val newStartDate = date.plusDays(1)
        val adjustedEndDate = if (schedule.endDate.isBefore(newStartDate)) newStartDate else schedule.endDate
        val updatedSchedule = schedule.copy(
            startDate = newStartDate,
            endDate = adjustedEndDate,
            totalPages = remainingPages
        )
        val error = applyGoalSchedule(
            updatedSchedule,
            startOverride = updatedSchedule.startDate,
            totalPagesOverride = remainingPages,
            startingPage = nextStartPage,
            preserveBefore = updatedSchedule.startDate
        )
        return if (error == null) {
            goalSchedules[updatedSchedule.id] = updatedSchedule
            persistState()
            true
        } else {
            planAdjustError = error
            false
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { openGoalSheet(null) }) {
                Icon(Icons.Default.Add, contentDescription = "퀘스트 추가", tint = Color.White)
            }
        }
    ) { padding ->
        val configuration = LocalConfiguration.current
        val orientation = configuration.orientation
        val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE
        val screenHeightDp = configuration.screenHeightDp.dp
        val maxVisibleRows = when {
            screenHeightDp > 720.dp -> 6
            screenHeightDp > 600.dp -> 5
            else -> 4
        }
        val calendarWeight = if (isLandscape) 1.2f else 1.3f
        val scheduleWeight = if (isLandscape) 0.8f else 1f
        val dayCellAspectRatio = if (isLandscape) 0.9f else 1.05f
        val weekFields = remember { WeekFields.of(Locale.getDefault()) }
        val firstDayOfWeek = weekFields.firstDayOfWeek
        var scheduleExpanded by rememberSaveable { mutableStateOf(false) }
        val dayTasks = taskMap[selectedDate]

        @Composable
        fun CalendarSection(
            modifier: Modifier = Modifier,
            totalHeight: Dp,
            dayAspect: Float,
            allowedRows: Int,
            calendarExpanded: Boolean,
            onCalendarToggle: () -> Unit,
            useFixedHeights: Boolean,
        ) {
            val density = LocalDensity.current
            val weeks = remember(displayedMonth, firstDayOfWeek) {
                buildCalendarWeeks(displayedMonth, firstDayOfWeek)
            }
            val totalWeeks = weeks.size.coerceAtLeast(1)
            val expandedRows = min(3, min(allowedRows.coerceAtLeast(1), totalWeeks))
            val rowsForLayout = if (calendarExpanded) expandedRows else 1

            val baseHeaderHeight = totalHeight * 0.0685f
            val monthBarHeight = totalHeight * 0.0411f
            val weekdayHeight = totalHeight * 0.0345f
            val expandedGridHeight = totalHeight * 0.50f

            Column(modifier = modifier.fillMaxWidth()) {
                var headerLineCount by remember(userMotto) { mutableStateOf(1) }
                val headerHeight = baseHeaderHeight * headerLineCount

                val headerModifier = if (useFixedHeights) {
                    Modifier
                        .fillMaxWidth()
                        .height(headerHeight)
                        .padding(horizontal = 0.dp)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp)
                }

                BoxWithConstraints(modifier = headerModifier) {
                    val widthPx = constraints.maxWidth.toFloat()
                    val dynamicFont = ((widthPx / density.density) * 0.06f).coerceIn(16f, 28f)
                    val titleFont = if (useFixedHeights) (headerHeight.value * 0.30f).sp else (dynamicFont * 0.30f).sp

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val combined = userMotto.ifBlank { stringResource(StringKey.CalendarAppBarTitle) }
                        Text(
                            text = combined,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = titleFont,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                            onTextLayout = { layoutResult ->
                                val lines = layoutResult.lineCount.coerceAtLeast(1)
                                val newCount = lines.coerceIn(1, 2)
                                if (headerLineCount != newCount) {
                                    headerLineCount = newCount
                                }
                            }
                        )

                        if (useFixedHeights) {
                            IconButton(onClick = onCalendarToggle) {
                                val icon = if (calendarExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
                                val description = if (calendarExpanded) "달력 접기" else "달력 펼치기"
                                Icon(imageVector = icon, contentDescription = description, tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                val monthModifier = if (useFixedHeights) {
                    Modifier
                        .fillMaxWidth()
                        .height(monthBarHeight)
                        .padding(horizontal = 0.dp)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp)
                }

                CalendarMonthHeader(
                    month = displayedMonth,
                    onPrevious = { displayedMonth = displayedMonth.minusMonths(1) },
                    onNext = { displayedMonth = displayedMonth.plusMonths(1) },
                    modifier = monthModifier,
                    textSize = if (useFixedHeights) (monthBarHeight.value * 0.45f).sp else 20.sp
                )

                val weekdayModifier = if (useFixedHeights) {
                    Modifier
                        .fillMaxWidth()
                        .height(weekdayHeight)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp, vertical = 6.dp)
                }

                BoxWithConstraints(modifier = weekdayModifier) {
                    val totalWidthDp = constraints.maxWidth / density.density
                    val weekdayFont = if (useFixedHeights) {
                        (weekdayHeight.value * 0.55f).sp
                    } else {
                        (totalWidthDp / 7f * 0.5f).coerceIn(12f, 18f).sp
                    }

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DayOfWeek.values().forEach { day ->
                            Text(
                                text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = weekdayFont,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4A4A4A)
                            )
                        }
                    }
                }

                val gridModifier = if (useFixedHeights) {
                    val targetHeight = if (calendarExpanded) {
                        expandedGridHeight
                    } else {
                        (expandedGridHeight / expandedRows).coerceAtLeast(0.dp)
                    }
                    Modifier
                        .fillMaxWidth()
                        .height(targetHeight)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true)
                }

                BoxWithConstraints(modifier = gridModifier) {
                    val columns = 7
                    val spacing = 4.dp
                    val totalWidthPx = constraints.maxWidth.toFloat()
                    val horizontalSpacingPx = with(density) { spacing.toPx() }
                    val baseCellPx = (totalWidthPx - horizontalSpacingPx * (columns - 1)) / columns
                    val verticalSpacingPx = with(density) { 6.dp.toPx() }

                    val cellAspectRatio = if (useFixedHeights) {
                        val gridHeightPx = constraints.maxHeight.toFloat()
                        val perRowHeightPx = if (rowsForLayout > 0) {
                            (gridHeightPx - verticalSpacingPx * (rowsForLayout - 1)).coerceAtLeast(0f) / rowsForLayout
                        } else {
                            baseCellPx / dayAspect
                        }
                        if (perRowHeightPx > 0f) baseCellPx / perRowHeightPx else dayAspect
                    } else {
                        dayAspect
                    }

                    val totalRows = weeks.size
                    val enableScroll = totalRows > rowsForLayout

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = enableScroll,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(weeks) { _, weekDates ->
                            val bars = computeWeekBars(
                                weekDates = weekDates,
                                firstDayOfWeek = firstDayOfWeek,
                                getTasksForDate = { date -> taskMap[date].orEmpty() },
                                findSchedule = { goalId -> goalSchedules[goalId] }
                            )
                            WeekRow(
                                weekDates = weekDates,
                                bars = bars,
                                spacing = spacing,
                                cellAspectRatio = cellAspectRatio,
                                selectedDate = selectedDate,
                                displayedMonth = displayedMonth,
                                onDateSelected = { pickedDate ->
                                    selectedDate = pickedDate
                                }
                            )
                        }
                    }
                }
            }
        }
        @Composable
        fun ScheduleSection(
            modifier: Modifier = Modifier,
            fillAvailableHeight: Boolean,
            tasks: MutableList<CalendarTask>?,
        ) {
            val baseModifier = modifier
                .fillMaxWidth()
                .then(if (fillAvailableHeight) Modifier.fillMaxHeight() else Modifier)
            Column(
                modifier = baseModifier.animateContentSize(),
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            StringKey.CalendarSelectedDateTitle,
                            selectedDate.format(dateFormatter)
                        ),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (tasks.isNullOrEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        color = Color.White.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(StringKey.CalendarNoTasks),
                            modifier = Modifier.padding(24.dp),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    return@Column
                }

                val fontScale = fontSize / 16f
                val listModifier = if (fillAvailableHeight) {
                    Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true)
                } else {
                    Modifier.fillMaxWidth()
                }
                LazyColumn(
                    modifier = listModifier,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(tasks, key = { _, task -> task.id }) { index, task ->
                        CalendarTaskRow(
                            task = task,
                            fontScale = fontScale,
                            textColor = textColor,
                            onComplete = {
                                val toggled = !tasks[index].done
                                tasks[index] = tasks[index].copy(done = toggled)
                                markTaskCompletion(selectedDate, task.id, toggled)
                            },
                            onPlanEdit = {
                                val target = tasks[index]
                                requireAd {
                                    planAdjustDate = selectedDate
                                    planAdjustTask = target
                                    planAdjustInput = max(
                                        0,
                                        target.pageEnd - target.pageStart + 1
                                    ).toString()
                                    planAdjustError = null
                                }
                            },
                            onDelete = {
                                deleteDialogTask = task
                                deleteDialogDate = selectedDate
                            }
                        )
                    }
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                ) {
                    drawCircle(Color(0xFFE0F7FA), radius = 90f, center = Offset(220f, 110f))
                    drawCircle(Color(0xFFB3E5FC), radius = 70f, center = Offset(360f, 90f))
                    drawCircle(Color(0xFFE1F5FE), radius = 60f, center = Offset(120f, 140f))
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(padding)
        ) {
            val outerModifier = Modifier
                .fillMaxSize()
                .padding(
                    start = if (isLandscape) 20.dp else 12.dp,
                    end = if (isLandscape) 20.dp else 12.dp,
                    top = 0.dp,
                    bottom = 12.dp
                )

            if (isLandscape) {
                Row(
                    modifier = outerModifier,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    CalendarSection(
                        modifier = Modifier
                            .weight(calendarWeight)
                            .fillMaxHeight(),
                        totalHeight = screenHeightDp,
                        dayAspect = dayCellAspectRatio,
                        allowedRows = maxVisibleRows,
                        calendarExpanded = calendarExpanded,
                        onCalendarToggle = { calendarExpanded = !calendarExpanded },
                        useFixedHeights = false
                    )
                    ScheduleSection(
                        modifier = Modifier
                            .weight(scheduleWeight)
                            .fillMaxHeight(),
                        expanded = scheduleExpanded,
                        onExpandedChange = { scheduleExpanded = it },
                        fillAvailableHeight = true,
                        tasks = dayTasks
                    )
                }
            } else {
                Column(
                    modifier = outerModifier,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CalendarSection(
                        modifier = Modifier.fillMaxWidth(),
                        totalHeight = screenHeightDp,
                        dayAspect = dayCellAspectRatio,
                        allowedRows = maxVisibleRows,
                        calendarExpanded = calendarExpanded,
                        onCalendarToggle = { calendarExpanded = !calendarExpanded },
                        useFixedHeights = true
                    )
                    ScheduleSection(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(scheduleWeight),
                        expanded = scheduleExpanded,
                        onExpandedChange = { scheduleExpanded = it },
                        fillAvailableHeight = true,
                        tasks = dayTasks
                    )
                }
            }
        }
    }
    if (showGoalSheet) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                showGoalSheet = false
            }
        ) {
            val sheetScrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(sheetScrollState)
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (editingGoalId == null) "새 목표 등록" else "목표 수정",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                OutlinedTextField(
                    value = goalTitle,
                    onValueChange = { goalTitle = it },
                    label = { Text("목표 책 이름") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = totalPages,
                    onValueChange = { totalPages = it.filter { ch -> ch.isDigit() } },
                    label = { Text("전체 페이지 수") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    DateSelectionField(
                        value = startDateInput,
                        label = "시작 날짜",
                        placeholder = "YYYY-MM-DD",
                        modifier = Modifier.weight(1f),
                        onClick = { showStartDatePicker = true }
                    )
                    DateSelectionField(
                        value = endDateInput,
                        label = "종료 날짜",
                        placeholder = "YYYY-MM-DD",
                        modifier = Modifier.weight(1f),
                        onClick = { showEndDatePicker = true }
                    )
                }
                OutlinedTextField(
                    value = repeatCountInput,
                    onValueChange = { repeatCountInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("반복 횟수") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "학습 색상",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    goalColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = 2.dp,
                                    color = if (color == selectedColor) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }

                Text(
                    text = "제외 요일",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DayOfWeek.values().forEach { day ->
                        val selected = excludedWeekdays.contains(day)
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    excludedWeekdays = excludedWeekdays.toMutableSet().apply {
                                        if (selected) remove(day) else add(day)
                                    }.toSet()
                                },
                                label = { Text(day.getDisplayName(TextStyle.SHORT, Locale.getDefault())) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                val excludedDateList = remember(excludedDatesInput) { parseExcludedDates(excludedDatesInput) }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "제외 날짜", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                    if (excludedDateList.isEmpty()) {
                        Text(
                            text = "제외할 특정 날짜를 추가해 주세요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            excludedDateList.forEach { date ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(50)
                                ) {
                                            Row(
                                                modifier = Modifier
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                                    .clickable {
                                                        val updated = excludedDateList.filterNot { it == date }
                                                        excludedDatesInput = updated.joinToString(",") { it.format(dateFormatter) }
                                                    },
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(text = date.format(dateFormatter), style = MaterialTheme.typography.bodySmall)
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "제거",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                        }
                    }
                    OutlinedButton(onClick = { showExcludedDatePicker = true }) {
                        Text("제외 날짜 추가")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("스터디 캠프 참여")
                    Switch(checked = joinStudyCamp, onCheckedChange = { joinStudyCamp = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("알림 받기")
                    Switch(checked = enableReminder, onCheckedChange = { enableReminder = it })
                }

                goalError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showGoalSheet = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("취소")
                    }
                    Button(
                        onClick = {
                            val parsedStart = runCatching {
                                if (startDateInput.isBlank()) selectedDate else LocalDate.parse(startDateInput.trim())
                            }.getOrNull()
                            val parsedEnd = runCatching {
                                if (endDateInput.isBlank()) parsedStart ?: selectedDate else LocalDate.parse(endDateInput.trim())
                            }.getOrNull()
                            val totalPagesInt = totalPages.toIntOrNull()
                            val repeatInt = repeatCountInput.toIntOrNull()?.coerceAtLeast(1) ?: 1
                            val excludeDates = excludedDatesInput.split(",")
                                .mapNotNull { it.trim().takeIf { str -> str.isNotEmpty() } }
                                .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
                                .toSet()

                            when {
                                parsedStart == null || parsedEnd == null -> {
                                    goalError = "날짜 형식이 올바르지 않습니다."
                                }
                                parsedEnd.isBefore(parsedStart) -> {
                                    goalError = "종료 날짜가 시작 날짜보다 앞설 수 없습니다."
                                }
                                totalPagesInt == null || totalPagesInt <= 0 -> {
                                    goalError = "전체 페이지 수를 올바르게 입력해주세요."
                                }
                                else -> {
                                    val schedule = GoalSchedule(
                                        id = editingGoalId ?: UUID.randomUUID().toString(),
                                        goalTitle = goalTitle.ifBlank { "새 목표" },
                                        totalPages = totalPagesInt,
                                        startDate = parsedStart,
                                        endDate = parsedEnd,
                                        repeatCount = repeatInt,
                                        color = selectedColor,
                                        excludedWeekdays = excludedWeekdays,
                                        excludedDates = excludeDates,
                                        joinStudyCamp = joinStudyCamp,
                                        enableReminder = enableReminder,
                                    )
                                    val error = applyGoalSchedule(schedule)
                                    if (error != null) {
                                        goalError = error
                                    } else {
                                        selectedDate = schedule.startDate
                                        displayedMonth = YearMonth.from(schedule.startDate)
                                        goalSchedules[schedule.id] = schedule
                                        persistState()
                                        coroutineScope.launch {
                                            sheetState.hide()
                                        }.invokeOnCompletion {
                                            if (!sheetState.isVisible) {
                                                showGoalSheet = false
                                                goalError = null
                                                resetGoalFields(parsedStart)
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = goalTitle.isNotBlank() && totalPages.isNotBlank()
                    ) {
                        Text("완료")
                    }
                }
            }
        }
    }

    if (showStartDatePicker) {
        val initialStartDate = parseLocalDateOrNull(startDateInput) ?: selectedDate
        val startPickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialStartDate.toEpochMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = startPickerState.selectedDateMillis
                    if (millis != null) {
                        val picked = epochMillisToLocalDate(millis)
                        startDateInput = picked.format(dateFormatter)
                    }
                    showStartDatePicker = false
                }) {
                    Text("선택")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = startPickerState)
        }
    }

    if (showEndDatePicker) {
        val initialEndDate = parseLocalDateOrNull(endDateInput)
            ?: parseLocalDateOrNull(startDateInput)
            ?: selectedDate
        val endPickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialEndDate.toEpochMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = endPickerState.selectedDateMillis
                    if (millis != null) {
                        val picked = epochMillisToLocalDate(millis)
                        endDateInput = picked.format(dateFormatter)
                    }
                    showEndDatePicker = false
                }) {
                    Text("선택")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = endPickerState)
        }
    }

    if (showExcludedDatePicker) {
        val baseDate = parseLocalDateOrNull(startDateInput) ?: selectedDate
        val excludedPickerState = rememberDatePickerState(
            initialSelectedDateMillis = baseDate.toEpochMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showExcludedDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = excludedPickerState.selectedDateMillis
                    if (millis != null) {
                        val picked = epochMillisToLocalDate(millis)
                        val updated = (parseExcludedDates(excludedDatesInput) + picked)
                            .toSet()
                            .sorted()
                        excludedDatesInput = updated.joinToString(",") { it.format(dateFormatter) }
                    }
                    showExcludedDatePicker = false
                }) {
                    Text("선택")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExcludedDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = excludedPickerState)
        }
    }

    val planTask = planAdjustTask
    val planDate = planAdjustDate
    if (planTask != null && planDate != null) {
        AlertDialog(
            onDismissRequest = {
                planAdjustTask = null
                planAdjustDate = null
                planAdjustError = null
            },
            title = { Text("계획 수정") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("오늘 완료한 페이지 수를 입력하세요.")
                    OutlinedTextField(
                        value = planAdjustInput,
                        onValueChange = { planAdjustInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("완료한 페이지 수") },
                        singleLine = true
                    )
                    planAdjustError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val pages = planAdjustInput.toIntOrNull()
                    if (pages == null) {
                        planAdjustError = "숫자를 입력해주세요."
                    } else {
                        val success = adjustGoalProgress(planTask, planDate, pages)
                        if (success) {
                            planAdjustTask = null
                            planAdjustDate = null
                            planAdjustError = null
                        }
                    }
                }) {
                    Text("저장")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    planAdjustTask = null
                    planAdjustDate = null
                    planAdjustError = null
                }) {
                    Text("취소")
                }
            }
        )
    }

    val pendingDeleteTask = deleteDialogTask
    val pendingDeleteDate = deleteDialogDate
    if (pendingDeleteTask != null && pendingDeleteDate != null) {
        AlertDialog(
            onDismissRequest = {
                deleteDialogTask = null
                deleteDialogDate = null
            },
            title = { Text("일정 관리") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("\"${pendingDeleteTask.displayTitle}\" 일정을 어떻게 처리할까요?")
                    Text(
                        text = "전체 삭제를 선택하면 해당 목표의 모든 일정이 삭제됩니다.\n오늘만 미루기는 이 일정만 다음 날로 이동합니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    requestGoalDeletion(pendingDeleteTask.goalId)
                    deleteDialogTask = null
                    deleteDialogDate = null
                }) {
                    Text("전체 삭제")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = {
                        requireAd {
                            postponeTask(pendingDeleteTask, pendingDeleteDate)
                            deleteDialogTask = null
                            deleteDialogDate = null
                        }
                    }) {
                        Text("오늘만 미루기")
                    }
                    TextButton(onClick = {
                        deleteDialogTask = null
                        deleteDialogDate = null
                    }) {
                        Text("취소")
                    }
                }
            }
        )
    }
}

@Composable
private fun CalendarMonthHeader(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    textSize: TextUnit = 20.sp,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
        }
        Text(
            text = month.format(monthFormatter),
            fontWeight = FontWeight.Bold,
            fontSize = textSize
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Next month")
        }
    }
}

@Composable
private fun WeekRow(
    weekDates: List<LocalDate>,
    bars: List<WeekGoalBar>,
    spacing: Dp,
    cellAspectRatio: Float,
    selectedDate: LocalDate,
    displayedMonth: YearMonth,
    onDateSelected: (LocalDate) -> Unit,
) {
    val density = LocalDensity.current
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columns = weekDates.size.coerceAtLeast(1)
        val spacingPx = with(density) { spacing.toPx() }
        val totalWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(0f)
        val cellWidthPx = ((totalWidthPx - spacingPx * (columns - 1)).coerceAtLeast(0f)) / columns
        val cellHeightPx = if (cellAspectRatio > 0f) cellWidthPx / cellAspectRatio else cellWidthPx
        val rowHeightDp = with(density) { cellHeightPx.toDp() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeightDp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                weekDates.forEach { date ->
                    val isCurrentMonth = YearMonth.from(date) == displayedMonth
                    CalendarDayCell(
                        date = date,
                        isSelected = date == selectedDate,
                        isCurrentMonth = isCurrentMonth,
                        onDateSelected = onDateSelected,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }

            val slotRatio = 0.25f
            val maxBarsToShow = 3
            bars.take(maxBarsToShow).forEachIndexed { index, bar ->
                val span = (bar.endIndex - bar.startIndex + 1).coerceAtLeast(1)
                val startX = bar.startIndex * (cellWidthPx + spacingPx)
                val barWidthPx = cellWidthPx * span + spacingPx * (span - 1)
                val slotTop = cellHeightPx * ((index + 1) * slotRatio)
                val slotHeight = cellHeightPx * slotRatio
                val barHeightPx = (slotHeight * 0.65f).coerceAtLeast(12f)
                val verticalOffset = (slotTop + (slotHeight - barHeightPx) / 2f).coerceAtLeast(0f)
                Box(
                    modifier = Modifier
                        .offset { IntOffset(startX.roundToInt(), verticalOffset.roundToInt()) }
                        .width(with(density) { barWidthPx.toDp() })
                        .height(with(density) { barHeightPx.toDp() })
                        .clip(RoundedCornerShape(10.dp))
                        .background(bar.color.copy(alpha = 0.28f))
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = bar.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = bar.color,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun computeWeekBars(
    weekDates: List<LocalDate>,
    firstDayOfWeek: DayOfWeek,
    getTasksForDate: (LocalDate) -> List<CalendarTask>,
    findSchedule: (String) -> GoalSchedule?,
): List<WeekGoalBar> {
    if (weekDates.isEmpty()) return emptyList()

    val grouped = mutableMapOf<String, MutableList<Triple<Int, LocalDate, CalendarTask>>>()
    weekDates.forEachIndexed { index, date ->
        getTasksForDate(date).forEach { task ->
            grouped.getOrPut(task.goalId) { mutableListOf() }
                .add(Triple(index, date, task))
        }
    }

    val weekStart = weekDates.first()

    return grouped.values.mapNotNull { occurrences ->
        if (occurrences.isEmpty()) return@mapNotNull null
        val sorted = occurrences.sortedBy { it.first }
        val startIndex = sorted.first().first
        val endIndex = sorted.last().first
        val tasks = sorted.map { it.third }
        val minPage = tasks.minOf { it.pageStart }
        val maxPage = tasks.maxOf { it.pageEnd }
        val sample = tasks.first()
        val schedule = findSchedule(sample.goalId)
        val label = buildWeekBarLabel(
            goalTitle = sample.goalTitle,
            schedule = schedule,
            weekStart = weekStart,
            firstDayOfWeek = firstDayOfWeek,
            minPage = minPage,
            maxPage = maxPage
        )
        WeekGoalBar(
            goalId = sample.goalId,
            label = label,
            color = schedule?.color ?: sample.color,
            startIndex = startIndex,
            endIndex = endIndex
        )
    }.sortedWith(compareBy({ it.startIndex }, { it.goalId }))
}

private fun buildWeekBarLabel(
    goalTitle: String,
    schedule: GoalSchedule?,
    weekStart: LocalDate,
    firstDayOfWeek: DayOfWeek,
    minPage: Int,
    maxPage: Int,
): String {
    val trimmedTitle = if (goalTitle.length > 18) {
        goalTitle.take(17) + "…"
    } else {
        goalTitle
    }
    val pageRange = "${minPage}~${maxPage}p"

    if (schedule == null) {
        return "\"$trimmedTitle\" : $pageRange"
    }

    val cycleRanges = computeCycleRanges(schedule.startDate, schedule.endDate, schedule.repeatCount)
    if (cycleRanges.isEmpty()) {
        return "\"$trimmedTitle\" : $pageRange"
    }

    val targetWeekStart = weekStart.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
    val cycleIndex = cycleRanges.indexOfFirst { range ->
        !targetWeekStart.isBefore(range.first) && !targetWeekStart.isAfter(range.second)
    }.let { if (it == -1) 0 else it.coerceAtMost(cycleRanges.lastIndex) }

    val selectedRange = cycleRanges[cycleIndex]
    val cycleStartWeek = selectedRange.first.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
    val cycleEndWeek = selectedRange.second.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
    val cycleWeeks = max(1, ChronoUnit.WEEKS.between(cycleStartWeek, cycleEndWeek).toInt() + 1)
    val weekInCycle = (ChronoUnit.WEEKS.between(cycleStartWeek, targetWeekStart).toInt().coerceAtLeast(0) + 1)
        .coerceAtMost(cycleWeeks)
    val cycleLabel = "(${cycleIndex + 1}회독)"
    val prefix = if (cycleIndex == 0 && weekInCycle == 1) "시작 - " else ""

    return "$prefix\"$trimmedTitle\" : ${weekInCycle}주 $pageRange $cycleLabel"
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    isSelected: Boolean,
    isCurrentMonth: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()

    val backgroundColor = when {
        date == today -> Color(0xFFB3E5FC)
        isSelected -> Color(0xFF81D4FA)
        else -> Color.Transparent
    }

    val scale by animateFloatAsState(targetValue = if (isSelected) 1.05f else 1f, label = "dayScale")
    val dayTextColor = when {
        isCurrentMonth -> Color.Black
        else -> Color(0xFF9AA0A6)
    }

    BoxWithConstraints(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .clickable { onDateSelected(date) }
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        val density = LocalDensity.current
        val cellWidthDp = constraints.maxWidth / density.density
        val dayFontSize = (cellWidthDp * 0.22f).coerceIn(14f, 20f).sp

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontWeight = FontWeight.SemiBold,
                fontSize = dayFontSize,
                color = dayTextColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelectionField(
    value: String,
    label: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true)
            ) { onClick() }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = "$label 선택") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CalendarTaskRow(
    task: CalendarTask,
    fontScale: Float,
    textColor: Color,
    onComplete: () -> Unit,
    onPlanEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = if (task.done) 0.65f else 0.9f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        val density = LocalDensity.current
        val widthDp = constraints.maxWidth / density.density
        val titleSize = (widthDp * 0.12f * fontScale).coerceIn(12f, 22f).sp
        val captionSize = (widthDp * 0.09f * fontScale).coerceIn(10f, 18f).sp

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(task.color)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.displayTitle,
                        color = if (task.done) Color.Gray else textColor,
                        fontSize = titleSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.done) TextDecoration.LineThrough else null
                    )
                    Text(
                        text = task.goalTitle,
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = captionSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.done) TextDecoration.LineThrough else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "분량: ${task.pageStart}~${task.pageEnd}p (${(task.pageEnd - task.pageStart + 1).coerceAtLeast(0)}p)",
                color = textColor.copy(alpha = 0.85f),
                fontSize = captionSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "목표: ${task.goalTitle}",
                color = textColor.copy(alpha = 0.7f),
                fontSize = captionSize,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (task.done) "취소" else "완료")
                }
                OutlinedButton(
                    onClick = onPlanEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("수정")
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("삭제")
                }
            }
        }
    }
}

private fun buildCalendarWeeks(month: YearMonth, firstDayOfWeek: DayOfWeek): List<List<LocalDate>> {
    val firstDay = month.atDay(1)
    val lastDay = month.atEndOfMonth()
    val start = firstDay.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))

    val days = mutableListOf<LocalDate>()
    var cursor = start
    while (true) {
        days += cursor
        if (cursor >= lastDay && days.size % 7 == 0) break
        cursor = cursor.plusDays(1)
    }
    return days.chunked(7)
}

private fun parseLocalDateOrNull(value: String): LocalDate? =
    runCatching { LocalDate.parse(value, dateFormatter) }.getOrNull()

private fun LocalDate.toEpochMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long =
    this.atStartOfDay(zoneId).toInstant().toEpochMilli()

private fun epochMillisToLocalDate(millis: Long, zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()

private fun parseExcludedDates(raw: String): List<LocalDate> =
    raw.split(",")
        .mapNotNull { it.trim().takeIf { str -> str.isNotEmpty() } }
        .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
        .sorted()

private fun computeCycleRanges(
    startDate: LocalDate,
    endDate: LocalDate,
    repeatCount: Int,
): List<Pair<LocalDate, LocalDate>> {
    val totalSpanDays = ChronoUnit.DAYS.between(startDate, endDate) + 1
    if (repeatCount <= 0 || totalSpanDays <= 0) return emptyList()
    val baseSpan = (totalSpanDays / repeatCount).toInt()
    var remainder = (totalSpanDays % repeatCount).toInt()
    if (baseSpan <= 0) return emptyList()
    val ranges = mutableListOf<Pair<LocalDate, LocalDate>>()
    var offset = 0L
    for (i in 0 until repeatCount) {
        var spanDays = baseSpan
        if (remainder > 0) {
            spanDays += 1
            remainder -= 1
        }
        val cycleStart = startDate.plusDays(offset)
        val cycleEnd = cycleStart.plusDays(spanDays.toLong() - 1)
        val boundedEnd = if (cycleEnd.isAfter(endDate)) endDate else cycleEnd
        ranges += (cycleStart to boundedEnd)
        offset += spanDays.toLong()
    }
    return ranges
}
