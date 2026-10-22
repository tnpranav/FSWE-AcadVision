-- Additional synthetic student accounts for local payment demonstrations.
-- Default demo password: Password@123 (same BCrypt hash as the existing demo users).

INSERT INTO app_users (user_id, full_name, email, password_hash, role, active) VALUES
('AV24009', 'Aarush Menon', 'aarush.menon@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'STUDENT', TRUE),
('AV24010', 'Kavya Iyer', 'kavya.iyer@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'STUDENT', TRUE),
('AV24011', 'Neel Joshi', 'neel.joshi@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'STUDENT', TRUE),
('AV24012', 'Tara Nair', 'tara.nair@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'STUDENT', TRUE),
('AV24013', 'Vivaan Shah', 'vivaan.shah@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'STUDENT', TRUE),
('AV24014', 'Aadhya Reddy', 'aadhya.reddy@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'STUDENT', TRUE),
('AV24015', 'Reyansh Kapoor', 'reyansh.kapoor@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'STUDENT', TRUE),
('AV24016', 'Isha Desai', 'isha.desai@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'STUDENT', TRUE),
('AV24017', 'Dev Malhotra', 'dev.malhotra@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'STUDENT', TRUE),
('AV24018', 'Myra Kulkarni', 'myra.kulkarni@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'STUDENT', TRUE)
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO students (
  student_id, user_id, full_name, email, department, department_code, program_code,
  semester, batch_year, date_of_admission, phone, cgpa, earned_credits, attendance,
  status, hobbies, extracurriculars, advisor_faculty_id
) VALUES
('AV24009', 'AV24009', 'Aarush Menon', 'aarush.menon@campus.edu', 'Computer Science & Engineering', 'CSE', 'BTECH-CSE', 5, 2024, '2024-08-01', '+91 90000 24009', 8.12, 82, 88.00, 'Active', 'Chess, App Development', 'Coding Club Member', 'FAC1001'),
('AV24010', 'AV24010', 'Kavya Iyer', 'kavya.iyer@campus.edu', 'Information Technology', 'IT', 'BTECH-IT', 5, 2024, '2024-08-01', '+91 90000 24010', 8.76, 86, 93.00, 'Active', 'Badminton, Design', 'Student Tech Forum', 'FAC1004'),
('AV24011', 'AV24011', 'Neel Joshi', 'neel.joshi@campus.edu', 'Artificial Intelligence & Data Science', 'AIDS', 'BTECH-AIDS', 5, 2024, '2024-08-01', '+91 90000 24011', 7.94, 78, 84.00, 'Active', 'Robotics, Cricket', 'Robotics Society', 'FAC1003'),
('AV24012', 'AV24012', 'Tara Nair', 'tara.nair@campus.edu', 'Computer Science & Engineering', 'CSE', 'BTECH-CSE', 3, 2025, '2025-08-01', '+91 90000 24012', 9.02, 50, 96.00, 'Active', 'Painting, Music', 'Fine Arts Society', 'FAC1001'),
('AV24013', 'AV24013', 'Vivaan Shah', 'vivaan.shah@campus.edu', 'Information Technology', 'IT', 'BTECH-IT', 7, 2023, '2023-08-01', '+91 90000 24013', 8.31, 122, 87.00, 'Active', 'Cybersecurity, Football', 'Security Club Member', 'FAC1004'),
('AV24014', 'AV24014', 'Aadhya Reddy', 'aadhya.reddy@campus.edu', 'Artificial Intelligence & Data Science', 'AIDS', 'BTECH-AIDS', 5, 2024, '2024-08-01', '+91 90000 24014', 8.63, 84, 91.00, 'Active', 'Machine Learning, Reading', 'AI Club Volunteer', 'FAC1003'),
('AV24015', 'AV24015', 'Reyansh Kapoor', 'reyansh.kapoor@campus.edu', 'Computer Science & Engineering', 'CSE', 'BTECH-CSE', 5, 2024, '2024-08-01', '+91 90000 24015', 7.88, 80, 82.00, 'Active', 'Photography, Basketball', 'Basketball Club', 'FAC1002'),
('AV24016', 'AV24016', 'Isha Desai', 'isha.desai@campus.edu', 'Information Technology', 'IT', 'BTECH-IT', 3, 2025, '2025-08-01', '+91 90000 24016', 8.54, 49, 90.00, 'Active', 'Web Development, Dance', 'Cultural Committee', 'FAC1004'),
('AV24017', 'AV24017', 'Dev Malhotra', 'dev.malhotra@campus.edu', 'Artificial Intelligence & Data Science', 'AIDS', 'BTECH-AIDS', 7, 2023, '2023-08-01', '+91 90000 24017', 8.07, 118, 86.00, 'Active', 'Data Science, Table Tennis', 'Data Science Society', 'FAC1003'),
('AV24018', 'AV24018', 'Myra Kulkarni', 'myra.kulkarni@campus.edu', 'Computer Science & Engineering', 'CSE', 'BTECH-CSE', 5, 2024, '2024-08-01', '+91 90000 24018', 9.21, 88, 97.00, 'Active', 'Open Source, Debate', 'Developer Student Club', 'FAC1001')
ON CONFLICT (student_id) DO NOTHING;

INSERT INTO fee_invoices (
  invoice_id, student_id, semester_id, fee_type, description, amount,
  discount_amount, net_payable, due_date, status
) VALUES
('INV-2610', 'AV24009', 1, 'TUITION', 'Tuition Fee - Semester 5', 30000.00, 0.00, 30000.00, '2026-11-30', 'PENDING'),
('INV-2611', 'AV24010', 1, 'EXAMINATION', 'Semester 5 Examination Fee', 10000.00, 0.00, 10000.00, '2026-11-30', 'PENDING'),
('INV-2612', 'AV24011', 1, 'LIBRARY_LAB', 'Computing Lab and Library Fee', 15000.00, 0.00, 15000.00, '2026-11-30', 'PENDING'),
('INV-2613', 'AV24012', 1, 'TUITION', 'Tuition Fee - Semester 3', 25000.00, 0.00, 25000.00, '2026-11-30', 'PENDING'),
('INV-2614', 'AV24013', 1, 'TUITION', 'Tuition Fee - Semester 7', 35000.00, 0.00, 35000.00, '2026-11-30', 'PENDING'),
('INV-2615', 'AV24014', 1, 'EXAMINATION', 'Semester 5 Examination Fee', 12000.00, 0.00, 12000.00, '2026-11-30', 'PENDING'),
('INV-2616', 'AV24015', 1, 'LIBRARY_LAB', 'Advanced Computing Lab Fee', 18000.00, 0.00, 18000.00, '2026-11-30', 'PENDING'),
('INV-2617', 'AV24016', 1, 'TUITION', 'Tuition Fee - Semester 3', 28000.00, 0.00, 28000.00, '2026-11-30', 'PENDING'),
('INV-2618', 'AV24017', 1, 'TRANSPORT', 'Campus Transport Fee', 8000.00, 0.00, 8000.00, '2026-11-30', 'PENDING'),
('INV-2619', 'AV24018', 1, 'TUITION', 'Tuition Fee - Semester 5', 40000.00, 0.00, 40000.00, '2026-11-30', 'PENDING')
ON CONFLICT (invoice_id) DO NOTHING;

INSERT INTO fee_ledger_entries (invoice_id, student_id, entry_type, amount, balance_after, description) VALUES
('INV-2610', 'AV24009', 'DEBIT', 30000.00, 30000.00, 'Invoice issued: Tuition Fee - Semester 5'),
('INV-2611', 'AV24010', 'DEBIT', 10000.00, 10000.00, 'Invoice issued: Semester 5 Examination Fee'),
('INV-2612', 'AV24011', 'DEBIT', 15000.00, 15000.00, 'Invoice issued: Computing Lab and Library Fee'),
('INV-2613', 'AV24012', 'DEBIT', 25000.00, 25000.00, 'Invoice issued: Tuition Fee - Semester 3'),
('INV-2614', 'AV24013', 'DEBIT', 35000.00, 35000.00, 'Invoice issued: Tuition Fee - Semester 7'),
('INV-2615', 'AV24014', 'DEBIT', 12000.00, 12000.00, 'Invoice issued: Semester 5 Examination Fee'),
('INV-2616', 'AV24015', 'DEBIT', 18000.00, 18000.00, 'Invoice issued: Advanced Computing Lab Fee'),
('INV-2617', 'AV24016', 'DEBIT', 28000.00, 28000.00, 'Invoice issued: Tuition Fee - Semester 3'),
('INV-2618', 'AV24017', 'DEBIT', 8000.00, 8000.00, 'Invoice issued: Campus Transport Fee'),
('INV-2619', 'AV24018', 'DEBIT', 40000.00, 40000.00, 'Invoice issued: Tuition Fee - Semester 5');
