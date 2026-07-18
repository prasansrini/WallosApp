---
name: wallosapp-development
description: "Always activate this skill when working on the WallosApp project to enforce the caveman and ponytail coding standards."
---

# WallosApp Developer Guidelines

This repository enforces the use of the `caveman` and `ponytail` skills for all agent operations to ensure token-efficient communication and extremely clean, non-overengineered code.

---

## 🚀 Session Initialization
At the start of your session, initialize the active modes by executing these commands:
* `/caveman full` (or `ultra` for maximum token savings)
* `/ponytail full` (or `ultra` for maximum code conciseness)

---

## 📐 Ponytail Coding Ladder
Before writing or modifying any code, stop at the first rung of the ladder that holds:
1. **YAGNI (You Aren't Gonna Need It)**: Does this feature or logic actually need to exist? If not, skip it.
2. **Reuse**: Is there a similar function, utility, or class already in the codebase? Reuse it instead of writing a new one.
3. **Stdlib**: Can the Kotlin standard library or Android SDK handle this directly?
4. **Platform**: Is there a native platform/Jetpack Compose element that solves this? Use it (e.g., standard input fields/selectors instead of importing complex custom UI components).
5. **Dependency**: Can an already-installed third-party dependency fulfill the requirement?
6. **One Line**: Can it be safely written in a single line?
7. **Minimum**: Write the simplest working implementation. No boilerplate, no unrequested abstractions, and no unnecessary layers.

### 🏷️ Ponytail Comments
If you must make a deliberate simplification that cuts a corner for a known limitation, add a comment naming the ceiling and upgrade path:
```kotlin
// ponytail: [ceiling & upgrade path]
```

---

## 💬 Caveman Prose Rules
* Drop all articles, filler, pleasantries, and introductory text (e.g. do not say "Here is...", "Sure...", "I can help with that...").
* Respond in short fragments, bullet points, or grunts.
* Keep exact technical terms and code snippets unchanged.
* **Grammar Pattern**: `[thing] [action] [reason]. [next step].`

---

## 🤖 Subagent Delegation (Cavecrew)
Use the `invoke_subagent` tool with the following specialized roles to conserve parent context tokens:
* **`cavecrew-investigator`**: Delegate for read-only actions like finding code definitions, mapping directories, or listing usages of a symbol.
* **`cavecrew-builder`**: Delegate for surgical, 1-2 file edits (typo fixes, refactoring a single function).
* **`cavecrew-reviewer`**: Delegate for reviewing pull requests or diffs (produces one line per severity finding, skips praise).

---

## 🛠️ Utility Commands
* `/ponytail-review` / `/caveman-review`: Audits staged files or diffs for over-engineering.
* `/ponytail-audit`: Audits the whole repository to identify dead code, boilerplate, or potential areas to delete.
* `/caveman-compress`: Compresses local logs, developer notes, or memory files to save context window tokens.
* `/caveman-stats` / `/ponytail-gain`: Prints session token savings, execution time savings, and cost benefits.
