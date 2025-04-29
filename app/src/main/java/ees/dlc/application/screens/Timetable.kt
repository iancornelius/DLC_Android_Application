package ees.dlc.application.screens

import android.annotation.SuppressLint
import kotlinx.coroutines.async
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ees.dlc.application.API
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

suspend fun GetSemester(): Int {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val todays_date = LocalDateTime.now()
    val apiUrl = "http://192.168.1.36:5000/api/current-semester/20/01/2025"
    return withContext(Dispatchers.IO) {
        val response = JSONObject(API.CallApi(apiUrl, "GET"))
        response.getInt("current-semester")
    }

}

suspend fun GetWeek(): Int {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val todays_date = LocalDateTime.now()
    val apiUrl = "http://192.168.1.36:5000/api/current-week/20/01/2025"
    return withContext(Dispatchers.IO) {
        val response = JSONObject(API.CallApi(apiUrl, "GET"))
        response.getInt("current-week")
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float
) {
    Text(
        text = text,
        Modifier
            .border(1.dp, Color.Black)
            .weight(weight)
            .padding(8.dp)
    )
}

@Composable
fun TimetableScreen(){
    val week = remember { mutableIntStateOf(-1) }
    val semester = remember { mutableIntStateOf(-1) }
    val timetableData = remember { mutableStateOf<JSONObject?>(null) }

    LaunchedEffect(Unit) {
        val weekDeferred = async { GetWeek() }
        val semesterDeferred = async { GetSemester() }

        week.intValue = weekDeferred.await()
        semester.intValue = semesterDeferred.await()
    }

    LaunchedEffect(week.intValue, semester.intValue) {
        if (week.intValue != -1 && semester.intValue != -1) {
            val apiUrl = "http://192.168.1.36:5000/timetable/${semester.intValue}/${week.intValue}"
            val response = withContext(Dispatchers.IO) {
                JSONObject(API.CallApi(apiUrl, "GET"))
            }
            Log.d("TimeTable-API", response.toString())
            timetableData.value = response
        }
    }

    if (week.intValue == -1 || semester.intValue == -1) {
        Text("Loading...")
        return
    }

    val tableData = (1..100).map { it to "Item $it" }
    val column1Weight = .3f
    val column2Weight = .7f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Row(Modifier.background(Color.Gray)) {
                TableCell("Column 1", column1Weight)
                TableCell("Column 2", column2Weight)
            }
        }
        items(tableData) { (id, text) ->
            Row(Modifier.fillMaxWidth()) {
                TableCell(id.toString(), column1Weight)
                TableCell(text, column2Weight)
            }
        }
    }
}
