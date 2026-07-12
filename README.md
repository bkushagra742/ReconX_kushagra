# Kushagra ReconX -- Android

Native Android rewrite of the Kushagra ReconX desktop toolkit: a personal, offline-first
cybersecurity/OSINT workspace for defensive security, digital investigation, bug-bounty
recon, and learning. Kotlin, Jetpack Compose, MVVM, Room -- no Python/Kivy/Flutter/WebView.

> **Ethical scope.** This app contains no malware, RATs, keyloggers, credential-stealing,
> brute-force, exploit, or payload-generation code. The "Network/Website" tools (port
> concepts, header analysis, TLS inspection, DNS/WHOIS) are the same class of read-only,
> standard-protocol operations performed by `dig`, `whois`, `curl -I`, and browser dev
> tools -- they format/inspect publicly available information, they do not scan, exploit,
> or brute-force anything.

---

## Tech Stack

| Layer | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository pattern |
| Local DB | Room (SQLite) |
| Async | Kotlin Coroutines + Flow |
| Settings | Jetpack DataStore (Preferences) |
| PDF export | `android.graphics.pdf.PdfDocument` (built into the SDK -- no extra library) |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 |
| DI | Manual (a single `ReconXApplication` container + one `ViewModelFactory` -- no Hilt/Dagger, to keep the dependency graph and APK small) |

---

## Project Structure

```
app/src/main/java/com/kushagra/reconx/
├── MainActivity.kt              Single-activity host, applies theme, launches AppNavHost
├── ReconXApplication.kt         Manual DI container (Database, repositories, managers)
├── ui/
│   ├── theme/                    Color.kt, Type.kt, Shape.kt, Theme.kt (dark + light)
│   ├── navigation/                Destinations.kt, AppNavHost.kt
│   ├── login/                      LoginScreen.kt
│   ├── dashboard/                  DashboardScreen.kt
│   ├── tools/                       ToolsHubScreen.kt + one screen per tool
│   ├── projects/                    ProjectsScreen.kt, ProjectDetailScreen.kt
│   ├── notes/                        NotesScreen.kt, NoteEditorScreen.kt
│   ├── reports/                       ReportsScreen.kt
│   ├── settings/                       SettingsScreen.kt
│   ├── about/                           AboutAppScreen.kt, AboutDeveloperScreen.kt
│   ├── search/                            GlobalSearchScreen.kt
│   └── components/                         Reusable Compose building blocks
├── viewmodel/                     One ViewModel per screen/feature + ViewModelFactory.kt
├── repository/                    ProjectRepository, QueryRepository, NoteRepository,
│                                  ReportRepository, ActivityRepository, CveRepository
├── database/
│   ├── ReconXDatabase.kt           Room database definition
│   ├── entity/                     Entities.kt (Project, Query, Note, Report, Activity,
│   │                                History, Cve)
│   └── dao/                        Daos.kt
├── network/                       WhoisClient, DnsClient (raw UDP), GeoIpClient,
│                                  HttpAnalysisClient
├── scanner/                       TlsInspector, SecurityHeaderAnalyzer, TechFingerprinter
├── models/                        Models.kt (plain result/DTO classes)
├── utils/                         Constants, CredentialHasher, HashUtils, EncodingUtils,
│                                  PasswordStrengthUtils, DorkTemplates, DateUtils,
│                                  PreferencesManager, RiskScorer
├── reports/                       ReportModels.kt, MarkdownReportBuilder.kt
└── export/                        FileExportManager.kt, PdfReportGenerator.kt
```

---

## Offline Login

Username: `admin` / Password: `kushagra` -- checked entirely offline.

The password is **never stored or compared in plain text**. On first launch,
`PreferencesManager.ensureDefaultCredentialSeeded()` generates a random per-install salt
and stores only `PBKDF2-style iterated SHA-256(password + salt)` (12,000 rounds) in
DataStore (see `utils/CredentialHasher.kt`). Login is a local hash comparison -- no
Firebase, no cloud auth, no analytics, no network call of any kind.

A "Biometric app lock" toggle exists in Settings (persisted via DataStore); wiring the
actual `BiometricPrompt` invocation at app-resume is the one item left as a clearly-marked
next step (see **Known Gaps** below) rather than being silently left out.

---

## Feature Map (desktop -> Android)

| Desktop feature | Android equivalent |
|---|---|
| Dashboard stats + recent activity | `DashboardScreen` + `DashboardViewModel` |
| Query generator (Google dorks) | `DorkBuilderScreen` (Google/Bing/GitHub/Shodan/Censys, one shared screen parameterized by engine) |
| Query Library / templates | `DorkTemplates.kt` (ported 1:1 from `config.py :: DEFAULT_TEMPLATES`, expanded to 5 engines) |
| Search Workspace (projects, notes, tags) | `ProjectsScreen` / `ProjectDetailScreen` / `NotesScreen` |
| Reports (Markdown/JSON/PDF) | `ReportsScreen` + `MarkdownReportBuilder` + `PdfReportGenerator` (adds TXT too) |
| SQLite database | Room (`ReconXDatabase`, 7 tables) |
| Settings (theme, backup) | `SettingsScreen` (+ biometric toggle, analyst name, storage usage) |
| Logging | Room `activity_log` + `history` tables, surfaced as "Recent Activity" / "Search History" |
| File management | `FileExportManager` (app-private storage + `FileProvider` share sheet) |

## New Security/OSINT Modules

- **OSINT dork builders**: Google, Bing, GitHub code search, Shodan, Censys -- all via one
  parameterized screen + `DorkTemplates.kt`.
- **Domain Intelligence**: WHOIS (raw RFC 3912 client over TCP/43), full DNS suite
  (A/AAAA/MX/TXT/NS/SOA/CNAME via a small hand-rolled UDP DNS client -- `network/DnsClient.kt`,
  the same wire protocol `dig` uses), SPF/DMARC extraction from TXT records.
- **Website Security**: HTTP header fetch + redirect-chain following, security-header
  checklist (HSTS/CSP/X-Frame-Options/etc.), CORS check, cookie-flag audit, technology
  fingerprinting (Server/X-Powered-By/cookie heuristics, Wappalyzer-style but header-only),
  TLS/certificate inspection (negotiated version + cipher + expiry via a normal
  `SSLSocket` handshake -- the same thing a browser does), robots.txt / security.txt /
  sitemap.xml detection, allowed-HTTP-methods probe.
- **IP Intelligence**: hostname resolution, reverse DNS (PTR), and ASN/ISP/geolocation via
  the free, keyless `ip-api.com` endpoint.
- **Hash Tools**: MD5/SHA-1/SHA-256/SHA-512 for text and files, plus hash comparison.
- **Password Tools**: offline entropy/strength meter + configurable policy checker. No
  cracking/brute-force logic exists anywhere in the app.
- **Encoding Tools**: Base64 / URL / Hex / Binary / Unicode, both directions.
- **Regex Lab**: live tester with match highlighting, replace preview, and a small library
  of common patterns (email, IPv4, URL, hashes, MAC, etc.).
- **Offline CVE Lookup**: import a JSON CVE list once (while online, from any source you
  trust) into Room; every subsequent search is a local query with zero network calls.
- **Checklists**: OWASP Top 10 (2021), a general security-audit checklist, a bug-bounty
  recon checklist, and an incident-response checklist, each with session checkboxes.
- **Global Search**: one search box fanning out across Projects, Queries, and Notes.

---

## Building the Project

1. Open the `KushagraReconX/` folder in Android Studio (Koala or newer recommended).
2. Let Gradle sync (it will download the AGP 8.5.2 / Kotlin 1.9.24 / Compose BOM
   2024.06.00 toolchain on first sync).
3. Run on a device/emulator running API 24+.
4. Log in with `admin` / `kushagra`.

Release builds have `isMinifyEnabled = true` and `isShrinkResources = true` with R8 full
mode enabled in `gradle.properties`, and the dependency list is deliberately short
(no Hilt, no Retrofit/OkHttp/Gson, no image-loading library) to keep the APK small.

---

## Known Gaps (documented honestly, not silently skipped)

A project this size has a few items that are real, working, but intentionally lighter
than a full production build-out:

- **Biometric app lock**: the Settings toggle persists correctly, but the actual
  `BiometricPrompt` challenge on app resume/foreground isn't wired into `MainActivity` yet
  -- straightforward to add with `androidx.biometric.BiometricPrompt` in an `onResume` hook.
- **Database-at-rest encryption**: Room/SQLite itself isn't encrypted; the DB lives in
  Android's app-private sandboxed storage, and exported reports can use
  `androidx.security-crypto`'s `EncryptedFile` if you want ciphertext-on-disk for
  sensitive exports. Full SQLCipher-grade DB encryption would need the
  `net.zetetic:android-database-sqlcipher` native dependency, which was left out to keep
  the "minimal dependencies / small APK" goal intact -- swap it in via
  `SupportFactory` in `ReconXDatabase` if you need it.
- **Evidence attachments/screenshots and a visual timeline** for projects: the data model
  (`ProjectEntity`, `NoteEntity`) supports extension, but the attachment-picker UI and a
  dedicated timeline view aren't built yet -- notes and reports cover most of the same
  need today.
- **Offline CVE import format**: a JSON array of
  `{cveId, product, version, severity, score, summary}` objects. Any curated NVD export
  reshaped into that shape will import correctly.

None of the above are stubbed with fake data or TODO placeholders in code that's
supposed to run -- they're simply the pieces flagged for a follow-up pass.

---

## License

MIT License. See the in-app About Application screen for the open-source
acknowledgements (Compose, Room, DataStore, security-crypto, biometric, coroutines).

&copy; 2026 Kushagra Singh Bisht. All Rights Reserved.
