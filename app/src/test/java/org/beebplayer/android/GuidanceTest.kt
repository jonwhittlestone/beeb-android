package org.beebplayer.android

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class GuidanceTest {
    private fun episode(version: String) = JSONObject("""{"id":"m0000001","title":"Show","subtitle":"Series 1: 1","versions":[$version]}""")

    @Test fun readsTheLargeGuidanceTextFromThePlayableVersion() {
        val p = Programme.episode(episode("""{"id":"m0000011","kind":"original","guidance":{"text":{"small":"Strong language","large":"Contains strong language and violence."}}}"""))
        assertEquals("Contains strong language and violence.", p.guidance)
    }
    @Test fun noGuidanceMeansEmpty() {
        assertEquals("", Programme.episode(episode("""{"id":"m0000011","kind":"original"}""")).guidance)
    }
    @Test fun guidanceSurvivesSavingAndRestoring() {
        val p = Programme("m0000001", "Show", guidance = "Contains scenes of peril.")
        assertEquals(p, Programme.restore(p.json()))
    }
}
