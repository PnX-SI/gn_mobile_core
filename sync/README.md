# Sync

![PNV](https://raw.githubusercontent.com/PnX-SI/gn_mobile_core/develop/sync/src/pnv/res/mipmap-xxxhdpi/ic_launcher.png)
![PNE](https://raw.githubusercontent.com/PnX-SI/gn_mobile_core/develop/sync/src/pne/res/mipmap-xxxhdpi/ic_launcher.png)

Synchronize local database through GeoNature API:

- Users (e.g. Observers)
- Taxa (with "color" by areas and taxonomy)
- Dataset
- Nomenclature

Synchronize observers inputs from synchronized apps (e.g. "Occtax").

## Settings

Example:

```json
{
  "application_id": 3,
  "users_menu_id": 1,
  "taxref_list_id": 100
}
```

### Parameters description

| Parameter        | UI      | Description                      |
| ---------------- | ------- | -------------------------------- |
| `application_id` | &#9744; | GeoNature application ID         |
| `users_menu_id`  | &#9744; | GeoNature selected users menu ID |
| `taxref_list_id` | &#9744; | GeoNature selected taxa list ID  |

## Content Provider

This app expose synchronized data from a GeoNature instance through a content provider.
The authority of this content provider is `fr.geonature.sync.provider`.

### Exposed content URIs

Base URI: `content://fr.geonature.sync.provider`

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

## Full Build

A full build can be executed with the following command:

```
../gradlew clean assembleDebug
```
