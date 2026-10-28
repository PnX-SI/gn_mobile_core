# API module

Describes the _GeoNature_ and _TaxHub_ APIs consumed by the GeoNature mobile applications:
* Retrofit services: [IGeoNatureService](./src/main/java/fr/geonature/api/IGeoNatureService.kt)
  and [ITaxHubService](./src/main/java/fr/geonature/api/ITaxHubService.kt).
* Models and errors (`fr.geonature.api.model`, `fr.geonature.api.error`).
* HTTP client helper ([client.kt](./src/main/java/fr/geonature/api/client.kt)) and cookie management
  (`ICookieManager`).
* Hilt module providing `ICookieManager` and `IGeoNatureAPIClient`.

## _GeoNature_ APIs

See [IGeoNatureService](./src/main/java/fr/geonature/api/IGeoNatureService.kt)
and [ITaxHubService](./src/main/java/fr/geonature/api/ITaxHubService.kt) interfaces
definition about _GeoNature_ and _TaxHub_ APIs endpoints consumed:

| Route                                                      | Method | Description                                          |
|------------------------------------------------------------|--------|------------------------------------------------------|
| `/api/auth/login`                                          | `POST` | Performs authentication.                             |
| `/api/gn_commons/t_mobile_apps`                            | `GET`  | Fetch available applications with its configuration. |
| `/api/meta/datasets`                                       | `POST` | Fetch datasets.                                      |
| `/api/users/menu/{{observers_list_id}}`                    | `GET`  | Fetch observers.                                     |
| `/api/taxref/regnewithgroupe2`                             | `GET`  | Fetch taxonomic ranks.                               |
| `/api/nomenclatures/nomenclatures/taxonomy`                | `GET`  | Fetch the nomenclature.                              |
| `/api/{{module}}/defaultNomenclatures`                     | `GET`  | Fetch default nomenclature values.                   |
| `/api/taxref/version`                                      | `GET`  | Fetch the current version of the taxa database.      |
| `/api/taxref`                                              | `GET`  | Fetch all taxa.                                      |
| `/api/synthese/color_taxon`                                | `GET`  | Fetch additional data for taxa.                      |
| `/api/gn_commons/additional_fields?module_name={{module}}` | `GET`  | Fetch additional fields.                             |

A [Postman collection](../docs/postman/gn_mobile_core.postman_collection.json) is available showing
all the routes consumed by this module.

## Usage

```groovy
implementation "fr.geonature:api:0.1.0"
implementation "fr.geonature:auth:0.1.0"
```
