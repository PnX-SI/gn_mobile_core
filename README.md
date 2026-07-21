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
            url = uri("https://maven.pkg.github.com/PnX-SI/gn_mobile_maps")
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
            url = uri("https://maven.pkg.github.com/PnX-SI/gn_mobile_maps")
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
    implementation 'fr.geonature:mountpoint:<version>'
}
```

**Kotlin DSL (`build.gradle.kts`)**

```kotlin
dependencies {
    implementation("fr.geonature:mountpoint:<version>")
}
```