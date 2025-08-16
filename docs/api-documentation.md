# Pahana Edu Backend API Documentation

This document describes the available API endpoints, their use cases, and requirements for the Pahana Edu backend system.

---

## Authentication Endpoints

### POST `/api/auth/login`

Authenticate a user with username and password.

- **Request Body:** `{ "username": "string", "password": "string" }`
- **Response:** User info and authentication status.

### POST `/api/auth/logout`

Logout the currently authenticated user.

- **Response:** Success or warning message.

### GET `/api/auth/profile`

Get the profile information of the currently authenticated user.

- **Response:** User profile data.
- **Requires Authentication**

---

## API Status Endpoint

### GET `/api`

Check if the API is running.

- **Response:** Status, message, and version info.

---

## Book Endpoints

### GET `/api/books`

Get all books, or filter by search term or category.

- **Query Parameters:** `search`, `category`
- **Response:** List of books.
- **Requires Authentication**

### GET `/api/books/{id}`

Get a book by its ID.

- **Response:** Book details.
- **Requires Authentication**

### GET `/api/books/isbn/{isbn}`

Get a book by its ISBN.

- **Response:** Book details.
- **Requires Authentication**

### POST `/api/books`

Create a new book.

- **Request Body:** Book data (JSON)
- **Response:** Created book info or error.
- **Requires Authentication**

---

## Category Endpoints

### GET `/api/categories`

Get all categories.

- **Response:** List of categories.
- **Requires Authentication**

### GET `/api/categories/{id}`

Get a category by its ID.

- **Response:** Category details.
- **Requires Authentication**

---

## Customer Endpoints

### GET `/api/customers`

Get all customers.

- **Response:** List of customers.
- **Requires Authentication**

### GET `/api/customers/{id}`

Get a customer by their ID.

- **Response:** Customer details.
- **Requires Authentication**

---

## Invoice Endpoints

### GET `/api/invoices`

Get all invoices.

- **Response:** List of invoices.
- **Requires Authentication**

### GET `/api/invoices/{id}`

Get an invoice by its ID.

- **Response:** Invoice details.
- **Requires Authentication**

---

## Publisher Endpoints

### GET `/api/publishers`

Get all publishers.

- **Response:** List of publishers.
- **Requires Authentication**

### GET `/api/publishers/{id}`

Get a publisher by their ID.

- **Response:** Publisher details.
- **Requires Authentication**

---

## Notes

- All endpoints (except `/api` and authentication endpoints) require the user to be authenticated.
- Data is exchanged in JSON format.
- For POST endpoints, provide the required data in the request body as JSON.
