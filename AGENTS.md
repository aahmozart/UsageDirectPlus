# Agent Guidelines

## Testing Requirements

Every time a new functionality is added or a bug is fixed, a corresponding test must be created to cover the change. This applies to:

- New features: add unit tests (and instrumented tests if Android-specific) covering the new behavior
- Bug fixes: add a test that reproduces the bug scenario and verifies the fix
- Refactors: ensure existing tests still pass; add tests if coverage gaps are identified

### Pre-Commit

All tests must pass before committing. Run `./gradlew :Application:testDatabaseDebugUnitTest` and verify a clean result before creating any commit.

### Test Structure

- **JVM unit tests** (`src/test/` or `src/testDatabase/`): Use JUnit 5, MockK, and Truth
- **Instrumented tests** (`src/androidTestDatabase/`): Use JUnit 4 with AndroidX Test, Room testing, and Truth

## Build Verification

After tests pass for a new feature or bug fix, build debug APKs to verify the build is not broken:

```bash
./gradlew :Application:assembleDatabaseDebug :Application:assembleSystemDebug
```

APK output paths:
- `Application/build/outputs/apk/database/debug/`
- `Application/build/outputs/apk/system/debug/`

## Commit Convention

This project uses **conventional commits** for automated version bumping and changelog generation. All commit messages must follow this format:

```
<type>(<optional scope>): <description>
```

| Type | Bump | Description |
|---|---|---|
| `feat` | minor | A new feature |
| `fix` | patch | A bug fix |
| `perf` | patch | A performance improvement |
| `refactor` | — | Code refactoring (no bump) |
| `test` | — | Adding or updating tests (no bump) |
| `docs` | — | Documentation changes (no bump) |
| `chore` | — | Build, CI, tooling changes (no bump) |

Breaking changes: add `!` after the type (e.g., `feat!: remove old API`) to trigger a major bump.
