# Stranger Blocker — Agent Guidelines

## Versioning Scheme

```
vMAJOR.MINOR.PATCH
```

| Bump | When | Example |
|------|------|---------|
| PATCH | Bug fixes, compile errors, small tweaks | `1.8.2` → `1.8.3` |
| MINOR | Features, new screens, settings, UI changes | `1.8.0` → `1.9.0` |
| MAJOR | Breaking changes, full redesigns | `1.x` → `2.0.0` |
| PREVIEW | Pre-release builds between MINOR and MAJOR | `1.9.5-p01`, `1.9.5-p02` |

Always bump `versionCode` by 1 alongside `versionName` in `app/build.gradle.kts`.

Rules:
- The git tag must match `versionName` exactly — preview tags include the `-pNN` suffix (e.g. `v1.9.5-p36`), stable tags do not (`v2.0.0`).
- Preview releases ship as GitHub **pre-releases** (the CI workflow marks any tag containing `-` as prerelease).
- A stable release (no `-p` suffix) ends the preview line: `versionCode` keeps incrementing, `versionName` drops the suffix.

## Commit Convention

```
type: message
```

Types: `feat`, `fix`, `chore`, `refactor`, `docs`, `ci`

Recent preview commits use the `pNN: <summary>` prefix (e.g. `p36: date filters, batch select counts`) — keep using that style for previews, and `type: message` for stable and CI-only changes.

Every commit must include:
```
Ultraworked with [Sisyphus](https://github.com/code-yeongyu/oh-my-openagent)

Co-authored-by: Sisyphus <clio-agent@sisyphuslabs.ai>
```

## Release & CI Recovery

- After pushing a release tag, **monitor the CI build**. Do not continue to the next task until the tagged build succeeds.
- If the CI build for a tagged release **fails**:
  1. **Delete the tag** both locally and on origin: `git tag -d vX.Y.Z` then `git push origin :refs/tags/vX.Y.Z`.
  2. **Fix the issue** and commit + push the fix on `main` as usual.
  3. **Re-tag with the same version number** (`vX.Y.Z`) and push it again. **Do NOT bump into the next iteration** — the fix belongs under the current version's tag.
  - Note: the CI workflow already deletes any existing release for a tag before creating it (`gh release delete ... --yes`), so re-pushing the same tag will not hit "a release with the same tag already exists" (catalog #12).

## Pre-Commit Quality Gate

Before any `git commit`, run this checklist:

- [ ] Bracket balance: `(` count == `)` count, `{` count == `}` count
- [ ] No stale references to removed functions, classes, or state variables
- [ ] No duplicate function definitions
- [ ] All imports resolve — every import must reference an actual symbol in the current dependency version. Especially:
  - `R` in sub-packages (`import com.strangerblocker.R`)
  - `Uri` in ViewModel (`import android.net.Uri`)
  - Compose experimental annotation classes (`import androidx.compose.foundation.ExperimentalFoundationApi`)
  - `ThemeMode` in MainScreen (`import com.strangerblocker.ui.theme.ThemeMode`)
- [ ] No imports from `androidx.compose.foundation.lazy` for functions that are member functions of `LazyListScope` (e.g. `stickyHeader` is a member, not a top-level import)
- [ ] Variables used before declaration — `pagerState` and `scope` must be declared before `TabBar` that references them
- [ ] Generic type inference with enums — `listOf<Pair<ThemeMode, String>>(...)` for paired enum+string lists. Use explicit type or a typed variable
- [ ] `setAutoCancel`, `setSilent` — only available on `NotificationCompat.Builder`, NOT on platform `Notification.Builder`
- [ ] `weight()` — only works inside `ColumnScope` or `RowScope`; never on a standalone composable. Use `Box(weight(1f))` wrapper when weight is needed outside direct scope.
- [ ] `@OptIn` annotations present for experimental Compose APIs — annotate the composable function, not just the call site (`ExperimentalFoundationApi`, `ExperimentalMaterial3Api`). Verify the annotation class itself is **also imported**.
- [ ] `versionName` in `build.gradle.kts` matches the git tag (preview tags include the `-pN` suffix)
- [ ] `versionCode` incremented
- [ ] `latestChangelog()` updated in `MainScreen.kt`
- [ ] README version history updated if needed

### Historic CI error catalog (all resolved)
| # | Error | Root cause | Fix |
|---|-------|-----------|-----|
| 1 | `Unresolved reference 'R'` | Missing `import com.strangerblocker.R` in `.service` sub-package | Added explicit import |
| 2 | `Unresolved reference 'stickyHeader'` | Imported as top-level function; it's a `LazyListScope` member | Removed import |
| 3 | `This foundation API is experimental` | Missing `@OptIn(ExperimentalFoundationApi::class)` on composable | Added annotation |
| 4 | `Unresolved reference 'ExperimentalFoundationApi'` | Annotation import missing | Added import |
| 5 | `Conflicting overloads: openUpdateDialog()` | Duplicate function definitions from edit artifact | Removed duplicates |
| 6 | `Unresolved reference 'Uri'` | Missing `import android.net.Uri` in ViewModel | Added import |
| 7 | `Unresolved reference 'weight'` | Used inside a separate composable, not in `ColumnScope` | Wrapped in `Box(weight(1f))` at parent scope |
| 8 | `Unresolved reference 'setSilent'` / `setAutoCancel` | Used on platform `Notification.Builder` instead of `NotificationCompat.Builder` | Removed from platform builder; kept only on compat builder |
| 9 | `Unresolved reference 'scope'` / `'pagerState'` | Used before variable declaration | Moved declarations above usage |
| 10 | `Unresolved reference 'ThemeMode'` | Missing import in MainScreen.kt | Added import |
| 11 | `Cannot infer type for parameter` | `listOf(ThemeMode.SYSTEM to "System", ...)` — generic type inference fails for enum pairs | Use explicit `List<Pair<ThemeMode, String>>` type |
| 12 | `a release with the same tag name already exists` | Force-pushed tag triggers CI to create duplicate release | CI uses `--generate-notes`; use new tag instead of force-push or delete old release first |
| 13 | Notification counter shows +1 extra | SharedPref counter initialized from DB (which already includes current call), then incremented again | Initialize from DB without extra increment; only increment on subsequent calls |
| 14 | `Unresolved reference 'animateDpAsState'` | Wrong import path — used `androidx.compose.animation.animateDpAsState` instead of `androidx.compose.animation.core.animateDpAsState` | Fixed import path |
| 15 | `Unresolved reference 'offset'` | `Modifier.offset(x)` needs `import androidx.compose.foundation.layout.offset` | Added explicit import |
| 16 | `Cannot infer type for parameter` (animateDpAsState) | `tabWidth * slideProgress` type can't be inferred as `Dp` | Declared explicit `val indicatorTarget: Dp = ...` before passing to `animateDpAsState` |
| 17 | Version comparison fails for preview tags | `isNewerThan("1.9.5-p01")` split on `.` gives `"5-p01"` which `toIntOrNull()` converts to `0` | Strip `-pNN` suffix before semver compare, then compare preview numbers separately |
| 18 | `Unresolved reference 'UpdateCheckResult'` / delegate getValue error cascade | New data class added to `data/` but not imported in ViewModel/MainScreen — cascades into fake type-inference errors elsewhere | When adding a type in `data/`, verify ALL files referencing it have the import (grep `\bType\b` in each file) |
| 19 | `Property delegate must have a 'getValue' method` | Cascade symptom — `collectAsState()` on a StateFlow whose type is unresolved (root cause is a missing import elsewhere) | Fix the missing type import first; the delegate error disappears |
| 20 | `Popup` offset type mismatch | `DpOffset` not accepted by `Popup.offset` in this Compose version | Use `IntOffset` computed with `LocalDensity`: `with(LocalDensity.current) { IntOffset(0, (-104).dp.toPx().toInt()) }` |
| 21 | `Unresolved reference 'roundToPx'` / `toPx` as member | `roundToPx` import doesn't resolve and `Density.toPx(Dp)` isn't a member in this version | `toPx` is a `Dp` extension: call `dpValue.toPx()` inside a Density receiver (`with(LocalDensity.current)`) — never `density.toPx(dpValue)` |
| 22 | `This foundation API is experimental` on a row composable | `combinedClickable` used in a composable without the opt-in (only the parent had it) | Annotate every composable that uses the API, not just its caller |
| 23 | Notification number badge shows +1 | `NotificationCompat.Builder.setNumber(count + 1)` — count already includes the current call | Use `setNumber(count)`; the +1 was the catalog-#13 pattern resurfacing |
| 24 | SMS blocking behaves on before ever enabled | `SmsReceiver`/`SmsNotificationListener` defaulted `sms_blocking_enabled` to `true`, ViewModel to `false` | Align all three default reads to `false` |

## App UI & Navigation Guidelines

### Screen Architecture

The app has four bottom-nav tabs managed by `BottomNavTab` enum + `bottomNavTab` StateFlow in `MainViewModel`:

```
DASHBOARD (default)
  ├── Blocking status banner (effective state)
  ├── Total Blocked Today / Calls & SMS This Week / Weekly chart / Recent Activity
  └── FAB (quick actions: Manual block, Whitelist)
CALLS
  ├── Search + Blocked | Whitelist tabs (Blocked FIRST) in a shared card
  └── Card header: filter (date range) + clear; "N selected" during batch mode
SMS
  ├── Search + Blocked | Whitelist tabs (Blocked FIRST)
  └── Card header: filter (date range) + clear; "N selected" during batch mode
SETTINGS
  ├── Blocking (permission-aware toggles for calls & SMS)
  ├── Notifications / Notification icon / SMS keywords / Data (CSV export) / Theme / Updates / About
  └── Section titles flush-left, section content indented 16dp
```

### Rules

- Toggles live ONLY in Settings. Calls and SMS tabs are pure data views (search, tabs, card).
- **Tab order is Blocked-first everywhere**: `Tab` enum order is `BLOCKED, WHITELIST`, the `TabBar` renders "Blocked" then "Whitelist", pager page 0 = Blocked content, and `selectedTab` defaults to `Tab.BLOCKED`.
- Navigation uses stacked screens (no nav library). Each screen gets its own `Scaffold` with top bar, back arrow always `Icons.AutoMirrored.Filled.ArrowBack`.
- Global dialogs (update, clear history, whitelist/manual-block confirms, keyword removal) live in the root `MainScreen` composable.
- `viewModel.resetUiState()` is called when the app returns to the foreground (lifecycle `ON_START`, skipping first launch) — the app always reopens on the Dashboard with cleared search/filters/dialogs.

### UI Style & Conventions

- Single accent: Emerald `#10B981`. Cards: 16dp radius, `surfaceVariant` container, subtle elevation. Sentence case everywhere. Monospace for phone numbers.
- **Overlay tint**: every dialog, popup bubble, and bottom sheet uses `MaterialTheme.colorScheme.surfaceVariant` as its container — never the default M3 surface colors, which look different from the app.
- **Text inputs**: use the compact `SearchField` composable (38dp tall, `bodySmall` text/placeholder, 12dp radius) for ALL inputs — tab searches, dialog fields, keyword input. NEVER use M3 `OutlinedTextField` for new inputs (it cannot shrink its internal padding).
- **Blocking status banner** reflects the *effective* state per channel, not just the role:
  - `callsActive = isRoleHeld && isBlockingEnabled && !isPaused`
  - `smsActive = smsPermissionGranted && smsBlockingEnabled && !isPaused`
  - States: `ROLE_MISSING` (red, tap→grant) / `ALL_ACTIVE` (green, not tappable) / `CALLS_ONLY` / `SMS_ONLY` / `PAUSED` (tap→resume) / `NONE` (tap→Settings).
- **FAB quick actions** open a small floating `Popup` bubble anchored to the FAB's Box (alignment `TopEnd`, `IntOffset` up ~104dp), NOT a bottom sheet.
- **Batch selection** state is hoisted to the screen composable (`selectedIds`, `selectionMode`). The card header shows "N selected" while selecting; long-press enters selection mode. Works identically for Calls and SMS.
- **Date-range filters**: filter icon in the card header opens a small popup with From/To date pickers + Confirm/Clear. Filter state lives in the ViewModel (`callFilterFrom/To`, `smsFilterFrom/To` + count flows) and merges into `filteredGroupedCalls`/`filteredGroupedSms` via `combine`. An active filter shows a red badge with the matching count.
- **CSV export** lives in Settings → Data as a button. Export icons do NOT live in card headers.
- **Whitelist adds** always go through a confirmation dialog with duplicate validation ("Already on the whitelist") and a success toast. Direct `addToWhitelist` calls are only for internal/confirmed paths.
- **Search/filtered lists**: screens always use the ViewModel's filtered flows (`filteredGroupedCalls`, `filteredGroupedSms`, `filteredWhitelisted*`) — they return the unfiltered list when no query/filter is active, so no branching is needed at call sites.

### Permission handling

- Call Screening role and `RECEIVE_SMS` are granted from the Settings toggles (tap-to-grant). Toggling ON without the permission launches the grant flow and auto-enables on success; on rejection a toast explains blocking can't work without it.
- Both `SmsReceiver` and `SmsNotificationListener` read `sms_blocking_enabled` with default `false` — keep this consistent with the ViewModel.

## Attribution

When adding/changing code (not docs/readme), append:
```
Ultraworked with [Sisyphus](https://github.com/code-yeongyu/oh-my-openagent)

Co-authored-by: Sisyphus <clio-agent@sisyphuslabs.ai>
```
