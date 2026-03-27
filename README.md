# Georgia TB Reference Guide Android App

This repository contains the Android application for the Georgia TB Reference Guide, developed for the Georgia Department of Public Health. The app serves as a comprehensive resource for Tuberculosis Management guidelines in the state of Georgia.

## Overview

The Georgia TB Reference Guide App is a mobile application designed to provide healthcare professionals and TB coordinators with easy access to up-to-date information about tuberculosis management guidelines. The app includes various features to enhance user experience and facilitate quick access to critical information.

This app has been featured in the following publication:
- Armistead B, Arconada Y, Hochberg NS, Ray SM, Anderson EJ (2023) Development of a mobile application to support tuberculosis care in Georgia, USA. PLOS ONE 18(8): e0298758. https://doi.org/10.1371/journal.pone.0298758

## Features

- **Updated TB Management Guidelines**: Comprehensive and current information on tuberculosis management protocols
- **TB Coordinator Directory**: Complete list of TB Coordinators across the state of Georgia
- **Personalized Features**:
  - Note-taking capabilities
  - Bookmark important sections
  - Content sharing functionality

## Installation

The app is available on the Play Store: [Georgia TB Reference Guide](https://play.google.com/store/apps/details?id=org.apphatchery.gatbreferenceguide&hl=en-US&pli=1)

## Usage

After installation, users can:
1. Access TB management guidelines
2. Look up contact information for TB Coordinators
3. Create and save personal notes
4. Bookmark frequently accessed sections
5. Share content with colleagues

## Support

For technical support or questions about the app, please contact [Contact Information TBD].

## Acknowledgements

This project is supported by:

- The National Center for Advancing Translational Sciences of the National Institutes of Health under Award Number UL1TR002378. The content is solely the responsibility of the authors and does not necessarily represent the official views of the National Institutes of Health.

- The Georgia Department of Public Health through Contract 40500-046-21203197

## License

[License Information TBD]

---

## Technical Overview

This section describes how the app is structured and how its core processes work. It is intended for developers maintaining or extending the codebase.

### App Startup

When the app process starts, the `App` class runs before any screen is shown. It initializes Firebase, forces the system font scale to a fixed value (so device accessibility settings do not distort the layout), applies the user's saved theme preference (light, dark, or system), and sets up Pendo for analytics tracking.

`MainActivity` then creates the main navigation structure: a custom ActionBar with a back button, a bottom navigation bar with three tabs (Home, Search, Settings), and a NavController that manages all screen transitions. It also locks the app to portrait orientation and triggers a Firebase Remote Config fetch in the background.

### First Launch vs. Subsequent Launches

Every time the app opens, `MainFragment` reads a stored `BUILD_VERSION` integer from DataStore and compares it to the value hardcoded in the app. This is the primary mechanism that determines whether the app needs to reinitialize its content.

- **If the version does not match** (first ever install, or the app was updated with new content): the app runs a full initialization sequence called `firstLaunch()`.
- **If the version matches**: the app runs a lighter startup called `init()`.

### Full Initialization Flow (`firstLaunch`)

This process runs on a background thread and typically completes in under 100 milliseconds.

1. The progress bar is shown and bottom navigation is disabled so the user cannot interact while the database is being built.
2. An initialization lock (`AppInitLock`) is acquired. This is a global mutex that ensures only one coroutine can run initialization at a time, preventing race conditions if multiple fragments happen to trigger it simultaneously.
3. The web content cache is rebuilt. The app deletes the existing `pages/`, `assets/`, and `images/` directories from the device's cache folder, then copies fresh versions of all HTML, CSS, JavaScript, and image files from the bundled APK assets. This step also serves as a self-repair mechanism: if the user has cleared the app's cache, the content is restored automatically.
4. A legacy note migration runs. This updates any user-created notes that referenced old internal table names, ensuring they still point to the correct content after a rename.
5. The database is purged and reseeded from the JSON asset files:
   - `chapter.json`, `subchapter.json`, and `chart.json` are parsed from the `assets/json/` folder.
   - All existing content rows (chapters, subchapters, charts, HTML info, search index) are deleted in a single atomic transaction.
   - The fresh data from JSON is inserted.
   - Plain text is extracted from every HTML page using Jsoup and stored in `HtmlInfoEntity` to power the search feature.
   - The full-text search index (`GlobalSearchEntity`) is rebuilt by combining chapter titles, subchapter titles, and the extracted body text.
6. The new `BUILD_VERSION` is written to DataStore so this heavy initialization does not run again until the next content update.
7. Control returns to the main thread: the progress bar is hidden, the bottom nav is re-enabled, and the app proceeds with the regular `init()` flow.

### Regular Launch Flow (`init`)

On every launch (including after `firstLaunch` completes), `init()` runs to set up the live UI:

- It checks whether the HTML/CSS/JS files still exist in the cache folder. If a user has cleared the app's storage since the last launch, the cache is rebuilt without touching the database.
- Room-backed adapters are attached to the chapter list, chart list, recents, and bookmarks so the UI reflects the current database state reactively.
- A Firebase Remote Config listener is connected. If the remote `update_value` key has changed since the last launch, a prompt appears offering the user a download of updated district coordinator content.
- Firebase Dynamic Link handling is configured so that deep links shared externally open the correct screen directly.

### Content Delivery

All TB guideline content — the HTML articles, stylesheets, scripts, and images — is bundled inside the APK under the `assets/` directory. On first launch this content is copied to the device's cache folder, where it is served to the WebView. This means the app works fully offline.

The one exception is the district TB coordinator directory page, which is downloaded from a remote GitHub Pages URL and cached locally. It is re-downloaded when the Firebase Remote Config `update_value` key changes, allowing the coordinator list to be updated without releasing a new version of the app.

### Full-Text Search

During database seeding, the app builds a Room FTS4 (Full-Text Search version 4) virtual table called `GlobalSearchEntity`. Each row in this index holds the chapter title, subchapter title, full article body text, and metadata needed to navigate to the result.

When the user types in the search bar, the query is split on whitespace and punctuation, and the terms are joined with `OR` operators before being sent to Room. Results are returned with the matching text highlighted.

### User Data

Notes, bookmarks, and recently viewed items are stored in their own Room tables and are never deleted during the purge-and-reseed process. The user's personal contact entries (added manually through the app) are similarly preserved. Only the seeded content tables are wiped on `BUILD_VERSION` changes.

### Navigation

The app uses a single-Activity architecture with Jetpack Navigation. The bottom navigation bar has three root destinations: Home, Search, and Settings. From Home, the user can drill down through Chapters → Subchapters → Article body, or browse the Charts list directly to an article. A Contacts section allows browsing public TB coordinator information and managing personal contacts. The bottom nav bar is hidden on all non-root screens, and the ActionBar back button appears in its place.

---

## Breaking Changes and Safe Update Guide

This section documents the changes that have caused crashes or data loss in the past, and how to make similar changes safely in the future. It reflects real incidents from the app's development history, not just hypothetical risks.

---

### The Core Vulnerability: Bookmarks and Notes Are Tied to Content IDs

Before covering specific scenarios, it is important to understand a fundamental design constraint.

When a user bookmarks a chart/table, the app stores the chart's `id` field from `chart.json` (a human-readable string like `"table_9_pediatric_dosage_isoniazid_in_children_..."`) as the bookmark's primary key in the database. When a user creates a note while reading a chart, the same string is stored as the note's reference key. For subchapter-based bookmarks and notes, the stored key is the subchapter's numeric ID.

This means that **if a chart ID is renamed or removed in `chart.json`, every existing user who had bookmarked or annotated that chart now holds a stale reference**. After the app reseeds its database from the updated JSON, those old IDs no longer exist in the charts table. Any attempt to navigate to that bookmark will find nothing and crash, or silently show an empty screen.

This is the class of crash that caused the creation of `LegacyRedirects.kt` and `LegacyNotesMigrator.kt`.

---

### A. Renaming or Removing Charts in `chart.json`

This is the highest-risk change in the codebase and has caused crashes in production.

**What happened:** Charts were renumbered (e.g., what was formerly Table 10 became Table 9 after another table was removed) and some charts were removed entirely. The `id` field in `chart.json` encodes the table number in its string (e.g., `"table_10_pediatric_dosages_rifampin_..."`), so any renumbering changes the ID. After the next app update, the database was reseeded with the new IDs, but existing users' bookmarks and notes still referenced the old IDs. Those references were now orphaned.

**How the fix works:**

- `LegacyRedirects.kt` contains a static map of old chart IDs to their new equivalents. When a user opens their bookmarks, the app looks up each bookmark ID in this map. If a redirect exists, the user sees a message that the content has changed and is navigated to the new version. The bookmark is then silently updated in the database to the new ID so the redirect only triggers once.
- `LegacyNotesMigrator.kt` runs automatically at every `firstLaunch` (app update), before the database is reseeded. It scans all stored notes, applies the same redirect map, and updates any stale note references in place. By the time the user interacts with their notes, the migration has already run.

**How to handle future chart changes safely:**

1. When renaming or removing a chart, add an entry to the `CHART_REDIRECTS` map in `LegacyRedirects.kt` mapping the old `id` to the new one. If the chart is deleted with no replacement, map it to the closest surviving chart so the user lands somewhere sensible.
2. Update `chart.json` with the new or revised entries.
3. Increment `BUILD_VERSION` in `MainFragment` so `firstLaunch()` runs on the next install and `LegacyNotesMigrator` processes the changes.
4. Never reuse an old chart ID string for a different chart. Once an ID has been shipped to users, it is a permanent reference in their databases.

---

### B. Renaming or Removing Subchapters in `subchapter.json`

Subchapter bookmarks and notes store the subchapter's numeric ID, not its title or URL. Because subchapter IDs are auto-generated by Room during seeding, they are relatively stable as long as entries are not removed and re-added in a different order. However, if a subchapter is removed and another is added in its place, a numeric collision is possible.

**What can break:**

- A user's note says it belongs to subchapter ID 24. After the JSON is changed and the database is reseeded, subchapter 24 now refers to a different section — the note appears under the wrong article.
- A subchapter is deleted entirely — bookmarks referencing it navigate to nothing and crash.

**How to handle safely:**

- `LegacyNotesMigrator` also handles subchapter migrations. It applies the redirect map and then attempts to re-resolve the subchapter by searching the freshly seeded database. If a match is found by title or key, it updates the note's stored ID to the new value.
- If a subchapter is renamed, add a corresponding entry to the `SUBCHAPTER_REDIRECTS` map in `LegacyRedirects.kt` before shipping the change.
- Avoid removing subchapters outright. Prefer renaming or merging them, and always provide a redirect to an existing destination.

---

### C. JSON Field Structure Changes (`chart.json`, `chapter.json`, `subchapter.json`)

Beyond ID changes, the structure of the JSON files themselves can cause crashes during the seeding process.

**What can break:**

- **Renaming a JSON key** (e.g., `"chartTitle"` → `"title"`) — Gson maps JSON keys to Kotlin property names by exact string match. A rename silently leaves the field as `null` or `0`, which can produce NullPointerExceptions when the app tries to display or navigate to that entry.
- **Changing a field's data type** (e.g., `chapterId` from integer to string) — Gson throws a parse exception during seeding, preventing the database from being built at all. The app will appear to hang or crash on the loading screen.
- **Adding a non-null Kotlin property to an entity** without providing a default value and without adding the field to the JSON — Gson deserialization fails for every entry in that file.

**How to handle safely:**

- New fields on data classes must have a default value or be nullable so that older JSON files (or partially updated files) still deserialize cleanly.
- Never rename an existing JSON key. If a rename is needed, add the new key alongside the old one, update the consuming code to prefer the new key, and remove the old key in a later release after all users have updated.
- After any structural change to a JSON file, test seeding locally by clearing app data and reinstalling before shipping.
- Increment `BUILD_VERSION` after any JSON change so that all users receive a fresh reseed on their next launch.

---

### D. HTML Asset Files (`pages/`, `assets/`, `images/`)

Each subchapter entry in `subchapter.json` has a `url` field that is the filename (without extension) of an HTML page bundled in the APK. The app loads this file from the device's cache when the user opens that article.

**What can break:**

- **Renaming an HTML file** without updating the matching `url` in `subchapter.json` — the WebView loads nothing, showing a blank screen.
- **Deleting a referenced HTML file** — same blank result; navigation appears to succeed but no content is shown.
- **Reorganizing the directory layout** — `GuideContentUpdater` copies files from the APK's assets using hardcoded paths (`pages/`, `assets/`, `images/`). Moving those directories without updating that class leaves the cache empty and all articles unreadable.

**How to handle safely:**

- Any HTML filename change must be applied to both the file itself and its `url` entry in `subchapter.json` in the same commit, and the old filename must be removed.
- Do not restructure the top-level asset directories without updating `GuideContentUpdater` to match.
- After any HTML file change, bump `BUILD_VERSION` so the updated files are copied to the cache on the next launch.

---

### E. Room Database Schema

Room validates the database schema on every open by comparing a stored hash against the current entity definitions. Any mismatch that is not covered by a registered migration causes an immediate crash before the app renders anything.

**What can break:**

- **Incrementing `@Database(version = X)` without providing a migration** — Room throws `IllegalStateException` on the first open after the update. There is no recovery other than clearing app data, which would destroy all user notes, bookmarks, and contacts.
- **Adding a non-null column to an entity** — the schema hash changes; without a migration, the app crashes.
- **Changing a primary key type or name** — same schema mismatch crash.

**How to handle safely:**

- Every increment to the `@Database` version number requires a corresponding `Migration(oldVersion, newVersion)` object added to `AppModule` and registered with `.addMigrations(...)`.
- For new tables or nullable columns, a simple `CREATE TABLE` or `ALTER TABLE ADD COLUMN` migration is sufficient.
- For structural changes to seeded content tables only, `fallbackToDestructiveMigration()` combined with a `BUILD_VERSION` bump is acceptable — the data will be reseeded automatically. **Do not use this for tables that hold user data** (`NoteEntity`, `BookmarkEntity`, `RecentEntity`, `Contact`, `PrivateContact`). Destroying those tables destroys the user's work with no way to recover it.

---

### F. `BUILD_VERSION` Constant

`BUILD_VERSION` in `MainFragment` is the single trigger for all content reinitialization. It has no connection to the Play Store version code or the Room database version — it is purely a content version.

**What can break:**

- **Forgetting to increment it** after a content change — existing users' apps see a matching version, skip `firstLaunch()`, and continue showing stale data. The database still holds old chart entries, old HTML is still in the cache, and `LegacyNotesMigrator` does not run.

**How to handle safely:**

- Increment `BUILD_VERSION` for every change to `chart.json`, `chapter.json`, `subchapter.json`, or any HTML asset file.
- Add a brief inline comment next to the constant recording the date and reason (e.g., `// v15: March 2026 — removed tables 10–11, renumbered to 9`). This comment becomes the audit trail in `git log` for understanding when each migration was needed.
- Skipping numbers is safe. Incrementing by more than one at a time is also safe.

---

### G. Firebase Remote Config (`update_value` key)

The district TB coordinator directory is the only content that can be updated without shipping a new APK. The app checks the `update_value` integer in Firebase Remote Config on every launch and downloads a refreshed coordinator page when the value changes.

**What can break:**

- **Changing the remote page URL** without updating the hardcoded URL in `FAMainViewModel` — downloads silently fail; users see outdated or missing coordinator information with no error message.
- **Incrementing `update_value` before the new page is live** — all users are prompted to download content that does not yet exist.

**How to handle safely:**

- Deploy the updated HTML page first, then increment `update_value` in Firebase Remote Config only after the new page is confirmed live and accessible.
- If the page URL itself changes, update both the hardcoded fallback URL in `FAMainViewModel` and the Remote Config value in the same deployment.
