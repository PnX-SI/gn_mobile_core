# Contributing to gn_mobile_core

Thank you for your interest in contributing to **gn_mobile_core**! 🎉

This Git repository contains a set of shared libraries that are part of the [_GeoNature_](https://github.com/PnX-SI/GeoNature)
ecosystem, a biodiversity data management platform. The initial idea was to share common components
among several applications that might use the _GeoNature_ APIs.
Every contribution, whether you're a developer or not, helps improve the project.

---

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
  - [Non-Developer Contributions](#non-developer-contributions)
  - [Reporting a Bug](#reporting-a-bug)
  - [Suggesting a New Feature or Improvement](#suggesting-a-new-feature-or-improvement)
  - [Developer Contributions](#developer-contributions)
- [Setting Up the Development Environment](#setting-up-the-development-environment)
- [Branching Strategy](#branching-strategy)
- [Submitting a Pull Request](#submitting-a-pull-request)
- [Coding Standards](#coding-standards)
- [Testing](#testing)
- [Documentation](#documentation)
- [Commit Message Guidelines](#commit-message-guidelines)

---

## Code of Conduct

This project is open and welcoming to everyone. By participating, you agree to maintain a respectful
and constructive environment. Please be kind, inclusive, and professional in all interactions.

---

## How Can I Contribute?

### Non-Developer Contributions

You don't need to write code to contribute! Here are ways you can help:

- **Report bugs** you encounter while using the library.
- **Suggest improvements** to existing features or the documentation.
- **Propose new features** that would be useful to the community.
- **Improve documentation**, fix typos, clarify explanations, add examples.
- **Share feedback**, tell us what works well and what doesn't.
- **Translate** documentation or UI strings to another language.

All these contributions are made through **GitHub Issues** (see below).

---

### Reporting a Bug

If you find a bug, please [open a new issue](../../issues/new/choose) and select the **Bug Report**
template.

Before opening a new issue, please:

1. **Search existing issues** to avoid duplicates, your bug may already be reported.
2. Make sure you are using the latest available version.

A good bug report should include:

| Field                  | What to provide                                                      |
|------------------------|----------------------------------------------------------------------|
| **Title**              | A short, descriptive title                                           |
| **Description**        | A clear description of the unexpected behavior                       |
| **Steps to reproduce** | Step-by-step instructions to reproduce the bug                       |
| **Expected behavior**  | What you expected to happen                                          |
| **Actual behavior**    | What actually happened                                               |
| **Screenshots / logs** | Any relevant screenshot, stack trace, or log output                  |
| **Environment**        | Android version, device model, library version, `minSdk`/`targetSdk` |
| **Additional context** | Any other relevant information                                       |

> **Tip:** The more detail you provide, the easier it is for maintainers to reproduce and fix the
> issue.

---

### Suggesting a New Feature or Improvement

Have an idea to make `gn_mobile_core` better? We'd love to hear it!

Please [open a new issue](../../issues/new/choose) and select the appropriate template:

- **Feature Request** - for a brand-new capability (e.g. support for a new tile format, new
  gesture, new layer type…).
- **Improvement / Enhancement** - for an improvement of an existing feature (e.g. better
  performance, improved configuration options, refined UI behavior…).

A good feature request should include:

| Field                       | What to provide                                         |
|-----------------------------|---------------------------------------------------------|
| **Title**                   | A concise title                                         |
| **Problem / motivation**    | Why is this feature needed? What problem does it solve? |
| **Proposed solution**       | Describe your idea in as much detail as possible        |
| **Alternatives considered** | Have you considered other approaches?                   |
| **Additional context**      | Mockups, links to similar implementations, references…  |

> **Note:** Feature requests are not guaranteed to be implemented. The maintainers will review them
> and prioritize according to the project roadmap.

---

### Developer Contributions

If you want to contribute code, please follow the workflow described in the sections below.

---

## Setting Up the Development Environment

### Prerequisites

- **Android Studio** (latest stable release recommended)
- **JDK 17** or later
- **Android SDK** with API level 29+ installed
- A GitHub account

### Fork & Clone

1. **Fork** the repository by clicking the *Fork* button on GitHub.
2. **Clone** your fork locally:

   ```bash
   git clone https://github.com/<your-username>/gn_mobile_core.git
   cd gn_mobile_core
   ```

3. Add the upstream remote:

   ```bash
   git remote add upstream https://github.com/PnX-SI/gn_mobile_core.git
   ```

### Configure GitHub Packages Access

Some dependencies are published to GitHub Packages. Add your credentials to your global
`local.properties` file (located at `~/local.properties`):

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_PERSONAL_ACCESS_TOKEN
```

> Generate a Personal Access Token (PAT) with `read:packages` scope in
> *GitHub Settings -> Developer settings -> Personal access tokens*.

### Build the Project

```bash
./gradlew clean assembleDebug
```

The project contains the following modules:

| Module      | Description                                                                                                 |
|-------------|-------------------------------------------------------------------------------------------------------------|
| `commons`   | The core library, which, among other things, manages local data using SQLite                                |
| `compat`    | Allows access to new APIs on older API versions of the Android platform.                                    |
| `datasync`  | Manages the synchronization of local data through _GeoNature_ APIs                                          |
| `viewpager` | A small library that allows you to manage and provide more extensive support for the `ViewPager2` component |

---

## Branching Strategy

| Branch               | Purpose                                      |
|----------------------|----------------------------------------------|
| `master`             | Latest stable release                        |
| `develop`            | Integration branch - base your work here     |
| `feat/<short-name>`  | New feature (e.g. `feat/geopackage-support`) |
| `fix/<short-name>`   | Bug fix (e.g. `fix/crash-on-rotation`)       |
| `docs/<short-name>`  | Documentation update                         |
| `chore/<short-name>` | Build, tooling, dependency updates           |

Always branch off `develop` (or `master` for hotfixes):

```bash
git fetch upstream
git checkout -b feature/my-feature upstream/develop
```

---

## Submitting a Pull Request

1. **Sync your fork** with the upstream before starting:

   ```bash
   git fetch upstream
   git rebase upstream/develop
   ```

2. Implement your changes, following the [coding standards](#coding-standards) below.

3. **Run all tests** and make sure they pass:

   ```bash
   ./gradlew test
   ```

4. **Push** your branch to your fork:

   ```bash
   git push origin feature/my-feature
   ```

5. Open a **Pull Request** against the `develop` branch of the upstream repository.

6. Fill in the PR template:
   - Reference the related issue(s) with `Closes #<issue-number>`.
   - Describe *what* you changed and *why*.
   - Include screenshots or recordings if the change affects the UI.

7. A maintainer will review your PR. Be prepared to address feedback and push additional commits.

> **Note:** Small, focused PRs are much easier to review than large ones. When in doubt, split your
> work into multiple smaller PRs.

---

## Coding Standards

This project is written in **Kotlin** and follows the official
[Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).

- Use **4-space indentation** (no tabs).
- Keep lines under **120 characters**.
- Write **self-documenting code**: use meaningful names for variables, functions, and classes.
- Add **KDoc comments** to all public API elements (classes, functions, properties).
- Avoid introducing new dependencies unless strictly necessary; discuss in an issue first.
- Dependency injection is handled via **Hilt**, follow existing patterns.
- Prefer **coroutines** to callbacks or RxJava for asynchronous operations.

---

## Testing

- All new features and bug fixes **must** include unit tests.
- Tests are located in the `src/test/` directory of each module.
- The project uses **JUnit 4**, [**MockK**](https://mockk.io), and [**Robolectric**](https://robolectric.org).
- Run all tests with:

  ```bash
  ./gradlew test
  ```

- Run tests for a specific module:

  ```bash
  ./gradlew :commons:test
  ./gradlew :datasync:test
  ```

---

## Documentation

- Keep the `README.md` and all module-specific `README.md` files up to date with your changes.
- Document configuration parameters in the settings table if you add or modify any.
- Use [PlantUML](https://plantuml.com) diagrams where appropriate to illustrate flows or architecture.

---

## Commit Message Guidelines

This project follows the [Conventional Commits](https://www.conventionalcommits.org/) specification.

```
<type>(<scope>): <short description>

[optional body]

[optional footer: Closes #<issue-number>]
```

### Types

| Type       | When to use                                          |
|------------|------------------------------------------------------|
| `feat`     | A new feature                                        |
| `fix`      | A bug fix                                            |
| `docs`     | Documentation changes only                           |
| `style`    | Code style changes (formatting, missing semicolons…) |
| `refactor` | Code refactoring without feature change or bug fix   |
| `perf`     | Performance improvements                             |
| `test`     | Adding or updating tests                             |
| `chore`    | Build process, tooling, or dependency updates        |

### Examples

```
feat(commons): add support for additional fields

Closes #122

docs: update local data synchronization workflow in README.md
```

---

## Questions?

If you have any question that is not covered here, feel free to
[open a discussion](../../issues/new) or to reach out via the existing issue tracker.

Thank you for helping make `gn_mobile_core` better!

