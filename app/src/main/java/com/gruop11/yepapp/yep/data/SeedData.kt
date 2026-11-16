package com.gruop11.yepapp.yep.data

import com.gruop11.yepapp.yep.model.*

object SeedData {
    val regionalHubs = listOf(
        "Kampala, Uganda",
        "Nairobi, Kenya",
        "Kigali, Rwanda",
        "Dar es Salaam, Tanzania",
        "Lagos, Nigeria",
        "Accra, Ghana",
        "Cairo, Egypt",
        "Johannesburg, South Africa",
        "Programme Secretariat",
    )

    fun createInitialSnapshot(): YepStateSnapshot {
        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        val day = 86_400_000L

        val users = listOf(
            UserProfile(
                id = "u_amara",
                name = "Amara Okafor",
                email = "amara.okafor@outlook.com",
                password = "YepUser@2026",
                role = UserRole.PARTICIPANT,
                title = "Full-Stack Cloud Fellow",
                organization = "YEP West Africa Cohort",
                regionalHub = "Lagos, Nigeria",
                age = 22,
                skills = listOf("TypeScript", "React", "Node.js", "PostgreSQL", "Git Workflow"),
                interests = listOf("Cloud Infrastructure", "FinTech APIs", "Accessible Design"),
                bio = "Software engineering fellow building resilient offline-first web and mobile systems for emerging African enterprises.",
                careerGoal = "Join a high-growth cloud infrastructure team building developer tools across Africa.",
                consentDataSharing = true,
                badges = listOf("Cloud Builder", "Verified Capstone", "Consent Steward"),
            ),
            UserProfile(
                id = "u_kofi",
                name = "Kofi Mensah",
                email = "kofi.mensah@yep-cohort.org",
                password = "YepUser@2026",
                role = UserRole.PARTICIPANT,
                title = "Agritech Data Fellow",
                organization = "YEP Accra Hub",
                regionalHub = "Accra, Ghana",
                age = 24,
                skills = listOf("Python", "Remote Sensing", "SQL", "QGIS", "Pandas"),
                interests = listOf("Climate Tech", "Smallholder Telemetry", "Data Pipelines"),
                bio = "Environmental data analyst turning satellite and IoT soil moisture feeds into SMS advisories for farmers.",
                careerGoal = "Lead smallholder climate telemetry pipelines at an East or West African agritech.",
                consentDataSharing = true,
                badges = listOf("Data Explorer"),
            ),
            UserProfile(
                id = "u_fatima",
                name = "Fatima Al-Mansoor",
                email = "fatima.mansoor@yep-cohort.org",
                password = "YepUser@2026",
                role = UserRole.PARTICIPANT,
                title = "Inclusive UX & Frontend Learner",
                organization = "YEP Cairo Hub",
                regionalHub = "Cairo, Egypt",
                age = 21,
                skills = listOf("Figma", "WCAG Accessibility", "UI Systems", "HTML/CSS"),
                interests = listOf("Civic Tech", "Design Systems", "Mobile UX"),
                bio = "Product designer focused on low-bandwidth mobile accessibility and multilingual civic interfaces.",
                careerGoal = "Become an Accessibility Product Designer for pan-African public digital services.",
                consentDataSharing = false,
                badges = listOf("Accessibility Advocate"),
            ),
            UserProfile(
                id = "u_marcus",
                name = "Dr. Marcus Vance",
                email = "marcus.vance@opendev.foundation",
                password = "YepMentor@2026",
                role = UserRole.MENTOR,
                title = "Principal Cloud Architect & Curriculum Fellow",
                organization = "OpenDev Foundation",
                regionalHub = "Nairobi, Kenya",
                age = 38,
                skills = listOf("Kubernetes", "Distributed Systems", "Python", "System Design", "Rubric Coaching"),
                interests = listOf("Open Source Mentorship", "Low-Bandwidth Architecture", "Youth Engineering"),
                bio = "Principal architect mentoring YEP cohorts across Nairobi, Kampala, and Lagos on production-grade systems.",
                careerGoal = "Scale standardized technical mentorship across 10,000 East & West African youth.",
                consentDataSharing = true,
                badges = listOf("Lead Curriculum Fellow"),
            ),
            UserProfile(
                id = "u_elena",
                name = "Elena Rostova",
                email = "elena.rostova@afritechhorizons.io",
                password = "YepPartner@2026",
                role = UserRole.PARTNER,
                title = "Head of Emerging Talent",
                organization = "AfriTech Horizons & Global Labs",
                regionalHub = "Johannesburg, South Africa",
                age = 34,
                skills = listOf("Talent Partnerships", "Technical Recruiting", "Early-Career Programs"),
                interests = listOf("Cloud Associates", "Remote Engineering", "Verified Portfolios"),
                bio = "Connecting verified YEP capstone graduates to paid remote and hybrid engineering apprenticeships.",
                careerGoal = "Hire 50+ verified YEP cohort fellows into paid technical roles this year.",
                consentDataSharing = true,
                badges = listOf("Verified Hiring Partner"),
            ),
            UserProfile(
                id = "u_sarah",
                name = "Sarah Chen",
                email = "sarah.chen@yep-initiative.org",
                password = "YepAdmin@2026",
                role = UserRole.ADMIN,
                title = "Programme Director & Operations Lead",
                organization = "YEP Initiative Secretariat",
                regionalHub = "Programme Secretariat",
                age = 36,
                skills = listOf("M&E Impact Analytics", "Program Governance", "Safeguarding", "Zero-PII Reporting"),
                interests = listOf("Youth Employment", "Policy Compliance", "East Africa Expansion"),
                bio = "Overseeing program governance, cohort equity, safeguarding compliance, and donor M&E analytics.",
                careerGoal = "Ensure transparent, ethical, high-impact youth transitions from learning to employment.",
                consentDataSharing = true,
                badges = listOf("Secretariat Director"),
            ),
        )

        val exchanges = listOf(
            VirtualExchange(
                id = "ex_cloud",
                title = "Cloud-Native Web Architecture & Microservices",
                theme = "Cloud & DevOps",
                skillLevel = SkillTier.INTERMEDIATE,
                formatLabel = "4-Week Cohort",
                durationLabel = "4 Weeks · 6 hrs/wk",
                enrolledCount = 36,
                maxCapacity = 40,
                description = "Design resilient microservices, REST/PostgreSQL APIs, containerized deployments, and offline-first sync queues tailored for variable connectivity.",
                outcomes = listOf(
                    "Build fault-tolerant Node.js / TypeScript microservices with PostgreSQL migrations",
                    "Implement idempotent offline sync queues and retry backoff policies",
                    "Instrument health probes, structured logging, and zero-downtime rollouts",
                ),
                prerequisites = listOf("TypeScript or JavaScript fundamentals", "Basic SQL & Git workflow"),
                assignmentPrompt = "Build a multi-tenant service with PostgreSQL persistence, idempotent sync endpoint, and architectural decision record (ADR).",
                studyPackMarkdown = """
                    # Cloud-Native Offline Study Pack
                    ## 1. Resilient Sync Patterns
                    - Use client-generated UUIDs so offline mutations retry safely without duplicates.
                    - Store queued payloads locally with `pending_sync` state and flush chronologically.
                    
                    ## 2. Service Boundaries & PostgreSQL
                    - Keep read queries indexed by `(tenant_id, updated_at DESC)`.
                    - Prefer explicit connection pooling and bounded timeouts (<= 5s).
                    
                    ## 3. Capstone Readiness Checklist
                    - Include an Architecture Diagram or clear README summary.
                    - Verify unit tests pass offline before pushing to GitHub.
                """.trimIndent(),
                studyChecklist = listOf(
                    StudyChecklistItem("chk_c1", "Complete Module 1: Idempotent Queue Design"),
                    StudyChecklistItem("chk_c2", "Configure PostgreSQL Schema & Migrations"),
                    StudyChecklistItem("chk_c3", "Write Architecture Decision Record (ADR)"),
                    StudyChecklistItem("chk_c4", "Verify Capstone Repo & Demo Links"),
                ),
                mentorId = "u_marcus",
            ),
            VirtualExchange(
                id = "ex_agri",
                title = "Agritech Data Systems & Remote Sensing for Smallholders",
                theme = "Agritech & Climate",
                skillLevel = SkillTier.BEGINNER,
                formatLabel = "Virtual Bootcamp",
                durationLabel = "3 Weeks · 5 hrs/wk",
                enrolledCount = 50,
                maxCapacity = 50,
                description = "Process satellite NDVI vegetation indices, rainfall anomalies, and field telemetry to generate actionable alerts for smallholder cooperatives.",
                outcomes = listOf(
                    "Clean and aggregate Sentinel-2 NDVI raster summaries in Python",
                    "Design low-bandwidth SMS/USSD weather and irrigation advisories",
                    "Evaluate telemetry accuracy against ground-truth cooperative logs",
                ),
                prerequisites = listOf("Basic spreadsheet or Python familiarity", "Interest in climate resilience"),
                assignmentPrompt = "Deliver a smallholder telemetry notebook or lightweight dashboard translating regional moisture data into local-language advisories.",
                studyPackMarkdown = """
                    # Agritech Remote Sensing Study Pack
                    ## 1. Vegetation Indices (NDVI)
                    - `NDVI = (NIR - Red) / (NIR + Red)` values between `0.2` and `0.8` reflect crop vigor.
                    - Flag 14-day drops > 15% for early extension officer outreach.
                    
                    ## 2. Low-Bandwidth Farmer Delivery
                    - Keep advisory payloads under 160 characters for SMS fallback.
                    - Localize units and crop calendar stages into Kiswahili, Luganda, or Acholi.
                """.trimIndent(),
                studyChecklist = listOf(
                    StudyChecklistItem("chk_a1", "Inspect Sample Sentinel-2 Dataset"),
                    StudyChecklistItem("chk_a2", "Compute 30-Day Moisture Anomaly Trend"),
                    StudyChecklistItem("chk_a3", "Draft Localized Farmer SMS Advisory"),
                ),
                mentorId = "u_marcus",
            ),
            VirtualExchange(
                id = "ex_ux",
                title = "Human-Centered UI/UX & Accessible Design Systems",
                theme = "Design & Accessibility",
                skillLevel = SkillTier.BEGINNER,
                formatLabel = "Weekend Sprint",
                durationLabel = "2 Weeks · Sprint",
                enrolledCount = 18,
                maxCapacity = 30,
                description = "Craft WCAG AA accessible interfaces, 48dp touch-target component tokens, and multilingual mobile patterns for East African users.",
                outcomes = listOf(
                    "Audit mobile flows for WCAG contrast, TalkBack semantics, and 48dp targets",
                    "Build reusable Material 3 design tokens and bilingual layout specs",
                    "Conduct rapid 5-user usability testing on mid-range Android devices",
                ),
                prerequisites = listOf("No prior coding required", "Curiosity about mobile usability"),
                assignmentPrompt = "Submit an accessible 4-screen mobile prototype with contrast audit, semantic labels, and user testing synthesis.",
                studyPackMarkdown = """
                    # Accessible Design Systems Pack
                    ## 1. Mobile Ergonomics & Contrast
                    - Maintain >= 4.5:1 contrast for body copy and >= 48dp tap targets.
                    - Design for 30%+ text expansion when translating English to Luganda or Kinyarwanda.
                    
                    ## 2. Low-Data Visual Assets
                    - Prefer vector graphics and system iconography over heavy hero bitmaps.
                """.trimIndent(),
                studyChecklist = listOf(
                    StudyChecklistItem("chk_u1", "Run WCAG Contrast Audit on Primary Palette"),
                    StudyChecklistItem("chk_u2", "Document 48dp Touch-Target Grid"),
                    StudyChecklistItem("chk_u3", "Synthesize 3 Usability Feedback Findings"),
                ),
                mentorId = "u_marcus",
            ),
            VirtualExchange(
                id = "ex_ai",
                title = "Practical AI & Data Pipelines with Python",
                theme = "AI & Data Engineering",
                skillLevel = SkillTier.INTERMEDIATE,
                formatLabel = "4-Week Cohort",
                durationLabel = "4 Weeks · 6 hrs/wk",
                enrolledCount = 42,
                maxCapacity = 45,
                description = "Build reproducible ETL pipelines, feature stores, and grounded evaluation harnesses for practical applied ML in emerging markets.",
                outcomes = listOf(
                    "Construct clean ETL pipelines with schema validation and anomaly alerts",
                    "Evaluate model precision, recall, and demographic fairness slices",
                    "Package lightweight inference endpoints with deterministic offline fallbacks",
                ),
                prerequisites = listOf("Python functions & data structures", "Basic statistics"),
                assignmentPrompt = "Build a reproducible Python data pipeline with validation checks, baseline model evaluation, and fairness report.",
                studyPackMarkdown = """
                    # Practical AI & Pipelines Study Pack
                    ## 1. Deterministic Data Validation
                    - Validate null rates, timestamp monotonicity, and out-of-range metrics before training.
                    
                    ## 2. Offline-First Evaluation
                    - Always ship a deterministic rule-based baseline alongside ML predictors.
                """.trimIndent(),
                studyChecklist = listOf(
                    StudyChecklistItem("chk_p1", "Implement Schema Validation Stage"),
                    StudyChecklistItem("chk_p2", "Benchmark Deterministic Baseline"),
                    StudyChecklistItem("chk_p3", "Write Model Fairness & Error Analysis"),
                ),
                mentorId = "u_marcus",
            ),
        )

        val liveSessions = listOf(
            LiveSession(
                id = "ses_1",
                exchangeId = "ex_cloud",
                title = "Live Architecture Clinic: Idempotent Offline Sync",
                scheduledAt = "Thu, Nov 19 · 16:00 EAT",
                durationLabel = "60 min",
                agenda = "Deep-dive on client queue replay, conflict resolution, and PostgreSQL transactional locking.",
                meetingUrl = "https://meet.yep-initiative.org/cloud-sync-clinic",
            ),
            LiveSession(
                id = "ses_2",
                exchangeId = "ex_ai",
                title = "Data Pipeline Debugging & Fairness Slices",
                scheduledAt = "Sat, Nov 21 · 11:00 EAT",
                durationLabel = "75 min",
                agenda = "Live review of ETL validation tests and handling missing regional sensor intervals.",
                meetingUrl = "https://meet.yep-initiative.org/ai-pipelines-lab",
            ),
            LiveSession(
                id = "ses_3",
                exchangeId = "ex_ux",
                title = "Multilingual Mobile Critique & TalkBack Lab",
                scheduledAt = "Sun, Nov 22 · 14:00 EAT",
                durationLabel = "60 min",
                agenda = "Testing Swahili & Luganda string expansion and screen reader focus order.",
                meetingUrl = "https://meet.yep-initiative.org/ux-accessibility",
            ),
        )

        val enrollments = listOf(
            Enrollment(
                id = "enr_amara_cloud",
                userId = "u_amara",
                exchangeId = "ex_cloud",
                progressPercent = 85,
                completedChecklistIds = listOf("chk_c1", "chk_c2", "chk_c3"),
                enrolledAt = now - 14 * day,
            ),
            Enrollment(
                id = "enr_amara_ai",
                userId = "u_amara",
                exchangeId = "ex_ai",
                progressPercent = 60,
                completedChecklistIds = listOf("chk_p1", "chk_p2"),
                enrolledAt = now - 7 * day,
            ),
            Enrollment(
                id = "enr_kofi_agri",
                userId = "u_kofi",
                exchangeId = "ex_agri",
                progressPercent = 78,
                completedChecklistIds = listOf("chk_a1", "chk_a2"),
                enrolledAt = now - 12 * day,
            ),
            Enrollment(
                id = "enr_kofi_ai",
                userId = "u_kofi",
                exchangeId = "ex_ai",
                progressPercent = 45,
                completedChecklistIds = listOf("chk_p1"),
                enrolledAt = now - 5 * day,
            ),
            Enrollment(
                id = "enr_fatima_ux",
                userId = "u_fatima",
                exchangeId = "ex_ux",
                progressPercent = 72,
                completedChecklistIds = listOf("chk_u1", "chk_u2"),
                enrolledAt = now - 10 * day,
            ),
        )

        val submissions = listOf(
            CapstoneSubmission(
                id = "sub_amara_1",
                userId = "u_amara",
                exchangeId = "ex_cloud",
                projectTitle = "ResiliSync Micro-Ledger API",
                architectureDescription = "Event-sourced TypeScript + PostgreSQL microservice supporting offline mobile ledger mutations with idempotency keys.",
                githubUrl = "https://github.com/amara-okafor/resilisync-api",
                demoUrl = "https://demo.resilisync.yep.dev",
                keyTakeaways = "Idempotency keys eliminated 100% of duplicate sync writes during 2G reconnect simulations.",
                syncStatus = SyncStatus.SYNCED,
                submittedAt = now - 4 * day,
                rubricScores = RubricScores(
                    technicalExecution = 5,
                    problemSolving = 4,
                    practicalImpact = 5,
                    presentationClarity = 4,
                ),
                qualitativeFeedback = "Outstanding architecture and clear PostgreSQL indexing. Consider adding rate-limit backoff headers in v2.",
                evaluatedByMentorId = "u_marcus",
                evaluatedAt = now - 2 * day,
            ),
            CapstoneSubmission(
                id = "sub_amara_2",
                userId = "u_amara",
                exchangeId = "ex_ai",
                projectTitle = "Low-Bandwidth Career Skill Matcher",
                architectureDescription = "Python ETL pipeline normalizing East African job postings and matching verified capstone skills offline.",
                githubUrl = "https://github.com/amara-okafor/yep-skill-matcher",
                demoUrl = "https://matcher.yep.dev",
                keyTakeaways = "Lightweight TF-IDF + rule hybrid runs in <40ms on low-memory devices.",
                syncStatus = SyncStatus.SYNCED,
                submittedAt = now - 10 * hour,
                rubricScores = null,
            ),
            CapstoneSubmission(
                id = "sub_kofi_1",
                userId = "u_kofi",
                exchangeId = "ex_agri",
                projectTitle = "Volta Soil & NDVI SMS Alerter",
                architectureDescription = "Python remote-sensing pipeline computing 14-day crop stress alerts delivered via concise SMS templates.",
                githubUrl = "https://github.com/kofi-mensah/volta-ndvi-sms",
                demoUrl = "https://volta-alerts.yep.dev",
                keyTakeaways = "Compressing advisories to 140 chars cut rural SMS delivery failures dramatically.",
                syncStatus = SyncStatus.SYNCED,
                submittedAt = now - 3 * day,
                rubricScores = RubricScores(
                    technicalExecution = 4,
                    problemSolving = 4,
                    practicalImpact = 5,
                    presentationClarity = 3,
                ),
                qualitativeFeedback = "Strong smallholder impact. Expand unit tests around cloud-cover masking edge cases.",
                evaluatedByMentorId = "u_marcus",
                evaluatedAt = now - 1 * day,
            ),
            CapstoneSubmission(
                id = "sub_fatima_1",
                userId = "u_fatima",
                exchangeId = "ex_ux",
                projectTitle = "Bilingual Clinic Queue Companion",
                architectureDescription = "WCAG AA accessible mobile triage UI designed for 48dp touch targets and offline status clarity.",
                githubUrl = "https://github.com/fatima-ux/clinic-companion-kit",
                demoUrl = "https://figma.com/file/yep-clinic-companion",
                keyTakeaways = "Usability tests with 6 caregivers showed 40% faster form completion with large icon-paired labels.",
                syncStatus = SyncStatus.SYNCED,
                submittedAt = now - 16 * hour,
                rubricScores = null,
            ),
        )

        val opportunities = listOf(
            Opportunity(
                id = "opp_afritech",
                partnerId = "u_elena",
                organization = "AfriTech Horizons",
                title = "Junior Cloud Infrastructure Associate",
                compensation = "$1,200–$1,600/mo",
                workType = "Remote",
                location = "Remote (Pan-African)",
                description = "Support containerized microservices, CI/CD pipelines, PostgreSQL reliability, and low-latency API monitoring across African fintech products.",
                requiredSkills = listOf("TypeScript", "Node.js", "PostgreSQL", "Git Workflow", "Cloud"),
                createdAt = now - 6 * day,
            ),
            Opportunity(
                id = "opp_greengrid",
                partnerId = "u_elena",
                organization = "GreenGrid Agritech",
                title = "Data & Telemetry Support Fellow",
                compensation = "$950/mo + Hardware",
                workType = "Hybrid",
                location = "Nairobi / Hybrid",
                description = "Maintain smallholder sensor ETL jobs, validate regional climate feeds, and partner with field agronomists on SMS insight delivery.",
                requiredSkills = listOf("Python", "SQL", "Remote Sensing", "Data Pipelines"),
                createdAt = now - 4 * day,
            ),
            Opportunity(
                id = "opp_civicsphere",
                partnerId = "u_elena",
                organization = "CivicSphere Studio",
                title = "Accessible Interface Design Contractor",
                compensation = "$35/hr",
                workType = "Remote",
                location = "Remote",
                description = "Design multilingual Material 3 mobile flows, WCAG contrast audits, and component specs for public-interest civic apps.",
                requiredSkills = listOf("Figma", "WCAG Accessibility", "UI Systems", "React"),
                createdAt = now - 2 * day,
            ),
        )

        val eois = listOf(
            ExpressionOfInterest(
                id = "eoi_1",
                opportunityId = "opp_afritech",
                participantId = "u_amara",
                status = EoiStatus.INTERVIEW_INVITED,
                consentConfirmed = true,
                coverNote = "Excited to bring my ResiliSync PostgreSQL microservice capstone and TypeScript skills to AfriTech Horizons.",
                createdAt = now - 2 * day,
            ),
            ExpressionOfInterest(
                id = "eoi_2",
                opportunityId = "opp_greengrid",
                participantId = "u_kofi",
                status = EoiStatus.REVIEWING,
                consentConfirmed = true,
                coverNote = "Built the Volta NDVI SMS Alerter and eager to scale smallholder telemetry with GreenGrid.",
                createdAt = now - 1 * day,
            ),
        )

        val messages = listOf(
            DirectMessage(
                id = "msg_1",
                eoiId = "eoi_1",
                senderId = "u_elena",
                senderName = "Elena Rostova",
                body = "Hi Amara! Our engineering leads reviewed your 4.50 rubric capstone on ResiliSync. Are you free Thursday at 15:00 EAT for a technical chat?",
                timestamp = now - 18 * hour,
            ),
            DirectMessage(
                id = "msg_2",
                eoiId = "eoi_1",
                senderId = "u_amara",
                senderName = "Amara Okafor",
                body = "Thank you Elena! Thursday at 15:00 EAT works great. Looking forward to walking through our offline sync architecture.",
                timestamp = now - 12 * hour,
            ),
        )

        val notifications = listOf(
            YepNotification(
                id = "notif_1",
                category = NotificationCategory.MENTOR_FEEDBACK,
                title = "Capstone Evaluated: 4.50 / 5.00",
                message = "Dr. Marcus Vance graded 'ResiliSync Micro-Ledger API' and awarded Verified Capstone.",
                recipientId = "u_amara",
                targetScreen = "dashboard",
                targetEntityId = "sub_amara_1",
                createdAt = now - 2 * day,
            ),
            YepNotification(
                id = "notif_2",
                category = NotificationCategory.OPPORTUNITY,
                title = "New Paid Opening: AfriTech Horizons",
                message = "Junior Cloud Infrastructure Associate ($1,200–$1,600/mo, Remote) matches your TypeScript & PostgreSQL skills.",
                recipientRole = UserRole.PARTICIPANT,
                targetScreen = "opportunities",
                targetEntityId = "opp_afritech",
                createdAt = now - 36 * hour,
            ),
            YepNotification(
                id = "notif_3",
                category = NotificationCategory.SUBMISSION,
                title = "2 Capstones Ready for Rubric Review",
                message = "Amara Okafor (Practical AI) and Fatima Al-Mansoor (UI/UX) submitted capstones for 1–5 evaluation.",
                recipientRole = UserRole.MENTOR,
                targetScreen = "mentor",
                targetEntityId = "sub_amara_2",
                createdAt = now - 10 * hour,
            ),
            YepNotification(
                id = "notif_4",
                category = NotificationCategory.OPPORTUNITY,
                title = "Consented Candidate EOI Received",
                message = "Amara Okafor shared a verified portfolio snapshot for Junior Cloud Infrastructure Associate.",
                recipientRole = UserRole.PARTNER,
                targetScreen = "partner-manage",
                targetEntityId = "eoi_1",
                createdAt = now - 2 * day,
            ),
        )

        val auditLogs = listOf(
            AuditLogEntry(
                id = "aud_1",
                actionType = AuditActionType.USER_REGISTRATION,
                actorId = "u_amara",
                actorName = "Amara Okafor",
                summary = "Verified youth participant registration (Age 22, Lagos Hub).",
                timestamp = now - 15 * day,
            ),
            AuditLogEntry(
                id = "aud_2",
                actionType = AuditActionType.EXCHANGE_ENROLLED,
                actorId = "u_amara",
                actorName = "Amara Okafor",
                summary = "Enrolled in Cloud-Native Web Architecture & Microservices (36/40 capacity).",
                timestamp = now - 14 * day,
            ),
            AuditLogEntry(
                id = "aud_3",
                actionType = AuditActionType.PROJECT_SUBMITTED,
                actorId = "u_amara",
                actorName = "Amara Okafor",
                summary = "Submitted capstone 'ResiliSync Micro-Ledger API' (SYNCED).",
                timestamp = now - 4 * day,
            ),
            AuditLogEntry(
                id = "aud_4",
                actionType = AuditActionType.RUBRIC_EVALUATED,
                actorId = "u_marcus",
                actorName = "Dr. Marcus Vance",
                summary = "Evaluated 'ResiliSync Micro-Ledger API' with mean rubric 4.50 / 5.00.",
                timestamp = now - 2 * day,
            ),
            AuditLogEntry(
                id = "aud_5",
                actionType = AuditActionType.OPPORTUNITY_PUBLISHED,
                actorId = "u_elena",
                actorName = "Elena Rostova",
                summary = "Published paid role 'Junior Cloud Infrastructure Associate' at AfriTech Horizons.",
                timestamp = now - 6 * day,
            ),
            AuditLogEntry(
                id = "aud_6",
                actionType = AuditActionType.CONSENT_GRANTED_EOI,
                actorId = "u_amara",
                actorName = "Amara Okafor",
                summary = "Explicitly consented to share verified capstone & rubric scores with AfriTech Horizons.",
                timestamp = now - 2 * day,
            ),
        )

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
}
