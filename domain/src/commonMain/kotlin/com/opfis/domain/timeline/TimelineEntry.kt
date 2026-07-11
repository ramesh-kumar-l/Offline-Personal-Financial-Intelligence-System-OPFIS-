package com.opfis.domain.timeline

import com.opfis.domain.transaction.Transaction

/**
 * A chronological entry in the transaction timeline (ROADMAP Phase 4,
 * "Timeline search"), paired with its assigned tag ids. Future phases
 * (5-6: documents, memory events) are expected to widen this into a
 * sealed type; Phase 4 scope is transactions only.
 */
data class TimelineEntry(
    val transaction: Transaction,
    val tagIds: List<String>,
)
