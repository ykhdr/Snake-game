package view.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameStartButton(modifier: Modifier = Modifier, onClick: () -> Unit, isEnabled : Boolean) {
    Box(modifier) {
        Button(
            modifier = Modifier.fillMaxSize(0.9f),
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            enabled = isEnabled,
            content = {
                Text(
                    text = "New Game",
                    style = TextStyle(fontSize = 18.sp),
                    textAlign = TextAlign.Center
                )
            }
        )
    }
}