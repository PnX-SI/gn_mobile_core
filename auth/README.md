# Auth module

Manage _GeoNature_ authentication (package `fr.geonature.auth`):

* Login / logout through `IAuthManager` (implemented by `AuthManagerImpl`, using `IGeoNatureAPIClient`
  from the [`api`](../api/README.md) module).
* Ready-to-use `LoginActivity` (`fr.geonature.auth.login.ui`) driven by `AuthLoginViewModel`.
* Secure storage of the authentication cookie (`CookieManagerImpl`, implementation of `ICookieManager`).
* Authentication settings (`fr.geonature.auth.settings`).

## Settings

The _GeoNature_ application ID used for login is loaded from the same JSON settings file as the
`datasync` module, as an anonymous object:

```json
{
  "gn_application_id": 3
}
```

or from an object with an `auth` property:

```json
{
  "auth": {
    "gn_application_id": 3
  }
}
```

For compatibility with existing settings files, `gn_application_id` is also read from the `sync`
object if not defined elsewhere (`auth` takes precedence). The deprecated `uh_application_id`
property is still accepted.

### Parameters description

| Parameter           | Description                                |
|---------------------|--------------------------------------------|
| `gn_application_id` | _GeoNature_ application ID in _UsersHub_   |

The settings filename is provided by the application through the `@AppSettingsFilename` qualifier
(`fr.geonature.commons.settings.AppSettingsFilename`).

## Authentication cookie security

The authentication cookie is stored encrypted (AES-256-GCM, [Tink](https://github.com/tink-crypto/tink-java))
by [SecureCookieStore](./src/main/java/fr/geonature/auth/SecureCookieStore.kt). The Tink keyset
is wrapped by a non-exportable Android Keystore key. A cookie stored by a previous version in the
default `SharedPreferences` is migrated and removed on first access.

The encrypted files (`gn_secure_auth.xml` and `gn_secure_auth_keyset.xml`) **must be excluded from
backups**. This module provides ready-to-use rules to reference from the application manifest:

```xml
<application
    android:dataExtractionRules="@xml/gn_auth_data_extraction_rules"
    android:fullBackupContent="@xml/gn_auth_backup_rules" />
```

If the application already defines its own rules, merge the `exclude` entries into them.

## Usage

```groovy
implementation "fr.geonature:api:0.1.0"
implementation "fr.geonature:auth:0.1.0"
```

`LoginActivity` is declared in the module manifest. It can be started with
`LoginActivity.newIntent(context)`.

## Full Build

```
./gradlew :auth:assembleDebug
```
