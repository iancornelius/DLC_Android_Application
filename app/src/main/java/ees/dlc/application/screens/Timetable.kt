package ees.dlc.application.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ees.dlc.application.API
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class TimeSlot(
    val time: String,
    val staff: List<String>
)

suspend fun getSemester(): Int {
    // val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    // val current_date = LocalDateTime.now()
    val apiUrl = "http://10.16.82.7:5000/api/current-semester/20/01/2025"
    return withContext(Dispatchers.IO) {
        val response = JSONObject(API.CallApi(apiUrl, "GET"))
        response.getInt("current-semester")
    }
}

suspend fun getWeek(): Int {
//    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//    val current_date = LocalDateTime.now()
    val apiUrl = "http://10.16.82.7:5000/api/current-week/20/01/2025"
    return withContext(Dispatchers.IO) {
        val response = JSONObject(API.CallApi(apiUrl, "GET"))
        response.getInt("current-week")
    }
}

@Composable
fun TimetableScreen() {
    val semester = remember { mutableIntStateOf(-1) }
    val week = remember { mutableIntStateOf(-1) }
    val currentDay = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val timetableData = remember { mutableStateOf<JSONObject?>(null) }

    LaunchedEffect(Unit) {
        coroutineScope {
            val weekDeferred = async { getWeek() }
            val semesterDeferred = async { getSemester() }

            week.intValue = weekDeferred.await()
            semester.intValue = semesterDeferred.await()
        }
    }

    LaunchedEffect(week.intValue, semester.intValue) {
        if (week.intValue != -1 && semester.intValue != -1) {
//            val apiUrl = "http://192.168.1.36:5000/api/timetable/${semester.intValue}/${week.intValue}/${currentDay}"
            val apiUrl = "http://10.16.82.7:5000/api/timetable/1/6/${currentDay.lowercase()}"
            val response = withContext(Dispatchers.IO) {
                JSONObject(API.CallApi(apiUrl, "GET"))
            }
            timetableData.value = response
        }
    }

    if (week.intValue == -1 || semester.intValue == -1 || timetableData.value == null) {
        Text("Loading...")
        return
    }

    val mondaySchedule = remember(timetableData) {
        parseDaySchedule(timetableData.value!!, currentDay.lowercase())
    }

    SingleDayTimetable(day = "Monday", schedule = mondaySchedule)


}

fun parseDaySchedule(json: JSONObject, day: String): List<TimeSlot> {
    val dayObject = json.optJSONObject(day) ?: return emptyList()
    val timeSlots = mutableListOf<TimeSlot>()

    val keys = dayObject.keys()
    while (keys.hasNext()) {
        val time = keys.next()
        val staffArray = dayObject.optJSONArray(time)
        val staffList = mutableListOf<String>()
        for (i in 0 until (staffArray?.length() ?: 0)) {
            staffList.add(staffArray!!.getString(i))
        }
        timeSlots.add(TimeSlot(time, staffList))
    }

    return timeSlots.sortedBy { it.time }
}

@Composable
fun SingleDayTimetable(day: String, schedule: List<TimeSlot>) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")
    val currentDate = LocalDateTime.now().format(formatter)

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "$currentDate - ${day.replaceFirstChar { it.uppercase() }}",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyColumn {
            items(schedule) { slot ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .border(1.dp, Color.Gray, shape = RoundedCornerShape(16.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = slot.time,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    Column(modifier = Modifier.weight(2f)) {
                        slot.staff.forEach { staffMember ->
                            Text(text = "\u2022 $staffMember")
                        }
                    }
                }
            }
        }
    }
}