# UI Design System
Premium, calm, trustworthy. Follow /frontend-design.

## Phase 3 - Dashboard

`DashboardScreen` (`composeApp/.../dashboard/`) is the app's home
screen: Net Worth (headline + Asset Allocation donut + legend), Cash
Flow (net total + grouped bar chart + legend), Recent Activity (list,
one row per transaction), Search (text field + results), Trust
Indicators (compact, replaces the retired Phase 0
`SystemStatusScreen`). Charts are custom Canvas draws (no third-party
charting library): flat fills, no gradients/3D/animation, fixed
never-cycled categorical colors, every colored element paired with an
icon/marker + text label so identity never depends on color alone
(SystemPrompt Part 3). Money renders via a hand-rolled
`MoneyFormatter` (thousands grouping, 2 decimals, no locale API, no
currency symbol - consistent with the single-currency decision).
`dataviz` skill's `validate_palette.js` could not run in this
environment (no `node`); the existing icon+text+color convention is the
practical substitute for a formal CVD check.

## Phase 4 - Search & navigation

`App.kt` now hosts a bottom `NavigationBar` (unicode glyph markers, no
Material Icons dependency, consistent with the rest of the app)
switching between `DashboardScreen` and the new `SearchScreen`.
`SearchScreen` (state/wiring) + `SearchScreenBody` (layout) show a
query field, an entity-type filter bar, tag filter chips, and either
the FTS5 global-results list (query non-blank) or a chronological,
taggable `TimelineSection` (query blank). Timeline rows use "▲"/"▼"/"⇄"
markers for income/expense/transfer (paired with color, never color
alone) and "#tagname ✕" text buttons to remove an assigned tag.

## Phase 5 - Receipt Vault

`App.kt`'s bottom `NavigationBar` gains a third destination, "Vault"
(`AppDestination.Vault`), alongside Dashboard and Search.
`DocumentVaultScreen` (state/wiring) + `DocumentVaultScreenBody`
(layout) show an import button (launches the OS file picker), a list
of imported documents (`DocumentRow` - filename, type, optional linked
transaction), and per-row actions to link/unlink a transaction or
delete the document.

## Phase 6 - Financial Memory

`App.kt`'s bottom `NavigationBar` gains a fourth destination, "Memory".
`MemoryScreen` (state/wiring) + `MemoryScreenBody` (layout) show an
inline record-a-memory form (title, description, a NOTE/MILESTONE
toggle rendered as two `TextButton`s with a "●" marking the selected
type - text, never color alone) above the chronological timeline.
`MemoryEventRow` renders each entry with a "★" (milestone,
`OpfisColors.Warning`) or "✎" (note, `OpfisColors.InformationNeutralBlue`)
glyph paired with its tint, plus a delete action.

## Phase 7 - AI Assistant

`App.kt`'s bottom `NavigationBar` gains a fifth destination,
"Assistant" (`AppDestination.Assistant`, "🤖"). `AiAssistantScreen`
(state/wiring) + `AiAssistantScreenBody` (layout) show a question input
card above a session-local conversation history - each `AiExchange`
renders the question, the answer text, and its `AiCitation`s via
`AiCitationRow`, which pairs every entity kind with a fixed
`OpfisColors` tint and a text prefix (`"ACCOUNT: ..."` etc.) - never
color alone, matching `SearchResultRow`'s convention.