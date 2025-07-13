# User Service

The User Service has two roles in our system: 

**GraphQL Interface for User Metadata:** The service has a GraphQL interface that makes it simple to get user information stored in Keycloak. 

**OAuth2 Access Token Management:** Handles generation, storage, refresh, and retrieval of OAuth2 access tokens for external service providers (e.g., GitHub).


## Purpose

 **GraphQL Interface for User Metadata Retrieval:**

   With the GraphQL interface, the User Service simplifies the process of getting user information from Keycloak. This can include personal details. It provides an easy and standardized way for other parts of the system to access this data.

## Environment variables 
### Relevant for deployment
| Name                                     | Description                                                | Value in Dev Environment | Value in Prod Environment |
|------------------------------------------|------------------------------------------------------------|--------------------------|---------------------------|
| DAPR_HTTP_PORT                           | Dapr HTTP Port                                             | 5000                     | 3500                      |
| server.port                              | Port on which the application runs                         | 5001                     | 5001                      |
| KEYCLOAK_URL                             | Keycloak URL                                               | http://localhost:9009/   | http://keycloak/keycloak  |
| KEYCLOAK_PASSWORD                        | Keycloak admin password                                    | admin                    | *secret*                  |
| keycloak.clientId                        | Keycloak client ID                                         | admin-cli                | admin-cli                 |
| keycloak.masterRealm                     | Keycloak master realm                                      | master                   | master                    |
| keycloak.realm                           | Keycloak realm for the application                         | GITS                     | GITS                      |
| keycloak.username                        | Keycloak admin username                                    | admin                    | admin                     |
| thirdparty.providers.github.clientId     | GitHub app client ID used to manage user access tokens     | Iv23liynxdcJafLw0ptQ     | Iv23liNIRTUsh31DAv4u      |
| thirdparty.providers.github.clientSecret | GitHub app client secret used to manage user access tokens | *dev secret*             | *secret*                  |

thirdparty.providers.github.tokenRequestUrl
thirdparty.providers.github.externalUserIdUrl
### Other properties
| Name                                          | Description                                                                | Value in Dev Environment                    | Value in Prod Environment               |
|-----------------------------------------------|----------------------------------------------------------------------------|---------------------------------------------|-----------------------------------------|
| spring.graphql.graphiql.enabled               | Enable GraphiQL web interface for GraphQL                                  | true                                        | true                                    |
| spring.graphql.graphiql.path                  | Path for GraphiQL when enabled                                             | /graphiql                                   | /graphiql                               |
| spring.profiles.active                        | Active Spring profile                                                      | dev                                         | prod                                    |
| DAPR_GRPC_PORT                                | Dapr gRPC Port                                                             | -                                           | 50001                                   |
| spring.jpa.properties.hibernate.dialect       | Hibernate dialect for PostgreSQL                                           | org.hibernate.dialect.PostgreSQLDialect     | org.hibernate.dialect.PostgreSQLDialect |
| spring.datasource.driver-class-name           | JDBC driver class                                                          | org.postgresql.Driver                       | org.postgresql.Driver                   |
| spring.sql.init.mode                          | SQL initialization mode                                                    | always                                      | always                                  |
| spring.jpa.show-sql                           | Show SQL queries in logs                                                   | true                                        | false                                   |
| spring.sql.init.continue-on-error             | Continue on SQL init error                                                 | true                                        | true                                    |
| spring.jpa.hibernate.ddl-auto                 | Hibernate DDL auto strategy                                                | create                                      | update                                  |
| logging.level.root                            | Logging level for root logger                                              | DEBUG                                       | -                                       |
| DAPR_GRPC_PORT                                | Dapr gRPC Port                                                             | -                                           | 50001                                   |
| thirdparty.providers.github.tokenRequestUrl   | GitHub endpoint to exchange auth codes and refresh tokens                  | https://github.com/login/oauth/access_token | same                                    |
| thirdparty.providers.github.externalUserIdUrl | GitHub endpoint to fetch a user's GitHub username using their access token | https://api.github.com/user                 | same                                    |
## API description

The GraphQL API is described in the [api.md file](api.md).

The endpoint for the GraphQL API is `/graphql`. The GraphQL Playground is available at `/graphiql`.

## Get started

A guide how to start development can be
found in the [wiki](https://meitrex.readthedocs.io/en/latest/dev-manuals/backend/get-started.html).

