# User Service

The User Service has one role in our system: 

**GraphQL Interface for User Metadata:** The service has a GraphQL interface that makes it simple to get user information stored in Keycloak. 

## Purpose

 **GraphQL Interface for User Metadata Retrieval:**

   With the GraphQL interface, the User Service simplifies the process of getting user information from Keycloak. This can include personal details. It provides an easy and standardized way for other parts of the system to access this data.

## Environment variables 
### Relevant for deployment
| Name                       | Description                        | Value in Dev Environment                      | Value in Prod Environment                                      |
|----------------------------|------------------------------------|-----------------------------------------------|----------------------------------------------------------------|
| DAPR_HTTP_PORT             | Dapr HTTP Port                     | 5000                                          | 3500                                                           |
| server.port                | Port on which the application runs | 5001                                          | 5001                                                           |
| KEYCLOAK_URL               | Keycloak URL                       | http://localhost:9009/                        | http://keycloak/keycloak                                       |
| KEYCLOAK_PASSWORD          | Keycloak admin password            | admin                                         | *secret*                                                       |
| keycloak.clientId          | Keycloak client ID                 | admin-cli                                     | admin-cli                                                      |
| keycloak.masterRealm       | Keycloak master realm              | master                                        | master                                                         |
| keycloak.realm             | Keycloak realm for the application | GITS                                          | GITS                                                           |
| keycloak.username          | Keycloak admin username            | admin                                         | admin                                                          |

### Other properties
| Name                                    | Description                               | Value in Dev Environment                | Value in Prod Environment               |
|-----------------------------------------|-------------------------------------------|-----------------------------------------|-----------------------------------------|
| spring.graphql.graphiql.enabled         | Enable GraphiQL web interface for GraphQL | true                                    | true                                    |
| spring.graphql.graphiql.path            | Path for GraphiQL when enabled            | /graphiql                               | /graphiql                               |
| spring.profiles.active                  | Active Spring profile                     | dev                                     | prod                                    |
| DAPR_GRPC_PORT                          | Dapr gRPC Port                            | -                                       | 50001                                   |

## API description

The GraphQL API is described in the [api.md file](api.md).

The endpoint for the GraphQL API is `/graphql`. The GraphQL Playground is available at `/graphiql`.

## Get started

A guide how to start development can be
found [wiki](https://gits-enpro.readthedocs.io/en/latest/dev-manuals/backend/get-started.html).

