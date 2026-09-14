package com.ar9988.domain.util

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class AssistantQueryCleanerTest {
    @Test
    fun `English request words are removed as whole tokens`() {
        assertEquals(listOf("receipts"), AssistantQueryCleaner.clean("Please show me all my receiptS files"))
        assertEquals(listOf("finder", "showreel", "profile"), AssistantQueryCleaner.clean("finder showreel profile"))
        assertEquals(listOf("important", "documents"), AssistantQueryCleaner.clean("Find important documents"))
    }

    @Test
    fun `English normalization does not depend on device locale`() {
        val original = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            assertEquals(listOf("invoices"), AssistantQueryCleaner.clean("FIND INVOICES"))
        } finally {
            Locale.setDefault(original)
        }
    }
}
