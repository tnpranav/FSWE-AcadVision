# AcadVision implementation map

AcadVision is a runnable full-stack development portal with persisted workflows for the core student, faculty, and administrator tasks. It is not yet a complete production campus ERP. Use synthetic records until institutional policy and external services are configured.

## Connected in API mode

- PostgreSQL schema and Flyway seed data; Spring Boot API; React/TypeScript frontend; Docker Compose services and frontend-to-API proxy.
- Authentication with role checks, BCrypt passwords, JWT access tokens, rotating refresh tokens, logout, deactivation checks, and mandatory first-login password change.
- Admin student/faculty account management, activation, forced password change, and audit events; student, faculty, and course record management.
- Student portal profile, enrolled courses/progress, fee ledger, timetable, assignments/submissions, announcements, notifications, and support requests.
- Faculty course roster, attendance, assignments, grading, and class announcements.
- Course registration with server-side eligibility, prerequisite, capacity, schedule-conflict, and enrollment checks.
- Profile editing with ownership rules; admin user listing/create; notification handling and support ticket creation.
- Development-only simulated fee payment endpoint, disabled unless explicitly enabled in configuration. It records a clearly marked synthetic transaction and does not process money.
- Demo mode remains available separately from API mode. API failures do not represent a successful persisted operation.

## Remaining before production or full brief coverage

- Complete the remaining brief modules and workflows: calendar/event management, scholarships, attendance correction review UI, transcript/PDF generation, richer academic/grade policy management, and operational/admin reporting.
- Add institution-specific grading/GPA rules, academic calendar and registration dates, attendance thresholds, and permission policy.
- Add secure document upload/storage/scanning and validated bulk import/export beyond current CSV utilities.
- Configure institutional email for password reset delivery; add rate limiting and MFA if required by the institution.
- Integrate a selected payment provider with verified webhook processing before accepting real payments.
- Add production deployment secrets, backups, monitoring, audit coverage review, accessibility review, and automated frontend/backend/browser test suites.

## Information needed for external integrations

- Institutional SMTP or transactional email provider and sender domain for password recovery.
- Selected payment provider and sandbox/webhook configuration if real provider processing is desired. Do not send live credentials in chat; use local environment secrets.
- Institutional grading scale, GPA/CGPA rules, attendance thresholds, academic calendar, enrollment dates, and credit requirements.
