# Commons module

Manage local database with data coming from a _GeoNature_ instance:

- Users (i.e. Observers)
- Taxa (with additional data like "color" by areas and taxonomy)
- Dataset
- Nomenclature

Expose synchronized data from a GeoNature instance through a content provider.

## Content Provider

All synchronized data from a GeoNature instance are exposed through a content provider.
The authority of this content provider is `<application package>.provider`.

### Exposed content URIs

**Base URI:** `content://<application package>.provider`

| URI                                                | Parameters                              | Description                                                                            |
|----------------------------------------------------|-----------------------------------------|----------------------------------------------------------------------------------------|
| **\<Base URI\>**/dataset                           | String                                  | Fetch all dataset                                                                      |
| **\<Base URI\>**/dataset/active                    | String                                  | Fetch all active dataset                                                               |
| **\<Base URI\>**/dataset/#                         | Number                                  | Fetch dataset by ID                                                                    |
| **\<Base URI\>**/observers                         | n/a                                     | Fetch all registered observers                                                         |
| **\<Base URI\>**/observers/\*                      | String (list of comma separated values) | Fetch all registered observers matching a list of IDs                                  |
| **\<Base URI\>**/observers/#                       | Number                                  | Fetch an observer by ID                                                                |
| **\<Base URI\>**/taxonomy                          | n/a                                     | Fetch taxonomy                                                                         |
| **\<Base URI\>**/taxonomy/\*                       | String                                  | Fetch taxonomy matching a given kingdom                                                |
| **\<Base URI\>**/taxonomy/\*/\*                    | String, String                          | Fetch taxonomy matching a given kingdom and group                                      |
| **\<Base URI\>**/taxa                              | n/a                                     | Fetch all taxa                                                                         |
| **\<Base URI\>**/taxa/list/#                       | Number                                  | Fetch all taxa matching a given list ID                                                |
| **\<Base URI\>**/taxa/area/#                       | Number                                  | Fetch all taxa matching a given area ID                                                |
| **\<Base URI\>**/taxa/list/#/area/#                | Number, Number                          | Fetch all taxa matching a given list ID and area ID                                    |
| **\<Base URI\>**/taxa/#                            | Number                                  | Fetch a taxon by ID                                                                    |
| **\<Base URI\>**/taxa/#/area/#                     | Number, Number                          | Fetch a taxon by ID matching a given area ID                                           |
| **\<Base URI\>**/nomenclature_types                | n/a                                     | Fetch all nomenclature types                                                           |
| **\<Base URI\>**/nomenclature_types/\*/default     | String                                  | Fetch all default nomenclature definitions from given module (e.g. `occtax`)           |
| **\<Base URI\>**/nomenclature_types/\*/items/\*    | String, String                          | Fetch all nomenclature definitions from given type, matching a given kingdom           |
| **\<Base URI\>**/nomenclature_types/\*/items/\*/\* | String, String, String                  | Fetch all nomenclature definitions from given type, matching a given kingdom and group |
