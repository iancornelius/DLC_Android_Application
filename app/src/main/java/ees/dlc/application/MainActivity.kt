package ees.dlc.application

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ees.dlc.application.ui.theme.DLCTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import coil.compose.AsyncImage


class MainActivity : ComponentActivity() {

    data class UserItem(
        val id: Int,
        val username: String,
        val name: String,
        val profile_picture: String,
        val booking_link: String?
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

        Column(modifier = modifier.fillMaxSize()) {
            Button(
                onClick = {
                    asyncGetHttpRequest(
                        endpoint = "http://192.168.1.36:5000/api/staff-list",
                        onSuccess = { users ->
                            userList.value = users
                        },
                        onError = { error ->
                            Log.e("HTTP", "Failed: ${error.message}")
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Get Users")
            }

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
                                    .clip(CircleShape)
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
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DLCTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GetStaffMembers(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
