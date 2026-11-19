# Basis path workings (WB-01, WB-02)

Method from the Week 9 lecture: draw the control flow graph, calculate V(G), list the independent paths, design one test per path. V(G) is calculated two ways as a cross-check: V(G) = P + 1 (P = predicate nodes) and V(G) = E - N + 2.

Counting rule used for both methods: a compound condition joined by `||` is one predicate node. Its four simple conditions are covered separately by condition coverage. A call that can end the method with an exception (`orElseThrow`, `courseService.getCourse`) counts as a decision, because control leaves the method on one branch.

## WB-01: `AuthService.login(String username, String plainPassword)` (lines 70 to 91)

| Node | Code | Type |
|---|---|---|
| 1 | `if (username == null \|\| username.trim().isEmpty() \|\| plainPassword == null \|\| plainPassword.isEmpty())` | predicate |
| 2 | `throw IllegalArgumentException("Username and password are required.")` | |
| 3 | `findCredentialByUsername(username).orElseThrow(...)`: is the credential present? | predicate |
| 4 | `throw IllegalArgumentException("Incorrect username or password.")` (unknown user) | |
| 5 | `if (credential.isLocked())` | predicate |
| 6 | `throw IllegalStateException(lockedMessage())` | |
| 7 | `if (!PasswordHasher.matches(...))` | predicate |
| 8 | `credential.recordFailedAttempt(); if (credential.isLocked())` | predicate |
| 9 | `throw IllegalStateException(lockedMessage())` (third failure) | |
| 10 | `throw IllegalArgumentException("Incorrect username or password.")` | |
| 11 | `credential.resetFailedAttempts(); findUserById(...).orElseThrow(...)`: is the user present? | predicate |
| 12 | `throw IllegalStateException("Registered user record is missing.")` | |
| 13 | `return user` | |
| 14 | exit | |

Edges (19): 1-2, 1-3, 3-4, 3-5, 5-6, 5-7, 7-8, 7-11, 8-9, 8-10, 11-12, 11-13, and 2, 4, 6, 9, 10, 12, 13 to 14.

- P = 6 (nodes 1, 3, 5, 7, 8, 11), so V(G) = P + 1 = **7**
- E - N + 2 = 19 - 14 + 2 = **7**

| Path | Route | Test input (WB-01) | Expected |
|---|---|---|---|
| P1 | 1-2-14 | username "   " | "Username and password are required." |
| P2 | 1-3-4-14 | username "nobody" | "Incorrect username or password." |
| P3 | 1-3-5-6-14 | locked account, correct password | "Account locked after 3 incorrect passwords." |
| P4 | 1-3-5-7-8-10-14 | wrong password, failures < 3 | "Incorrect username or password." |
| P5 | 1-3-5-7-8-9-14 | third wrong password | "Account locked after 3 incorrect passwords." |
| P6 | 1-3-5-7-11-13-14 | correct password | user returned (U1 / U2) |
| P7 | 1-3-5-7-11-12-14 | credential without user record (test double) | "Registered user record is missing." |

P7 cannot be reached through `InMemoryUserRepository`, which always saves the credential and the user together, so WB-01 uses a test double that implements `UserRepository`.

Condition coverage of node 1: C1 username null, C2 username blank, C3 password null, C4 password empty. Each is made true once, and all four are false in P2 to P7.

## WB-02: `EnrolmentService.enrol(Learner learner, String courseId)` (lines 48 to 68)

| Node | Code | Type |
|---|---|---|
| 1 | `if (learner == null)` | predicate |
| 2 | `throw IllegalArgumentException("Learner is required.")` | |
| 3 | `courseService.getCourse(courseId)`: is the course resolved? | predicate |
| 4 | exception from `getCourse` ("Course ID is required." / "Unknown course ID: ...") | |
| 5 | `if (course.getStatus() != CourseStatus.PUBLISHED)` | predicate |
| 6 | `throw IllegalStateException("This course is not published.")` | |
| 7 | `if (enrolmentRepository.exists(learnerId, savedCourseId))` | predicate |
| 8 | `throw IllegalStateException("You are already enrolled in this course.")` | |
| 9 | `if (enrolmentRepository.countByCourse(savedCourseId) >= course.getCapacity())` | predicate |
| 10 | `throw IllegalStateException("This course is full.")` | |
| 11 | save and `return enrolment` | |
| 12 | exit | |

Edges (16): 1-2, 1-3, 3-4, 3-5, 5-6, 5-7, 7-8, 7-9, 9-10, 9-11, and 2, 4, 6, 8, 10, 11 to 12.

- P = 5 (nodes 1, 3, 5, 7, 9), so V(G) = P + 1 = **6**
- E - N + 2 = 16 - 12 + 2 = **6**

| Path | Route | Test input (WB-02) | Expected |
|---|---|---|---|
| E1 | 1-2-12 | learner null | "Learner is required." |
| E2 | 1-3-4-12 | course "C7" (unknown) | "Unknown course ID: C7" |
| E3 | 1-3-5-6-12 | draft course | "This course is not published." |
| E4 | 1-3-5-7-8-12 | already enrolled | "You are already enrolled in this course." |
| E5 | 1-3-5-7-9-10-12 | new learner, course full | "This course is full." |
| E6 | 1-3-5-7-9-11-12 | new learner, seat free | enrolment saved and returned |

## Enrolment decision table (ENROL-05)

| Rule | C1 course exists | C2 published | C3 already enrolled | C4 seat free | Action |
|---|---|---|---|---|---|
| R1 | N | - | - | - | "Unknown course ID: ..." (blank ID: "Course ID is required.") |
| R2 | Y | N | - | - | "This course is not published." |
| R3 | Y | Y | Y | Y | "You are already enrolled in this course." |
| R4 | Y | Y | Y | N | "You are already enrolled in this course." (duplicate checked first) |
| R5 | Y | Y | N | N | "This course is full." |
| R6 | Y | Y | N | Y | Enrol and save |

"-" means the condition is not evaluated. Extra rules tested in ENROL-05: R7 null learner, and R8 lower-case ID "c1", which is treated as unknown (OBS-04).
