# Changelog

## [0.9.1] - 2026-10-01

### Fixed

- prevent weekday average chart crash for colored apps

### Changed

- add screenshots taken on an emulator with generated data

## [0.9.0] - 2026-10-01

### Added

- remember the last export directory
- offer to share the database after exporting
- add browser tab capture history
- add remove old exports option for periodic export cleanup
- add unlock timeline chart showing lock/unlock periods per day
- add one-time export dialog with directory picker and GZIP compression
- add optional GZIP compression for periodic database export
- add fixed filename option for periodic database export
- add usage timeline chart with full history navigation

### Fixed

- target the installed package in every build variant
- reopen browser session after transient interruptions
- detect incognito mode in Vanadium browser
- resolve usage interval storage bugs (double-counting, zero-duration, midnight-crossing)
- add QUERY_ALL_PACKAGES permission for app icon visibility on Android 11+
- prevent NPE in EventLogWrapper when className is null on Android 16

### Changed

- regenerate Gradle wrapper from the official 8.4 release
- rewrite store metadata for the fork
- exclude dependency metadata block from APKs
- remove browser tab capture accessibility service
- change application ID to aah.mozart.usagedirectplus
- replace upstream contact details with fork's own
- simplify browser sessions to hostname-only tracking
- ignore compiled artifacts
- commit remaining workspace changes
- normalize app ids and vacuum after upgrade
- Add CLAUDE.md project instructions
- Add CI/CD with versioning, changelog, and APK publishing
- Add test infrastructure and comprehensive test suite
- Update README with modernization details
- Modernize app to 2025 Android standards
- Add usage intervals detail screen for database flavor
- Fix duplicate usage intervals when EventLogRunnable runs twice per day
- Rebrand usageDirect to UsageDirectPlus
- Add usage intervals, screen event tracking, and periodic DB export
- Update donation name
- Update translation contributor name

## [0.8.1] - 2025-01-01

### Added

- Usage intervals detail screen for database flavor
- Test infrastructure and comprehensive test suite
- Modernized app to 2025 Android standards (Room, Kotlin coroutines, ViewBinding)

### Fixed

- Fix duplicate usage intervals when EventLogRunnable runs twice per day