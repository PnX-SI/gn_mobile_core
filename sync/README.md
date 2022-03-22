# Sync

Synchronize local database through GeoNature API and observers inputs from synchronized apps (e.g. "Occtax"), through [datasync module](../datasync/README.md).

## Launcher icons

| Name    | Flavor    | Launcher icon                                                                                                                                                                                                                                            |
| ------- | --------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Default | _generic_ | ![PNX](https://raw.githubusercontent.com/PnX-SI/gn_mobile_core/develop/sync/src/main/res/mipmap-xxxhdpi/ic_launcher.png) ![PNX_debug](https://raw.githubusercontent.com/PnX-SI/gn_mobile_core/develop/sync/src/debug/res/mipmap-xxxhdpi/ic_launcher.png) |

## Settings

Example:

```json
{
  "geonature_url": "https://demo.geonature/geonature",
  "taxhub_url": "https://demo.geonature/taxhub",
  "uh_application_id": 3,
  "observers_list_id": 1,
  "taxa_list_id": 100,
  "code_area_type": "M1",
  "page_size": 10000
}
```

### Parameters description

See: [parameters description from datasync module](../datasync/README.md#Settings).

### Data synchronization periodicity

See: [data synchronization periodicity from datasync module](../datasync/README.md#Data-synchronization-periodicity).

## Content Provider

See: [Content Provider from commons module](../commons/README.md#Content-Provider).

## Full Build

A full build can be executed with the following command:

```
../gradlew clean assembleDebug
```
