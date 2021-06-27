# Sync

Synchronize local database through GeoNature API:

- Users (i.e. Observers)
- Taxa (with additional data like "color" by areas and taxonomy)
- Dataset
- Nomenclature

Synchronize observers inputs from synchronized apps (e.g. "Occtax").

## Launcher icons

| Name                                                                 | Flavor    | Launcher icon                                                                                                          |
| -------------------------------------------------------------------- | --------- | ---------------------------------------------------------------------------------------------------------------------- |
| Default                                                              | _generic_ | ![PNX](https://raw.githubusercontent.com/PnX-SI/gn_mobile_core/develop/sync/src/main/res/mipmap-xhdpi/ic_launcher.png) |
| [Parc National des Cévennes](http://www.cevennes-parcnational.fr)    | _pnc_     | ![PNC](https://raw.githubusercontent.com/PnX-SI/gn_mobile_core/develop/sync/src/pnc/res/mipmap-xhdpi/ic_launcher.png)  |
| [Parc National des Écrins](http://www.ecrins-parcnational.fr)        | _pne_     | ![PNE](https://raw.githubusercontent.com/PnX-SI/gn_mobile_core/develop/sync/src/pne/res/mipmap-xhdpi/ic_launcher.png)  |
| [Parc National du Mercantour](http://www.mercantour-parcnational.fr) | _pnm_     | ![PNE](https://raw.githubusercontent.com/PnX-SI/gn_mobile_core/develop/sync/src/pnm/res/mipmap-xhdpi/ic_launcher.png)  |
| [Parc National de la Vanoise](http://www.vanoise-parcnational.fr)    | _pnv_     | ![PNE](https://raw.githubusercontent.com/PnX-SI/gn_mobile_core/develop/sync/src/pnv/res/mipmap-xhdpi/ic_launcher.png)  |

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

| Parameter                         | UI      | Description                                          | Default value |
| --------------------------------- | ------- | ---------------------------------------------------- | ------------- |
| `geonature_url`                   | &#9745; | GeoNature URL                                        |               |
| `taxhub_url`                      | &#9745; | TaxHub URL                                           |               |
| `uh_application_id`               | &#9744; | GeoNature application ID in UsersHub                 |               |
| `observers_list_id`               | &#9744; | GeoNature selected observer list ID in UsersHub      |               |
| `taxa_list_id`                    | &#9744; | GeoNature selected taxa list ID                      |               |
| `code_area_type`                  | &#9744; | GeoNature selected area type                         |               |
| `page_size`                       | &#9744; | Default page size while fetching paginated values    | 10000         |
| `sync_periodicity_data_essential` | &#9744; | Configure essential data synchronization periodicity |               |
| `sync_periodicity_data`           | &#9744; | Configure all data synchronization periodicity       |               |

### Data synchronization periodicity

By default, data synchronization is done manually through app interface.
If one of these parameters are set (`sync_periodicity_data_essential` or `sync_periodicity_data`), data synchronization is also made automatically according to these parameters.

The expected format describing a periodic synchronization must following the pattern `DdHhMmSs` where `d`, `h`, `m`, `s` represents the time unit of the duration.
Each part (duration value and its time unit) of the duration is optional. A time unit represents time durations at a given unit of granularity:

- `d`: time unit representing 24 hours (i.e. one day)
- `h`: time unit representing 60 minutes (i.e. one hour)
- `m`: time unit representing 60 seconds (i.e. one minute)
- `s`: time unit representing one second

Examples of valid durations:

- `1d12h`: 36 hours (i.e. 1.5 days)
- `1d`: 24 hours (i.e. one day)
- `4h30m`: 4.5 hours
- `15m`: 15 minutes

A valid synchronization periodicity should not be less than 15 minutes: Such a configuration will be ignored.
If only one of these parameters is set, data synchronization involves all data.
If both of these parameters are set, `sync_periodicity_data` parameter should be greater than `sync_periodicity_data_essential` parameter.

## Content Provider

This app exposes synchronized data from a GeoNature instance through a content provider.
The authority of this content provider is `fr.geonature.sync.provider`.

### Exposed content URIs

**Base URI:** `content://fr.geonature.sync.provider`

| URI                                                | Parameters                              | Description                                                                                               |
| -------------------------------------------------- | --------------------------------------- | --------------------------------------------------------------------------------------------------------- |
| **\<Base URI\>**/app_sync/\*                       | String                                  | Fetch synchronization status by application package ID (e.g. `fr.geonature.occtax`)                       |
| **\<Base URI\>**/dataset/\*                        | String                                  | Fetch all dataset from given application package ID (e.g. `fr.geonature.occtax`)                          |
| **\<Base URI\>**/dataset/\*/active                 | String                                  | Fetch all active dataset from given application package ID (e.g. `fr.geonature.occtax`)                   |
| **\<Base URI\>**/dataset/\*/#                      | String, Number                          | Fetch dataset by ID from given application package ID (e.g. `fr.geonature.occtax`)                        |
| **\<Base URI\>**/observers                         | n/a                                     | Fetch all registered observers                                                                            |
| **\<Base URI\>**/observers/\*                      | String (list of comma separated values) | Fetch all registered observers matching a list of IDs                                                     |
| **\<Base URI\>**/observers/#                       | Number                                  | Fetch an observer by ID                                                                                   |
| **\<Base URI\>**/taxonomy                          | n/a                                     | Fetch taxonomy                                                                                            |
| **\<Base URI\>**/taxonomy/\*                       | String                                  | Fetch taxonomy matching a given kingdom                                                                   |
| **\<Base URI\>**/taxonomy/\*/\*                    | String, String                          | Fetch taxonomy matching a given kingdom and group                                                         |
| **\<Base URI\>**/taxa                              | n/a                                     | Fetch all taxa                                                                                            |
| **\<Base URI\>**/taxa/area/#                       | Number                                  | Fetch all taxa matching a given area ID                                                                   |
| **\<Base URI\>**/taxa/#                            | Number                                  | Fetch a taxon by ID                                                                                       |
| **\<Base URI\>**/taxa/#/area/#                     | Number, Number                          | Fetch a taxon by ID matching a given area ID                                                              |
| **\<Base URI\>**/nomenclature_types                | n/a                                     | Fetch all nomenclature types                                                                              |
| **\<Base URI\>**/nomenclature_types/\*/default     | String                                  | Fetch all default nomenclature definitions from given application package ID (e.g. `fr.geonature.occtax`) |
| **\<Base URI\>**/nomenclature_types/\*/items/\*    | String, String                          | Fetch all nomenclature definitions from given type, matching a given kingdom                              |
| **\<Base URI\>**/nomenclature_types/\*/items/\*/\* | String, String, String                  | Fetch all nomenclature definitions from given type, matching a given kingdom and group                    |
| **\<Base URI\>**/settings/\*                       | String                                  | Fetch app settings JSON file                                                                              |

## Full Build

A full build can be executed with the following command:

```
../gradlew clean assembleDebug
```
