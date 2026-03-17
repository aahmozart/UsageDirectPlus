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
