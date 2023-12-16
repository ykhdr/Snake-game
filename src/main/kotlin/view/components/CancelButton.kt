package view.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun CancelButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier) {
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            content = {
                Text(
                    text = "Отмена",
                )
            }
        )
    }
}