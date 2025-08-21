# Student Grade Management System (SGMS)

> Desktop app to manage students, courses, assignments, grades, attendance, feedback, and report cards. Built with plain Java + Swing and an MS Access database via UCanAccess.
---

## Contents

* [Features](#features)
* [Tech Stack](#tech-stack)
* [Project Layout](#project-layout)
* [How to Run (NetBeans/Ant)](#how-to-run-netbeansant)
* [Database Setup (MS Access)](#database-setup-ms-access)
* [Login & Users](#login--users)
* [Key Screens](#key-screens)
* [Data Model](#data-model)
* [Validation Rules](#validation-rules)
* [Algorithms (short summary)](#algorithms-short-summary)
* [Report Cards (DOCX)](#report-cards-docx)
* [Testing & Javadoc](#testing--javadoc)
* [IEB Alignment Notes](#ieb-alignment-notes)
* [Troubleshooting](#troubleshooting)
* [Acknowledgements](#acknowledgements)

---

## Features

* **Students**

  * Add/edit/delete students (with safe validation)
  * Filter and sort by any column
* **Courses**

  * Add/edit/delete courses (unique code per grade)
  * Enrol/unenrol students via a checkbox selection view
* **Assignments & Grades**

  * Create assignments (title, max marks, term, due date)
  * Enter raw marks per student per assignment
  * Live term averages and overall **Final %** (weighted; missing terms allowed)
* **Attendance**

  * Week view (Mon–Fri), yellow column = **today**
  * Only changed cells are written to the DB
* **Feedback**

  * Per-student free-text note per course (empty note deletes)
* **Report Cards**

  * Generate a **.docx** from a Word template using simple merge placeholders
* **Search & Sort**

  * Case-insensitive search box
  * Click headers to sort (stable collator for names)
* **Auth**

  * Login, Signup (admin-gated), Reset password (admin-gated)

---

## Tech Stack

* **Language/GUI:** Java SE + Swing
* **DB:** MS Access (`School.accdb`) via **UCanAccess** JDBC driver
* **Build:** NetBeans / Ant
* **Docs:** Javadoc
* **No external code pasted** (libraries only for JDBC and standard JDK)

---

## Project Layout

```
src/
  sgms/main/StudentGradeManagementSystem.java   // app entry
  sgms/ui/                                      // frames, table models
    LoginPage.java, SignupPage.java, MainPage.java, ...
    *TableModel.java (Students, Courses, Assignments, Grades, Attendance, Feedback)
  sgms/dao/                                     // DAO interfaces + DB helper
  sgms/dao/impl/                                // UCanAccess implementations
  sgms/model/                                   // POJOs: Student, Course, Assignment, ...
  sgms/service/                                 // AuthService, ValidationService, ReportService
  sgms/util/                                    // GradeCalculator, ReportCardGenerator, SearchUtil, DBSetup, ...
resources/
  sgms/data/Report_Card.docx                    // report template (bundled)
```

---

## How to Run (NetBeans/Ant)

**NetBeans**

1. Open the project.
2. Make sure the UCanAccess JARs are on the classpath (see Troubleshooting).
3. Run **`sgms.main.StudentGradeManagementSystem`**.

**Ant (from project root)**

```bash
ant clean jar
java -jar dist/StudentGradeManagementSystem.jar
```

**Generate API docs**

```bash
ant javadoc
# open dist/javadoc/index.html
```

---

## Database Setup (MS Access)

1. Place **`School.accdb`** in your working directory (or configure `sgms.dao.DB` if you moved it).
2. First-time only: run **`sgms.util.DBSetup`** (has a `main`) to create tables and seed an administrator user.

   * This is **one-off**. If tables already exist, Access will complain—ignore or don’t run it again.
3. UCanAccess needs these JARs on the classpath:

   * `ucanaccess-x.y.z.jar`, `jackcess-*.jar`, `commons-lang3-*.jar`, `commons-logging-*.jar`, `hsqldb-*.jar`

> The included database has a list of about 300 students, however if you would like to test other parts of the program, you'd need to add them to courses, give them marks, etc.

---

## Login & Users

* **Roles:** Administrator, Teacher (default for normal users).
* **Signup/Reset:** Both require the **administrator password** for authorisation (typed by a teacher/admin).
* If you need to set/reset the admin password manually, update `tblUsers.passwordHash` for username `admin` or use the reset screen if you know the current admin password.

> For demonstration purposes, the default Administrator password is simply `admin`. Same goes for both username and password for login if you don't want to create an account.
> Passwords are stored with a **simple educational hash** (see `CredentialManager`). For a real app, use BCrypt/Argon2.

---

## Key Screens

* **View Students:** full list or by course; add/edit/delete; search & sort.
* **Manage Courses:** add (code+name), rename, mark for deletion; **red highlight** = pending delete.
* **Manage Assignments:** add/edit; mark for deletion; validation on term/max/due date.
* **Student Grades:** grid per course; double-click to edit raw marks.
* **Final Grades:** read-only overview (T1–T4 + Final %); sorted by Final % desc by default.
* **Attendance:** week grid per course; **yellow** = today; toggle present/absent; save deltas.
* **Feedback:** single-line preview; click to view the full note; empty = delete on save.
* **Create Report Card:** select a student → generate .docx from template.

---

## Data Model

Main tables created by `DBSetup`:

* `tblStudents(studentId, firstName, lastName, gradeLevel)`
* `tblCourses(courseId, courseCode UNIQUE, courseName, gradeLevel)`
* `tblStudentCourses(studentId, courseId, PRIMARY KEY(studentId, courseId))` *(enrolments)*
* `tblAssignments(assignmentId, courseId, title, maxMarks, term, dueDate)`
* `tblGrades(studentId, assignmentId, markAwarded, PRIMARY KEY(studentId, assignmentId))`
* `tblFinalGrades(studentId, term1..term4, finalGrade)` *(optional cache)*
* `tblAttendance(studentId, courseId, lessonDate, present, PRIMARY KEY(studentId, courseId, lessonDate))`
* `tblFeedback(feedbackId, studentId, courseId, note, entryDate)`
* `tblUsers(userId, fullName, username UNIQUE, passwordHash, role)`

---

## Validation Rules

* **Student:** first/last name **not blank**; gradeLevel **positive integer**.
* **Course:** `courseCode` **valid format** and **unique** per DB; `courseName` **not blank**.
* **Assignment:** title **not blank**; `maxMarks ∈ [0, 1000]`; `term ∈ {1..4}`; `dueDate` not in the past.
* **Grades:** per cell numeric; **0 ≤ mark ≤ maxMarks**.
* **Auth:** username `[A-Za-z0-9_]{4,20}`; password length ≥ 6.

> All validation shows friendly `JOptionPane` messages and prevents the bad value being committed.

---

## Algorithms (short summary)

* **Final Grade (weighted, missing terms allowed)**
  `GradeCalculator.calculateFinalGrade(t1..t4)` – uses weights **12.5%, 25%, 12.5%, 50%**; re-normalises if some terms are missing.

* **Term Average per Student**
  `StudentGradesTableModel.getTermAveragePercent(id, term)` – averages each `(raw/max)*100` over that term’s assignments; `null` if none.

* **Attendance change tracking**
  `AttendanceTableModel` keeps a `changes` map; the Save button writes only the changed cells (faster and cleaner).

* **Report Card merge**
  `ReportCardGenerator` edits DOCX XML parts directly and replaces placeholders like `{{KEY}}`, `${KEY}`, `<<KEY>>`, and `«KEY»`. Also removes `<w:mailMerge>` so Word doesn’t nag.

---

## Report Cards (DOCX)

* Template is bundled at `resources/sgms/data/Report_Card.docx`.
* I support:

  * Simple placeholders: `{{KEY}}`, `${KEY}`, `<<KEY>>`, `«KEY»`
  * Word merge fields: `<w:fldSimple ... MERGEFIELD KEY ...>` and run-based field codes
* Generated file is opened with the OS default (Word) using `Desktop.open`.

> **Please note that you will ned Microsoft Word installed to use this feature.**

---

## Testing & Javadoc

* **Functional tests** I ran include:

  * Duplicate course code → rejected on Save
  * Invalid term (e.g. 5) → error message
  * Mark > max → error message
  * Attendance toggles persist for the selected week only
  * Final % recalculates correctly with missing terms
  * Feedback empty on Save → row is deleted

* **Two input-variable test plans** (examples to document with screenshots):

  * Student `gradeLevel`: normal/extreme/abnormal data
  * Assignment `maxMarks`: normal/extreme/abnormal data

* **API docs**
  `ant javadoc` → open `dist/javadoc/index.html`
  *(Note: escape `<` in Javadoc comments, e.g. `&lt;30` in `Grades.java` to avoid the “malformed HTML” error.)*

---

## Troubleshooting

**`ClassNotFoundException: net.ucanaccess.jdbc.UcanaccessDriver`**
→ Add all UCanAccess JARs to the project Libraries (and to your runtime classpath).

**“Table already exists” when running `DBSetup`**
→ Don’t run `DBSetup` again on an existing database.

**Javadoc fails: “malformed HTML”**
→ Replace `<` with `&lt;` in comments (e.g., `G (&lt;30)`).

**Report Card doesn’t open**
→ Ensure your OS can open `.docx` and `Desktop.isDesktopSupported()` returns true. The file is still created in your temp folder.

**Access locking**
→ Close the `.accdb` in Access while the app is running to avoid file locks.

---

## Acknowledgements

* UCanAccess team for the free JDBC driver.
* Oracle Java Docs for Swing/TableRowSorter/SwingWorker references.
* Microsoft Docs for WordprocessingML structure (DOCX internals).

---

### Quick Start

1. Run `DBSetup` once to create tables.
2. Launch the app; sign in (admin authorises signups/resets).
3. Create courses, add students, enrol them, add assignments, capture marks.
4. Mark attendance and feedback as needed.
5. Generate report cards for selected students.
