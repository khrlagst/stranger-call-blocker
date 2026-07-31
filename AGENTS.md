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

## Commit Convention

```
type: message
```

Types: `feat`, `fix`, `chore`, `refactor`, `docs`, `ci`

Every commit must include:
```
Ultraworked with [Sisyphus](https://github.com/code-yeongyu/oh-my-openagent)

Co-authored-by: Sisyphus <clio-agent@sisyphuslabs.ai>
```

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

## App UI & Navigation Guidelines

### Screen Architecture

The app has three screens managed by `Screen` enum + `currentScreen` StateFlow in `MainViewModel`:

```
HOME (default)
  ├── Toggle: Block strangers on/off
  ├── TabBar: Whitelist | Blocked
  ├── Shared Card with tab content
  └── Settings gear icon (top right)
        │
SETTINGS
  ├── Back arrow
  ├── Notification controls (Block alerts, Icon style)
  └── About row
        │
ABOUT
  ├── Back arrow → SETTINGS
  ├── Version, description
  ├── Update row (if available)
  └── Changelog + GitHub link
```

### Rules for adding new screens
- Add to `Screen` enum in `MainViewModel.kt`
- Navigation via `viewModel.navigateTo(Screen.XXX)` and `viewModel.goHome()`
- Use stacked screens (not nested) — no navigation library
- Each screen gets its own `Scaffold` with top bar
- Back navigation always uses `ArrowBack` icon
- Global dialogs (update, clear history, add whitelist) live in the root `MainScreen` composable, not inside individual screens

### UI style
- Single accent: Emerald `#10B981`
- Cards: 16dp radius, subtle elevation (`defaultElevation = 2.dp`), no flat borders
- Section headers: uppercase, emerald color
- Toggle switches: emerald when active
- Sentence case everywhere — no ALL CAPS
- Monospace font for phone numbers

## Attribution

When adding/changing code (not docs/readme), append:
```
Ultraworked with [Sisyphus](https://github.com/code-yeongyu/oh-my-openagent)

Co-authored-by: Sisyphus <clio-agent@sisyphuslabs.ai>
```
