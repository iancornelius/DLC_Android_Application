package ees.dlc.application

import android.util.Log
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class API {
    companion object {
        fun CallApi(apiUrl: String, httpMethod: String, requestModel: Any? = null): String {
            val response = StringBuilder()

            try {
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = httpMethod

                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")

                if (httpMethod == "POST" || httpMethod == "PUT") {
                    connection.doOutput = true
                    requestModel?.let {
                        val jsonInput = Gson().toJson(it)
                        OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                            writer.write(jsonInput)
                            writer.flush()
                        }
                    }
                }

                val responseCode = connection.responseCode
                val inputStream: InputStream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream ?: throw IOException("Invalid Response Stream")
                }

                BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                }

            } catch (e: Exception) {
                return e.message ?: "Unknown Error"
            }

            return response.toString()
        }
    }
}
