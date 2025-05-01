package ees.dlc.application.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen() {
    DLCInformation()
}

@Composable
fun DLCInformation() {
    val dropInSessionString: AnnotatedString = remember {
        buildAnnotatedString {
            val styleCenter = SpanStyle(
                color = Color(0xff4582c4),
                textDecoration = TextDecoration.Underline
            )
            withLink(LinkAnnotation.Url(url = "https://webeec01.coventry.ac.uk/DLC")) {
                withStyle(
                    style = styleCenter
                ) {
                    append("drop in sessions")
                }
            }
        }
    }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text(
                text = "What is the DLC?",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = "The Digital Literacy Centre (DLC) helps you develop a range of digital " +
                            "and IT skills that will help you succeed while at university and in " +
                            "your future career."
                )
                Text(
                    text = "The DLC can help you with a range of digital skills, from programming, " +
                            "email etiquette, and from Word to enhancing your Google-Fu. We offer a" +
                            " range of online resources as well as regular " + dropInSessionString +
                            " throughout the week. We can also provide you with the details of other " +
                            "parts of the University who can help you with technical issues. You can " +
                            "find the resources and details of the $dropInSessionString on this " +
                            "website which we will be building up over the next few months."
                )
                Text(
                    text = dropInSessionString
                )
            }
        }
    }
}
