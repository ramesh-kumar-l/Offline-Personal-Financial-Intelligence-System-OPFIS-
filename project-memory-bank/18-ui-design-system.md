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