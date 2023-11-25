package view.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import model.dto.messages.Announcement

@Composable
fun Lobbies(modifier: Modifier = Modifier, announcements: List<Announcement> = listOf()) {
    Box(modifier) {
        Surface(
            border = BorderStroke(1.dp, Color.Black),
            modifier = Modifier.fillMaxSize()
        ) {
            Row {
                LazyColumn(
                    modifier = Modifier
                        .border(BorderStroke(1.dp, color = Color.Gray))
                        .weight(2F),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text( textAlign = TextAlign.Center,
                            text="a")
                    }
                    items(announcements.size) { index ->
                        val announcement = announcements[index]
                        Text("[ ${announcement.address.address.hostAddress}]")
                    }
                }
            }
        }
    }
}