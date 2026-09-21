package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TimerOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R

@Composable
fun SleepTimerDialog(
    remainingSeconds: Long?,
    onSetTimer: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(
                    text = stringResource(R.string.sleep_timer),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (remainingSeconds != null && remainingSeconds > 0) {
                    val minutesLeft = (remainingSeconds / 60) + 1
                    Text(
                        text = stringResource(R.string.sleep_timer_active, minutesLeft),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                } else {
                    Text(
                        text = stringResource(R.string.timer_turn_off_in),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val timerOptions = listOf(10, 15, 30, 45, 60)
                timerOptions.chunked(3).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowOptions.forEach { minutes ->
                            FilledTonalButton(
                                onClick = {
                                    onSetTimer(minutes)
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("timer_btn_${minutes}m"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = "$minutes m")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (remainingSeconds != null && remainingSeconds > 0) {
                OutlinedButton(
                    onClick = {
                        onCancelTimer()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("cancel_timer_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TimerOff,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(stringResource(R.string.sleep_timer_off))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_timer_dialog")
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
