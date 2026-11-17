# YEP Platform — Youth Empowerment Project (Android App)

[![Android SDK](https://img.shields.io/badge/Android%20SDK-API%2024%2B%20(Target%2036)-brightgreen)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue)](https://developer.android.com/compose)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0%2B-purple)](https://kotlinlang.org)
[![Firebase](https://img.shields.io/badge/Firebase-App%20Check%20%26%20AI-orange)](https://firebase.google.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

The official **YEP Platform** Android application designed to empower youth across East and West Africa through cohort-based virtual exchanges, standardized mentor rubrics, explicit portfolio consent, and low-bandwidth offline-first sync.

---

## 🌟 Key Features & Capabilities

### 1. 🌐 Full Multilingual Localization
- Seamlessly switch between **6 African & Global Languages** instantly across the entire app interface:
  - **English (EN)**
  - **Kiswahili (SW)**
  - **Oluganda (LG)**
  - **Runyankole (NYN)**
  - **Ikinyarwanda (RW)**
  - **Leb Acoli (ACH)**

### 2. 📶 Low-Bandwidth Offline-First Architecture
- **Offline Data Saver Mode**: Toggle data saver instantly to queue capstone submissions and mutations safely offline (`PENDING_SYNC`).
- **1-Tap Sync**: Automatically flush queued mutations and synchronize live state once connectivity is restored.
- **Room Database & DataStore**: Persistent local caching of state snapshots with Moshi JSON serialization.

### 3. 📝 1–5 Standardized Mentor Grading Rubrics
- Transparent evaluation across **4 Standardized Pillars**:
  1. *Technical Execution*
  2. *Problem Solving*
  3. *Practical Impact*
  4. *Presentation Clarity*
- 1–5 scoring scale with qualitative mentor feedback and verified badge issuance.

### 4. 🔒 Explicit Portfolio Consent & Data Sovereignty
- Participant capstones, rubric scores, and contact details remain strictly private by default.
- Explicit consent toggles required per profile or Expression of Interest (EOI) before sharing data with hiring partners.

### 5. 🤖 Grounded AI Career & Learning Advisor
- Powered by Gemini models with robust deterministic offline-first fallbacks.
- Context-aware responses grounded in participant profile, learning track, rubric ratings, and open partner roles.
- Facilitator AI diagnostic assistant for balanced mixed-tier squad formation and cohort competency evaluation.

### 6. 👥 Role-Based Workspaces
- **Participant / Learner**: Learning track stages, enrolled virtual exchanges, capstone submission, offline study packs, and AI advisor.
- **Mentor / Facilitator**: Competency tracker, pending capstone review queue, 1–5 rubric grader, and live session scheduler.
- **Employer / Partner**: Talent pipeline, consented EOI review, job opening publisher, and direct candidate messaging.
- **Administrator**: Impact analytics dashboard with custom Jetpack Compose canvas charts, user governance, content moderation, and zero-PII M&E summary export.

---

## 📱 Tech Stack & Architecture

- **UI Framework**: Jetpack Compose & Material 3 (`androidx.compose`)
- **Navigation & Lifecycle**: Navigation Compose, ViewModel, StateFlow, Lifecycle Runtime Compose
- **Local Persistence**: Room Database, Jetpack DataStore Preferences
- **Networking & Serialization**: OkHttp, Retrofit, Moshi Kotlin Codegen
- **AI & Security**: Firebase AI (Gemini), Firebase App Check (reCAPTCHA / Debug)
- **Testing**: JUnit, Robolectric, AndroidX Test, Compose UI Testing

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug / Koala (or newer)
- JDK 11 or higher
- Android SDK API 36+

### Installation & Build
1. Clone the repository:
   ```bash
   git clone https://github.com/Masa010pro/YEP-Android-App.git
   ```
2. Open the project folder in Android Studio.
3. Sync Gradle and build the project:
   ```bash
   ./gradlew assembleDebug
   ```
4. Run unit tests:
   ```bash
   ./gradlew testDebugUnitTest
   ```

---

## 📦 Project Package Name
- **Namespace & Application ID**: `com.gruop11.yepapp`

---

## 📄 License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
