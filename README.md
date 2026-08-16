<div align="center">

# Finance Tracker

**A shared expense tracker for a household.**
Two to four people log what they spend, and everyone sees the same numbers on their own phone — updated live.

![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)
![Min SDK](https://img.shields.io/badge/min%20SDK-26-2E7D32)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4)

<img src="screenshots/dashboard.png" width="260" alt="Dashboard showing the monthly total, budget progress and a category breakdown" />
<img src="screenshots/expenses.png" width="260" alt="Expense list with the search and filter bar" />
<img src="screenshots/dark-mode.png" width="260" alt="The dashboard in dark mode" />

### [⬇ Download the latest APK](https://github.com/grvig/finance-tracker-android/releases/latest)

<sub>Requires Android 8.0 (API 26) or newer</sub>

</div>

---

I built this for my own family, so it optimises for a small trusted group rather than for scale. There's no ledger and no settling up — just a straight answer to *"what did we spend this month, and on what?"*

## Features

### Shared, and live

Sign in with an email, then create a household or join one with a six-character code. Everything is scoped to that household.

When one person adds an expense, it appears on everyone else's phone straight away. Screens read from Firestore snapshot listeners rather than one-shot fetches, so there is no refresh button anywhere in the app.

### Expenses worth searching

Every expense records amount, category, payment method, card name, date, time, description, notes, and who added it.

On top of that sits a filter bar: free-text search, a date range (this week, this month, last 30 days, or dates you pick), category, payment method, and sorting by date or amount. A running count and total always reflects what's currently on screen. Tapping a category on the Dashboard or in Reports jumps straight to those expenses.

### Budgets and recurring bills

<img src="screenshots/budgets.png" width="260" align="right" alt="Budget screen with a progress bar against actual spending" />

Set a monthly limit per category with a warning threshold, and track it as a progress bar against real spending.

Recurring bills — weekly or monthly — turn themselves into real expenses when they fall due. Generation runs inside a Firestore transaction that re-checks the due date before claiming it, so a bill is logged exactly once no matter how many household members open the app at the same moment.

Reports break a month down by category and by member.

<br clear="right" />

### Share anything as an image

Any expense, filtered list, budget, recurring bill or monthly report renders to a PNG and goes out through the Android share sheet. You see a preview of exactly what will be sent before it goes, and exports are always light-themed so they stay readable for whoever receives them.

<p>
  <img src="screenshots/share.png" width="260" alt="Share preview showing the image that will be sent" />
  <img src="screenshots/shared-image.png" width="300" alt="An exported PNG listing five expenses with a total" />
</p>

### Smaller things

A home screen widget and a long-press app shortcut that jump straight to the Add Expense form, and a light / dark / follow-system theme setting.

## Built with

| | |
|---|---|
| Language | Kotlin 2.2 |
| UI | Jetpack Compose, Material 3 |
| Auth & data | Firebase Auth (email/password), Cloud Firestore |
| Architecture | MVVM — Repository → ViewModel → Composable |
| Async | Coroutines and Flow; snapshot listeners exposed as `StateFlow` |
| Charts | Hand-drawn on Compose `Canvas` — no charting library |
| Min / target SDK | 26 / 36 |

Two deliberate omissions: there's no dependency injection framework — ViewModels are wired with hand-written factories — and navigation is an enum plus a `mutableStateListOf` back stack rather than the Navigation component. At this size both stay easier to follow than the machinery they'd replace.

```
app/src/main/java/com/grvig/financetracker/
├── data/            Plain data classes
├── repository/      Firestore reads/writes, live queries as callbackFlow
├── viewmodel/       ViewModels and their factories
├── ui/theme/        Colour scheme, typography
├── *Screen.kt       One file per screen
├── ExpenseFilters   Filtering/sorting model — pure Kotlin, unit tested
├── ShareCard.kt     The layouts that get rendered to PNG
└── MainActivity.kt  Navigation back stack, drawer, theme wiring
```

## Building it yourself

You'll need Android Studio — its bundled JDK is sufficient.

This app points at my Firebase project, so a clone won't authenticate against it. To run your own:

1. Create a Firebase project and add an Android app with the applicationId `com.grvig.financetracker` (or change it in `app/build.gradle.kts`).
2. Enable **Authentication → Email/Password** and create a **Cloud Firestore** database.
3. Replace `app/google-services.json` with your own.
4. Publish the rules in [`firestore.rules`](firestore.rules) — they scope every read and write to members of the household.

```bash
./gradlew installDebug
```

Filtering, sorting and label formatting are pure functions with unit tests:

```bash
./gradlew testDebugUnitTest
```

Screens are verified by hand on an emulator.

### Release builds

Signing reads from a `keystore.properties` at the repo root, gitignored along with the keystore itself:

```properties
storeFile=keystore.jks
storePassword=...
keyAlias=...
keyPassword=...
```

Without that file the signing config is skipped, so debug builds still work.

```bash
./gradlew assembleRelease
```

## Release history

| Version | Highlights |
|---|---|
| **1.3** | Live sync, home screen widget, dark mode, new app icon, recurring duplicate fix |
| **1.2** | Filter/sort system, PNG sharing, UI modernisation, card names |
| **1.1** | Navigation drawer, green Material 3 theme, charts rebuilt, My Expenses |
| **1.0** | Multi-user households on Firebase |
