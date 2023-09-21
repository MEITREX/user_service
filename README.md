# User Service

The User Service has two main roles in our system:

1. **User-Course Association:** It stores who is in which course, making it easier to manage courses and users.

2. **GraphQL Interface for User Metadata:** The service also has a GraphQL interface that makes it simple to get user information stored in Keycloak. 

## Purpose

1. **User-Course Association:**

   The User Service efficiently manages which users are in which courses. This is vital for keeping track of who is enrolled in educational or training programs.

2. **GraphQL Interface for User Metadata Retrieval:**

   With the GraphQL interface, the User Service simplifies the process of getting user information from Keycloak. This can include personal details. It provides an easy and standardized way for other parts of the system to access this data.

## Environment variables 
### Relevant for deployment
| Name                       | Description                        | Value in Dev Environment                      | Value in Prod Environment                                      |
|----------------------------|------------------------------------|-----------------------------------------------|----------------------------------------------------------------|
| spring.datasource.url      | PostgreSQL database URL            | jdbc:postgresql://localhost:5032/user_service | jdbc:postgresql://user-service-db-postgresql:5432/user-service |
| spring.datasource.username | Database username                  | root                                          | gits                                                           |
| spring.datasource.password | Database password                  | root                                          | *secret*                                                       |
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
| spring.jpa.properties.hibernate.dialect | Hibernate dialect for PostgreSQL          | org.hibernate.dialect.PostgreSQLDialect | org.hibernate.dialect.PostgreSQLDialect |
| spring.sql.init.mode                    | SQL initialization mode                   | always                                  | always                                  |
| spring.jpa.show-sql                     | Show SQL queries in logs                  | true                                    | true                                    |
| spring.sql.init.continue-on-error       | Continue on SQL init error                | true                                    | true                                    |
| spring.jpa.hibernate.ddl-auto           | Hibernate DDL auto strategy               | create                                  | update                                  |
| DAPR_GRPC_PORT                          | Dapr gRPC Port                            | -                                       | 50001                                   |

## API description

The GraphQL API is described in the [api.md file](api.md).

The endpoint for the GraphQL API is `/graphql`. The GraphQL Playground is available at `/graphiql`.

## How to run

How to run services locally is described in
the [wiki](https://gits-enpro.readthedocs.io/en/latest/dev-manuals/backend/get-started.html).