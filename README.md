# MindNote

An Android notes + AI‑chat app. Capture short notes locally, keep them in sync with a backend, and chat with an assistant that has your notes as context.

---

## Features

- **Onboarding** — 2 steps: pick what you use MindNote for, then your preferred name. Runs once — a DataStore flag skips it on subsequent launches.
- **Home (Chat tab)** — greeting derived from time‑of‑day + saved name, a chat input field, a Suggested prompt list, and a horizontal "Recent notes" row. Tapping a suggestion pre‑fills the input; tapping Send (or the ⬆ icon) opens Chat with the typed text as the first user turn.
- **Notes** — list of captured notes, live‑filter search, topic chips derived from tags, favorites filter, per‑card heart toggle. Pull‑through sync from the backend on open.
- **Note detail** — shows title / date / tags / body; favorite toggle + delete (with confirmation dialog).
- **Capture** — title + body + inline tag editor (dialog‑based) + `Save` (saves to backend → Room → UI updates live).
- **Chat** — real‑time streaming replies from Claude via SSE. User notes are injected as system context for RAG. Conversation persists on the backend.
- **Offline‑friendly** — Room is the single source of truth for the UI; the API refreshes Room on demand.

---

## How to run

### Requirements

- Android Studio (Iguana or newer)
- JDK 17
- Android SDK 34
- An Android device or emulator on API 26+

The backend URL is baked into the build via `BuildConfig.BASE_URL`, currently pointing at the hosted instance:

```
https://api-production-6707b.up.railway.app/
```

If you need to point at a different backend, edit the `buildConfigField("String", "BASE_URL", …)` line in `app/build.gradle.kts` and rebuild.

### Build + run

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug   # requires a connected device/emulator
```

Or just click ▶ in Android Studio.

### Unit tests

```bash
./gradlew :app:testDebugUnitTest
```

26 tests cover ViewModels (MVI flows, filtering, SSE plumbing) and DTO ↔ entity mapping.

---

## Architecture

The app is a single Gradle module organized by layer + feature. The UI layer reads exclusively from Room flows; network calls mutate Room, never the UI state directly. This keeps the app responsive when offline and avoids UI flicker while requests are in flight.

```
┌──────────────────────────────────────────────────────────┐
│  UI   (Compose screens) — features/{home, notes, ...}    │
│    └─ ViewModels extend MviViewModel (MVI: Intent → State │
│       / Effect)                                           │
└──────────────────────────────────────────────────────────┘
           │ collects Flow from repositories
           ▼
┌──────────────────────────────────────────────────────────┐
│  Domain — domain/repository, domain/model                │
│    Interfaces:  NotesRepository, FavoritesRepository,    │
│                 UserRepository                            │
└──────────────────────────────────────────────────────────┘
           ▲                    ▲
           │ impl               │ impl
┌──────────┴───────────┐  ┌─────┴──────────┐
│  data/repository     │  │ data/remote    │
│  Room‑backed impls   │  │ Ktor HTTP      │
│  + API on writes;    │  │ client + SSE   │
│  UI reads Room Flow  │  │ DTOs + Api     │
└──────────┬───────────┘  └────────────────┘
           │
           ▼
┌──────────────────────────────────────────────────────────┐
│  Room DB  (data/db) — notes, topics, favorites, users,   │
│  conversations‑schema. Flows feed the UI.                 │
└──────────────────────────────────────────────────────────┘
```

### Layered folders (inside `app/src/main/java/com/mindnote/`)

| Package | Purpose |
|---|---|
| `MindNoteApp.kt` / `MainActivity.kt` | Application (Koin start) + single activity host. Start destination is chosen here (Home if onboarded, else Onboarding). |
| `core/navigation` | `Routes` + `MindNoteNavHost` (Navigation Compose). Chat route carries an optional URL‑encoded `?text=` so prompts can be forwarded. |
| `core/mvi` | Tiny MVI base class — `MviViewModel<Intent, State, Effect>` holds a `StateFlow<State>` and a `Channel<Effect>`, exposes `setState`, `emit`, `send(intent)`. |
| `core/ext` | `Result<T>` sealed interface + `safeApiCall { }` + `Throwable.toResult()` + `Result.Error.userMessage()`. Every API call in VMs goes through `safeApiCall`. |
| `core/storage` | `UserPrefs` — DataStore wrapper for username + onboarded flag. |
| `core/di` | `appModule` — Koin module that wires HttpClient, Room DB, DAOs, repositories, and every ViewModel. |
| `design/` | Compose design system: colors, typography, dimens, shared components (`EmptyState`, `LoadingIndicator`, `RowWithSpaceBetween`, `TagChip`, …), `MindNoteTheme` with `ProvidableCompositionLocal`s for colors, typography, dimens, and a shared `SnackbarHostState`. |
| `domain/model` | Plain Kotlin data classes (`Note`, `ChatMessage`, `OnboardingOption`, `NoteFilter`, …). No Android / Room / Ktor dependencies. |
| `domain/repository` | Repository interfaces — the UI layer only ever talks to these. |
| `data/db` | Room: entities, DAOs, `MindNoteDatabase`, type converters. |
| `data/remote` | Ktor client DTOs + API classes (`NotesApi`, `ChatApi`) and DTO ↔ entity mapping. |
| `data/repository` | Room‑backed repository implementations: `RoomNotesRepository`, `RoomFavoritesRepository`, `LocalUserRepository`. |
| `features/*` | One subpackage per screen (`home`, `notes`, `notedetail`, `capture`, `chat`, `onboarding`). Each has `…Screen.kt`, `…ViewModel.kt`, `…Contract.kt` (sealed `Intent` + data `State` + sealed `Effect`). |

### MVI in practice

Every feature follows the same shape:

- **State** — immutable data class, collected via `vm.state.collectAsStateWithLifecycle()`.
- **Intent** — sealed interface covering user actions. `vm.send(intent)` routes to `handle(intent)`.
- **Effect** — sealed interface for one‑shot UI side effects (navigation, snackbar). Collected once via `LaunchedEffect(Unit) { vm.effects.collect { … } }`.

### Offline‑first pattern

- Each screen's VM subscribes to a Room `Flow<…>` in `init { }` and mirrors it into state.
- On the same `init`, the VM also kicks off a background `refresh()` via the repo, which `runCatching`s the remote call and upserts into Room. Room emits new values → state updates → UI re‑renders.
- Writes are remote‑first (so the UI reflects the persisted server truth) but failures are surfaced as `Effect.ShowError` + left recoverable.

### Chat streaming

- `ChatApi.stream(conversationId, text)` returns `Flow<StreamEvent>` backed by the Ktor `HttpClient.sse { }` DSL (POST + `Accept: text/event-stream`).
- Server streams `content_block_delta` events; client re‑emits them as `StreamEvent.Token(text)` / `StreamEvent.Done` / `StreamEvent.Error(msg)`.
- `ChatViewModel` optimistically appends a user message + a "thinking…" assistant placeholder, then mutates the placeholder's text on every token arrival until `Done`.

### Error handling

Every API call in a VM goes through `safeApiCall { }` → `Result<T>`. The `userMessage()` helper maps `ApiError.NETWORK` / `SERVER` / response body to a user‑facing string. Each feature's `Effect.ShowError(message)` is collected in the screen and shown via `MindNoteTheme.snackbar.showSnackbar(...)` — the `SnackbarHost` is mounted once at the theme root via `LocalSnackbarHostState`.

### Localisation

All user‑visible strings live in `res/values/strings.xml` and are referenced via `stringResource(R.string.*)`. Format args are declared (`%1$d`, `%1$s`) so a future translation pass is a drop‑in.

---

## Tech stack

| Concern | Library |
|---|---|
| UI | Jetpack Compose (BOM) + Material 3 |
| Navigation | androidx.navigation.compose |
| Dependency injection | Koin 4 (`koin-android`, `koin-androidx-compose`) |
| Local DB | Room 2.6 (KSP) |
| Preferences | AndroidX DataStore Preferences |
| HTTP + SSE | Ktor Client 3 (`ktor-client-core` + `okhttp` engine + `content-negotiation` + `logging`) |
| JSON | kotlinx.serialization 1.7 |
| Coroutines | kotlinx.coroutines 1.9 |
| Testing | JUnit 4 + `kotlinx-coroutines-test` |
| Build | Android Gradle Plugin 8.5, Kotlin 2.0, KSP 2.0 |

---

## Testing

- Hand‑rolled `FakeNotesRepository` / `FakeFavoritesRepository` / `FakeUserRepository` in `src/test/java/com/mindnote/util/Fakes.kt` back each interface with `MutableStateFlow`.
- `MainDispatcherRule` swaps `Dispatchers.Main` for an `UnconfinedTestDispatcher` so `viewModelScope.launch` + `collect { … }` run eagerly — VM state is assertable on the next line.
- `HomeViewModelTest`, `NotesViewModelTest`, `NoteDetailViewModelTest`, `CaptureViewModelTest`, `OnboardingViewModelTest`, `DtoMappingTest` — 26 tests total.

---

## Backend

The app is paired with a Ktor + Postgres backend (separate repo/project) deployed at `https://api-production-6707b.up.railway.app/` and exposing:

- `GET /notes` / `GET /notes/{id}` / `POST /notes` / `DELETE /notes/{id}`
- `GET /favorites` / `POST /favorites/{noteId}` / `DELETE /favorites/{noteId}`
- `GET /conversations/{id}/messages` / `POST /conversations/{id}/stream` (SSE)

The SSE endpoint streams Claude Haiku 4.5 completions with the user's notes injected as system context for retrieval‑augmented chat.

---
