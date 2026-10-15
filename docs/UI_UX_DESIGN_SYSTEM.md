# Sentinel Mobile — UI/UX Design System Guidelines & Readiness

## 1. Overview & Architectural Grounding

This document outlines the UI/UX architecture and design system guidelines for **Sentinel Mobile**, based on:
1. The vetted companion architecture specification in [`SENTINEL_MOBILE_ARCHITECTURE_AND_IMPLEMENTATION_PLAN.md`](file:///c:/Users/omkar/OMKAR/alert/SENTINEL_MOBILE_ARCHITECTURE_AND_IMPLEMENTATION_PLAN.md).
2. The installed `ui-ux` Design Intelligence Skill (`.agents/skills/ui-ux`), specifically incorporating **Jetpack Compose stack rules** (`data/stacks/jetpack-compose.csv`) and **WCAG 2.2 AA / Mobile Accessibility standards**.

**Core Product Mandate:** Sentinel Mobile is a high-utility, low-friction, native Android companion application. It is **not** an administration console or an analytics dashboard. Its UI is strictly focused on:
- High/Critical threat push triage and notification actions.
- Real-time Alert Queue inspection (`GET /alerts/recent`).
- Security Event Logs stream (`GET /logs/recent`).
- Detailed forensic alert inspection (`GET /alerts/{alert_id}`).
- Operator session state and tenant context switching (`TenantSession`).

---

## 2. Jetpack Compose UI Architecture & Stack Rules

Derived from `data/stacks/jetpack-compose.csv` in the local design intelligence database:

### 2.1 State Management & Unidirectional Data Flow (UDF)
- **Single Source of Truth:** UI state is exposed strictly via immutable `StateFlow<UiState>` from Hilt ViewModels (`@HiltViewModel`).
- **Explicit Modeling:** UI states are defined using sealed interfaces (e.g., `AlertsUiState.Loading`, `AlertsUiState.Success`, `AlertsUiState.Error`, `AlertsUiState.Empty`). No boolean flag combinations.
- **Stateless Composables by Default:** Composables must accept data classes and event lambdas (`onEvent: (AlertsEvent) -> Unit`). Hoist all state to callers or ViewModels.
- **Lifecycle-Aware State Collection:** All UI flows must be collected with `collectAsStateWithLifecycle()` in composables to prevent background coroutine leaks when the activity is stopped.
- **Remember Rules:** Use `remember` only for transient UI calculation (e.g., scroll state, animations). Business state must reside in ViewModel. Use `rememberSaveable` for UI state that must survive activity recreation or configuration changes.

### 2.2 Recomposition & Performance
- **Immutable/Stable Parameters:** Annotate immutable domain/UI data classes with `@Immutable` to allow the Compose compiler to skip redundant recompositions.
- **Lazy Lists & Stable Keys:** All `LazyColumn` items must supply a unique, stable key (`items(alerts, key = { it.id })`).
- **No Heavy Work in Composables:** Formatting, grouping, or complex sorting must be computed inside ViewModels or utility mappers off the main thread, not inside the composable rendering body.
- **Flat Layout Hierarchy:** Prefer modifier chaining (`Modifier.padding(...)`) over deeply nested layout wrappers (`Box -> Column -> Box -> Row`). Use `Box` only when elements overlap.

### 2.3 Navigation & Side Effects
- **Decoupled Navigation:** ViewModels emit navigation events using Kotlin `Channel` / `receiveAsFlow()`. Avoid `StateFlow` for navigation to prevent replaying events on screen rotation.
- **Type-Safe Routes:** Sealed classes or Kotlin serialization objects for routes instead of error-prone raw strings.
- **SideEffect Keying:** `LaunchedEffect` must specify exact reactive keys (e.g., `LaunchedEffect(alertId)`), avoiding `LaunchedEffect(Unit)` unless genuinely running once for the composition lifecycle.

---

## 3. Visual Language, Theme & Typography

### 3.1 Aesthetic Direction: "Security Operations Center — Field Dark"
- **Style Archetype:** Modern Security Operations / Dark High-Contrast. Dark surface base for optimal battery conservation on OLED screens, low night-shift glare for SOC operators, and high-visibility alert accents.
- **Color Tokens (Material 3 Dynamic Theming Baseline):**
  - **Surface / Background:** `#0B0F17` (Deep Obsidian Void), `#131B26` (Card Elevation 1), `#1B2636` (Card Elevation 2 / Borders).
  - **Primary / Brand:** `#2563EB` (Cobalt Core) / `#3B82F6` (Electric Blue).
  - **Severity Chips & Accents:**
    - `CRITICAL`: `#EF4444` (Crimson Alert) on `#450A0A` (Dark Crimson Container).
    - `HIGH`: `#F97316` (Amber Hazard) on `#431407` (Dark Amber Container).
    - `MEDIUM`: `#FBBF24` (Warning Yellow) on `#451A03` (Dark Gold Container).
    - `LOW` / `INFO`: `#38BDF8` (Sky Info) on `#082F49` (Dark Sky Container).
    - `RESOLVED` / `NORMAL`: `#10B981` (Emerald Secure) on `#064E3B` (Dark Emerald Container).
  - **Text & Foreground:**
    - `On-Surface (Primary)`: `#F8FAFC` (Slate 50) — High contrast (> 12:1 against `#0B0F17`).
    - `On-Surface-Variant (Muted)`: `#94A3B8` (Slate 400) — Contrast ratio > 5.5:1.
    - `Monospace Code/IP`: JetBrains Mono or System Monospace for IP addresses, hashes, and timestamps.

### 3.2 Typography Ramp (MaterialTheme.typography)
- No hardcoded `sp` font sizes in UI components.
- `titleLarge`: Screen headers (22sp, Medium/Bold).
- `titleMedium`: Alert threat titles (16sp, SemiBold).
- `bodyMedium`: Log details and metadata descriptions (14sp, Regular).
- `labelSmall`: Severity badges, source tags, timestamps (11–12sp, Monospace or Medium).

---

## 4. Accessibility (WCAG 2.2 AA Floor) & Touch Standards

1. **Color Contrast:** Minimum 4.5:1 contrast for normal text against background; 3:1 for graphical UI elements and severe state borders.
2. **Never Color Alone:** Severe alerts must include both an explicit textual badge (`CRITICAL`, `HIGH`) and a distinct icon (e.g., `Warning`, `ShieldAlert`), never color-only indicators.
3. **Touch Targets:** Minimum 48×48dp touch target size for all interactive controls (buttons, tabs, filter chips, sheet dismissals) with at least 8dp padding between adjacent targets.
4. **Semantics & Screen Readers:**
   - Explicit `contentDescription` on all icon buttons and status indicators.
   - Decorative icons must pass `contentDescription = null`.
   - Complex card items must combine accessibility semantics (`Modifier.semantics(mergeDescendants = true) { ... }`) to provide screen reader users with a cohesive summary in one swipe.
5. **System Text Scaling & Dynamic Type:** All layouts must wrap or scroll gracefully when users set system font scaling up to 200%. No fixed-height containers on text blocks.

---

## 5. UI Implementation Readiness Checklist

| Category | Requirement | Status |
| :--- | :--- | :--- |
| **Theme** | Material 3 ColorScheme definition with Dark theme default | Ready for Scaffolding |
| **State** | Sealed `UiState` and `UiEvent` contracts for Alerts and Logs | Ready for Scaffolding |
| **Components** | `SeverityChip`, `AlertCard`, `LogCard`, `EmptyStateView`, `ErrorStateView` | Ready for Scaffolding |
| **Navigation** | Type-safe Compose navigation destinations | Ready for Scaffolding |
| **Screens** | `AlertsScreen`, `AlertDetailSheet`, `LogsScreen`, `SettingsScreen` | Blocked on Phase Approval |
