# ====================================================================
# SYSTEM_PROMPT.md
# PART 2
#
# Architecture Constitution
#
# This document defines the permanent architecture of OPFIS.
#
# Architecture is considered immutable unless explicitly approved.
#
# Features may evolve.
#
# Architecture must remain stable.
# ====================================================================

# ARCHITECTURE PHILOSOPHY

Architecture always wins over implementation speed.

A feature can always be delayed.

Architecture mistakes survive for years.

Whenever implementation conflicts with architecture

STOP

Explain the issue.

Recommend alternatives.

Wait for approval.

Never silently violate architecture.

----------------------------------------------------------------

# SYSTEM GOALS

The architecture must support

• 100% Offline Usage

• Zero Mandatory Cloud Dependencies

• Complete Local Ownership

• Long-term Maintainability

• AI Integration

• Plugin Architecture

• Modular Growth

• Cross Platform Expansion

• Millions of Financial Records

• Decades of User History

----------------------------------------------------------------

# PRIMARY DESIGN PRINCIPLES

The system follows

Offline First

↓

Local First

↓

Security First

↓

Reliability First

↓

Performance First

↓

Extensibility

↓

Developer Experience

Features are the final concern.

----------------------------------------------------------------

# CLEAN ARCHITECTURE

The repository shall follow strict Clean Architecture.

Presentation

↓

Application

↓

Domain

↓

Infrastructure

Dependencies only point inward.

Presentation

MUST NEVER

directly access

SQLite

Storage

HTTP

File System

AI Models

Infrastructure

Presentation only communicates through

Use Cases.

----------------------------------------------------------------

# DOMAIN DRIVEN DESIGN

The Domain Layer is the heart.

Everything else depends upon it.

The Domain Layer must contain

Entities

Value Objects

Aggregates

Repository Interfaces

Domain Services

Policies

Specifications

Business Rules

Domain Events

No framework code.

No Android code.

No SQLite code.

No networking.

Pure business logic only.

----------------------------------------------------------------

# LAYER RESPONSIBILITIES

Presentation Layer

Responsible for

UI

Navigation

State

Accessibility

Animations

User Interaction

Never contains business logic.

----------------------------------------------------------------

Application Layer

Responsible for

Use Cases

Transactions

Validation

Coordination

Authorization

Workflow

Application Services

No SQL.

No Android.

No framework logic.

----------------------------------------------------------------

Domain Layer

Responsible for

Business Logic

Financial Rules

Policies

Calculations

Entities

Knowledge Graph

Financial Memory

Completely platform independent.

----------------------------------------------------------------

Infrastructure Layer

Responsible for

SQLite

SQLCipher

Storage

OCR

LLM Runtime

Embeddings

Vector Search

Logging

Backups

Import

Export

External integrations.

----------------------------------------------------------------

# MODULES

Repository should evolve as

core/

financial-engine/

memory-engine/

search-engine/

document-engine/

security/

analytics/

ai/

backup/

settings/

ui/

shared/

Each module owns its own APIs.

Avoid cyclic dependencies.

----------------------------------------------------------------

# MODULE COMMUNICATION

Modules never call each other directly.

Communication happens through

Interfaces

Events

Use Cases

Dependency Injection

Never through static utilities.

----------------------------------------------------------------

# DEPENDENCY RULES

Allowed

Presentation

↓

Application

↓

Domain

↓

Infrastructure

Forbidden

Infrastructure

↓

Presentation

Domain

↓

Infrastructure

UI

↓

Database

Never violate dependency direction.

----------------------------------------------------------------

# STORAGE ARCHITECTURE

Primary Database

SQLCipher

Reason

Reliable

Embedded

Encrypted

Transactional

Portable

No cloud database.

No Firebase.

No Realm.

No mandatory sync engine.

----------------------------------------------------------------

# DATABASE PRINCIPLES

Everything important

belongs inside SQLCipher.

Never spread critical data
across multiple storage systems.

The database is

The Single Source of Truth.

----------------------------------------------------------------

# PERSISTENCE

All writes

must be

Atomic

Transactional

Recoverable

Idempotent where applicable.

Never leave partial writes.

----------------------------------------------------------------

# STORAGE MODEL

Persistent

↓

Encrypted

↓

Indexed

↓

Auditable

↓

Recoverable

----------------------------------------------------------------

# DATABASE DESIGN

Prefer

Normalized tables.

Avoid

Duplicate data.

Every table

must have

Primary Key

Created Time

Updated Time

Version

Audit metadata where appropriate.

Soft delete only where justified.

----------------------------------------------------------------

# SEARCH

Two search systems.

Structured Search

SQLite indexes

FTS5

Semantic Search

Embedded vector index

Results merged using ranking strategies.

Search must work

Offline.

----------------------------------------------------------------

# KNOWLEDGE GRAPH

Knowledge Graph

is

Core Infrastructure.

Not an optional feature.

Every important entity

can be connected.

Account

↓

Investment

↓

Goal

↓

Document

↓

Transaction

↓

Person

↓

Institution

↓

Asset

↓

Loan

Graph enables

Reasoning

Recommendations

Timeline

Context

Explainability

----------------------------------------------------------------

# FINANCIAL MEMORY

Every financial event

is preserved.

Examples

Salary

Bonus

Loan

Investment

Insurance

Dividend

Property Purchase

Promotion

EMI

Tax

Receipt

Everything becomes

Long Term Memory.

Never discard history.

----------------------------------------------------------------

# AI ARCHITECTURE

AI is

Local.

Never assume internet.

AI modules

must be replaceable.

Model abstraction

↓

Inference interface

↓

Prompt interface

↓

Context provider

↓

Response parser

UI never depends on model.

----------------------------------------------------------------

# AI PROVIDERS

Support

ONNX Runtime

GGUF

MediaPipe

Future NPUs

Implementation must remain provider independent.

----------------------------------------------------------------

# DOCUMENT ENGINE

Supports

PDF

Image

OCR

Bank Statements

Salary Slips

Insurance

Invoices

Tax Documents

Everything processed locally.

----------------------------------------------------------------

# BACKUP ENGINE

Backups are

Encrypted

Portable

Versioned

Recoverable

User Controlled

Never upload automatically.

----------------------------------------------------------------

# IMPORT ENGINE

Importers

must be isolated.

CSV Import

PDF Import

Broker Import

Bank Import

Future formats

Each importer

is an independent plugin.

----------------------------------------------------------------

# EXPORT ENGINE

Export must support

CSV

JSON

Encrypted Backup

Reports

No proprietary lock-in.

----------------------------------------------------------------

# EVENT SYSTEM

Prefer

Domain Events

Examples

TransactionAdded

InvestmentCreated

GoalCompleted

BackupCreated

MemoryIndexed

Avoid tightly coupled services.

----------------------------------------------------------------

# LOGGING

Logs must be

Structured

Useful

Minimal

Privacy Safe

Never log

Financial values unnecessarily

Passwords

Keys

Tokens

PII

Logs must never leak sensitive data.

----------------------------------------------------------------

# ERROR HANDLING

Every failure

must

Explain

Recover

Preserve data

Never crash silently.

Never lose information.

----------------------------------------------------------------

# SECURITY

Security is

Architecture.

Not middleware.

Every feature

must be reviewed

for

Privacy

Encryption

Access Control

Data Exposure

Before implementation.

----------------------------------------------------------------

# PERFORMANCE BUDGETS

Cold Start

<1 second

Database Query

<50ms

Search

<100ms

Dashboard

<300ms

AI Response

<2 seconds

Document Import

Background

Never sacrifice responsiveness.

----------------------------------------------------------------

# MEMORY BUDGET

Application should remain efficient.

Avoid

Large object graphs

Memory leaks

Repeated parsing

Repeated allocations

Cache intelligently.

----------------------------------------------------------------

# TESTABILITY

Every module

must be independently testable.

Business logic

must not require

Android

UI

Database

Network

Framework

to execute.

----------------------------------------------------------------

# FEATURE IMPLEMENTATION CONTRACT

Every feature must include

Architecture review

Implementation

Unit tests

Integration tests

Documentation

Memory updates

No feature is complete

without all six.

----------------------------------------------------------------

# ENGINEERING CHECKPOINT

Before merging code

verify

Architecture preserved

No regressions

Tests passing

Documentation updated

Memory updated

Performance maintained

Security maintained

If any answer is NO

Implementation is incomplete.

----------------------------------------------------------------

# END OF PART 2