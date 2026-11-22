# EduTech LMS: Secure Login and Course Enrolment

A Java console prototype for course publishing and learner enrolment, built as a team project. It demonstrates role-based login and enrolment validation. Data is stored in memory and resets when the program exits.

**My contribution (Tasbiul Huda Tawhid):** I helped integrate the MVC components and designed 15 of the team’s 34 test cases. Our recorded run passed all 34 cases across 236 checks. See [the test suite and evidence](partB-tests/README.md).

**ICT602 Software Engineering · Assessment 3 Part A · Group Sydney 3 · Victorian Institute of Technology (2026)**

This repository contains a **runnable Java console prototype** of the EduTech Learning Management System (LMS). The module uses the Model–View–Controller (MVC) pattern. It demonstrates authentication, role-based menus, course creation and publishing, and learner enrolment. The wider LMS described in our Software Design Specification is **conceptual**, not fully implemented here.

## Requirements

- JDK **17 or newer** (tested with OpenJDK 21 using Java 17 compatibility).
- No external libraries, database, build tool or internet connection.
- Commands below run from the **repository root**.

## Compile and run

```sh
javac --release 17 -encoding UTF-8 -d out -sourcepath src src/edutech/Main.java
java -cp out edutech.Main
```

Optional executable JAR:

```sh
jar --create --file edutech-lms.jar --main-class edutech.Main -C out .
java -jar edutech-lms.jar
```

Use a terminal for hidden password entry. Some IDE consoles may echo typed passwords.

## Demo accounts (test data only)

| Role | Username | Password |
|---|---|---|
| Learner (student) | `alex` | `Learner2026!` |
| Learner (corporate trainee) | `maya` | `Learner2026!` |
| Instructor | `priya` | `Teach2026!` |
| Administrator | `sam` | `Admin2026!` |

These are **fictional demonstration credentials**, not real-user passwords. The application prints them on startup for assessment testing.

## Features to verify

- **Login and role-based access (FR-01, NFR-01):** Sign in as different roles to see their respective menus. Three unsuccessful passwords lock an account for the current run.
- **Account management (FR-13):** The administrator can create and list users. Passwords shorter than eight characters are rejected.
- **Course publishing (FR-02):** The instructor can create a draft and publish it.
- **Learner enrolment (FR-04):** Learners can list published courses, enrol and view their enrolments. Unpublished, duplicate and full-course requests are rejected.
- **Password storage:** Accounts use salted PBKDF2-HMAC-SHA256 hashes. This is implementation evidence for password protection, **not a claim of complete NFR-02 compliance**.

Initial course data: **C1 “Introduction to Java”** is published with capacity **1**; **C2 “Software Testing Basics”** starts as a draft. Enrol Alex in C1, then try Maya to reproduce the full-course rejection. Data is stored **in memory** and resets when the program exits.

## Repository layout

```text
src/edutech/Main.java                Application entry point and MVC wiring
src/edutech/model/                  Domain classes
src/edutech/model/repository/       Repository interfaces and in-memory implementations
src/edutech/model/service/          Authentication, course and enrolment rules
src/edutech/controller/             Console workflow controllers
src/edutech/view/                   Menus and input/output
evidence/                            Example integrated terminal run
partB-tests/                         Assessment 3 Part B test suite and recorded test run
```

## Assessment 3 Part B: testing

The `partB-tests/` folder contains the Part B test suite (34 test cases, standard library only) and the evidence from the recorded run of 18 November 2026: **34 of 34 cases passed, 236 checks**. The source in `src/` is unchanged since commit `80bbd44`, the version tested. To re-run every case:

```sh
cd partB-tests
bash run_all_partB.sh ..
```

See [`partB-tests/README.md`](partB-tests/README.md) for requirements, folder contents and options.

The submitted Software Design Specification contains the full conceptual UML diagrams, selected implementation excerpts and screenshots. This repository provides the executable source for independent verification.

## Group contributions

- **Tasbiul Huda Tawhid:** Group coordination; shared Java classes and `Main`; MVC integration, verification and report assembly.
- **Rahul Subedi:** Learning and enrolment design; course and enrolment implementation and validation.
- **Md Abdul Kaium:** Identity, access and integration design; authentication and account management implementation and testing.

**Academic prototype:** Intended to demonstrate a defined subset of the EduTech LMS requirements, not a production LMS. No open-source license is granted by the repository at present.
