package sh.haven.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sh.haven.core.data.preferences.AgentMacro

@Composable
fun AgentMacroManagerDialog(
    macros: List<AgentMacro>,
    onDismiss: () -> Unit,
    onSave: (List<AgentMacro>) -> Unit,
    onResetDefaults: () -> Unit,
) {
    val items = remember(macros) { mutableStateListOf(*macros.toTypedArray()) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("macro_manager_dialog"),
        title = {
            Text(
                text = stringResource(R.string.settings_agent_macro_dialog_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = {
                            editingIndex = null
                            showEditDialog = true
                        },
                        modifier = Modifier.testTag("btn_add_macro")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.settings_agent_add_macro))
                    }

                    TextButton(onClick = {
                        onResetDefaults()
                        items.clear()
                        items.addAll(AgentMacro.DEFAULT_MACROS)
                    }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.common_reset))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(items) { index, macro ->
                        MacroRowCard(
                            macro = macro,
                            canMoveUp = index > 0,
                            canMoveDown = index < items.size - 1,
                            onMoveUp = {
                                val item = items.removeAt(index)
                                items.add(index - 1, item)
                            },
                            onMoveDown = {
                                val item = items.removeAt(index)
                                items.add(index + 1, item)
                            },
                            onEdit = {
                                editingIndex = index
                                showEditDialog = true
                            },
                            onDelete = {
                                items.removeAt(index)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(items.toList())
                onDismiss()
            }) {
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )

    if (showEditDialog) {
        val targetMacro = editingIndex?.let { items.getOrNull(it) }
        AgentMacroEditDialog(
            initialMacro = targetMacro,
            onDismiss = { showEditDialog = false },
            onConfirm = { edited ->
                val index = editingIndex
                if (index != null && index in items.indices) {
                    items[index] = edited
                } else {
                    items.add(edited)
                }
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun MacroRowCard(
    macro: AgentMacro,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (macro.isDestructive) Color(0xFF3F1D1D) else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = macro.label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (macro.isDestructive) Color(0xFFFCA5A5) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = macro.payload.replace("\n", "\\n").replace("\u0003", "^C"),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.settings_agent_macro_move_up), modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.settings_agent_macro_move_down), modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.settings_agent_macro_edit), modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.settings_agent_macro_delete), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AgentMacroEditDialog(
    initialMacro: AgentMacro?,
    onDismiss: () -> Unit,
    onConfirm: (AgentMacro) -> Unit,
) {
    var label by remember { mutableStateOf(initialMacro?.label ?: "") }
    var payload by remember { mutableStateOf(initialMacro?.payload?.replace("\n", "\\n")?.replace("\u0003", "^C") ?: "") }
    var isDestructive by remember { mutableStateOf(initialMacro?.isDestructive ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialMacro == null) stringResource(R.string.settings_agent_add_macro)
                else stringResource(R.string.settings_agent_edit_macro)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(stringResource(R.string.settings_agent_macro_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = payload,
                    onValueChange = { payload = it },
                    label = { Text(stringResource(R.string.settings_agent_macro_payload)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isDestructive,
                        onCheckedChange = { isDestructive = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.settings_agent_macro_destructive),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (label.isNotBlank() && payload.isNotBlank()) {
                        val parsedPayload = payload
                            .replace("\\n", "\n")
                            .replace("^C", "\u0003")
                        val id = initialMacro?.id ?: "macro_${System.currentTimeMillis()}"
                        onConfirm(
                            AgentMacro(
                                id = id,
                                label = label.trim(),
                                payload = parsedPayload,
                                isDestructive = isDestructive
                            )
                        )
                    }
                },
                enabled = label.isNotBlank() && payload.isNotBlank()
            ) {
                Text(stringResource(R.string.common_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
