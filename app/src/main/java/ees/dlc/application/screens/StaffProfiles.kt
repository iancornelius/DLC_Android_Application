package ees.dlc.application.screens

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.stream.Collectors



class StaffProfiles : ComponentActivity() {
    data class UserItem(
        val id: Int,
        val username: String,
        val name: String,
        val profile_picture: String,
        val booking_link: String,
        val skills: List<String>,
        val modules: List<String>
    )

    private inline fun <reified T> parseJson(text: String): T =
        Gson().fromJson(text, object : TypeToken<T>() {}.type)

    private fun asyncGetHttpRequest(
        endpoint: String,
        onSuccess: (List<UserItem>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL(endpoint)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                Log.d("HTTP", "Response Code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val responseText = reader.readText()
                    val users = parseJson<List<UserItem>>(responseText)
                    reader.close()

                    launch(Dispatchers.Main) {
                        onSuccess(users)
                    }
                } else {
                    launch(Dispatchers.Main) {
                        onError(Exception("HTTP Request failed with response code $responseCode"))
                    }
                }
            } catch (e: Exception) {
                Log.e("HTTP", "Request error: ${e.message}", e)
                launch(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    @Composable
    fun GetStaffMembers(modifier: Modifier = Modifier) {
        val userList = remember { mutableStateOf<List<UserItem>>(emptyList()) }

        asyncGetHttpRequest(
            endpoint = "http://192.168.1.36:5000/api/staff-list",
            onSuccess = { users ->
                userList.value = users
            },
            onError = { error ->
                Log.e("HTTP", "Failed: ${error.message}")
            }
        )

        Column(modifier = modifier.fillMaxSize()) {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(userList.value) { item ->
                    Card(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            AsyncImage(
                                model = "http://192.168.1.36:5000/static/images/profile_pictures/" + item.profile_picture,
                                contentDescription = "${item.name}'s profile picture",
                                modifier = Modifier
                                    .size(64.dp)
                                    .padding(end = 16.dp)
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Username: ${item.username}")
                                Text(text = "Name: ${item.name}")
                                if(item.booking_link != null) {
                                    Text(text = "Booking Link: ${item.booking_link}")
                                }
                                if(item.skills != null) {
                                    val tmpSkills: String =
                                        item.skills.stream().collect(Collectors.joining(", "))
                                    Text(text = "Skills: $tmpSkills")
                                }
                                if(item.modules != null) {
                                    val tmpModules: String =
                                        item.modules.stream().collect(Collectors.joining(", "))
                                    Text(text = "Modules: $tmpModules")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun StaffProfilesScreen(){

    val sp = StaffProfiles()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        sp.GetStaffMembers(modifier = Modifier.padding(innerPadding))
    }
}
