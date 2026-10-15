-- ============================================================================
-- AcadVision Full Database Schema (PostgreSQL)
-- Normalized 36-entity Relational Model for Smart Campus Portal
-- ============================================================================

-- 1. Identity & Access Management
CREATE TABLE IF NOT EXISTS app_users (
  id BIGSERIAL PRIMARY KEY,
  user_id VARCHAR(60) NOT NULL UNIQUE,
  full_name VARCHAR(160) NOT NULL,
  email VARCHAR(180) NOT NULL UNIQUE,
  password_hash VARCHAR(120) NOT NULL,
  role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'FACULTY', 'STUDENT')),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
  mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
  last_login_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
  id BIGSERIAL PRIMARY KEY,
  user_id VARCHAR(60) NOT NULL REFERENCES app_users(user_id) ON DELETE CASCADE,
  token VARCHAR(255) NOT NULL UNIQUE,
  expiry_date TIMESTAMPTZ NOT NULL,
  revoked BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id BIGSERIAL PRIMARY KEY,
  user_id VARCHAR(60) NOT NULL REFERENCES app_users(user_id) ON DELETE CASCADE,
  token VARCHAR(120) NOT NULL UNIQUE,
  expiry_date TIMESTAMPTZ NOT NULL,
  used BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGSERIAL PRIMARY KEY,
  actor_user_id VARCHAR(60) NOT NULL,
  action VARCHAR(100) NOT NULL,
  entity_type VARCHAR(80) NOT NULL,
  entity_id VARCHAR(100),
  details TEXT,
  impersonation_session BOOLEAN NOT NULL DEFAULT FALSE,
  impersonation_reason VARCHAR(255),
  ip_address VARCHAR(45),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Institutional Hierarchy & Academic Calendar
CREATE TABLE IF NOT EXISTS departments (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(150) NOT NULL,
  hod_name VARCHAR(120),
  contact_email VARCHAR(180),
  building VARCHAR(80),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS programs (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(180) NOT NULL,
  department_code VARCHAR(20) NOT NULL REFERENCES departments(code) ON DELETE CASCADE,
  degree_level VARCHAR(20) NOT NULL DEFAULT 'UNDERGRADUATE' CHECK (degree_level IN ('UNDERGRADUATE', 'POSTGRADUATE', 'DOCTORAL')),
  total_semesters INTEGER NOT NULL DEFAULT 8 CHECK (total_semesters > 0),
  total_credits_required INTEGER NOT NULL DEFAULT 160 CHECK (total_credits_required > 0),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS academic_years (
  id BIGSERIAL PRIMARY KEY,
  year_name VARCHAR(30) NOT NULL UNIQUE,
  is_current BOOLEAN NOT NULL DEFAULT FALSE,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS semesters (
  id BIGSERIAL PRIMARY KEY,
  academic_year_id BIGINT NOT NULL REFERENCES academic_years(id) ON DELETE CASCADE,
  semester_number INTEGER NOT NULL CHECK (semester_number BETWEEN 1 AND 12),
  term VARCHAR(20) NOT NULL CHECK (term IN ('FALL', 'SPRING', 'SUMMER')),
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  is_current BOOLEAN NOT NULL DEFAULT FALSE,
  registration_open BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(academic_year_id, semester_number, term)
);

CREATE TABLE IF NOT EXISTS classrooms (
  id BIGSERIAL PRIMARY KEY,
  room_number VARCHAR(40) NOT NULL UNIQUE,
  building VARCHAR(80) NOT NULL,
  capacity INTEGER NOT NULL DEFAULT 60 CHECK (capacity > 0),
  room_type VARCHAR(40) NOT NULL DEFAULT 'LECTURE_HALL' CHECK (room_type IN ('LECTURE_HALL', 'LABORATORY', 'SEMINAR_ROOM', 'AUDITORIUM'))
);

CREATE TABLE IF NOT EXISTS grading_policies (
  id BIGSERIAL PRIMARY KEY,
  grade_letter VARCHAR(5) NOT NULL UNIQUE,
  min_percentage NUMERIC(5,2) NOT NULL CHECK (min_percentage >= 0),
  max_percentage NUMERIC(5,2) NOT NULL CHECK (max_percentage <= 100),
  grade_point NUMERIC(4,2) NOT NULL CHECK (grade_point >= 0 AND grade_point <= 10),
  description VARCHAR(100) NOT NULL
);

-- 3. Faculty & Student Profiles
CREATE TABLE IF NOT EXISTS faculty (
  id BIGSERIAL PRIMARY KEY,
  faculty_id VARCHAR(60) NOT NULL UNIQUE,
  user_id VARCHAR(60) REFERENCES app_users(user_id) ON DELETE SET NULL,
  full_name VARCHAR(160) NOT NULL,
  email VARCHAR(180) NOT NULL,
  department VARCHAR(120) NOT NULL,
  department_code VARCHAR(20) REFERENCES departments(code) ON DELETE SET NULL,
  designation VARCHAR(100) NOT NULL DEFAULT 'Assistant Professor',
  qualification VARCHAR(120),
  specialization VARCHAR(160),
  office_room VARCHAR(40),
  phone VARCHAR(30),
  course_count INTEGER NOT NULL DEFAULT 0,
  status VARCHAR(30) NOT NULL DEFAULT 'Active',
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS students (
  id BIGSERIAL PRIMARY KEY,
  student_id VARCHAR(60) NOT NULL UNIQUE,
  user_id VARCHAR(60) REFERENCES app_users(user_id) ON DELETE SET NULL,
  full_name VARCHAR(160) NOT NULL,
  email VARCHAR(180) NOT NULL,
  department VARCHAR(120) NOT NULL,
  department_code VARCHAR(20) REFERENCES departments(code) ON DELETE SET NULL,
  program_code VARCHAR(30) REFERENCES programs(code) ON DELETE SET NULL,
  semester INTEGER NOT NULL DEFAULT 1 CHECK (semester > 0),
  batch_year INTEGER NOT NULL DEFAULT 2024,
  date_of_admission DATE,
  phone VARCHAR(30),
  cgpa NUMERIC(4,2) NOT NULL DEFAULT 0 CHECK (cgpa >= 0 AND cgpa <= 10),
  earned_credits INTEGER NOT NULL DEFAULT 0 CHECK (earned_credits >= 0),
  attendance NUMERIC(5,2) NOT NULL DEFAULT 100 CHECK (attendance >= 0 AND attendance <= 100),
  status VARCHAR(30) NOT NULL DEFAULT 'Active',
  hobbies TEXT,
  extracurriculars TEXT,
  advisor_faculty_id VARCHAR(60) REFERENCES faculty(faculty_id) ON DELETE SET NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Courses, Curriculum & Sections
CREATE TABLE IF NOT EXISTS courses (
  id BIGSERIAL PRIMARY KEY,
  course_code VARCHAR(40) NOT NULL UNIQUE,
  title VARCHAR(180) NOT NULL,
  department VARCHAR(120) NOT NULL,
  department_code VARCHAR(20) REFERENCES departments(code) ON DELETE SET NULL,
  faculty_name VARCHAR(160) NOT NULL DEFAULT 'TBD',
  primary_faculty_id VARCHAR(60) REFERENCES faculty(faculty_id) ON DELETE SET NULL,
  credits INTEGER NOT NULL DEFAULT 4 CHECK (credits > 0),
  lecture_hours INTEGER NOT NULL DEFAULT 3,
  lab_hours INTEGER NOT NULL DEFAULT 0,
  enrolled_students INTEGER NOT NULL DEFAULT 0,
  completion INTEGER NOT NULL DEFAULT 0 CHECK (completion BETWEEN 0 AND 100),
  syllabus TEXT,
  color VARCHAR(20) NOT NULL DEFAULT '#7065f0',
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS course_prerequisites (
  id BIGSERIAL PRIMARY KEY,
  course_code VARCHAR(40) NOT NULL REFERENCES courses(course_code) ON DELETE CASCADE,
  prerequisite_course_code VARCHAR(40) NOT NULL REFERENCES courses(course_code) ON DELETE CASCADE,
  min_grade VARCHAR(5) DEFAULT 'C',
  UNIQUE(course_code, prerequisite_course_code)
);

CREATE TABLE IF NOT EXISTS course_modules (
  id BIGSERIAL PRIMARY KEY,
  course_code VARCHAR(40) NOT NULL REFERENCES courses(course_code) ON DELETE CASCADE,
  module_number INTEGER NOT NULL CHECK (module_number > 0),
  title VARCHAR(180) NOT NULL,
  description TEXT,
  hours_allocated INTEGER NOT NULL DEFAULT 8,
  UNIQUE(course_code, module_number)
);

CREATE TABLE IF NOT EXISTS course_sections (
  id BIGSERIAL PRIMARY KEY,
  section_code VARCHAR(60) NOT NULL UNIQUE,
  course_code VARCHAR(40) NOT NULL REFERENCES courses(course_code) ON DELETE CASCADE,
  semester_id BIGINT REFERENCES semesters(id) ON DELETE SET NULL,
  faculty_id VARCHAR(60) NOT NULL REFERENCES faculty(faculty_id) ON DELETE CASCADE,
  classroom_id BIGINT REFERENCES classrooms(id) ON DELETE SET NULL,
  max_capacity INTEGER NOT NULL DEFAULT 60 CHECK (max_capacity > 0),
  enrolled_count INTEGER NOT NULL DEFAULT 0 CHECK (enrolled_count >= 0)
);

CREATE TABLE IF NOT EXISTS timetable_slots (
  id BIGSERIAL PRIMARY KEY,
  section_code VARCHAR(60) NOT NULL REFERENCES course_sections(section_code) ON DELETE CASCADE,
  day_of_week VARCHAR(15) NOT NULL CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY')),
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  classroom_id BIGINT REFERENCES classrooms(id) ON DELETE SET NULL,
  faculty_id VARCHAR(60) REFERENCES faculty(faculty_id) ON DELETE SET NULL,
  UNIQUE(classroom_id, day_of_week, start_time),
  UNIQUE(faculty_id, day_of_week, start_time)
);

-- 5. Enrollments & Real Milestone Progress
CREATE TABLE IF NOT EXISTS enrollments (
  id BIGSERIAL PRIMARY KEY,
  student_id VARCHAR(60) NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
  course_code VARCHAR(40) NOT NULL REFERENCES courses(course_code) ON DELETE CASCADE,
  section_code VARCHAR(60) REFERENCES course_sections(section_code) ON DELETE SET NULL,
  semester_id BIGINT REFERENCES semesters(id) ON DELETE SET NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ENROLLED' CHECK (status IN ('ENROLLED', 'DROPPED', 'COMPLETED', 'FAILED')),
  enrollment_date DATE NOT NULL DEFAULT CURRENT_DATE,
  drop_date DATE,
  UNIQUE(student_id, course_code, semester_id)
);

CREATE TABLE IF NOT EXISTS student_module_progress (
  id BIGSERIAL PRIMARY KEY,
  student_id VARCHAR(60) NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
  module_id BIGINT NOT NULL REFERENCES course_modules(id) ON DELETE CASCADE,
  is_completed BOOLEAN NOT NULL DEFAULT FALSE,
  completed_at TIMESTAMPTZ,
  UNIQUE(student_id, module_id)
);

-- 6. Class Sessions, Attendance & Correction Requests
CREATE TABLE IF NOT EXISTS class_sessions (
  id BIGSERIAL PRIMARY KEY,
  course_code VARCHAR(40) NOT NULL REFERENCES courses(course_code) ON DELETE CASCADE,
  section_code VARCHAR(60) REFERENCES course_sections(section_code) ON DELETE SET NULL,
  session_date DATE NOT NULL,
  session_time TIME,
  topic_covered VARCHAR(200),
  faculty_id VARCHAR(60) NOT NULL REFERENCES faculty(faculty_id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(course_code, section_code, session_date, session_time)
);

CREATE TABLE IF NOT EXISTS attendance_records (
  id BIGSERIAL PRIMARY KEY,
  student_id VARCHAR(60) NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
  course_code VARCHAR(40) NOT NULL REFERENCES courses(course_code) ON DELETE CASCADE,
  session_id BIGINT REFERENCES class_sessions(id) ON DELETE SET NULL,
  class_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL CHECK (status IN ('PRESENT', 'ABSENT', 'LATE', 'EXCUSED')),
  remarks VARCHAR(200),
  marked_by VARCHAR(60) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(student_id, course_code, class_date)
);

CREATE TABLE IF NOT EXISTS attendance_corrections (
  id BIGSERIAL PRIMARY KEY,
  attendance_record_id BIGINT NOT NULL REFERENCES attendance_records(id) ON DELETE CASCADE,
  student_id VARCHAR(60) NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
  requested_status VARCHAR(20) NOT NULL CHECK (requested_status IN ('PRESENT', 'EXCUSED')),
  reason TEXT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
  reviewed_by VARCHAR(60) REFERENCES app_users(user_id) ON DELETE SET NULL,
  reviewed_at TIMESTAMPTZ,
  reviewer_remarks TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 7. Assignments, Submissions & Assessments
CREATE TABLE IF NOT EXISTS assignments (
  id BIGSERIAL PRIMARY KEY,
  course_code VARCHAR(40) NOT NULL REFERENCES courses(course_code) ON DELETE CASCADE,
  section_code VARCHAR(60) REFERENCES course_sections(section_code) ON DELETE SET NULL,
  faculty_id VARCHAR(60) NOT NULL REFERENCES faculty(faculty_id) ON DELETE CASCADE,
  title VARCHAR(200) NOT NULL,
  description TEXT,
  total_marks NUMERIC(6,2) NOT NULL DEFAULT 100.0,
  weightage NUMERIC(5,2) NOT NULL DEFAULT 10.0,
  due_date TIMESTAMPTZ NOT NULL,
  attachment_url VARCHAR(255),
  published BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS assignment_submissions (
  id BIGSERIAL PRIMARY KEY,
  assignment_id BIGINT NOT NULL REFERENCES assignments(id) ON DELETE CASCADE,
  student_id VARCHAR(60) NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
  submission_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  file_url VARCHAR(255),
  content TEXT,
  score NUMERIC(6,2),
  feedback TEXT,
  status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED' CHECK (status IN ('SUBMITTED', 'LATE', 'GRADED', 'RETURNED')),
  graded_by VARCHAR(60) REFERENCES faculty(faculty_id) ON DELETE SET NULL,
  graded_at TIMESTAMPTZ,
  UNIQUE(assignment_id, student_id)
);

CREATE TABLE IF NOT EXISTS examinations (
  id BIGSERIAL PRIMARY KEY,
  course_code VARCHAR(40) NOT NULL REFERENCES courses(course_code) ON DELETE CASCADE,
  semester_id BIGINT REFERENCES semesters(id) ON DELETE SET NULL,
  exam_type VARCHAR(30) NOT NULL CHECK (exam_type IN ('MID_TERM', 'END_SEMESTER', 'QUIZ', 'PRACTICAL')),
  exam_date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  max_marks NUMERIC(6,2) NOT NULL DEFAULT 100,
  weightage NUMERIC(5,2) NOT NULL DEFAULT 30,
  classroom_id BIGINT REFERENCES classrooms(id) ON DELETE SET NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS assessment_marks (
  id BIGSERIAL PRIMARY KEY,
  examination_id BIGINT NOT NULL REFERENCES examinations(id) ON DELETE CASCADE,
  student_id VARCHAR(60) NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
  marks_obtained NUMERIC(6,2) NOT NULL CHECK (marks_obtained >= 0),
  is_absent BOOLEAN NOT NULL DEFAULT FALSE,
  remarks VARCHAR(200),
  entered_by VARCHAR(60) NOT NULL REFERENCES faculty(faculty_id) ON DELETE CASCADE,
  entered_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(examination_id, student_id)
);

CREATE TABLE IF NOT EXISTS student_grades (
  id BIGSERIAL PRIMARY KEY,
  student_id VARCHAR(60) NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
  course_code VARCHAR(40) NOT NULL REFERENCES courses(course_code) ON DELETE CASCADE,
  semester_id BIGINT REFERENCES semesters(id) ON DELETE SET NULL,
  total_score NUMERIC(5,2) NOT NULL CHECK (total_score >= 0 AND total_score <= 100),
  letter_grade VARCHAR(5) NOT NULL REFERENCES grading_policies(grade_letter),
  grade_point NUMERIC(4,2) NOT NULL CHECK (grade_point >= 0 AND grade_point <= 10),
  credits_earned INTEGER NOT NULL CHECK (credits_earned >= 0),
  published BOOLEAN NOT NULL DEFAULT TRUE,
  finalized_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(student_id, course_code, semester_id)
);

CREATE TABLE IF NOT EXISTS gpa_records (
  id BIGSERIAL PRIMARY KEY,
  student_id VARCHAR(60) NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
  semester_id BIGINT REFERENCES semesters(id) ON DELETE SET NULL,
  semester_number INTEGER NOT NULL CHECK (semester_number > 0),
  semester_gpa NUMERIC(4,2) NOT NULL CHECK (semester_gpa >= 0 AND semester_gpa <= 10),
  cumulative_cgpa NUMERIC(4,2) NOT NULL CHECK (cumulative_cgpa >= 0 AND cumulative_cgpa <= 10),
  credits_attempted INTEGER NOT NULL,
  credits_earned INTEGER NOT NULL,
  recorded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(student_id, semester_id)
);

-- 8. Fees, Financial Ledger & Verified Payments
CREATE TABLE IF NOT EXISTS fee_invoices (
  id BIGSERIAL PRIMARY KEY,
  invoice_id VARCHAR(60) NOT NULL UNIQUE,
  student_id VARCHAR(60) NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
  semester_id BIGINT REFERENCES semesters(id) ON DELETE SET NULL,
  fee_type VARCHAR(40) NOT NULL DEFAULT 'TUITION' CHECK (fee_type IN ('TUITION', 'EXAMINATION', 'LIBRARY_LAB', 'HOSTEL', 'TRANSPORT', 'MISC')),
  description VARCHAR(200) NOT NULL,
  amount NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
  discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
  net_payable NUMERIC(12,2) NOT NULL CHECK (net_payable >= 0),
  due_date DATE NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PAID', 'PARTIALLY_PAID', 'OVERDUE', 'CANCELLED')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS fee_ledger_entries (
  id BIGSERIAL PRIMARY KEY,
  invoice_id VARCHAR(60) REFERENCES fee_invoices(invoice_id) ON DELETE CASCADE,
  student_id VARCHAR(60) NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
  entry_type VARCHAR(10) NOT NULL CHECK (entry_type IN ('DEBIT', 'CREDIT')),
  amount NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
  balance_after NUMERIC(12,2) NOT NULL,
  description VARCHAR(255) NOT NULL,
  transaction_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payment_transactions (
  id BIGSERIAL PRIMARY KEY,
  transaction_reference VARCHAR(80) NOT NULL UNIQUE,
  invoice_id VARCHAR(60) NOT NULL REFERENCES fee_invoices(invoice_id) ON DELETE CASCADE,
  student_id VARCHAR(60) NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
  amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
  payment_method VARCHAR(30) NOT NULL CHECK (payment_method IN ('UPI', 'NET_BANKING', 'CARD', 'SCHOLARSHIP_ADJUSTMENT')),
  status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' CHECK (status IN ('INITIATED', 'SUCCESS', 'FAILED', 'REFUNDED')),
  provider_reference VARCHAR(160),
  receipt_number VARCHAR(80) UNIQUE,
  verified_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS scholarships (
  id BIGSERIAL PRIMARY KEY,
  student_id VARCHAR(60) NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
  scholarship_name VARCHAR(150) NOT NULL,
  amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
  academic_year VARCHAR(30) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'DISBURSED', 'REVOKED')),
  disbursed_date DATE
);

-- 9. Communications, Support & Institutional Configuration
CREATE TABLE IF NOT EXISTS announcements (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  target_audience VARCHAR(30) NOT NULL DEFAULT 'ALL' CHECK (target_audience IN ('ALL', 'STUDENTS', 'FACULTY', 'DEPARTMENT')),
  department_code VARCHAR(20) REFERENCES departments(code) ON DELETE SET NULL,
  author_id VARCHAR(60) NOT NULL,
  priority VARCHAR(15) NOT NULL DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
  is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notifications (
  id BIGSERIAL PRIMARY KEY,
  user_id VARCHAR(60) NOT NULL REFERENCES app_users(user_id) ON DELETE CASCADE,
  title VARCHAR(180) NOT NULL,
  message TEXT NOT NULL,
  notification_type VARCHAR(40) NOT NULL DEFAULT 'GENERAL' CHECK (notification_type IN ('ATTENDANCE_SHORTAGE', 'EXAM_SCHEDULE', 'FEE_DUE', 'GRADE_PUBLISHED', 'ASSIGNMENT_DEADLINE', 'GENERAL')),
  link_url VARCHAR(255),
  read BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS support_tickets (
  id BIGSERIAL PRIMARY KEY,
  ticket_number VARCHAR(40) NOT NULL UNIQUE,
  user_id VARCHAR(60) NOT NULL REFERENCES app_users(user_id) ON DELETE CASCADE,
  category VARCHAR(40) NOT NULL CHECK (category IN ('ACADEMIC', 'ATTENDANCE', 'FEE_PAYMENT', 'TIMETABLE', 'TECHNICAL')),
  subject VARCHAR(200) NOT NULL,
  description TEXT NOT NULL,
  priority VARCHAR(15) NOT NULL DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
  status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
  assigned_to VARCHAR(60) REFERENCES app_users(user_id) ON DELETE SET NULL,
  resolution_notes TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  resolved_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS system_settings (
  id BIGSERIAL PRIMARY KEY,
  setting_key VARCHAR(80) NOT NULL UNIQUE,
  setting_value VARCHAR(255) NOT NULL,
  description VARCHAR(255),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- Performance Indexes
-- ============================================================================
CREATE INDEX IF NOT EXISTS idx_users_email ON app_users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON app_users(role);
CREATE INDEX IF NOT EXISTS idx_students_name ON students(full_name);
CREATE INDEX IF NOT EXISTS idx_students_dept ON students(department_code);
CREATE INDEX IF NOT EXISTS idx_faculty_name ON faculty(full_name);
CREATE INDEX IF NOT EXISTS idx_faculty_dept ON faculty(department_code);
CREATE INDEX IF NOT EXISTS idx_courses_dept ON courses(department_code);
CREATE INDEX IF NOT EXISTS idx_enrollments_student ON enrollments(student_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_course ON enrollments(course_code);
CREATE INDEX IF NOT EXISTS idx_attendance_student_date ON attendance_records(student_id, class_date);
CREATE INDEX IF NOT EXISTS idx_attendance_course_date ON attendance_records(course_code, class_date);
CREATE INDEX IF NOT EXISTS idx_fees_student_status ON fee_invoices(student_id, status);
CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications(user_id, read);
CREATE INDEX IF NOT EXISTS idx_audit_actor_date ON audit_logs(actor_user_id, created_at);
