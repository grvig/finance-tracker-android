# Finance Tracker

An Android expense tracker shared by a household. Two to four people record what they spend and everyone sees the same data on their own phone, updated live.

<p>
  <img src="screenshots/dashboard.png" width="250" alt="Dashboard showing the monthly total, budget progress and a category breakdown" />
  <img src="screenshots/expenses.png" width="250" alt="Expense list with the search and filter bar" />
  <img src="screenshots/dark-mode.png" width="250" alt="The dashboard in dark mode" />
</p>

[Download the latest APK](https://github.com/grvig/finance-tracker-android/releases/latest) (Android 8.0 or newer)

I wrote this for my own family, so it assumes a small group of people who trust each other. There is no per-person ledger and no settling up. It answers one question: where did the money go this month.

## Features

### Households

Sign in with an email, then create a household or join one with a six character code. Expenses, budgets, categories and recurring bills all belong to the household rather than to an individual, and every expense records who entered it.

### Live updates

Screens read from Firestore snapshot listeners instead of one-shot fetches. An expense added on one phone shows up on the others within a second, so the app has no refresh button.

### Filtering

A filter bar sits above every expense list: text search, date range (this week, this month, last 30 days, or a custom range), category, payment method, and sorting by date or amount. The count and total above the list always describe what is currently visible. Tapping a category in the Dashboard or Reports opens the list already filtered to it.

### Budgets and recurring bills

<img src="screenshots/budgets.png" width="250" align="right" alt="Budget screen with a progress bar against actual spending" />

Budgets are a monthly limit per category plus a warning threshold, drawn as a progress bar against real spending.

Recurring bills repeat weekly or monthly and become ordinary expenses when they fall due. The app generates them inside a Firestore transaction that re-reads the due date before writing, so two people opening the app at the same time cannot both log the same bill.

Reports break a month down by category and by member.

<br clear="right" />

### Sharing

Any expense, filtered list, budget, recurring bill or monthly report can be rendered to a PNG and sent through the Android share sheet. The card is shown as a preview first, and exports are always drawn in the light theme so they stay legible for whoever receives them.

<p>
  <img src="screenshots/share.png" width="250" alt="Share preview showing the image that will be sent" />
  <img src="screenshots/shared-image.png" width="290" alt="An exported PNG listing five expenses with a total" />
</p>

### Adding an expense without opening the app

A home screen widget and a long press launcher shortcut both lead to a small card that floats over the launcher: type an amount, pick a category and a payment method, save. It writes on an application scope and closes immediately, so the card is gone before Firestore has finished. Both the category and the payment method start on whatever was used last, and "More options" hands the half typed expense to the full form.

Android widgets cannot hold a text field, which is why this is a floating activity rather than a keypad on the home screen.

### Notifications

Each person chooses, on their own phone, which household members they want to hear from. Adding an expense then notifies whoever follows you. Several expenses arriving at once are bundled into one summary rather than a stack of alerts, and tapping through opens the expense list.

There is no server. A WorkManager job polls for rows newer than the last one it saw, so a notification can take a few minutes to arrive. That keeps the project on Firebase's free tier, which no longer includes Cloud Functions.

### Theme

The theme can follow the system or be pinned to light or dark.

## Architecture

| | |
|---|---|
| Language | Kotlin 2.2 |
| UI | Jetpack Compose, Material 3 |
| Auth and data | Firebase Auth (email/password), Cloud Firestore |
| Pattern | MVVM: Repository → ViewModel → Composable |
| Async | Coroutines and Flow. Snapshot listeners are wrapped in `callbackFlow` and exposed as `StateFlow` |
| Charts | Drawn directly on a Compose `Canvas` |
| Min / target SDK | 26 / 36 |

There is no dependency injection framework. ViewModels are built by hand-written factories. Navigation is a `Screen` enum plus a `mutableStateListOf` back stack rather than the Navigation component. Both would be worth revisiting if the app grew, but at nine screens the plain versions are shorter than the setup they would replace.

```
app/src/main/java/com/grvig/financetracker/
├── data/            Plain data classes
├── repository/      Firestore reads and writes, live queries as callbackFlow
├── viewmodel/       ViewModels and their factories
├── ui/theme/        Colour scheme, typography
├── *Screen.kt       One file per screen
├── ExpenseFilters   Filtering and sorting model, pure Kotlin, unit tested
├── ShareCard.kt     The layouts that get rendered to PNG
└── MainActivity.kt  Navigation back stack, drawer, theme wiring
```

## Running it

The committed `google-services.json` points at my Firebase project, so a clone will not authenticate. To run your own:

1. Create a Firebase project and register an Android app using the applicationId `com.grvig.financetracker`, or change it in `app/build.gradle.kts`.
2. Enable Authentication with the Email/Password provider, and create a Cloud Firestore database.
3. Replace `app/google-services.json` with the one Firebase generates.
4. Publish [`firestore.rules`](firestore.rules). Every read and write is scoped to members of the household.

```bash
./gradlew installDebug
```

Filtering, sorting and label formatting are pure functions and have unit tests. Screens are checked by hand on an emulator.

```bash
./gradlew testDebugUnitTest
```

## Release builds

Signing reads a `keystore.properties` from the repository root. Both it and the keystore are gitignored.

```properties
storeFile=keystore.jks
storePassword=...
keyAlias=...
keyPassword=...
```

If that file is absent the signing config is skipped, so debug builds still work on a fresh clone.

```bash
./gradlew assembleRelease
```

## Release history

| Version | Changes |
|---|---|
| 1.4 | Quick add card, per member expense notifications |
| 1.3 | Live sync, home screen widget, dark mode, new launcher icon, recurring duplicate fix |
| 1.2 | Filtering and sorting, PNG sharing, UI rework, card names |
| 1.1 | Navigation drawer, Material 3 theme, rebuilt charts, My Expenses |
| 1.0 | Multi-user households on Firebase |
