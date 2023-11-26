package view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LeaveButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier) {
        Button(
            modifier = Modifier.fillMaxSize(0.9f),
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = "Leave lobby",
                style = TextStyle(fontSize = 18.sp),
                textAlign = TextAlign.Center
            )
        }
    }
}