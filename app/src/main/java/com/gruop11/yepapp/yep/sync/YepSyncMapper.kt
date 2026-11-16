package com.gruop11.yepapp.yep.sync

import com.gruop11.yepapp.yep.data.SeedData
import com.gruop11.yepapp.yep.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object YepSyncMapper {

    fun toJson(snapshot: YepStateSnapshot): JSONObject {
        return JSONObject().apply {
            put("users", JSONArray().apply {
                snapshot.users.forEach { u ->
                    put(JSONObject().apply {
                        put("id", u.id)
                        put("name", u.name)
                        put("email", u.email)
                        put("password", u.password)
                        put("role", u.role.name.lowercase(Locale.US))
                        put("title", u.title)
                        put("organization", u.organization)
                        put("regionalHub", u.regionalHub)
                        put("age", u.age)
                        put("skills", JSONArray(u.skills))
                        put("interests", JSONArray(u.interests))
                        put("bio", u.bio)
                        put("careerGoal", u.careerGoal)
                        put("photoUri", u.photoUri ?: JSONObject.NULL)
                        put("avatarUrl", u.photoUri ?: JSONObject.NULL)
                        put("consentDataSharing", u.consentDataSharing)
                        put("isSuspended", u.isSuspended)
                        put("suspensionReason", u.suspensionReason ?: JSONObject.NULL)
                        put("badges", JSONArray(u.badges))
                    })
                }
            })

            put("exchanges", JSONArray().apply {
                snapshot.exchanges.forEach { ex ->
                    put(JSONObject().apply {
                        put("id", ex.id)
                        put("title", ex.title)
                        put("theme", ex.theme)
                        put("skillLevel", ex.skillLevel.label)
                        put("formatLabel", ex.formatLabel)
                        put("format", ex.formatLabel)
                        put("durationLabel", ex.durationLabel)
                        put("duration", ex.durationLabel)
                        put("enrolledCount", ex.enrolledCount)
                        put("maxCapacity", ex.maxCapacity)
                        put("capacity", ex.maxCapacity)
                        put("description", ex.description)
                        put("outcomes", JSONArray(ex.outcomes))
                        put("prerequisites", JSONArray(ex.prerequisites))
                        put("assignmentPrompt", ex.assignmentPrompt)
                        put("studyPackMarkdown", ex.studyPackMarkdown)
                        put("studyChecklist", JSONArray().apply {
                            ex.studyChecklist.forEach { item ->
                                put(JSONObject().apply {
                                    put("id", item.id)
                                    put("title", item.title)
                                })
                            }
                        })
                        put("mentorId", ex.mentorId)
                        put("isFlagged", ex.isFlagged)
                    })
                }
            })

            put("liveSessions", JSONArray().apply {
                snapshot.liveSessions.forEach { ses ->
                    put(JSONObject().apply {
                        put("id", ses.id)
                        put("exchangeId", ses.exchangeId)
                        put("title", ses.title)
                        put("scheduledAt", ses.scheduledAt)
                        put("durationLabel", ses.durationLabel)
                        put("agenda", ses.agenda)
                        put("meetingUrl", ses.meetingUrl)
                    })
                }
            })

            put("enrollments", JSONArray().apply {
                snapshot.enrollments.forEach { enr ->
                    put(JSONObject().apply {
                        put("id", enr.id)
                        put("userId", enr.userId)
                        put("exchangeId", enr.exchangeId)
                        put("progressPercent", enr.progressPercent)
                        put("progress", enr.progressPercent)
                        put("completedChecklistIds", JSONArray(enr.completedChecklistIds))
                        put("enrolledAt", enr.enrolledAt)
                    })
                }
            })

            put("submissions", JSONArray().apply {
                snapshot.submissions.forEach { sub ->
                    put(JSONObject().apply {
                        put("id", sub.id)
                        put("userId", sub.userId)
                        put("exchangeId", sub.exchangeId)
                        put("projectTitle", sub.projectTitle)
                        put("architectureDescription", sub.architectureDescription)
                        put("githubUrl", sub.githubUrl)
                        put("demoUrl", sub.demoUrl)
                        put("keyTakeaways", sub.keyTakeaways)
                        put("syncStatus", sub.syncStatus.name.lowercase(Locale.US))
                        put("submittedAt", sub.submittedAt)
                        if (sub.rubricScores != null) {
                            put("rubricScores", JSONObject().apply {
                                put("technicalExecution", sub.rubricScores.technicalExecution)
                                put("problemSolving", sub.rubricScores.problemSolving)
                                put("practicalImpact", sub.rubricScores.practicalImpact)
                                put("presentationClarity", sub.rubricScores.presentationClarity)
                                put("mean", sub.rubricScores.mean)
                            })
                        } else {
                            put("rubricScores", JSONObject.NULL)
                        }
                        put("qualitativeFeedback", sub.qualitativeFeedback ?: JSONObject.NULL)
                        put("evaluatedByMentorId", sub.evaluatedByMentorId ?: JSONObject.NULL)
                        put("evaluatedAt", sub.evaluatedAt ?: JSONObject.NULL)
                    })
                }
            })

            put("opportunities", JSONArray().apply {
                snapshot.opportunities.forEach { opp ->
                    put(JSONObject().apply {
                        put("id", opp.id)
                        put("partnerId", opp.partnerId)
                        put("organization", opp.organization)
                        put("title", opp.title)
                        put("compensation", opp.compensation)
                        put("workType", opp.workType)
                        put("location", opp.location)
                        put("description", opp.description)
                        put("requiredSkills", JSONArray(opp.requiredSkills))
                        put("isFlagged", opp.isFlagged)
                        put("createdAt", opp.createdAt)
                    })
                }
            })

            put("expressionsOfInterest", JSONArray().apply {
                snapshot.eois.forEach { eoi ->
                    put(JSONObject().apply {
                        put("id", eoi.id)
                        put("opportunityId", eoi.opportunityId)
                        put("participantId", eoi.participantId)
                        put("status", eoi.status.key)
                        put("consentConfirmed", eoi.consentConfirmed)
                        put("coverNote", eoi.coverNote)
                        put("createdAt", eoi.createdAt)
                    })
                }
            })

            put("messages", JSONArray().apply {
                snapshot.messages.forEach { msg ->
                    put(JSONObject().apply {
                        put("id", msg.id)
                        put("eoiId", msg.eoiId)
                        put("senderId", msg.senderId)
                        put("senderName", msg.senderName)
                        put("body", msg.body)
                        put("timestamp", msg.timestamp)
                    })
                }
            })

            put("notifications", JSONArray().apply {
                snapshot.notifications.forEach { n ->
                    put(JSONObject().apply {
                        put("id", n.id)
                        put("category", n.category.key)
                        put("title", n.title)
                        put("message", n.message)
                        put("recipientId", n.recipientId ?: JSONObject.NULL)
                        put("recipientRole", n.recipientRole?.name?.lowercase(Locale.US) ?: JSONObject.NULL)
                        put("targetScreen", n.targetScreen)
                        put("targetEntityId", n.targetEntityId ?: JSONObject.NULL)
                        put("createdAt", n.createdAt)
                        put("readByUserIds", JSONArray(n.readByUserIds))
                        put("dismissedByUserIds", JSONArray(n.dismissedByUserIds))
                    })
                }
            })

            put("auditLogs", JSONArray().apply {
                snapshot.auditLogs.forEach { log ->
                    put(JSONObject().apply {
                        put("id", log.id)
                        put("actionType", log.actionType.name)
                        put("actorId", log.actorId)
                        put("actorName", log.actorName)
                        put("summary", log.summary)
                        put("timestamp", log.timestamp)
                    })
                }
            })
        }
    }

    /**
     * Parses remote JSON from the Web App and merges cleanly with local state,
     * preserving any local PENDING_SYNC capstone submissions that haven't uploaded yet.
     */
    fun mergeFromRemoteJson(
        remoteJson: JSONObject,
        localSnapshot: YepStateSnapshot,
    ): YepStateSnapshot {
        val fallback = SeedData.createInitialSnapshot()

        val users = parseUsers(remoteJson.optJSONArray("users"))
            .ifEmpty { localSnapshot.users }

        val exchanges = parseExchanges(remoteJson.optJSONArray("exchanges"), fallback.exchanges)
            .ifEmpty { localSnapshot.exchanges }

        val liveSessions = parseLiveSessions(remoteJson.optJSONArray("liveSessions"))
            .ifEmpty { localSnapshot.liveSessions }

        val enrollments = parseEnrollments(remoteJson.optJSONArray("enrollments"))
            .let { parsed ->
                if (remoteJson.has("enrollments")) parsed else localSnapshot.enrollments
            }

        val remoteSubmissions = parseSubmissions(remoteJson.optJSONArray("submissions"))
        val localPending = localSnapshot.submissions.filter { it.syncStatus == SyncStatus.PENDING_SYNC }
        val submissions = if (remoteJson.has("submissions")) {
            val remoteIds = remoteSubmissions.map { it.id }.toSet()
            remoteSubmissions + localPending.filter { it.id !in remoteIds }
        } else {
            localSnapshot.submissions
        }

        val opportunities = parseOpportunities(remoteJson.optJSONArray("opportunities"))
            .let { parsed ->
                if (remoteJson.has("opportunities") && parsed.isNotEmpty()) parsed else localSnapshot.opportunities
            }

        val eoisArray = remoteJson.optJSONArray("expressionsOfInterest")
            ?: remoteJson.optJSONArray("eois")
        val eois = if (eoisArray != null) parseEois(eoisArray) else localSnapshot.eois

        val messages = if (remoteJson.has("messages")) {
            parseMessages(remoteJson.optJSONArray("messages"))
        } else {
            localSnapshot.messages
        }

        val notifications = if (remoteJson.has("notifications")) {
            parseNotifications(remoteJson.optJSONArray("notifications"))
        } else {
            localSnapshot.notifications
        }

        val auditLogs = if (remoteJson.has("auditLogs")) {
            parseAuditLogs(remoteJson.optJSONArray("auditLogs"))
        } else {
            localSnapshot.auditLogs
        }

        return YepStateSnapshot(
            users = users,
            exchanges = exchanges,
            liveSessions = liveSessions,
            enrollments = enrollments,
            submissions = submissions,
            opportunities = opportunities,
            eois = eois,
            messages = messages,
            notifications = notifications,
            auditLogs = auditLogs,
        )
    }

    private fun parseUsers(arr: JSONArray?): List<UserProfile> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            val id = o.optString("id").ifBlank { return@mapNotNull null }
            val role = parseRole(o.optString("role", "participant")) ?: UserRole.PARTICIPANT
            val avatar = o.optStringOrNull("photoUri")
                ?: o.optStringOrNull("avatarUrl")
                ?: o.optStringOrNull("photoUrl")
                ?: o.optStringOrNull("avatar")
            UserProfile(
                id = id,
                name = o.optString("name", "YEP Member"),
                email = o.optString("email", ""),
                password = o.optString("password", "YepUser@2026"),
                role = role,
                title = o.optString("title", role.label),
                organization = o.optString("organization", "YEP Platform"),
                regionalHub = o.optString("regionalHub", o.optString("hub", "Kampala, Uganda")),
                age = o.optInt("age", 22),
                skills = o.optStringList("skills"),
                interests = o.optStringList("interests"),
                bio = o.optString("bio", ""),
                careerGoal = o.optString("careerGoal", ""),
                photoUri = avatar,
                consentDataSharing = o.optBoolean("consentDataSharing", o.optBoolean("consent", false)),
                isSuspended = o.optBoolean("isSuspended", o.optBoolean("suspended", false)),
                suspensionReason = o.optStringOrNull("suspensionReason"),
                badges = o.optStringList("badges"),
            )
        }
    }

    private fun parseExchanges(arr: JSONArray?, defaults: List<VirtualExchange>): List<VirtualExchange> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            val id = o.optString("id").ifBlank { return@mapNotNull null }
            val fallback = defaults.firstOrNull { it.id == id }
            val checklistArr = o.optJSONArray("studyChecklist")
            val checklist = if (checklistArr != null) {
                (0 until checklistArr.length()).mapNotNull { j ->
                    val c = checklistArr.optJSONObject(j) ?: return@mapNotNull null
                    StudyChecklistItem(
                        id = c.optString("id", "chk_$j"),
                        title = c.optString("title", ""),
                    )
                }
            } else {
                fallback?.studyChecklist ?: emptyList()
            }

            VirtualExchange(
                id = id,
                title = o.optString("title", fallback?.title ?: "Virtual Exchange"),
                theme = o.optString("theme", fallback?.theme ?: "Technology"),
                skillLevel = parseSkillTier(o.optString("skillLevel", o.optString("level", "Intermediate"))),
                formatLabel = o.optString("formatLabel", o.optString("format", fallback?.formatLabel ?: "4-Week Cohort")),
                durationLabel = o.optString("durationLabel", o.optString("duration", fallback?.durationLabel ?: "4 Weeks")),
                enrolledCount = o.optInt("enrolledCount", o.optInt("enrolled", fallback?.enrolledCount ?: 0)),
                maxCapacity = o.optInt("maxCapacity", o.optInt("capacity", fallback?.maxCapacity ?: 40)),
                description = o.optString("description", fallback?.description ?: ""),
                outcomes = o.optStringList("outcomes").ifEmpty { fallback?.outcomes ?: emptyList() },
                prerequisites = o.optStringList("prerequisites").ifEmpty { fallback?.prerequisites ?: emptyList() },
                assignmentPrompt = o.optString("assignmentPrompt", fallback?.assignmentPrompt ?: ""),
                studyPackMarkdown = o.optString("studyPackMarkdown", fallback?.studyPackMarkdown ?: ""),
                studyChecklist = checklist,
                mentorId = o.optString("mentorId", fallback?.mentorId ?: "u_marcus"),
                isFlagged = o.optBoolean("isFlagged", o.optBoolean("flagged", false)),
            )
        }
    }

    private fun parseLiveSessions(arr: JSONArray?): List<LiveSession> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            LiveSession(
                id = o.optString("id", "ses_$i"),
                exchangeId = o.optString("exchangeId", ""),
                title = o.optString("title", "Live Session"),
                scheduledAt = o.optString("scheduledAt", ""),
                durationLabel = o.optString("durationLabel", "60 min"),
                agenda = o.optString("agenda", ""),
                meetingUrl = o.optString("meetingUrl", ""),
            )
        }
    }

    private fun parseEnrollments(arr: JSONArray?): List<Enrollment> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            Enrollment(
                id = o.optString("id", "enr_$i"),
                userId = o.optString("userId", ""),
                exchangeId = o.optString("exchangeId", ""),
                progressPercent = o.optInt("progressPercent", o.optInt("progress", 0)).coerceIn(0, 100),
                completedChecklistIds = o.optStringList("completedChecklistIds"),
                enrolledAt = parseTimestamp(o, "enrolledAt", System.currentTimeMillis()),
            )
        }
    }

    private fun parseSubmissions(arr: JSONArray?): List<CapstoneSubmission> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            val rubricObj = o.optJSONObject("rubricScores") ?: o.optJSONObject("rubric")
            val rubric = rubricObj?.let { r ->
                RubricScores(
                    technicalExecution = r.optInt("technicalExecution", 4).coerceIn(1, 5),
                    problemSolving = r.optInt("problemSolving", 4).coerceIn(1, 5),
                    practicalImpact = r.optInt("practicalImpact", 4).coerceIn(1, 5),
                    presentationClarity = r.optInt("presentationClarity", 4).coerceIn(1, 5),
                )
            }
            val syncRaw = o.optString("syncStatus", "synced").uppercase(Locale.US)
            val syncStatus = if (syncRaw.contains("PENDING")) SyncStatus.PENDING_SYNC else SyncStatus.SYNCED

            CapstoneSubmission(
                id = o.optString("id", "sub_$i"),
                userId = o.optString("userId", ""),
                exchangeId = o.optString("exchangeId", ""),
                projectTitle = o.optString("projectTitle", o.optString("title", "Capstone")),
                architectureDescription = o.optString("architectureDescription", o.optString("description", "")),
                githubUrl = o.optString("githubUrl", ""),
                demoUrl = o.optString("demoUrl", ""),
                keyTakeaways = o.optString("keyTakeaways", ""),
                syncStatus = syncStatus,
                submittedAt = parseTimestamp(o, "submittedAt", System.currentTimeMillis()),
                rubricScores = rubric,
                qualitativeFeedback = o.optStringOrNull("qualitativeFeedback") ?: o.optStringOrNull("feedback"),
                evaluatedByMentorId = o.optStringOrNull("evaluatedByMentorId"),
                evaluatedAt = if (o.has("evaluatedAt") && !o.isNull("evaluatedAt")) parseTimestamp(o, "evaluatedAt", System.currentTimeMillis()) else null,
            )
        }
    }

    private fun parseOpportunities(arr: JSONArray?): List<Opportunity> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            Opportunity(
                id = o.optString("id", "opp_$i"),
                partnerId = o.optString("partnerId", "u_elena"),
                organization = o.optString("organization", "Partner Org"),
                title = o.optString("title", "Role"),
                compensation = o.optString("compensation", ""),
                workType = o.optString("workType", o.optString("type", "Remote")),
                location = o.optString("location", "Remote"),
                description = o.optString("description", ""),
                requiredSkills = o.optStringList("requiredSkills"),
                isFlagged = o.optBoolean("isFlagged", o.optBoolean("flagged", false)),
                createdAt = parseTimestamp(o, "createdAt", System.currentTimeMillis()),
            )
        }
    }

    private fun parseEois(arr: JSONArray?): List<ExpressionOfInterest> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            val statusStr = o.optString("status", "submitted").lowercase(Locale.US)
            val status = EoiStatus.entries.firstOrNull {
                it.key == statusStr || it.name.equals(statusStr, ignoreCase = true)
            } ?: EoiStatus.SUBMITTED
            ExpressionOfInterest(
                id = o.optString("id", "eoi_$i"),
                opportunityId = o.optString("opportunityId", ""),
                participantId = o.optString("participantId", o.optString("userId", "")),
                status = status,
                consentConfirmed = o.optBoolean("consentConfirmed", o.optBoolean("consentGranted", true)),
                coverNote = o.optString("coverNote", o.optString("note", "")),
                createdAt = parseTimestamp(o, "createdAt", System.currentTimeMillis()),
            )
        }
    }

    private fun parseMessages(arr: JSONArray?): List<DirectMessage> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            DirectMessage(
                id = o.optString("id", "msg_$i"),
                eoiId = o.optString("eoiId", ""),
                senderId = o.optString("senderId", ""),
                senderName = o.optString("senderName", "Member"),
                body = o.optString("body", ""),
                timestamp = parseTimestamp(o, "timestamp", System.currentTimeMillis()),
            )
        }
    }

    private fun parseNotifications(arr: JSONArray?): List<YepNotification> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            val catStr = o.optString("category", "opportunity").lowercase(Locale.US)
            val category = NotificationCategory.entries.firstOrNull {
                it.key == catStr || it.name.equals(catStr, ignoreCase = true)
            } ?: NotificationCategory.OPPORTUNITY
            val roleStr = o.optStringOrNull("recipientRole")
            YepNotification(
                id = o.optString("id", "notif_$i"),
                category = category,
                title = o.optString("title", "Notification"),
                message = o.optString("message", ""),
                recipientId = o.optStringOrNull("recipientId"),
                recipientRole = roleStr?.let { parseRole(it) },
                targetScreen = o.optString("targetScreen", "overview"),
                targetEntityId = o.optStringOrNull("targetEntityId"),
                createdAt = parseTimestamp(o, "createdAt", System.currentTimeMillis()),
                readByUserIds = o.optStringList("readByUserIds"),
                dismissedByUserIds = o.optStringList("dismissedByUserIds"),
            )
        }
    }

    private fun parseAuditLogs(arr: JSONArray?): List<AuditLogEntry> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            val actStr = o.optString("actionType", o.optString("action", "PROFILE_UPDATED"))
                .uppercase(Locale.US)
            val action = AuditActionType.entries.firstOrNull { it.name == actStr }
                ?: AuditActionType.PROFILE_UPDATED
            AuditLogEntry(
                id = o.optString("id", "aud_$i"),
                actionType = action,
                actorId = o.optString("actorId", ""),
                actorName = o.optString("actorName", "System"),
                summary = o.optString("summary", o.optString("details", "")),
                timestamp = parseTimestamp(o, "timestamp", System.currentTimeMillis()),
            )
        }
    }

    private fun parseRole(raw: String): UserRole? = when (raw.trim().lowercase(Locale.US)) {
        "participant", "learner", "youth" -> UserRole.PARTICIPANT
        "mentor", "facilitator" -> UserRole.MENTOR
        "partner", "employer" -> UserRole.PARTNER
        "admin", "administrator" -> UserRole.ADMIN
        else -> null
    }

    private fun parseSkillTier(raw: String): SkillTier = when (raw.trim().lowercase(Locale.US)) {
        "beginner" -> SkillTier.BEGINNER
        "advanced" -> SkillTier.ADVANCED
        else -> SkillTier.INTERMEDIATE
    }

    private fun JSONObject.optStringOrNull(key: String): String? {
        if (!has(key) || isNull(key)) return null
        return optString(key).takeIf { it.isNotBlank() && it != "null" }
    }

    private fun JSONObject.optStringList(key: String): List<String> {
        val arr = optJSONArray(key) ?: return emptyList()
        return (0 until arr.length()).mapNotNull { idx ->
            arr.optString(idx).takeIf { it.isNotBlank() }
        }
    }

    private fun parseTimestamp(obj: JSONObject, key: String, fallback: Long): Long {
        if (!obj.has(key) || obj.isNull(key)) return fallback
        val raw = obj.opt(key)
        if (raw is Number) return raw.toLong()
        val str = raw?.toString()?.trim().orEmpty()
        str.toLongOrNull()?.let { return it }
        return runCatching {
            val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val cleaned = str.substringBefore(".").removeSuffix("Z")
            val parsed: Date? = fmt.parse(cleaned)
            parsed?.time ?: fallback
        }.getOrDefault(fallback)
    }
}
