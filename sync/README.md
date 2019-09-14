# Sync

Synchronize local database through GeoNature API:

- Users (e.g. Observers)
- taxa (with "color" by areas and taxonomy)

## Content Provider

This app expose synchronised data from GeoNature through a content provider.
The authority of this content provider is `fr.geonature.sync.provider`.

### Exposed content URIs

Base URI: `content://fr.geonature.sync.provider`

| URI                             | Parameters                              | Description                                                                         |
| ------------------------------- | --------------------------------------- | ----------------------------------------------------------------------------------- |
| **\<Base URI\>**/app_sync/\*    | String                                  | Fetch synchronization status by application package ID (e.g. `fr.geonature.occtax`) |
| **\<Base URI\>**/observers      | n/a                                     | Fetch all registered observers                                                      |
| **\<Base URI\>**/observers/\*   | String (list of comma separated values) | Fetch all registered observers matching a list of IDs                               |
| **\<Base URI\>**/observers/#    | Number                                  | Fetch an observer by ID                                                             |
| **\<Base URI\>**/taxa           | n/a                                     | Fetch all taxa                                                                      |
| **\<Base URI\>**/taxa/area/#    | Number                                  | Fetch all taxa matching a given area ID                                             |
| **\<Base URI\>**/taxa/#         | Number                                  | Fetch a taxon by ID                                                                 |
| **\<Base URI\>**/taxa/#/area/#  | Number, Number                          | Fetch a taxon by ID matching a given area ID                                        |
| **\<Base URI\>**/taxonomy       | n/a                                     | Fetch taxonomy                                                                      |
| **\<Base URI\>**/taxonomy/\*    | String                                  | Fetch taxonomy matching a given kingdom                                             |
| **\<Base URI\>**/taxonomy/\*/\* | String, String                          | Fetch taxonomy matching a given kingdom and group                                   |
