package com.opfis.app.security

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.opfis.app.theme.OpfisColors
import com.opfis.domain.audit.AuditEventType
import com.opfis.domain.audit.AuditLogEntry

/** One entry in the audit trail - a fixed glyph+color pair per event kind, never color alone. */
@Composable
internal fun AuditLogRow(entry: AuditLogEntry) {
    val (glyph, tint) =
        when (entry.eventType) {
            AuditEventType.APP_UNLOCKED -> "🔓" to OpfisColors.Success
            AuditEventType.APP_UNLOCK_FAILED -> "⚠" to OpfisColors.Error
            AuditEventType.BACKUP_EXPORTED -> "⇧" to OpfisColors.InformationNeutralBlue
            AuditEventType.BACKUP_RESTORED -> "⇩" to OpfisColors.InformationNeutralBlue
        }
    Column {
        Text("$glyph ${entry.eventType.name}", style = MaterialTheme.typography.bodyLarge, color = tint)
        Text(entry.description, style = MaterialTheme.typography.bodySmall)
    }
}
