package view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LeaveButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier) {
        OutlinedButton(
            onClick = onClick,
            border = BorderStroke(width = 1.dp, color = Color.Gray),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = "Leave Game"
            )
        }
    }
}