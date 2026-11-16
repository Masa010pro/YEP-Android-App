package com.gruop11.yepapp

import com.gruop11.yepapp.yep.ai.YepAiEngine
import com.gruop11.yepapp.yep.data.SeedData
import com.gruop11.yepapp.yep.i18n.YepTranslations
import com.gruop11.yepapp.yep.model.AppLanguage
import com.gruop11.yepapp.yep.model.ParticipantStageSnapshot
import com.gruop11.yepapp.yep.model.RubricScores
import com.gruop11.yepapp.yep.model.SkillTier
import com.gruop11.yepapp.yep.model.StageMilestone
import com.gruop11.yepapp.yep.model.UserRole
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class YepDomainUnitTest {
    @Test
    fun seeded_accounts_and_courses_match_specification() {
        val snap = SeedData.createInitialSnapshot()
        assertEquals(6, snap.users.size)
        assertEquals(4, snap.exchanges.size)
        assertEquals(3, snap.opportunities.size)
        assertTrue(snap.exchanges.any { it.isFull && it.enrolledCount == 50 })
        assertTrue(snap.users.any { it.name == "Amara Okafor" && it.age == 22 })
        assertTrue(snap.users.any { it.name == "Dr. Marcus Vance" && it.role == UserRole.MENTOR })
        assertTrue(snap.users.any { it.name == "Elena Rostova" && it.role == UserRole.PARTNER })
        assertTrue(snap.users.any { it.name == "Sarah Chen" && it.role == UserRole.ADMIN })
    }

    @Test
    fun all_six_languages_have_non_blank_translations() {
        AppLanguage.entries.forEach { lang ->
            val t = YepTranslations.forLanguage(lang)
            assertTrue(t.navOverview.isNotBlank())
            assertTrue(t.aiAdvisorGreeting.isNotBlank())
            assertTrue(t.ageEligibilityBlocked.isNotBlank())
        }
    }

    @Test
    fun offline_ai_engine_generates_squads_and_advisor_reply() = runBlocking {
        val snap = SeedData.createInitialSnapshot()
        val engine = YepAiEngine()
        val amara = snap.users.first { it.id == "u_amara" }
        val stageSnap = ParticipantStageSnapshot(
            userId = amara.id,
            currentStage = 6,
            skillTier = SkillTier.ADVANCED,
            overallPercent = 92,
            stages = (1..6).map { StageMilestone(it, "s$it", 90, true) },
            rubricAverages = RubricScores(5, 4, 5, 4),
        )
        val (reply, usedCloud) = engine.askParticipantAdvisor(
            query = "Match partner jobs to my skills",
            participant = amara,
            language = AppLanguage.SW,
            isOfflineDataSaver = true,
            snapshot = snap,
            stageSnapshot = stageSnap,
        )
        assertEquals(false, usedCloud)
        assertTrue(reply.contains("AfriTech Horizons"))
    }
}
