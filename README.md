# Finanças API

REST API behind **Minhas Finanças**, a personal finance Android app ([financas-app-android](https://github.com/DevTonyz/financas-app-android)). It stores expenses, categories and shared groups, and settles who owes whom inside a group.

## Features

- **Auth:** sign-up and login with JWT (2 h tokens). Missing, invalid or expired token → `401`.
- **Categories per user**, with 7 defaults created on sign-up. Deleting a category in use → `409`.
- **Expenses** with payment method (credit, debit, Pix, cash). Credit purchases in installments create one expense per month, linked by a purchase id; edits and deletes can target *this one*, *this and the next ones* or *the whole purchase*. The last installment takes the rounding remainder.
- **Monthly summary** and expense listing filtered by category or period (paginated).
- **Groups (shared accounts):** add members by e-mail, split group expenses and keep a running balance across months. Members register payments between them, and the API suggests the fewest payments needed to settle up.
- **Join date:** each expense is only split among the members who were already in the group on that date, so a new member never inherits old debts.
- Errors with clear messages: `400`, `403`, `404` and `409`, never a bare `500`.

## Stack

Java 21 · Spring Boot 3.3 · Spring Security + JWT (auth0 java-jwt) · Spring Data JPA / Hibernate · PostgreSQL · springdoc-openapi (Swagger) · Lombok · Maven

## Running locally

1. Install **PostgreSQL** and create a user and database:
   ```sql
   CREATE USER financas WITH PASSWORD 'your_password';
   CREATE DATABASE financas_db OWNER financas;
   ```
2. Copy `src/main/resources/application.properties.example` to `application.properties` and fill in the password and a long random JWT secret. This file is git-ignored.
3. Start the API (tables are created automatically):
   ```bash
   ./mvnw spring-boot:run
   ```
4. Open the docs at **http://localhost:8080/swagger-ui.html**. Use `POST /login`, then click *Authorize* and paste the token.

Tests: `./mvnw test`.

## Endpoints

All routes except `/login` and `POST /usuarios` need `Authorization: Bearer <token>`. Dates are sent as `yyyy-MM-dd` and returned as `dd/MM/yyyy`.

| Area | Method and path | Notes |
|---|---|---|
| Auth | `POST /login` | `{login, senha}` → `{token}` |
| | `POST /usuarios` | Sign-up. `409` if the e-mail exists |
| Categories | `GET/POST /categorias`, `PUT/DELETE /categorias/{id}` | Only the logged-in user's |
| Expenses | `POST /transacoes` | Returns a list (one item per installment) |
| | `GET /transacoes`, `/transacoes/{id}` | Paginated (`page`, `size`, `sort`) |
| | `GET /transacoes/categoria/{id}`, `/transacoes/periodo?inicio=&fim=` | Filters |
| | `GET /transacoes/resumo?mes=&ano=` | Month total |
| | `PUT /transacoes/{id}?escopo=ESTA\|PROXIMAS` | Installment scope |
| | `DELETE /transacoes/{id}?escopo=ESTA\|PROXIMAS\|TODAS` | |
| Groups | `GET/POST /grupos`, `GET /grupos/{id}` | Members include their join date |
| | `POST /grupos/{id}/membros` | `{login, dataEntrada?}` |
| | `DELETE /grupos/{id}/membros/me` | Leave (only with a zero balance) |
| | `GET /grupos/{id}/saldos` | Running balance + settle-up suggestions |
| | `GET/POST /grupos/{id}/pagamentos`, `DELETE /grupos/{id}/pagamentos/{id}` | Payments between members |
| | `GET /transacoes/grupo/{id}`, `/grupo/{id}/resumo`, `/grupo/{id}/acerto` | Group expenses and month totals |

## Data model

```
Usuario ─┬─< Categoria
         ├─< Transacao >── Categoria
         │       └──────>── Grupo (optional)
         └─<  grupo_usuarios  >─ Grupo ─< Pagamento (payer → receiver)
                                   └──< EntradaNoGrupo (join date per member)
```

- `Transacao`: description, amount, date, payment method, installments (`compraId`, `numeroParcela`), owner and optional group.
- `Pagamento`: a settle-up between two members. It changes balances but is not an expense.
- Balance of a member = what they paid − their share of the expenses they split + payments made − payments received. Shares are split to the cent, so balances always add up to zero.

## Project layout

```
src/main/java/com/antony/financas_api/
├─ config/       security (JWT filter), CORS, Swagger
├─ controller/   REST endpoints
├─ dto/          request/response records
├─ exception/    error handler and custom exceptions
├─ model/        JPA entities
├─ repository/   Spring Data repositories
└─ service/      business rules (groups, balances, installments)
```
