-- ============================================================================
-- AcadVision Realistic Seed Data
-- Default password for all demo accounts: Password@123
-- Verified BCrypt hash: $2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.
-- ============================================================================

-- 1. Identity & Access Management (app_users)
INSERT INTO app_users (user_id, full_name, email, password_hash, role, active) VALUES
('admin', 'Alex Morgan', 'admin@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'ADMIN', TRUE),
('FAC1001', 'Dr. Ananya Rao', 'ananya.rao@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'FACULTY', TRUE),
('FAC1002', 'Prof. Vikram Das', 'vikram.das@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'FACULTY', TRUE),
('FAC1003', 'Dr. Sana Iqbal', 'sana.iqbal@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'FACULTY', TRUE),
('FAC1004', 'Dr. Rajesh Kumar', 'rajesh.kumar@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'FACULTY', TRUE),
('AV24001', 'Aarav Sharma', 'aarav.sharma@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'STUDENT', TRUE),
('AV24002', 'Diya Patel', 'diya.patel@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'STUDENT', TRUE),
('AV24003', 'Ishaan Reddy', 'ishaan.reddy@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'STUDENT', TRUE),
('AV24004', 'Meera Nair', 'meera.nair@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'STUDENT', TRUE),
('AV24005', 'Kabir Verma', 'kabir.verma@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'STUDENT', TRUE),
('AV24006', 'Sneha Kulkarni', 'sneha.kulkarni@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'STUDENT', TRUE),
('AV24007', 'Rohan Gupta', 'rohan.gupta@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'STUDENT', TRUE),
('AV24008', 'Priya Singh', 'priya.singh@campus.edu', '$2a$10$gaXOmTnw4MW/rtP9cU3wBeONNcI5FN0ORhyHj4cGiDXKy9WHMYf1.', 'STUDENT', TRUE)
ON CONFLICT (user_id) DO NOTHING;

-- 2. Departments
INSERT INTO departments (code, name, hod_name, contact_email, building, active) VALUES
('CSE', 'Computer Science & Engineering', 'Dr. K. S. Raman', 'hod.cse@campus.edu', 'Alan Turing Block', TRUE),
('AIDS', 'Artificial Intelligence & Data Science', 'Dr. Sana Iqbal', 'hod.aids@campus.edu', 'Von Neumann Block', TRUE),
('IT', 'Information Technology', 'Dr. Sunita Sen', 'hod.it@campus.edu', 'Ada Lovelace Block', TRUE),
('ECE', 'Electronics & Communication', 'Dr. Ramesh Pillai', 'hod.ece@campus.edu', 'Claude Shannon Block', TRUE)
ON CONFLICT (code) DO NOTHING;

-- 3. Programs
INSERT INTO programs (code, name, department_code, degree_level, total_semesters, total_credits_required) VALUES
('BTECH-CSE', 'B.Tech in Computer Science and Engineering', 'CSE', 'UNDERGRADUATE', 8, 160),
('BTECH-AIDS', 'B.Tech in Artificial Intelligence & Data Science', 'AIDS', 'UNDERGRADUATE', 8, 160),
('BTECH-IT', 'B.Tech in Information Technology', 'IT', 'UNDERGRADUATE', 8, 160),
('MTECH-CS', 'M.Tech in Advanced Computer Science', 'CSE', 'POSTGRADUATE', 4, 80)
ON CONFLICT (code) DO NOTHING;

-- 4. Academic Years & Semesters
INSERT INTO academic_years (id, year_name, is_current, start_date, end_date) VALUES
(1, '2026-2027', TRUE, '2026-07-01', '2027-06-30')
ON CONFLICT (id) DO NOTHING;

INSERT INTO semesters (id, academic_year_id, semester_number, term, start_date, end_date, is_current, registration_open) VALUES
(1, 1, 5, 'FALL', '2026-07-15', '2026-12-15', TRUE, TRUE),
(2, 1, 6, 'SPRING', '2027-01-05', '2027-05-30', FALSE, FALSE)
ON CONFLICT (id) DO NOTHING;

-- 5. Classrooms
INSERT INTO classrooms (id, room_number, building, capacity, room_type) VALUES
(1, 'Room B-204', 'Alan Turing Block', 70, 'LECTURE_HALL'),
(2, 'Room C-110', 'Von Neumann Block', 65, 'LECTURE_HALL'),
(3, 'Lab 3 - Block A', 'Turing Tech Labs', 45, 'LABORATORY'),
(4, 'Lab 1 - AI Innovation', 'Innovation Complex', 50, 'LABORATORY'),
(5, 'Auditorium 1', 'Main Academic Complex', 350, 'AUDITORIUM'),
(6, 'Seminar Room 2', 'Faculty Center', 40, 'SEMINAR_ROOM')
ON CONFLICT (id) DO NOTHING;

-- 6. Grading Policies
INSERT INTO grading_policies (grade_letter, min_percentage, max_percentage, grade_point, description) VALUES
('A+', 90.00, 100.00, 10.00, 'Outstanding - Exceptional Mastery'),
('A',  80.00, 89.99,  9.00,  'Excellent - Superior Achievement'),
('B+', 70.00, 79.99,  8.00,  'Very Good - High Level of Competence'),
('B',  60.00, 69.99,  7.00,  'Good - Competent Understanding'),
('C+', 55.00, 59.99,  6.00,  'Above Average - Satisfactory Knowledge'),
('C',  50.00, 54.99,  5.00,  'Average - Basic Competence Met'),
('D',  40.00, 49.99,  4.00,  'Marginal - Minimum Passing Grade'),
('F',  0.00,  39.99,  0.00,  'Fail - Course Must Be Repeated')
ON CONFLICT (grade_letter) DO NOTHING;

-- 7. Faculty Profiles
INSERT INTO faculty (faculty_id, user_id, full_name, email, department, department_code, designation, qualification, specialization, office_room, phone, course_count, status) VALUES
('FAC1001', 'FAC1001', 'Dr. Ananya Rao', 'ananya.rao@campus.edu', 'Computer Science & Engineering', 'CSE', 'Professor & Dean Academics', 'Ph.D. in Computer Science (IISc)', 'Algorithms, Distributed Systems, Graph Theory', 'Turing 304', '+91 98450 11223', 3, 'Active'),
('FAC1002', 'FAC1002', 'Prof. Vikram Das', 'vikram.das@campus.edu', 'Computer Science & Engineering', 'CSE', 'Associate Professor', 'M.Tech, Ph.D. (IIT Delhi)', 'Relational Databases, Cloud Architecture, Big Data', 'Turing 212', '+91 98450 22334', 2, 'Active'),
('FAC1003', 'FAC1003', 'Dr. Sana Iqbal', 'sana.iqbal@campus.edu', 'Artificial Intelligence & Data Science', 'AIDS', 'Associate Professor & HOD', 'Ph.D. in AI (CMU / IIT Bombay)', 'Machine Learning, Deep Neural Nets, NLP', 'Von Neumann 102', '+91 98450 33445', 4, 'Active'),
('FAC1004', 'FAC1004', 'Dr. Rajesh Kumar', 'rajesh.kumar@campus.edu', 'Information Technology', 'IT', 'Assistant Professor', 'Ph.D. in Software Engg', 'Web Frameworks, API Security, DevOps', 'Lovelace 405', '+91 98450 44556', 2, 'Active')
ON CONFLICT (faculty_id) DO NOTHING;

-- 8. Student Profiles
INSERT INTO students (student_id, user_id, full_name, email, department, department_code, program_code, semester, batch_year, date_of_admission, phone, cgpa, earned_credits, attendance, status, hobbies, extracurriculars, advisor_faculty_id) VALUES
('AV24001', 'AV24001', 'Aarav Sharma', 'aarav.sharma@campus.edu', 'Computer Science & Engineering', 'CSE', 'BTECH-CSE', 5, 2024, '2024-08-01', '+91 91234 56701', 8.72, 84, 92.00, 'Active', 'Photography, Competitive Chess, Algorithmic Trading', 'Campus Tech Club Lead, Hackathon Finalist', 'FAC1001'),
('AV24002', 'AV24002', 'Diya Patel', 'diya.patel@campus.edu', 'Information Technology', 'IT', 'BTECH-IT', 5, 2024, '2024-08-01', '+91 91234 56702', 9.14, 88, 96.00, 'Active', 'Reading, Badminton, Open Source Contributions', 'Developer Student Club Organizer', 'FAC1004'),
('AV24003', 'AV24003', 'Ishaan Reddy', 'ishaan.reddy@campus.edu', 'Artificial Intelligence & Data Science', 'AIDS', 'BTECH-AIDS', 5, 2024, '2024-08-01', '+91 91234 56703', 7.68, 76, 73.00, 'At risk', 'Robotics, Cricket, IoT Prototyping', 'Robotics Society Member', 'FAC1003'),
('AV24004', 'AV24004', 'Meera Nair', 'meera.nair@campus.edu', 'Computer Science & Engineering', 'CSE', 'BTECH-CSE', 3, 2025, '2025-08-01', '+91 91234 56704', 8.91, 48, 89.00, 'Active', 'Classical Music, Painting, UI/UX Design', 'Fine Arts Society Coordinator', 'FAC1001'),
('AV24005', 'AV24005', 'Kabir Verma', 'kabir.verma@campus.edu', 'Artificial Intelligence & Data Science', 'AIDS', 'BTECH-AIDS', 7, 2023, '2023-08-01', '+91 91234 56705', 8.26, 120, 81.00, 'Active', 'Travel Photography, Android Development', 'University Magazine Editor', 'FAC1003'),
('AV24006', 'AV24006', 'Sneha Kulkarni', 'sneha.kulkarni@campus.edu', 'Computer Science & Engineering', 'CSE', 'BTECH-CSE', 5, 2024, '2024-08-01', '+91 91234 56706', 8.45, 84, 91.50, 'Active', 'Table Tennis, Web Development', 'Women in Tech Student Chapter', 'FAC1002'),
('AV24007', 'AV24007', 'Rohan Gupta', 'rohan.gupta@campus.edu', 'Information Technology', 'IT', 'BTECH-IT', 5, 2024, '2024-08-01', '+91 91234 56707', 7.92, 80, 85.00, 'Active', 'Guitar, Cyber Security CTF Challenges', 'Campus Security Group', 'FAC1004'),
('AV24008', 'AV24008', 'Priya Singh', 'priya.singh@campus.edu', 'Computer Science & Engineering', 'CSE', 'BTECH-CSE', 5, 2024, '2024-08-01', '+91 91234 56708', 8.88, 84, 94.00, 'Active', 'Debating, Basketball, Cloud Architecture', 'College Basketball Team Captain', 'FAC1001')
ON CONFLICT (student_id) DO NOTHING;

-- 9. Courses
INSERT INTO courses (course_code, title, department, department_code, faculty_name, primary_faculty_id, credits, lecture_hours, lab_hours, enrolled_students, completion, syllabus, color, active) VALUES
('CS501', 'Data Structures & Algorithms', 'Computer Science & Engineering', 'CSE', 'Dr. Ananya Rao', 'FAC1001', 4, 3, 2, 68, 78, 'Advanced Trees, Heaps, Graph Algorithms, Dynamic Programming, Disjoint Sets, NP-Completeness', '#7065f0', TRUE),
('AI520', 'Machine Learning', 'Artificial Intelligence & Data Science', 'AIDS', 'Dr. Sana Iqbal', 'FAC1003', 4, 3, 2, 54, 64, 'Supervised Learning, Optimization, Neural Networks, Unsupervised Clustering, Ensemble Methods', '#28b89b', TRUE),
('DB510', 'Database Systems', 'Computer Science & Engineering', 'CSE', 'Prof. Vikram Das', 'FAC1002', 4, 3, 2, 72, 86, 'Relational Algebra, SQL, Normalization, Query Execution & Optimization, ACID Transactions, Concurrency', '#f5a544', TRUE),
('WD505', 'Full Stack Development', 'Information Technology', 'IT', 'Dr. Rajesh Kumar', 'FAC1004', 3, 2, 2, 48, 51, 'React, TypeScript, Spring Boot REST APIs, State Management, Docker, CI/CD, Microservices', '#ef7b9a', TRUE),
('CS503', 'Computer Networks', 'Computer Science & Engineering', 'CSE', 'Dr. Ananya Rao', 'FAC1001', 4, 3, 2, 60, 70, 'OSI Reference Model, TCP/UDP, IP Routing Protocols, Congestion Control, Network Security & TLS', '#6da7f7', TRUE)
ON CONFLICT (course_code) DO NOTHING;

-- 10. Course Modules (Milestone breakdown for real completion % calculation)
INSERT INTO course_modules (course_code, module_number, title, description, hours_allocated) VALUES
('CS501', 1, 'Module 1: Advanced Trees & Priority Queues', 'AVL Trees, Red-Black Trees, Binary & Binomial Heaps', 8),
('CS501', 2, 'Module 2: Graph Representations & Traversals', 'Adjacency matrices, BFS, DFS, Topological Sorting, Connected Components', 10),
('CS501', 3, 'Module 3: Shortest Paths & Spanning Trees', 'Dijkstra, Bellman-Ford, Floyd-Warshall, Kruskal and Prim algorithms', 10),
('CS501', 4, 'Module 4: Dynamic Programming & Greedy Strategies', 'Knapsack variants, Matrix Chain Multiplication, Huffman Coding', 12),
('CS501', 5, 'Module 5: Computational Complexity & String Algorithms', 'Tries, KMP string matching, P vs NP, NP-Complete reductions', 8),

('AI520', 1, 'Module 1: Mathematical Foundations & Regression', 'Linear Algebra for ML, Linear Regression, Regularization (L1/L2)', 8),
('AI520', 2, 'Module 2: Classification Models & SVM', 'Logistic Regression, Support Vector Machines, Kernel Methods', 10),
('AI520', 3, 'Module 3: Decision Trees & Ensemble Learning', 'Random Forests, AdaBoost, Gradient Boosting, XGBoost', 10),
('AI520', 4, 'Module 4: Unsupervised Learning & Dimensionality Reduction', 'K-Means, PCA, t-SNE, Gaussian Mixture Models', 8),
('AI520', 5, 'Module 5: Introduction to Neural Networks & PyTorch', 'Perceptrons, Backpropagation, Gradient Descent variants', 12),

('DB510', 1, 'Module 1: Relational Data Model & Advanced SQL', 'ER Modeling, Relational Schema, Complex Joins, Window Functions', 8),
('DB510', 2, 'Module 2: Schema Refinement & Normal Forms', 'Functional Dependencies, 1NF to BCNF, Multivalued Dependencies', 10),
('DB510', 3, 'Module 3: Storage Structures & Indexing', 'B+ Trees, Hash Indexes, Buffer Pool Management', 10),
('DB510', 4, 'Module 4: Query Evaluation & Optimization Engine', 'Operator Implementation, Cost Estimation, Relational Equivalence Rules', 10),
('DB510', 5, 'Module 5: Transaction Management & Recovery', 'ACID, Serializability, 2PL, WAL Logging, ARIES Algorithm', 10)
ON CONFLICT (course_code, module_number) DO NOTHING;

-- 11. Course Sections
INSERT INTO course_sections (id, section_code, course_code, semester_id, faculty_id, classroom_id, max_capacity, enrolled_count) VALUES
(1, 'CS501-A', 'CS501', 1, 'FAC1001', 1, 70, 68),
(2, 'AI520-A', 'AI520', 1, 'FAC1003', 2, 60, 54),
(3, 'DB510-A', 'DB510', 1, 'FAC1002', 1, 75, 72),
(4, 'WD505-A', 'WD505', 1, 'FAC1004', 3, 50, 48),
(5, 'CS503-A', 'CS503', 1, 'FAC1001', 1, 65, 60)
ON CONFLICT (id) DO NOTHING;

-- 12. Timetable Slots
INSERT INTO timetable_slots (section_code, day_of_week, start_time, end_time, classroom_id, faculty_id) VALUES
('CS501-A', 'MONDAY',    '09:00:00', '10:00:00', 1, 'FAC1001'),
('CS501-A', 'WEDNESDAY', '09:00:00', '10:00:00', 1, 'FAC1001'),
('CS501-A', 'FRIDAY',    '10:00:00', '11:00:00', 1, 'FAC1001'),
('AI520-A', 'MONDAY',    '11:00:00', '12:00:00', 2, 'FAC1003'),
('AI520-A', 'WEDNESDAY', '11:00:00', '12:00:00', 2, 'FAC1003'),
('AI520-A', 'THURSDAY',  '14:00:00', '16:00:00', 4, 'FAC1003'),
('DB510-A', 'TUESDAY',   '10:00:00', '11:00:00', 1, 'FAC1002'),
('DB510-A', 'THURSDAY',  '10:00:00', '11:00:00', 1, 'FAC1002'),
('DB510-A', 'FRIDAY',    '14:00:00', '16:00:00', 3, 'FAC1002'),
('WD505-A', 'TUESDAY',   '14:00:00', '16:00:00', 3, 'FAC1004'),
('WD505-A', 'THURSDAY',  '11:00:00', '12:00:00', 1, 'FAC1004')
ON CONFLICT DO NOTHING;

-- 13. Student Enrollments
INSERT INTO enrollments (student_id, course_code, section_code, semester_id, status) VALUES
('AV24001', 'CS501', 'CS501-A', 1, 'ENROLLED'),
('AV24001', 'AI520', 'AI520-A', 1, 'ENROLLED'),
('AV24001', 'DB510', 'DB510-A', 1, 'ENROLLED'),
('AV24001', 'WD505', 'WD505-A', 1, 'ENROLLED'),
('AV24001', 'CS503', 'CS503-A', 1, 'ENROLLED'),

('AV24002', 'CS501', 'CS501-A', 1, 'ENROLLED'),
('AV24002', 'DB510', 'DB510-A', 1, 'ENROLLED'),
('AV24002', 'WD505', 'WD505-A', 1, 'ENROLLED'),

('AV24003', 'CS501', 'CS501-A', 1, 'ENROLLED'),
('AV24003', 'AI520', 'AI520-A', 1, 'ENROLLED'),
('AV24003', 'DB510', 'DB510-A', 1, 'ENROLLED')
ON CONFLICT (student_id, course_code, semester_id) DO NOTHING;

-- 14. Real Milestone Progress for Aarav Sharma (CS501, AI520, DB510)
INSERT INTO student_module_progress (student_id, module_id, is_completed, completed_at) VALUES
('AV24001', 1, TRUE,  '2026-08-15 10:00:00Z'),
('AV24001', 2, TRUE,  '2026-09-02 11:30:00Z'),
('AV24001', 3, TRUE,  '2026-09-20 14:00:00Z'),
('AV24001', 4, TRUE,  '2026-10-05 16:45:00Z'),
('AV24001', 5, FALSE, NULL),

('AV24001', 6, TRUE,  '2026-08-20 09:30:00Z'),
('AV24001', 7, TRUE,  '2026-09-12 10:15:00Z'),
('AV24001', 8, TRUE,  '2026-09-28 15:00:00Z'),
('AV24001', 9, FALSE, NULL),
('AV24001', 10, FALSE, NULL),

('AV24001', 11, TRUE, '2026-08-18 11:00:00Z'),
('AV24001', 12, TRUE, '2026-09-05 14:30:00Z'),
('AV24001', 13, TRUE, '2026-09-22 16:00:00Z'),
('AV24001', 14, TRUE, '2026-10-08 10:00:00Z'),
('AV24001', 15, FALSE, NULL)
ON CONFLICT (student_id, module_id) DO NOTHING;

-- 15. Class Sessions & Attendance Records
INSERT INTO class_sessions (id, course_code, section_code, session_date, session_time, topic_covered, faculty_id) VALUES
(1, 'CS501', 'CS501-A', '2026-10-05', '09:00:00', 'Bellman-Ford Algorithm and Negative Cycles', 'FAC1001'),
(2, 'CS501', 'CS501-A', '2026-10-07', '09:00:00', 'Floyd-Warshall All-Pairs Shortest Path', 'FAC1001'),
(3, 'CS501', 'CS501-A', '2026-10-09', '10:00:00', 'Dynamic Programming Matrix Chain Multiplication', 'FAC1001'),
(4, 'AI520', 'AI520-A', '2026-10-05', '11:00:00', 'Random Forest Ensembles & Out-of-Bag Error', 'FAC1003'),
(5, 'AI520', 'AI520-A', '2026-10-07', '11:00:00', 'XGBoost Formulation and Gradient Boosting', 'FAC1003')
ON CONFLICT (id) DO NOTHING;

INSERT INTO attendance_records (student_id, course_code, session_id, class_date, status, remarks, marked_by) VALUES
('AV24001', 'CS501', 1, '2026-10-05', 'PRESENT', 'On time', 'FAC1001'),
('AV24001', 'CS501', 2, '2026-10-07', 'PRESENT', 'On time', 'FAC1001'),
('AV24001', 'CS501', 3, '2026-10-09', 'PRESENT', 'On time', 'FAC1001'),
('AV24002', 'CS501', 1, '2026-10-05', 'PRESENT', 'On time', 'FAC1001'),
('AV24002', 'CS501', 2, '2026-10-07', 'PRESENT', 'On time', 'FAC1001'),
('AV24002', 'CS501', 3, '2026-10-09', 'PRESENT', 'On time', 'FAC1001'),
('AV24003', 'CS501', 1, '2026-10-05', 'ABSENT',  'Unexcused', 'FAC1001'),
('AV24003', 'CS501', 2, '2026-10-07', 'LATE',    'Arrived 15 min late', 'FAC1001'),
('AV24003', 'CS501', 3, '2026-10-09', 'ABSENT',  'Medical reason pending review', 'FAC1001')
ON CONFLICT (student_id, course_code, class_date) DO NOTHING;

-- 16. Attendance Correction Request
INSERT INTO attendance_corrections (attendance_record_id, student_id, requested_status, reason, status) VALUES
(9, 'AV24003', 'EXCUSED', 'Was attending the Inter-College Robotics Championship semi-finals as university representative.', 'PENDING')
ON CONFLICT DO NOTHING;

-- 17. Assignments & Submissions
INSERT INTO assignments (id, course_code, section_code, faculty_id, title, description, total_marks, weightage, due_date) VALUES
(1, 'CS501', 'CS501-A', 'FAC1001', 'Assignment 3: Dynamic Programming Knapsack Benchmark', 'Implement 0/1 Knapsack using top-down memoization and bottom-up DP. Benchmark execution times on large inputs.', 100.00, 15.00, '2026-10-18 23:59:59Z'),
(2, 'AI520', 'AI520-A', 'FAC1003', 'Lab Project 2: Kaggle Titanic & Housing Price Predictors', 'Construct an end-to-end pipeline with data cleaning, feature engineering, and tuned XGBoost classifier.', 100.00, 20.00, '2026-10-22 23:59:59Z'),
(3, 'DB510', 'DB510-A', 'FAC1002', 'SQL Lab 4: Complex Analytical Queries & Query Plans', 'Write EXPLAIN ANALYZE queries and identify missing indexes for optimization.', 50.00, 10.00, '2026-10-15 17:00:00Z')
ON CONFLICT (id) DO NOTHING;

INSERT INTO assignment_submissions (assignment_id, student_id, submission_date, score, feedback, status, graded_by, graded_at) VALUES
(1, 'AV24001', '2026-10-12 18:20:00Z', 95.00, 'Superb implementation. Cache-friendly array layout achieved 3x speedup.', 'GRADED', 'FAC1001', '2026-10-13 10:00:00Z'),
(3, 'AV24001', '2026-10-11 14:10:00Z', 48.00, 'Correct index choices and accurate cost reduction analysis.', 'GRADED', 'FAC1002', '2026-10-12 11:30:00Z')
ON CONFLICT (assignment_id, student_id) DO NOTHING;

-- 18. Examinations & Assessment Marks
INSERT INTO examinations (id, course_code, semester_id, exam_type, exam_date, start_time, end_time, max_marks, weightage, classroom_id) VALUES
(1, 'CS501', 1, 'MID_TERM', '2026-09-25', '09:30:00', '11:30:00', 50.00, 25.00, 1),
(2, 'AI520', 1, 'MID_TERM', '2026-09-27', '09:30:00', '11:30:00', 50.00, 25.00, 2),
(3, 'DB510', 1, 'MID_TERM', '2026-09-29', '09:30:00', '11:30:00', 50.00, 25.00, 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO assessment_marks (examination_id, student_id, marks_obtained, is_absent, remarks, entered_by) VALUES
(1, 'AV24001', 46.50, FALSE, 'Top 5 percentile in Algorithms', 'FAC1001'),
(2, 'AV24001', 42.00, FALSE, 'Strong mathematical reasoning', 'FAC1003'),
(3, 'AV24001', 48.00, FALSE, 'Near perfect normalization solutions', 'FAC1002')
ON CONFLICT (examination_id, student_id) DO NOTHING;

-- 19. GPA Records (Semester-by-semester historical trend)
INSERT INTO gpa_records (student_id, semester_id, semester_number, semester_gpa, cumulative_cgpa, credits_attempted, credits_earned) VALUES
('AV24001', NULL, 1, 7.40, 7.40, 20, 20),
('AV24001', NULL, 2, 7.80, 7.60, 22, 22),
('AV24001', NULL, 3, 8.10, 7.78, 22, 22),
('AV24001', NULL, 4, 8.00, 7.84, 20, 20),
('AV24001', 1,    5, 8.72, 8.02, 20, 20)
ON CONFLICT DO NOTHING;

-- 20. Fee Invoices & Financial Ledger
INSERT INTO fee_invoices (invoice_id, student_id, semester_id, fee_type, description, amount, discount_amount, net_payable, due_date, status) VALUES
('INV-2601', 'AV24001', 1, 'TUITION',     'Tuition Fee - Semester 5 (Part 1)',  60000.00, 0, 60000.00, '2026-08-10', 'PAID'),
('INV-2602', 'AV24001', 1, 'EXAMINATION', 'Semester 5 Examination Fee',        10000.00, 0, 10000.00, '2026-09-15', 'PAID'),
('INV-2603', 'AV24001', 1, 'LIBRARY_LAB', 'Advanced Computing Labs & Library',  20000.00, 0, 20000.00, '2026-10-15', 'PAID'),
('INV-2604', 'AV24001', 1, 'TUITION',     'Tuition Fee - Semester 5 (Part 2)',  30000.00, 0, 30000.00, '2026-10-30', 'PENDING')
ON CONFLICT (invoice_id) DO NOTHING;

INSERT INTO fee_ledger_entries (invoice_id, student_id, entry_type, amount, balance_after, description) VALUES
('INV-2601', 'AV24001', 'DEBIT',  60000.00, 60000.00, 'Invoice issued: Tuition Fee Part 1'),
('INV-2601', 'AV24001', 'CREDIT', 60000.00, 0.00,     'Payment received via Net Banking (REF: NB98214)'),
('INV-2602', 'AV24001', 'DEBIT',  10000.00, 10000.00, 'Invoice issued: Examination Fee'),
('INV-2602', 'AV24001', 'CREDIT', 10000.00, 0.00,     'Payment received via UPI (REF: UPI44201)'),
('INV-2603', 'AV24001', 'DEBIT',  20000.00, 20000.00, 'Invoice issued: Library & Lab Fee'),
('INV-2603', 'AV24001', 'CREDIT', 20000.00, 0.00,     'Payment received via UPI (REF: UPI88129)'),
('INV-2604', 'AV24001', 'DEBIT',  30000.00, 30000.00, 'Invoice issued: Tuition Fee Part 2')
ON CONFLICT DO NOTHING;

INSERT INTO payment_transactions (transaction_reference, invoice_id, student_id, amount, payment_method, status, provider_reference, receipt_number) VALUES
('TXN-2026-001', 'INV-2601', 'AV24001', 60000.00, 'NET_BANKING', 'SUCCESS', 'HDFC-REF-8891024', 'RCPT-26-001'),
('TXN-2026-002', 'INV-2602', 'AV24001', 10000.00, 'UPI',         'SUCCESS', 'UPI-REF-7718291', 'RCPT-26-002'),
('TXN-2026-003', 'INV-2603', 'AV24001', 20000.00, 'UPI',         'SUCCESS', 'UPI-REF-9921043', 'RCPT-26-003')
ON CONFLICT (transaction_reference) DO NOTHING;

-- 21. Campus Announcements
INSERT INTO announcements (title, content, target_audience, department_code, author_id, priority, is_pinned) VALUES
('Mid-Semester Examination Schedule Published', 'The Mid-Semester timetable for all undergraduate B.Tech semesters is now active. Students can verify their assigned classrooms and timings in the Timetable tab.', 'ALL', NULL, 'admin', 'HIGH', TRUE),
('Spring 2027 Elective Course Registration Window', 'Elective course registration opens on Friday at 09:00 AM. Please ensure all prerequisites are met before registering.', 'STUDENTS', 'CSE', 'FAC1001', 'HIGH', TRUE),
('Faculty Development Program on Cloud Native Architectures', 'All faculty members are invited to participate in the 3-day IEEE FDP scheduled from Nov 5-7 in Auditorium 1.', 'FACULTY', NULL, 'admin', 'NORMAL', FALSE)
ON CONFLICT DO NOTHING;

-- 22. User Notifications
INSERT INTO notifications (user_id, title, message, notification_type, link_url, read) VALUES
('AV24001', 'Mid-Term Results Published', 'Your marks for Data Structures (CS501) Mid-Term have been finalized: 46.5 / 50.', 'GRADE_PUBLISHED', '/student/grades', FALSE),
('AV24001', 'Fee Installment Due Notice', 'Tuition fee installment of ₹30,000 for Semester 5 is due by 30 October 2026.', 'FEE_DUE', '/student/fees', FALSE),
('AV24001', 'Assignment 3 Graded', 'Dr. Ananya Rao posted feedback on Assignment 3: Knapsack Benchmark.', 'GENERAL', '/student/assignments', TRUE),
('FAC1001', 'Pending Attendance Submissions', 'Please confirm and submit attendance records for CS501-A Friday practical session.', 'GENERAL', '/faculty/attendance', FALSE),
('admin',   'New Grievance Ticket Raised', 'Ticket TICK-1001 raised regarding lab seating capacity.', 'GENERAL', '/admin/support', FALSE)
ON CONFLICT DO NOTHING;

-- 23. Support / Grievance Tickets
INSERT INTO support_tickets (ticket_number, user_id, category, subject, description, priority, status, assigned_to) VALUES
('TICK-1001', 'AV24003', 'ATTENDANCE', 'Attendance discrepancy in AI520 for 09 Oct', 'I was officially representing the college at the state robotics competition. Proof document submitted.', 'MEDIUM', 'IN_PROGRESS', 'admin'),
('TICK-1002', 'AV24001', 'FEE_PAYMENT', 'Receipt duplicate generation request', 'Need an official stamped receipt copy of Invoice INV-2601 for scholarship tax verification.', 'LOW', 'RESOLVED', 'admin')
ON CONFLICT (ticket_number) DO NOTHING;

-- 24. System Settings
INSERT INTO system_settings (setting_key, setting_value, description) VALUES
('ACADEMIC_YEAR_CURRENT', '2026-2027', 'Currently active academic year'),
('SEMESTER_CURRENT', '5', 'Currently active undergraduate semester'),
('ATTENDANCE_MIN_THRESHOLD', '75.0', 'Minimum percentage required to sit for examinations'),
('ATTENDANCE_SHORTAGE_WARNING', '80.0', 'Threshold below which automated alerts are dispatched'),
('COURSE_REGISTRATION_STATUS', 'OPEN', 'Status of course add/drop registration portal')
ON CONFLICT (setting_key) DO NOTHING;
