# User Service

The User Service has two main roles in our system:

1. **User-Course Association:** It stores who is in which course, making it easier to manage courses and users.

2. **GraphQL Interface for User Metadata:** The service also has a GraphQL interface that makes it simple to get user information stored in Keycloak. 

## Purpose

1. **User-Course Association:**

   The User Service efficiently manages which users are in which courses. This is vital for keeping track of who is enrolled in educational or training programs.

2. **GraphQL Interface for User Metadata Retrieval:**

   With the GraphQL interface, the User Service simplifies the process of getting user information from Keycloak. This can include personal details. It provides an easy and standardized way for other parts of the system to access this data.
