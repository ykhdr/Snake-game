package view.views

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import view.components.LeaveButton
import view.components.Lobbies
import view.components.NewGameButton

@Preview
@Composable
fun MenuView() = Surface(
    color = MaterialTheme.colors.surface,
    modifier = Modifier
        .fillMaxSize()
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.padding(5.dp)
    ) {
        Box(modifier = Modifier.weight(1F)) {
//            Lobbies()
        }
        Box(modifier = Modifier.weight(4F)) {
//            Lobbies()
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(1.dp),
            modifier = Modifier.weight(2F)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1F)
                    .padding(5.dp),
                horizontalArrangement = Arrangement.Center,
            ) {

                LeaveButton(
                    Modifier
                        .fillMaxSize()
                        .weight(2F)
                        .padding(5.dp)
                ) {

                }

                NewGameButton(
                    Modifier
                        .fillMaxSize()
                        .weight(2F)
                        .padding(5.dp)
                ) {
                }
            }

            Box(modifier = Modifier.fillMaxHeight().weight(6F)) {
                Lobbies()
            }
        }
    }


}