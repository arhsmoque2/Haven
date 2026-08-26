package sh.haven.feature.terminal.arh

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sh.haven.core.data.preferences.AgentMacro

/**
 * Quick-action rail rendering 1-tap agent approvals and custom macro buttons
 * directly above Haven's virtual keyboard toolbar.
 */
@Composable
fun AgentMacroBar(
    macros: List<AgentMacro>,
    onSendPayload: (String) -> Unit,
    onOpenCodeExtractor: () -> Unit,
    onOpenSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quick 1-Tap Code Extractor Trigger
            IconButton(
                onClick = onOpenCodeExtractor,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DataObject,
                    contentDescription = "Extract Code Blocks",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Macro Chips
            macros.forEach { macro ->
                val isApproval = macro.id.contains("approve", ignoreCase = true) || macro.label.startsWith("Approve", ignoreCase = true)
                val isRejection = macro.isDestructive || macro.id.contains("reject", ignoreCase = true) || macro.label.startsWith("Reject", ignoreCase = true)

                val chipColors = when {
                    isApproval -> FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF065F46),
                        labelColor = Color(0xFFD1FAE5),
                        iconColor = Color(0xFF34D399)
                    )
                    isRejection -> FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF7F1D1D),
                        labelColor = Color(0xFFFEE2E2),
                        iconColor = Color(0xFFF87171)
                    )
                    else -> FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        iconColor = MaterialTheme.colorScheme.primary
                    )
                }

                FilterChip(
                    selected = false,
                    onClick = { onSendPayload(macro.payload) },
                    label = {
                        Text(
                            text = macro.label,
                            fontSize = 11.sp,
                            fontWeight = if (isApproval || isRejection) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    leadingIcon = {
                        val icon = when {
                            isApproval -> Icons.Default.Check
                            isRejection -> Icons.Default.Close
                            else -> Icons.Default.Bolt
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    },
                    colors = chipColors,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(30.dp)
                )
            }

            if (onOpenSettings != null) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configure Macros",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
