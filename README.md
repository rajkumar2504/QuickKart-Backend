# QuickKart Backend

QuickKart Backend is a REST API for a quick-commerce application. It provides account authentication, product and store catalogs, shopping carts, order checkout, payment records, and picker and delivery workflows.

## Features

- Register and authenticate users with email and password; issue JWT access tokens.
- Manage stores, product categories, and products, including product search and stock updates.
- Create and update customer carts and calculate their subtotal.
- Place orders from a cart and query orders by customer or store.
- Create and confirm cash-on-delivery payment records.
- Manage picker and delivery-partner availability and assignments.
- Track order picking and delivery status.

## Technologies

- Java 17
- Spring Boot 4.1.1, Spring Web, Spring Data JPA
- Spring Security and JJWT 0.12.6
- MySQL with MySQL Connector/J
- Jakarta Bean Validation
- Lombok
- Maven Wrapper and Spring Boot Test

## Architecture

The application uses a conventional layered Spring structure:

```text
src/main/java/com/quickkart/
  config/       Application and security configuration
  controller/   HTTP routes and request/response handling
  dto/          API request and response records
  entity/       JPA domain entities and enums
  exception/    Application exception handling
  repository/   Spring Data JPA persistence interfaces
  security/     JWT filter, token service, and user details
  service/      Business logic
```

Controllers delegate to services; services use repositories to access MySQL entities. DTOs are used for authentication requests and responses, while most catalog and workflow endpoints currently accept or return entity objects directly.

## Getting Started

### Requirements

- JDK 17
- MySQL

Create the local database:

```sql
CREATE DATABASE quickkart;
```

Set `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`, and `jwt.secret` in `src/main/resources/application.properties` for your local environment. Do not commit real database credentials or JWT secrets. The checked-in configuration uses a local MySQL database on port `3306`; the API listens on port `8081` by default.

Run the application from the repository root:

```powershell
.\mvnw.cmd spring-boot:run
```

Run tests with:

```powershell
.\mvnw.cmd test
```

Hibernate is currently configured with `spring.jpa.hibernate.ddl-auto=update` and SQL logging enabled. Treat this as a development setup; use deliberate schema migrations and appropriate logging settings for production.

## Authentication

Registration and login are public. Successful authentication returns an `AuthResponse` containing `token`, `tokenType`, `userId`, `name`, `email`, and `role`. Send the returned token on protected requests:

```http
Authorization: Bearer <token>
```

The security configuration is stateless. `/api/auth/**` and `/api/hello` are public; other requests require authentication by default. Role matchers are configured for `/api/admin/**` (`ADMIN`), `/api/picker/**` (`PICKER`), and `/api/delivery/**` (`DELIVERY_PARTNER`). These patterns do not match the plural resource paths such as `/api/pickers` or `/api/delivery-assignments`; those paths currently fall under the authenticated-by-default rule. There are no `/api/admin/**` controller routes in the current source.

## API Reference

Base URL: `http://localhost:8081`

Unless noted otherwise, path and query parameters are required. JSON request bodies use `Content-Type: application/json`. `201 Created` is used for registration, order checkout, and the create/assignment operations that explicitly return that status; other successful controller responses generally use `200 OK`. Protected routes require a bearer token.

### Health and Authentication

| Method | Path | Parameters / body | Description |
| --- | --- | --- | --- |
| GET | `/api/hello` | None | Public health check. |
| POST | `/api/auth/register` | JSON: `name`, `email`, `password` | Register a user; password must be at least 6 characters. |
| POST | `/api/auth/login` | JSON: `email`, `password` | Authenticate and return a JWT. |
| GET | `/api/user/profile` | Bearer token | Return a greeting for the authenticated user. |

Example registration request:

```json
{
  "name": "Alex Customer",
  "email": "alex@example.com",
  "password": "choose-a-password"
}
```

### Stores, Categories, and Products

For create operations, send the corresponding entity as a JSON body.

| Method | Path | Parameters / body | Description |
| --- | --- | --- | --- |
| POST | `/api/stores` | Store JSON | Create a store. |
| GET | `/api/stores` | None | List stores. |
| GET | `/api/stores/{id}` | `id` path parameter | Get a store. |
| DELETE | `/api/stores/{id}` | `id` path parameter | Delete a store. |
| POST | `/api/categories` | Category JSON | Create a category. |
| GET | `/api/categories` | None | List categories. |
| GET | `/api/categories/{id}` | `id` path parameter | Get a category. |
| DELETE | `/api/categories/{id}` | `id` path parameter | Delete a category. |
| POST | `/api/products` | Product JSON | Create a product. |
| GET | `/api/products` | None | List products. |
| GET | `/api/products/{id}` | `id` path parameter | Get a product. |
| GET | `/api/products/store/{storeId}` | `storeId` path parameter | List products for a store. |
| GET | `/api/products/category/{categoryId}` | `categoryId` path parameter | List products for a category. |
| GET | `/api/products/search` | `name` query parameter | Search products by name. |
| PATCH | `/api/products/{id}/stock` | `quantity` query parameter | Update product stock. |
| DELETE | `/api/products/{id}` | `id` path parameter | Delete a product. |

### Cart and Orders

| Method | Path | Parameters / body | Description |
| --- | --- | --- | --- |
| GET | `/api/cart/{userId}` | `userId` path parameter | Get or create a customer's cart. |
| POST | `/api/cart/{userId}/items` | `productId`, `quantity` query parameters | Add a product to the cart. |
| PATCH | `/api/cart/{userId}/items/{productId}` | `quantity` query parameter | Set the cart item's quantity. |
| DELETE | `/api/cart/{userId}/items/{productId}` | `userId`, `productId` path parameters | Remove a product from the cart. |
| GET | `/api/cart/{userId}/subtotal` | `userId` path parameter | Calculate the cart subtotal. |
| DELETE | `/api/cart/{userId}/items` | `userId` path parameter | Clear the cart. |
| POST | `/api/orders/checkout` | `userId`, `deliveryAddress`, `paymentMethod` query parameters | Check out the customer's cart and create an order. |
| GET | `/api/orders/user/{userId}` | `userId` path parameter | List a customer's orders. |
| GET | `/api/orders/{orderId}/user/{userId}` | `orderId`, `userId` path parameters | Get a customer's order. |
| GET | `/api/orders/store/{storeId}` | `storeId` path parameter | List orders for a store. |
| PATCH | `/api/orders/{orderId}/status` | `status` query parameter | Update order status. |

`paymentMethod` and `status` must be valid values of the corresponding enums in `com.quickkart.entity`.

### Payments

| Method | Path | Parameters / body | Description |
| --- | --- | --- | --- |
| POST | `/api/payments` | `orderId`, `paymentMethod` query parameters | Create a payment for an order. |
| PATCH | `/api/payments/{paymentId}/confirm-cod` | `paymentId` path parameter | Confirm collection of a cash-on-delivery payment. |
| GET | `/api/payments/{paymentId}` | `paymentId` path parameter | Get a payment. |
| GET | `/api/payments/order/{orderId}` | `orderId` path parameter | Get the payment for an order. |
| GET | `/api/payments/transaction/{transactionId}` | `transactionId` path parameter | Get a payment by transaction ID. |

### Pickers and Assignments

| Method | Path | Parameters / body | Description |
| --- | --- | --- | --- |
| POST | `/api/pickers` | `userId`, `storeId` query parameters | Create a picker for a store. |
| GET | `/api/pickers` | None | List pickers. |
| GET | `/api/pickers/{pickerId}` | `pickerId` path parameter | Get a picker. |
| GET | `/api/pickers/store/{storeId}` | `storeId` path parameter | List a store's pickers. |
| GET | `/api/pickers/store/{storeId}/available` | `storeId` path parameter | List available pickers for a store. |
| PATCH | `/api/pickers/{pickerId}/availability` | `available` query parameter | Update picker availability. |
| POST | `/api/picker-assignments/assign` | `orderId`, `pickerId` query parameters | Assign a picker to an order. |
| PATCH | `/api/picker-assignments/{assignmentId}/start` | `assignmentId` path parameter | Start picking. |
| PATCH | `/api/picker-assignments/{assignmentId}/complete` | `assignmentId` path parameter; optional `notes` query parameter | Complete packing. |
| GET | `/api/picker-assignments/{assignmentId}` | `assignmentId` path parameter | Get an assignment. |
| GET | `/api/picker-assignments/order/{orderId}` | `orderId` path parameter | Get the assignment for an order. |
| GET | `/api/picker-assignments/picker/{pickerId}` | `pickerId` path parameter | List a picker's assignments. |

### Delivery Partners and Assignments

| Method | Path | Parameters / body | Description |
| --- | --- | --- | --- |
| POST | `/api/delivery-partners` | `userId`, `storeId`, `phoneNumber` query parameters | Create a delivery partner. |
| GET | `/api/delivery-partners` | None | List delivery partners. |
| GET | `/api/delivery-partners/{partnerId}` | `partnerId` path parameter | Get a delivery partner. |
| GET | `/api/delivery-partners/store/{storeId}` | `storeId` path parameter | List partners for a store. |
| GET | `/api/delivery-partners/store/{storeId}/available` | `storeId` path parameter | List available partners for a store. |
| PATCH | `/api/delivery-partners/{partnerId}/availability` | `available` query parameter | Update partner availability. |
| POST | `/api/delivery-assignments/assign` | `orderId`, `partnerId` query parameters | Assign a delivery partner to an order. |
| PATCH | `/api/delivery-assignments/{assignmentId}/pickup` | `assignmentId` path parameter | Mark an order picked up. |
| PATCH | `/api/delivery-assignments/{assignmentId}/start` | `assignmentId` path parameter | Start delivery. |
| PATCH | `/api/delivery-assignments/{assignmentId}/complete` | `assignmentId` path parameter; optional `notes` query parameter | Complete delivery. |
| GET | `/api/delivery-assignments/{assignmentId}` | `assignmentId` path parameter | Get an assignment. |
| GET | `/api/delivery-assignments/order/{orderId}` | `orderId` path parameter | Get the assignment for an order. |
| GET | `/api/delivery-assignments/partner/{partnerId}` | `partnerId` path parameter | List a partner's assignments. |
| GET | `/api/delivery-assignments/partner/{partnerId}/active` | `partnerId` path parameter | List active deliveries. |
| GET | `/api/delivery-assignments/partner/{partnerId}/completed` | `partnerId` path parameter | List completed deliveries. |

## Current Build Status

The current source does not compile, so the API cannot start as-is. Running `mvnw.cmd test` reports Java compilation errors caused by methods placed after the closing braces of `DeliveryAssignmentController`, `PickerAssignmentController`, `DeliveryAssignmentService`, and `PickerAssignmentService`. The routes above describe controller methods currently inside their class declarations; methods outside those declarations are not available routes until the source is corrected.