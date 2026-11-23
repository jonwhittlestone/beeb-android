package org.beebplayer.android

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Profiles on real SharedPreferences: the debug app's own store, cleared first. Debug app only. */
@RunWith(AndroidJUnit4::class)
class ProfileDeviceTest {
    @Test fun profilesKeepSeparateSavedListsAcrossFreshInstances() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        check(context.packageName.endsWith(".debug"))
        context.getSharedPreferences("BeebLocal", Context.MODE_PRIVATE).edit().clear().commit()
        val ep = Programme("m0000001", "Show", "Series 1: 1", showId = "b0000001")

        val profiles = ProfileStore(PrefsKeyValue(context))
        val alice = profiles.add("Alice")!!
        LocalStore(context).toggle(ep)                 // saved for "Me"
        profiles.select(alice.id)
        assertFalse(LocalStore(context).isSaved(ep))   // a new instance, as PlaybackActivity creates
        profiles.startup = "ask"

        val relaunched = ProfileStore(PrefsKeyValue(context))
        assertTrue(relaunched.launch())
        assertEquals(alice.id, relaunched.currentId)
        relaunched.select(ProfileStore.DEFAULT_ID)
        assertTrue(LocalStore(context).isSaved(ep))
        context.getSharedPreferences("BeebLocal", Context.MODE_PRIVATE).edit().clear().commit()
    }
}
