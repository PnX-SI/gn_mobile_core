# GeoNature - Sync-mobile

GeoNature Android mobile application to synchronize data from a GeoNature instance.

Used by [Occtax-mobile](https://github.com/PnX-SI/gn_mobile_occtax).

## Documentation

- Settings details: https://github.com/PnX-SI/gn_mobile_core/tree/develop/datasync
- Development: https://github.com/PnX-SI/gn_mobile_core/tree/develop/docs

## Full Build

A full build can be executed with the following command:

```bash
./gradlew clean assembleDebug
```

## Import the Library

### 1. Configure Gradle

Add your GitHub username and the generated token to your global `local.properties` file (located at
`~/local.properties`):
```
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_PERSONAL_ACCESS_TOKEN
```

### 2. Configure the GitHub Packages repository

Add the GitHub Packages Maven repository to your project's `settings.gradle` (or `settings.gradle.kts`):

**Groovy DSL (`settings.gradle`)**

```groovy
def localProperties = new Properties().tap {
   if (file("local.properties").exists()) {
      it.load(file("local.properties").newInputStream())
   }
}

dependencyResolutionManagement {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/PnX-SI/gn_mobile_core")
            credentials {
                username = localProperties.getProperty("gpr.user") ?: System.getenv("USERNAME")
                password = localProperties.getProperty("gpr.key") ?: System.getenv("TOKEN")
            }
        }
    }
}
```

**Kotlin DSL (`settings.gradle.kts`)**

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/PnX-SI/gn_mobile_core")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("USERNAME")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("TOKEN")
            }
        }
    }
}
```

### 3. Add the dependency

Add the following dependency to your module's `build.gradle` (or `build.gradle.kts`):

**Groovy DSL (`build.gradle`)**

```groovy
dependencies {
    implementation 'fr.geonature:commons:<version>'
    implementation 'fr.geonature:compat:<version>'
    implementation 'fr.geonature:datasync:<version>'
    implementation 'fr.geonature:viewpager:<version>'
}
```

**Kotlin DSL (`build.gradle.kts`)**

```kotlin
dependencies {
    implementation("fr.geonature:commons:<version>")
    implementation("fr.geonature:compat:<version>")
    implementation("fr.geonature:datasync:<version>")
    implementation("fr.geonature:viewpager:<version>")
}
```

---

## Configure GitHub Packages Access

To publish or consume packages from GitHub Packages, we need to authenticate using a Personal Access
Token (PAT).
1. **Generate a Personal Access Token (PAT):**
    * Go to GitHub Settings > Developer settings > Personal access tokens > Tokens (classic).
    * Generate a new token with the `write:packages` (for publishing) and `read:packages` (for downloading)
      scopes.
2. **Configure Gradle:**
    * Add your GitHub username and the generated token to your global `local.properties` file (located
      at `~/local.properties`):
    ```
    gpr.user=YOUR_GITHUB_USERNAME
    gpr.key=YOUR_PERSONAL_ACCESS_TOKEN
    ```

## Publish Android Libraries

Once authenticated, we can publish the library modules to the GitHub Package Registry.
Run the following command in the terminal:

```bash
./gradlew clean assembleRelease publish
```

This command will build the release version of all libraries (`commons`, `compat`, `datasync`, `viewpager`)
and upload the artifacts (AARs, POMs) to the configured repository.