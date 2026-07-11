package com.opfis.app.search

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.opfis.app.format.MoneyFormatter
import com.opfis.app.theme.OpfisColors
import com.opfis.domain.search.SearchResult

/**
 * Renders one [SearchResult], shared by the dashboard's quick search
 * entry (ROADMAP Phase 3) and global search (Phase 4). Every match
 * pairs a text prefix naming its record type with a fixed color - never
 * color alone.
 */
@Composable
fun SearchResultRow(result: SearchResult) {
    val (label, tint) =
        when (result) {
            is SearchResult.AccountMatch -> "Account: ${result.account.name}" to OpfisColors.ProfessionalBlue
            is SearchResult.CategoryMatch -> "Category: ${result.category.name}" to OpfisColors.InformationNeutralBlue
            is SearchResult.TransactionMatch ->
                "Transaction: ${result.transaction.description} (${MoneyFormatter.format(
                    result.transaction.amountMinorUnits,
                )})" to
                    OpfisColors.ProfessionalBlue
            is SearchResult.TagMatch -> "Tag: ${result.tag.name}" to OpfisColors.Warning
        }
    Text(label, style = MaterialTheme.typography.bodyMedium, color = tint)
}
