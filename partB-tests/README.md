# Assessment 3 Part B: test suite and evidence

**ICT602 Software Engineering · Group Sydney 3 · Victorian Institute of Technology (2026)**

This folder holds the Part B test suite for the EduTech LMS Secure Login and Course Enrolment module, together with the recorded run reported in the group's Software Test Plan. It is deliverable TD-04 in that plan.

The code under test is the `src/` folder of this repository. It is unchanged since commit `80bbd44`, the version recorded in the test plan. This folder only adds test code and evidence.

## Recorded result

Run of 18 November 2026 (Environment 1: Ubuntu 24.04.5, OpenJDK 21.0.12.1 and 17.0.21.0.3):

- **34 of 34 cases passed, 236 checks, 0 open defects**
- 9 observations recorded (OBS-01 to OBS-09)
- full run time 3 minutes 12 seconds

The run was executed by Tawhid in an AI-assisted cloud test environment, as declared in the test plan (Section 4 and Appendix F). Every case was cross-reviewed and accepted by a member who did not design it.

## Contents

| Path | What it is |
|---|---|
| `run_all_partB.sh` | Compiles the module and the test programs, runs all 34 cases and writes a new `evidence_<date_time>/` folder |
| `tests/PartBRahulCaseRunner.java` | Rahul: COURSE-01 to 06, ENROL-01 to 04 |
| `tests/KaiumAuthBoundaryAndAuth09Checks.java` | Kaium: AUTH-07, AUTH-09 |
| `tests/PartBSystemCases.java` | Runs the real program as a separate process: AUTH-01 to 06 and AUTH-08 (Kaium's designs), INT-01 to 04, REG-02, REG-03, USE-01, UAT-01 |
| `tests/PartBTawhidChecks.java` | COURSE-07, ENROL-05, WB-01, WB-02, PERF-01 |
| `tests/comp_reg_checks.py`, `tests/pty_drive.py` | REG-01 and COMP-01, which need a real terminal |
| `tests/inputs/` | The Part A end-to-end inputs and the recorded session REG-01 compares against |
| `evidence_2026-11-18_1744/` | The recorded run (TD-05): `RUN_HEADER.txt`, `results.csv`, `SUMMARY.txt`, one log per case, terminal recording |
| `register/` | Test case register (34 cases, 19 fields), observation log and basis path workings |
| `screenshots/` | Figures D1 to D11 of the test plan |

The tests use the Java standard library only (no JUnit, Maven or Gradle). All test data is synthetic.

## How to run

Linux or macOS, with JDK 21 as the default `java`, a JDK 17 for COMP-01, and Python 3:

```sh
git clone https://github.com/tasbiul-huda-tawhid/ICT602-EduTech-LMS.git
cd ICT602-EduTech-LMS/partB-tests
bash run_all_partB.sh ..
```

Two optional settings:

- `JDK17_HOME` gives the JDK 17 location if it is not `/usr/lib/jvm/java-17-openjdk-amd64` (on Arch Linux, for example, `JDK17_HOME=/usr/lib/jvm/java-17-openjdk`).
- `EXECUTED_BY` sets the name written to the run header. The default is the current user and host name.

Each run writes a new evidence folder and a `build/` folder here. Both are ignored by git, so the recorded run stays as submitted.

To run the module by hand on any OS with JDK 17 or later, use the commands in the main README from the repository root.

## Changes to members' test code

`PartBRahulCaseRunner.java` printed "not the final integrated group build" regardless of the source it ran against. That one output label now points to `RUN_HEADER.txt`; no test logic was changed. Kaium's program is unchanged.

After the recorded run, the runner was changed so that the JDK 17 location and the "Executed by" label can be set as described above. The test cases and checks are unchanged.
