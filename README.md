# AcadVision — Full-Stack Student Portal

AcadVision is a React/TypeScript campus portal backed by a Spring Boot REST API and PostgreSQL. It includes role-based authentication and persisted student, faculty, course, enrollment, attendance, assignment, grading, communication, profile, notification, support, and fee workflows.

> This is a development release, not a production-ready ERP. Use synthetic data only. See [IMPLEMENTATION_MAP.md](IMPLEMENTATION_MAP.md) for connected workflows and remaining production work.

## Run the full local stack

Requirements: Docker Desktop with Compose.

1. Copy `.env.example` to `.env` and replace `JWT_SECRET` with a base64-encoded random value containing at least 32 bytes. For example, use a trusted password manager or secret generator; do not commit `.env`.
2. Run `docker compose up --build` from the project root.
3. Open the portal at `http://localhost:5173`. The API is at `http://localhost:8000/api`.
4. Import `postman/AcadVision.postman_collection.json` and `postman/AcadVision.local.postman_environment.json` into Postman. Select the **AcadVision Local** environment and start with **Auth → Login (Admin)**. The collection saves access and refresh tokens automatically for subsequent requests.

The database is seeded with synthetic users. All demo accounts use the password `Password@123`:

| Role | User ID | Email |
| --- | --- | --- |
| Admin | `admin` | `admin@campus.edu` |
| Faculty | `FAC1001` | `ananya.rao@campus.edu` |
| Student | `AV24001` | `aarav.sharma@campus.edu` |

### Additional pending-fee demo students

These 10 student accounts are included for testing the student fee portal and Razorpay test checkout. They all use the demo password `Password@123`; each listed invoice starts with status `PENDING` and is due on 30 November 2026.

| User ID | Name | Email | Department | Semester | Pending invoice | Amount |
| --- | --- | --- | --- | ---: | --- | ---: |
| `AV24009` | Aarush Menon | `aarush.menon@campus.edu` | Computer Science & Engineering | 5 | `INV-2610` | INR 30,000 |
| `AV24010` | Kavya Iyer | `kavya.iyer@campus.edu` | Information Technology | 5 | `INV-2611` | INR 10,000 |
| `AV24011` | Neel Joshi | `neel.joshi@campus.edu` | Artificial Intelligence & Data Science | 5 | `INV-2612` | INR 15,000 |
| `AV24012` | Tara Nair | `tara.nair@campus.edu` | Computer Science & Engineering | 3 | `INV-2613` | INR 25,000 |
| `AV24013` | Vivaan Shah | `vivaan.shah@campus.edu` | Information Technology | 7 | `INV-2614` | INR 35,000 |
| `AV24014` | Aadhya Reddy | `aadhya.reddy@campus.edu` | Artificial Intelligence & Data Science | 5 | `INV-2615` | INR 12,000 |
| `AV24015` | Reyansh Kapoor | `reyansh.kapoor@campus.edu` | Computer Science & Engineering | 5 | `INV-2616` | INR 18,000 |
| `AV24016` | Isha Desai | `isha.desai@campus.edu` | Information Technology | 3 | `INV-2617` | INR 28,000 |
| `AV24017` | Dev Malhotra | `dev.malhotra@campus.edu` | Artificial Intelligence & Data Science | 7 | `INV-2618` | INR 8,000 |
| `AV24018` | Myra Kulkarni | `myra.kulkarni@campus.edu` | Computer Science & Engineering | 5 | `INV-2619` | INR 40,000 |

These shared demo credentials are for local development only. Change them before exposing the stack to a network.

## Run services separately

For a local Windows run without Docker, ensure the `acadvision` PostgreSQL database is available, then open two PowerShell terminals. In the first, run `cd backend` and `powershell -ExecutionPolicy Bypass -File .\run-local.ps1`. The script uses port 8000, generates a temporary JWT secret if needed, and applies the Flyway migrations on startup. In the second terminal, run `cd frontend`, ensure `.env` contains `VITE_USE_API=true` and `VITE_API_URL=http://localhost:8000/api`, then run `npm.cmd install` and `npm.cmd run dev`. Keep both terminals open.

For the frontend, use Node.js 20+:

```bash
cd frontend
npm install
npm run dev
```

For the API, use Java 21+, Maven 3.9+, and PostgreSQL 16+. Start PostgreSQL with `docker compose up -d postgres`, then run `mvn spring-boot:run` from `backend/`. Configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, and `FRONTEND_ORIGIN` as needed. `frontend/.env.example` documents the Vite API settings.

## Current implementation notes

- Server-side endpoint authorization is enforced by Spring Security. Admin routes require `ROLE_ADMIN`; faculty routes require faculty or admin; student routes enforce ownership for student-specific records.
- Login issues short-lived access tokens and refresh tokens. Refresh tokens rotate on use; password changes invalidate active refresh sessions.
- User administration supports listing, creating student/faculty accounts, activation/deactivation, and requiring a password change. New users must change their initial password after sign-in.
- Public password recovery intentionally returns a generic response and never exposes reset tokens. Email delivery is not configured yet, so self-service recovery requires an institution SMTP or transactional email service before it can be enabled.
- Student fee checkout uses Razorpay Standard Checkout in test mode. The backend creates each order, validates the checkout signature, confirms/captures the payment with Razorpay, and only then marks the invoice paid. Set `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET` to test-mode keys before starting the API; never commit the secret or use live keys in this test configuration.

### Razorpay test-mode setup

1. Generate API keys in the Razorpay Dashboard while Test Mode is selected.
2. Copy `backend/.env.local.example` to `backend/.env.local` and set `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET`. Keep this local file private; it is ignored by Git.
3. Restart the backend with `powershell -ExecutionPolicy Bypass -File .\run-local.ps1` from `backend/`.
4. Sign in as a student and open **Fees & payments**. Use Razorpay's test payment methods; no real money is collected.

### Payment receipt email testing

Successful, verified Razorpay payments are queued for an email receipt. Configure the admin Gmail mailbox as the sender in `backend/.env.local` with `MAIL_USERNAME` and its Google App Password in `MAIL_PASSWORD` (remove spaces from the displayed app password). The student enters the recipient address on the Fees & payments page. The backend sends the receipt from the configured admin mailbox to that address only after verifying the payment. It uses Gmail SMTP (`smtp.gmail.com`, port `587`, STARTTLS). Email delivery is retried if SMTP is unavailable and does not undo a verified payment. Keep the app password out of Git and chat. Without mail credentials, payments still succeed and receipts remain queued until email is configured.
- Demo mode remains available separately. API mode loads connected portal data from the server; some summary/dashboard visuals continue to use sample values.
- Administrator dashboard previews are read-only demos. There is no impersonation or authorization bypass.
- API requests and examples are maintained in the Postman collection under `postman/`; Swagger/OpenAPI UI and endpoints are not included.

## Useful paths

```text
frontend/                         React, TypeScript, Vite, Tailwind UI
backend/                          Spring Boot API and Flyway migrations
backend/src/main/resources/db/    PostgreSQL schema and seed data
docker-compose.yml                PostgreSQL, API, and frontend services
```
