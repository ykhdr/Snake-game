package view.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GameCreateButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier){
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            content = {
                Text(
                    text = "Создать игру",
                )
            }
        )

    }

}