package com.shrivatsav.monomail.core.data.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class DockConfigJsonTest {
    @Test
    fun parsesPersistedObjectShape() {
        assertEquals(
            listOf(DockTabId.INBOX, DockTabId.SENT, DockTabId.ARCHIVED),
            SettingsDataStore.parseDockConfigJson("""{"primaryTabs":["INBOX","SENT","ARCHIVED"]}""").primaryTabs
        )
    }

    @Test
    fun removesUnknownAndDuplicateTabs() {
        assertEquals(
            listOf(DockTabId.STARRED, DockTabId.SNOOZED),
            SettingsDataStore.parseDockConfigJson("""{"primaryTabs":["STARRED","UNKNOWN","STARRED","SNOOZED"]}""").primaryTabs
        )
    }

    @Test
    fun capsSavedConfigurationAtThreeSlots() {
        assertEquals(
            listOf(DockTabId.INBOX, DockTabId.SENT, DockTabId.TRASH),
            SettingsDataStore.parseDockConfigJson("""{"primaryTabs":["INBOX","SENT","TRASH","SPAM"]}""").primaryTabs
        )
    }

    @Test
    fun fallsBackForMalformedOrEmptyConfiguration() {
        assertEquals(DockConfig.defaults(), SettingsDataStore.parseDockConfigJson("not-json"))
        assertEquals(DockConfig.defaults(), SettingsDataStore.parseDockConfigJson("""{"primaryTabs":[]}"""))
    }

    @Test
    fun ignoresNonStringAndUnifiedTabs() {
        assertEquals(
            listOf(DockTabId.STARRED, DockTabId.SNOOZED),
            SettingsDataStore.parseDockConfigJson(
                """{"primaryTabs":["UNIFIED",true,17,"STARRED","SNOOZED"]}"""
            ).primaryTabs
        )
    }

    @Test
    fun normalizedConfigAlwaysHasAtMostThreeVisibleTabs() {
        assertEquals(
            listOf(DockTabId.INBOX, DockTabId.SENT, DockTabId.ARCHIVED),
            DockConfig.normalized(
                listOf(
                    DockTabId.UNIFIED,
                    DockTabId.INBOX,
                    DockTabId.SENT,
                    DockTabId.ARCHIVED,
                    DockTabId.SPAM
                )
            ).primaryTabs
        )
    }
}
