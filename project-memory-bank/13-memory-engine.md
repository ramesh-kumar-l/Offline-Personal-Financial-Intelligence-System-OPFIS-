# Memory Engine

Persistent financial timeline and semantic memory.

## Phase 6 - Financial Memory (implemented)

Two new domain packages, both following the existing entity+port+
use-case pattern - see `03-domain-model.md` for field lists.

- **`memory/`** - `MemoryEvent` (NOTE or MILESTONE, optionally attached
  to one `subject` entity) + `MemoryEventRepository` + 4 use cases
  (`ObserveMemoryTimelineUseCase`, `ObserveMemoryEventsForEntityUseCase`,
  `RecordMemoryEventUseCase`, `DeleteMemoryEventUseCase`). Deliberately
  manual-only: no other use case in the app automatically emits a
  `MemoryEvent` (e.g. on "budget exceeded" or "goal reached"). Automatic
  event generation was judged out of scope for this pass - it would
  mean touching many existing, already-tested use cases across every
  feature to emit events, a large blast-radius change better done as
  its own follow-up once there's a clearer picture of which events are
  actually worth surfacing (see `26-active-initiatives.md`).
- **`relationship/`** - `Relationship` (a typed, user-declared link
  between two `EntityRef`s) + `RelationshipRepository` + 3 CRUD use
  cases, plus a pure `KnowledgeGraphBuilder` (`ObserveKnowledgeGraphUseCase`)
  that projects a root entity's `Relationship`s into a 1-hop
  `KnowledgeGraph(root, neighbors, edges)` - ROADMAP's "Knowledge graph
  abstractions" deliverable. This is intentionally *not* a full
  transitive graph traversal across every entity's existing foreign
  keys (`Document.linkedTransactionId`, `transaction_tag`,
  `Category.parentId`, ...) - those remain each feature's own concern.
  `Relationship` only stores links the schema has no dedicated column
  for (e.g. a loan Document supporting a Liability).
- **`domain/entity/`** - `EntityType` (every addressable entity kind,
  including Asset/Liability/Budget/Goal which have no search presence)
  + `EntityRef(entityType, entityId)`, a cross-cutting pointer used by
  both `MemoryEvent.subject` and `Relationship`'s endpoints so neither
  package depends on every feature package directly. Deliberately
  separate from `SearchEntityType` (`14-search-engine.md`), which only
  covers FTS5-indexed kinds.

Schema v6 (`migrations/5.sqm`): `memory_event` (indexed on
`subject_entity_type`/`subject_entity_id`) and `relationship` (indexed
on both endpoint pairs). `memory_event` is wired into the Phase 4/5
`search_index` FTS5 table via the same trigger pattern (`title || ' '
|| description`), so recorded memories are globally searchable
(`SearchResult` gained `MemoryEventMatch`). `relationship` is not
search-indexed - it has no free text, only typed entity references.

Presentation: a 4th bottom-nav destination, "Memory"
(`composeApp/.../memory/MemoryScreen` + `MemoryScreenBody` +
`MemoryEventRow`) - an inline form to record a NOTE or MILESTONE
(title/description, always with `subject = null` from this screen; a
future entity-picker to attach a memory to a specific Account/Goal/etc.
is a known follow-up) above the chronological timeline. `Relationship`/
`KnowledgeGraph` have no dedicated screen yet - the domain/data layers
are complete and tested, but wiring a graph-browsing UI is deferred
(exit criteria was "Financial memory engine", not a full UI - see
`26-active-initiatives.md`).

## Known gaps

- No automatic `MemoryEvent` generation from other features (manual
  recording only) - a deliberate scope cut, not an oversight.
- No UI for creating/browsing `Relationship`s or the `KnowledgeGraph` -
  the engine (domain + data + tests) is complete; only presentation is
  deferred.
- `MemoryScreen`'s add-note form never sets `MemoryEvent.subject` - a
  memory can only be attached to an entity programmatically today, not
  through this screen.
