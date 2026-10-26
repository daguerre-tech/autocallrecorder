package com.example

import com.example.recorder.ContactsHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testNormalizePhoneNumber() {
        val raw = "+33 (0)6 12-34 56.78"
        val normalized = ContactsHelper.normalizePhoneNumber(raw)
        assertEquals("+330612345678", normalized)
    }

    @Test
    fun testFormatDisplayNumber() {
        val french = "0612345678"
        val formatted = ContactsHelper.formatDisplayNumber(french)
        assertEquals("06 12 34 56 78", formatted)
    }
}
