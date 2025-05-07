package ees.dlc.application.screens

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ees.dlc.application.API
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class StaffProfile(
    val username: String,
    val name: String,
    val profilePicture: String,
    val bookingLink: String?,
//    val skills: List<String>?,
//    val modules: List<String>?
)

suspend fun getStaffProfiles(): JSONArray {
    val apiUrl = "http://10.16.82.7:5000/api/staff-list"
    return withContext(Dispatchers.IO) {
        val response = JSONArray(API.CallApi(apiUrl, "GET"))
        response
    }
}

@Composable
fun StaffProfilesScreen() {
    val staffProfilesData = remember { mutableStateOf<JSONArray?>(null) }

    LaunchedEffect(Unit) {
        coroutineScope {
            val staffProfilesDeferred = async { getStaffProfiles() }
            staffProfilesData.value = staffProfilesDeferred.await()
        }
    }

    if (staffProfilesData.value == null) {
        Text("Loading...")
        return
    }

    val staffProfiles = parseStaffProfiles(staffProfilesData.value!!)
    StaffProfileCards(staffProfiles)
}

fun parseStaffProfiles(json: JSONArray): List<StaffProfile> {
    val staffProfiles = mutableListOf<StaffProfile>()
    for(i in 0..<json.length()) {
        staffProfiles.add(StaffProfile(
            username = json.getJSONObject(i).getString("username"),
            name = json.getJSONObject(i).getString("name"),
            profilePicture = json.getJSONObject(i).getString("profile_picture"),
            bookingLink = json.getJSONObject(i).getString("booking_link")
        ))
    }
    return staffProfiles
}

@Composable
fun StaffProfileCards(staffProfiles: List<StaffProfile>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Staff Profiles",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyColumn {
            items(staffProfiles) { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .border(1.dp, Color.Gray, shape = RoundedCornerShape(16.dp))
                        .padding(8.dp)
                ) {
                    AsyncImage(
                        model = "http://192.168.1.36:5000/static/images/profile_pictures/"
                                + user.profilePicture,
                        contentDescription = "${user.name}'s profile picture",
                        modifier = Modifier
                            .size(64.dp)
                            .padding(end = 16.dp)
                    )
                    Column(modifier = Modifier.weight(2f)) {
                        Text(
                            text = user.name,
                            fontWeight = FontWeight.Bold
                        )
                        if (user.bookingLink != "null") {
                           Text(text = user.bookingLink.toString())
                        } else {
                            Text(text = "Not Bookable")
                        }
                    }
                }
            }
        }
    }
}
