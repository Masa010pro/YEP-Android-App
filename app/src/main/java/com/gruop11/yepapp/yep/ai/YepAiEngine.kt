package com.gruop11.yepapp.yep.ai

import android.util.Log
import com.gruop11.yepapp.yep.model.*
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout
import java.util.Locale

private const val TAG = "YepAiEngine"

class YepAiEngine {
    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    suspend fun askParticipantAdvisor(
        query: String,
        participant: UserProfile,
        language: AppLanguage,
        isOfflineDataSaver: Boolean,
        snapshot: YepStateSnapshot,
        stageSnapshot: ParticipantStageSnapshot,
    ): Pair<String, Boolean> {
        if (!isOfflineDataSaver) {
            try {
                val contextPrompt = buildParticipantPrompt(
                    query = query,
                    participant = participant,
                    language = language,
                    snapshot = snapshot,
                    stageSnapshot = stageSnapshot,
                )
                val response = withTimeout(45_000L) {
                    model.generateContent(contextPrompt)
                }
                val text = response.text?.trim()
                if (!text.isNullOrBlank()) {
                    return text to true
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Falling back to deterministic offline advisor: ${e.message}")
            }
        }
        return deterministicParticipantReply(
            query = query,
            participant = participant,
            language = language,
            snapshot = snapshot,
            stageSnapshot = stageSnapshot,
        ) to false
    }

    suspend fun runFacilitatorDiagnostic(
        mode: String,
        squadSize: Int,
        customQuery: String,
        language: AppLanguage,
        isOfflineDataSaver: Boolean,
        snapshot: YepStateSnapshot,
        participantsWithStages: List<Pair<UserProfile, ParticipantStageSnapshot>>,
    ): Pair<String, Boolean> {
        val offlineResult = deterministicFacilitatorReply(
            mode = mode,
            squadSize = squadSize,
            customQuery = customQuery,
            language = language,
            snapshot = snapshot,
            participantsWithStages = participantsWithStages,
        )
        if (!isOfflineDataSaver && mode != "GROUP") {
            try {
                val prompt = buildFacilitatorPrompt(
                    mode = mode,
                    customQuery = customQuery,
                    language = language,
                    participantsWithStages = participantsWithStages,
                    snapshot = snapshot,
                )
                val response = withTimeout(45_000L) {
                    model.generateContent(prompt)
                }
                val text = response.text?.trim()
                if (!text.isNullOrBlank()) {
                    return text to true
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Falling back to deterministic facilitator engine: ${e.message}")
            }
        }
        return offlineResult to false
    }

    private fun buildParticipantPrompt(
        query: String,
        participant: UserProfile,
        language: AppLanguage,
        snapshot: YepStateSnapshot,
        stageSnapshot: ParticipantStageSnapshot,
    ): String {
        val myEnrollments = snapshot.enrollments.filter { it.userId == participant.id }
        val enrolledTitles = myEnrollments.mapNotNull { enr ->
            snapshot.exchanges.find { it.id == enr.exchangeId }?.let { "${it.title} (${enr.progressPercent}%)" }
        }
        val mySubs = snapshot.submissions.filter { it.userId == participant.id }.map { sub ->
            val score = sub.rubricScores?.let { String.format(Locale.US, "%.2f/5.00", it.mean) } ?: "Pending"
            "${sub.projectTitle}: $score (${sub.syncStatus.name})"
        }
        val openings = snapshot.opportunities.filter { !it.isFlagged }.map { opp ->
            "${opp.title} at ${opp.organization} (${opp.compensation}, ${opp.workType}) requiring ${opp.requiredSkills.joinToString()}"
        }
        val availableExchanges = snapshot.exchanges.filter { !it.isFlagged && !it.isFull }.map { ex ->
            "${ex.title} [${ex.skillLevel.label}, ${ex.enrolledCount}/${ex.maxCapacity}]"
        }

        return """
            You are the YEP Platform Career & Learning Advisor for an East & West African youth empowerment mobile app.
            Respond concisely (4-6 bullet points or 2 short paragraphs) in ${language.nativeName} (${language.code}).
            Ground every answer strictly in this live participant data:
            - Participant: ${participant.name}, Age ${participant.age}, Hub: ${participant.regionalHub}
            - Skills: ${participant.skills.joinToString()}
            - Career Goal: ${participant.careerGoal}
            - Partner Data Consent: ${if (participant.consentDataSharing) "ENABLED" else "DISABLED"}
            - Learning Stage: Stage ${stageSnapshot.currentStage}/6 (${stageSnapshot.skillTier.label}, ${stageSnapshot.overallPercent}%)
            - Enrolled Exchanges: ${enrolledTitles.ifEmpty { listOf("None yet") }.joinToString("; ")}
            - Submissions: ${mySubs.ifEmpty { listOf("None yet") }.joinToString("; ")}
            - Open Partner Roles: ${openings.joinToString("; ")}
            - Open Virtual Exchanges: ${availableExchanges.joinToString("; ")}
            - Rubric Rule: 4 standardized pillars (Technical Execution, Problem Solving, Practical Impact, Presentation Clarity) scored 1–5.
            - Consent Rule: Scores & capstones remain private until explicit consent is granted per EOI or profile switch.

            User Question: $query
        """.trimIndent()
    }

    private fun buildFacilitatorPrompt(
        mode: String,
        customQuery: String,
        language: AppLanguage,
        participantsWithStages: List<Pair<UserProfile, ParticipantStageSnapshot>>,
        snapshot: YepStateSnapshot,
    ): String {
        val roster = participantsWithStages.joinToString("\n") { (user, stage) ->
            val subs = snapshot.submissions.filter { it.userId == user.id }
            val graded = subs.mapNotNull { it.rubricScores?.mean }
            val avg = if (graded.isEmpty()) "Ungraded" else String.format(Locale.US, "%.2f/5.00", graded.average())
            "- ${user.name} (${user.regionalHub}): Stage ${stage.currentStage}/6, Tier=${stage.skillTier.label}, Overall=${stage.overallPercent}%, MeanRubric=$avg, Skills=[${user.skills.joinToString()}]"
        }
        val task = if (mode == "COMPETENCY") {
            "Provide a crisp cohort competency evaluation highlighting strengths, skill gaps, and 2 targeted mentorship actions."
        } else {
            "Answer this facilitator readiness query: $customQuery"
        }
        return """
            You are the YEP Facilitator AI Diagnostic Assistant.
            Respond concisely in ${language.nativeName} (${language.code}) using this live cohort data:
            $roster

            Task: $task
        """.trimIndent()
    }

    private fun deterministicParticipantReply(
        query: String,
        participant: UserProfile,
        language: AppLanguage,
        snapshot: YepStateSnapshot,
        stageSnapshot: ParticipantStageSnapshot,
    ): String {
        val q = query.lowercase(Locale.US)
        val userSkillsLower = participant.skills.map { it.lowercase(Locale.US) }

        val rankedJobs = snapshot.opportunities.filter { !it.isFlagged }.map { opp ->
            val overlap = opp.requiredSkills.count { req ->
                userSkillsLower.any { s -> s.contains(req.lowercase(Locale.US)) || req.lowercase(Locale.US).contains(s) }
            }
            opp to overlap
        }.sortedByDescending { it.second }

        val enrolledIds = snapshot.enrollments.filter { it.userId == participant.id }.map { it.exchangeId }.toSet()
        val nextExchange = snapshot.exchanges
            .filter { !it.isFlagged && !it.isFull && it.id !in enrolledIds }
            .firstOrNull() ?: snapshot.exchanges.firstOrNull { !it.isFlagged }

        val prefix = languageHeader(language)

        return when {
            q.contains("job") || q.contains("match") || q.contains("partner") || q.contains("kazi") || q.contains("rimu") || q.contains("tic") -> {
                val top = rankedJobs.take(2).joinToString("\n") { (opp, matches) ->
                    "• ${opp.title} (${opp.organization}) — ${opp.compensation}, ${opp.workType} [$matches skill matches]"
                }
                "$prefix\nTop Partner Matches for ${participant.name} (${participant.skills.take(3).joinToString()}):\n$top\n\nTip: Your Partner Consent is currently ${if (participant.consentDataSharing) "ON" else "OFF"}. Submit an Expression of Interest on the Opportunities tab to share your verified capstone."
            }
            q.contains("exchange") || q.contains("course") || q.contains("learn") || q.contains("mafunzo") || q.contains("somo") || q.contains("kwan") -> {
                val rec = nextExchange?.let {
                    "• Recommended: ${it.title} (${it.skillLevel.label}, ${it.enrolledCount}/${it.maxCapacity} seats)\n• Format: ${it.formatLabel} · ${it.durationLabel}\n• Key outcome: ${it.outcomes.firstOrNull().orEmpty()}"
                } ?: "• All open cohorts are currently enrolled."
                "$prefix\nStage ${stageSnapshot.currentStage}/6 (${stageSnapshot.overallPercent}% — ${stageSnapshot.skillTier.label}):\n$rec"
            }
            q.contains("rubric") || q.contains("score") || q.contains("grade") || q.contains("pimo") || q.contains("rubriki") -> {
                val mean = String.format(Locale.US, "%.2f", stageSnapshot.rubricAverages.mean)
                "$prefix\nCapstone Rubric Scoring (1–5 Scale):\n• 4 Standardized Pillars: Technical Execution, Problem Solving, Practical Impact, and Presentation Clarity.\n• Your current rubric mean: $mean / 5.00.\n• Mentors review both your GitHub repo and architecture summary before unlocking Partner Readiness (Stage 6)."
            }
            q.contains("consent") || q.contains("privacy") || q.contains("ridhaa") || q.contains("yee") -> {
                "$prefix\nPortfolio Consent & Data Sovereignty:\n• Your capstones, rubric scores, and contact details stay private by default.\n• Partners only view your snapshot when your Dashboard Consent switch is ON AND you confirm the explicit consent checkbox on an Expression of Interest.\n• Every consent grant is recorded in the immutable Audit Trail."
            }
            else -> {
                val bestJob = rankedJobs.firstOrNull()?.first
                "$prefix\nSummary for ${participant.name}: Stage ${stageSnapshot.currentStage}/6 (${stageSnapshot.overallPercent}%), Tier: ${stageSnapshot.skillTier.label}.\n• Best job match: ${bestJob?.title ?: "None"} at ${bestJob?.organization ?: "Partner"}.\n• Next recommended course: ${nextExchange?.title ?: "Complete active study pack"}.\n• Ask me about job matches, exchanges, rubrics, or portfolio consent!"
            }
        }
    }

    private fun deterministicFacilitatorReply(
        mode: String,
        squadSize: Int,
        customQuery: String,
        language: AppLanguage,
        snapshot: YepStateSnapshot,
        participantsWithStages: List<Pair<UserProfile, ParticipantStageSnapshot>>,
    ): String {
        val header = languageHeader(language)
        if (mode == "GROUP") {
            val size = squadSize.coerceIn(2, 4)
            val ordered = participantsWithStages.sortedWith(
                compareByDescending<Pair<UserProfile, ParticipantStageSnapshot>> { it.second.skillTier.ordinal }
                    .thenByDescending { it.second.overallPercent },
            )
            val squadCount = ((ordered.size + size - 1) / size).coerceAtLeast(1)
            val buckets = MutableList(squadCount) { mutableListOf<Pair<UserProfile, ParticipantStageSnapshot>>() }
            ordered.forEachIndexed { idx, pair ->
                val round = idx / squadCount
                val pos = idx % squadCount
                val targetBucket = if (round % 2 == 0) pos else (squadCount - 1 - pos)
                buckets[targetBucket].add(pair)
            }
            val squadText = buckets.mapIndexed { idx, members ->
                val memberLines = members.joinToString("\n") { (u, st) ->
                    "   - ${u.name} (${st.skillTier.label} · ${st.overallPercent}% · ${u.regionalHub})"
                }
                "Squad ${idx + 1} (${members.size} learners):\n$memberLines"
            }.joinToString("\n\n")

            return "$header\nBalanced Squad Formation (Target size: $size)\nPedagogical Rationale: Serpentine pairing distributes Advanced/Intermediate builders alongside Beginner learners for peer code reviews and regional cross-pollination.\n\n$squadText"
        }

        if (mode == "COMPETENCY") {
            val avgOverall = if (participantsWithStages.isEmpty()) 0 else participantsWithStages.map { it.second.overallPercent }.average().toInt()
            val pendingCount = snapshot.submissions.count { it.rubricScores == null }
            val ready = participantsWithStages.filter { it.second.overallPercent >= 75 }.joinToString { it.first.name }
            val needsSupport = participantsWithStages.filter { it.second.overallPercent < 65 }.joinToString { it.first.name }
            return buildString {
                append("$header\nCohort Competency Diagnostic:\n")
                append("• Cohort Readiness Mean: $avgOverall% across ${participantsWithStages.size} active learners.\n")
                append("• Pending Capstone Evaluations: $pendingCount queued for 1–5 rubric review.\n")
                append("• Partner-Ready Fellows: ${ready.ifBlank { "In progress" }}.\n")
                append("• Priority Mentorship Check-in: ${needsSupport.ifBlank { "All learners on track (>=65%)" }}.")
            }
        }

        val q = customQuery.lowercase(Locale.US)
        val readyList = participantsWithStages.filter { (_, st) ->
            st.currentStage >= 5 || st.overallPercent >= 75
        }
        val interventionList = participantsWithStages.filter { (u, st) ->
            val hasGraded = snapshot.submissions.any { it.userId == u.id && it.rubricScores != null }
            st.overallPercent < 70 || !hasGraded
        }

        return when {
            q.contains("interview") || q.contains("ready") || q.contains("partner") -> {
                val lines = readyList.joinToString("\n") { (u, st) ->
                    val mean = String.format(Locale.US, "%.2f", st.rubricAverages.mean)
                    "• ${u.name} (${u.regionalHub}) — Stage ${st.currentStage}/6, ${st.overallPercent}%, Mean Rubric: $mean/5.00, Consent: ${if (u.consentDataSharing) "Yes" else "Private"}"
                }.ifBlank { "• No participants have reached Stage 5+ yet." }
                "$header\nInterview-Ready Candidates:\n$lines"
            }
            q.contains("intervention") || q.contains("need") || q.contains("help") || q.contains("risk") -> {
                val lines = interventionList.joinToString("\n") { (u, st) ->
                    "• ${u.name} (${u.regionalHub}) — Stage ${st.currentStage}/6 (${st.overallPercent}%). Action: Schedule 1:1 rubric coaching or unblock capstone submission."
                }.ifBlank { "• All cohort participants are currently progressing smoothly." }
                "$header\nParticipants Flagged for Mentorship Intervention:\n$lines"
            }
            else -> {
                val summary = participantsWithStages.joinToString("\n") { (u, st) ->
                    "• ${u.name}: Stage ${st.currentStage}/6 (${st.skillTier.label}, ${st.overallPercent}%)"
                }
                "$header\nCohort Readiness Snapshot:\n$summary"
            }
        }
    }

    private fun languageHeader(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> "[EN · YEP Offline/Grounded Engine]"
        AppLanguage.SW -> "[SW · Injini ya Ushauri ya YEP]"
        AppLanguage.LG -> "[LG · Omuyambi wa YEP]"
        AppLanguage.NYN -> "[NYN · Omuhanuuzi wa YEP]"
        AppLanguage.RW -> "[RW · Umujyanama wa YEP]"
        AppLanguage.ACH -> "[ACH · Lakony pa YEP]"
    }
}
