package com.opfis.domain.ai.engine.responder

import com.opfis.domain.ai.AiAnswer
import com.opfis.domain.ai.AiCitation
import com.opfis.domain.ai.AiMoneyFormatter
import com.opfis.domain.ai.FinancialSnapshot
import com.opfis.domain.entity.EntityType
import com.opfis.domain.networth.NetWorthCalculator

/** Answers NET_WORTH questions by reusing Phase 3's [NetWorthCalculator], citing every contributing record. */
internal object NetWorthResponder {
    fun respond(snapshot: FinancialSnapshot): AiAnswer {
        val summary = NetWorthCalculator.calculate(snapshot.accounts, snapshot.assets, snapshot.liabilities)
        val text =
            "Your current net worth is ${AiMoneyFormatter.format(summary.netWorthMinorUnits)}: " +
                "accounts ${AiMoneyFormatter.format(summary.accountBalanceMinorUnits)} + " +
                "assets ${AiMoneyFormatter.format(summary.assetValueMinorUnits)} - " +
                "liabilities ${AiMoneyFormatter.format(summary.liabilityBalanceMinorUnits)}."
        val citations =
            snapshot.accounts.filterNot { it.isArchived }.map { AiCitation(EntityType.ACCOUNT, it.id, it.name) } +
                snapshot.assets.map { AiCitation(EntityType.ASSET, it.id, it.name) } +
                snapshot.liabilities.map { AiCitation(EntityType.LIABILITY, it.id, it.name) }
        return AiAnswer(text, citations)
    }
}
