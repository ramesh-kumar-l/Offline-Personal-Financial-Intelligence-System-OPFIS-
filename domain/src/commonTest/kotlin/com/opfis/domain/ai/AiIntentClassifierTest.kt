package com.opfis.domain.ai

import kotlin.test.Test
import kotlin.test.assertEquals

class AiIntentClassifierTest {
    @Test
    fun `classifies net worth questions`() {
        assertEquals(AiIntent.NET_WORTH, AiIntentClassifier.classify("What is my net worth?"))
    }

    @Test
    fun `classifies cash flow questions`() {
        assertEquals(AiIntent.CASH_FLOW, AiIntentClassifier.classify("How is my cash flow this month?"))
    }

    @Test
    fun `classifies budget questions before the generic spending bucket`() {
        assertEquals(AiIntent.BUDGET, AiIntentClassifier.classify("Am I over my Groceries budget?"))
    }

    @Test
    fun `classifies goal questions before the generic spending bucket`() {
        assertEquals(AiIntent.GOAL, AiIntentClassifier.classify("What is my progress toward my Emergency Fund goal?"))
    }

    @Test
    fun `classifies spending questions`() {
        assertEquals(AiIntent.SPENDING, AiIntentClassifier.classify("How much did I spend on Groceries?"))
    }

    @Test
    fun `falls back to general for unrecognized questions`() {
        assertEquals(AiIntent.GENERAL, AiIntentClassifier.classify("Tell me about my receipts from last week"))
    }
}
