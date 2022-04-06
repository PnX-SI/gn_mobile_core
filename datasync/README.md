# DataSync module

Authenticate GeoNature user.

Synchronize local database through GeoNature APIs:

- Users (i.e. Observers)
- Taxa (with additional data like "color" by areas and taxonomy)
- Dataset
- Nomenclature

Synchronize observers inputs from synchronized apps (e.g. "Occtax").

Manage available applications registered from GeoNature and installed ones.

## GeoNature APIs

See [IGeoNatureService](./src/main/java/fr/geonature/datasync/api/IGeoNatureService.kt) and [ITaxHubService](./src/main/java/fr/geonature/datasync/api/ITaxHubService.kt) interfaces definition about GeoNature and TaxHub APIs endpoints consumed:

| Route                                       | Method | Description                                                      |
| ------------------------------------------- | ------ | ---------------------------------------------------------------- |
| `/api/auth/login`                           | `POST` | Performs authentication.                                         |
| `/api/{module}/releve`                      | `POST` | Send observer's input to given GeoNature module (e.g. `occtax`). |
| `/api/meta/datasets`                        | `GET`  | Fetch datasets.                                                  |
| `/api/users/menu/{id}`                      | `GET`  | Fetch observers.                                                 |
| `/api/synthese/color_taxon`                 | `GET`  | Fetch additional data for taxa.                                  |
| `/api/nomenclatures/nomenclatures/taxonomy` | `GET`  | Fetch the nomenclature.                                          |
| `/api/{module}/defaultNomenclatures`        | `GET`  | Fetch default nomenclature values.                               |
| `/api/gn_commons/t_mobile_apps`             | `GET`  | Fetch available applications.                                    |
| `/api/taxref/regnewithgroupe2`              | `GET`  | Fetch taxonomic ranks.                                           |
| `/api/taxref/allnamebylist/{id}`            | `GET`  | Fetch taxa.                                                      |

## Settings

Module settings can be loaded from JSON as follow:

```json
{
  "sync": {
    "geonature_url": "https://demo.geonature/geonature",
    "taxhub_url": "https://demo.geonature/taxhub",
    "gn_application_id": 3,
    "observers_list_id": 1,
    "taxa_list_id": 100,
    "code_area_type": "M1",
    "page_size": 10000
  }
}
```

or

```json
{
  "geonature_url": "https://demo.geonature/geonature",
  "taxhub_url": "https://demo.geonature/taxhub",
  "gn_application_id": 3,
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
| `gn_application_id`               | &#9744; | GeoNature application ID in UsersHub                 |               |
| `observers_list_id`               | &#9744; | GeoNature selected observer list ID in UsersHub      |               |
| `taxa_list_id`                    | &#9744; | GeoNature selected taxa list ID                      |               |
| `code_area_type`                  | &#9744; | GeoNature selected area type                         |               |
| `page_size`                       | &#9744; | Default page size while fetching paginated values    | 10000         |
| `sync_periodicity_data_essential` | &#9744; | Configure essential data synchronization periodicity | null          |
| `sync_periodicity_data`           | &#9744; | Configure all data synchronization periodicity       | null          |

### Data synchronization periodicity

By default, data synchronization is done manually through app interface.
If one of these parameters are set (`sync_periodicity_data_essential` or `sync_periodicity_data`),
data synchronization is also made automatically according to these parameters.

The expected format describing a periodic synchronization must following the pattern `DdHhMmSs`
where `d`, `h`, `m`, `s` represents the time unit of the duration.
Each part (duration value and its time unit) of the duration is optional. A time unit represents
time durations at a given unit of granularity:

- `d`: time unit representing 24 hours (i.e. one day)
- `h`: time unit representing 60 minutes (i.e. one hour)
- `m`: time unit representing 60 seconds (i.e. one minute)
- `s`: time unit representing one second

Examples of valid durations:

- `1d12h`: 36 hours (i.e. 1.5 days)
- `1d`: 24 hours (i.e. one day)
- `4h30m`: 4.5 hours
- `15m`: 15 minutes

A valid synchronization periodicity should not be less than 15 minutes: Such a configuration will be
ignored. If only one of these parameters is set, data synchronization involves all data. If both of
these parameters are set, `sync_periodicity_data` parameter should be greater than
`sync_periodicity_data_essential` parameter.

## Full Build

A full build can be executed with the following command:

```
../gradlew clean assembleDebug
```
