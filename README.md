# FarmTrack — Farm Income & Expense Tracking System

FarmTrack helps farmers record crops, farming expenses and crop sales, and shows their profit or loss automatically.

**Stack:** Java 21 · Spring Boot 4 · Spring Data JPA (Hibernate) · Spring Security + JWT · MySQL · HTML, CSS, JavaScript

```
profit = totalIncome - totalExpenses
```

## Project structure

```
farmtrack/
├── src/main/java/com/farmtrack/farmtrack/
│   ├── config/        SecurityConfig (JWT, CORS, rules), AdminInitializer
│   ├── controller/    REST controllers (Auth, Farmer, Crop, Expense, Income, Dashboard, Report)
│   ├── dto/           Request/response records with Bean Validation
│   ├── entity/        JPA entities: Farmer, Crop, Expense, Income, User (+ enums)
│   ├── exception/     Custom exceptions + GlobalExceptionHandler (uniform JSON errors)
│   ├── repository/    Spring Data JPA repositories
│   ├── security/      JwtService, JwtAuthenticationFilter, CurrentUserService (ownership checks)
│   └── service/       Business logic and profit/loss calculations
├── src/main/resources/static/   Frontend (served by Spring Boot)
│   ├── *.html         One page per screen (login, dashboard, crops, expenses, income, reports, profile)
│   ├── css/style.css  All styling
│   └── js/            common.js (API calls with the JWT, formatting) + one script per page
└── src/test/          Integration tests (H2 in-memory DB, MockMvc)
```

### How the frontend talks to the backend

1. On login, the backend returns a JWT. `common.js` saves it in the browser's `localStorage`.
2. Every page calls the API with `fetch()` through `apiRequest()` in `common.js`, which adds
   `Authorization: Bearer <token>` to the request.
3. The page builds table rows from the JSON response and puts them into the HTML.
4. If there is no token, `requireLogin()` sends the user back to the login page.

## Database

| Table    | Key columns                                                                 |
|----------|------------------------------------------------------------------------------|
| farmer   | id, name, phone (unique), village                                            |
| crop     | id, crop_name, land_details, season, sowing_date, expected_harvest_date, additional_notes, farmer_id |
| expense  | id, category, amount, expense_date, description, crop_id                     |
| income   | id, quantity, unit, price_per_unit, total_amount, sale_date, description, crop_id |
| users    | id, email, password_hash (BCrypt), role (FARMER/ADMIN), farmer_id            |

Farmer 1—N Crop 1—N Expense, Crop 1—N Income. Deleting a crop deletes its expenses and sales.
Tables are created/updated automatically by Hibernate (`ddl-auto=update`).

## Running locally

Needs Java 21+ and MySQL running on `localhost:3306`.

```bash
./mvnw spring-boot:run
```

Open http://localhost:8080. The same app serves the web pages and the API (`/api/...`).
The database `farmtrack_db` is created if it does not exist.

### Settings

| File | Pushed to GitHub? | Contains |
|---|---|---|
| `application.properties` | yes | shared settings; secrets read from environment variables |
| `application-local.properties` | **no** (git-ignored) | your local MySQL password and a dev JWT secret |
| `application-local.properties.example` | yes | template for the file above |

**First time on a new laptop:** copy `application-local.properties.example` to `application-local.properties`
(same folder) and fill in your MySQL password and a generated secret. The `local` profile is active by default,
so this file is loaded automatically.

**On a server (e.g. Railway):** set these environment variables: `JWT_SECRET`, `DB_URL`, `DB_USERNAME`,
`DB_PASSWORD`, and `SPRING_PROFILES_ACTIVE=prod`. Optional: `GUEST_PASSWORD`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`.

To test the API without the web pages, import `FarmTrack.postman_collection.json` into Postman.

### Guest login (try it without registering)

The login page has a **Login as Guest** button. Guest credentials: phone `9999999999`, password `guest123`.
The guest account comes with sample crops, expenses and sales (created by `GuestAccountInitializer` at startup).
Because it is shared, the guest can't change the account's details or password, or delete it; the sample
data is restored on every restart. Settings: `app.guest.*` in `application.properties`.

**Tests**

```bash
./mvnw test
```

## API

All endpoints except register/login/forgot/reset need the header `Authorization: Bearer <token>`.
Farmers can only see and change their own data (403 otherwise).

| Method | Endpoint | Purpose |
|---|---|---|
| POST | /api/auth/register | Register farmer + login account, returns JWT |
| POST | /api/auth/login | Login with phone or email + password |
| POST | /api/auth/forgot-password | Create a one-time reset token (15 min) |
| POST | /api/auth/reset-password | Set a new password with the token |
| POST | /api/auth/change-password | Change password (logged in) |
| GET | /api/auth/me | Current user |
| POST | /api/auth/logout | Client-side logout helper (JWT is stateless) |
| POST | /api/farmers | Create farmer record (ADMIN) |
| GET | /api/farmers | All farmers (ADMIN) |
| GET / PUT / DELETE | /api/farmers/{id} | Get / update / delete farmer |
| POST / GET | /api/farmers/{farmerId}/crops | Add crop / list farmer's crops |
| GET / PUT / DELETE | /api/crops/{cropId} | Get / update / delete crop |
| POST / GET | /api/crops/{cropId}/expenses | Add / list crop expenses |
| GET | /api/expenses?category= | Expense history across crops |
| GET / PUT / DELETE | /api/expenses/{id} | Get / update / delete expense |
| POST / GET | /api/crops/{cropId}/incomes | Record / list crop sales |
| GET | /api/incomes | Sales history across crops |
| GET / PUT / DELETE | /api/incomes/{id} | Get / update / delete sale |
| GET | /api/dashboard/summary | Totals, crop-wise profit, expenses by category, best crop |
| GET | /api/dashboard/crops/{cropId} | One crop's financial summary |
| GET | /api/dashboard/monthly?year= | Monthly income, expenses, profit |
| GET | /api/reports/crops | Crop-wise profit and loss |
| GET | /api/reports/monthly?year= | Monthly report |
| GET | /api/reports/best-crop | Best-performing crop (204 if no sales) |
| GET | /api/reports/download?type=crops\|monthly\|expenses\|incomes | CSV download |

Admins add `?farmerId=` to the dashboard, report, expense-history and sales-history endpoints.

Example — add an expense:

```http
POST /api/crops/1/expenses
Authorization: Bearer <token>
Content-Type: application/json

{ "category": "SEEDS", "amount": 2000, "expenseDate": "2026-06-10", "description": "Paddy seed" }
```

Expense categories: `SEEDS, FERTILIZER, LABOUR, PESTICIDE, IRRIGATION, MACHINERY, TRANSPORT, OTHER`.


