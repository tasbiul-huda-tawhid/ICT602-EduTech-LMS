#!/usr/bin/env bash
# ICT602 A3 Part B (Group Sydney 3): run all 34 test cases against the integrated EduTech LMS.
# Test code only; no project source file is changed.
# Usage: bash run_all_partB.sh /path/to/EduTechLMS   (the folder that contains src/)
# Needs: JDK 21 (default java), JDK 17 for COMP-01, python3.
# Optional: JDK17_HOME=/path/to/jdk17 (default /usr/lib/jvm/java-17-openjdk-amd64)
#           EXECUTED_BY="name and machine" (default: current user and host name)
set -uo pipefail
unset JAVA_TOOL_OPTIONS
export JDK17_HOME="${JDK17_HOME:-/usr/lib/jvm/java-17-openjdk-amd64}"
EXECUTED_BY="${EXECUTED_BY:-$(id -un) on $(hostname)}"
REPO="$(cd "${1:?Give the project root that contains src/}" && pwd)"
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC="$REPO/src"
STAMP="$(date +%Y-%m-%d_%H%M)"
OUT="$HERE/evidence_$STAMP"
BUILD="$HERE/build"
rm -rf "$BUILD"; mkdir -p "$OUT" "$BUILD"

{
  echo "ICT602 A3 Part B test run, Group Sydney 3"
  echo "Run started : $(date '+%Y-%m-%d %H:%M:%S %Z')"
  echo "Executed by : $EXECUTED_BY"
  echo "Project root: $REPO"
  echo "Commit      : $(git -C "$REPO" rev-parse --short HEAD 2>/dev/null || echo 'not a git checkout; source tree fingerprint below')"
  echo "Source SHA-256 (all .java files, sorted): $(cd "$SRC" && find . -name '*.java' | sort | xargs sha256sum | sha256sum | cut -c1-16)"
  echo "Java source files: $(find "$SRC" -name '*.java' | wc -l)"
  echo "OS          : $(. /etc/os-release; echo "$PRETTY_NAME") | kernel $(uname -r) | $(uname -m)"
  echo "CPU         : $(lscpu | sed -n 's/^Model name: *//p') | $(nproc) vCPU"
  echo "Memory      : $(free -h | awk '/^Mem:/{print $2}') RAM"
  echo "Default JDK : $(java -version 2>&1 | head -1) | $(javac -version 2>&1)"
  echo "Second JDK  : $("$JDK17_HOME/bin/java" -version 2>&1 | head -1)"
  echo "Python      : $(python3 --version)"
  echo "Build flags : javac --release 17 -encoding UTF-8 -Xlint:all"
} > "$OUT/RUN_HEADER.txt"
cat "$OUT/RUN_HEADER.txt"

echo; echo "== Compile system under test and test programs =="
javac --release 17 -encoding UTF-8 -Xlint:all -d "$BUILD/sut" -sourcepath "$SRC" "$SRC/edutech/Main.java" || exit 1
javac --release 17 -encoding UTF-8 -Xlint:all -d "$BUILD/rahul" -sourcepath "$SRC:$HERE/tests" "$HERE/tests/PartBRahulCaseRunner.java" || exit 1
javac --release 17 -encoding UTF-8 -d "$BUILD/kaium" -sourcepath "$SRC" "$SRC/edutech/Main.java" "$HERE/tests/KaiumAuthBoundaryAndAuth09Checks.java" || exit 1
javac --release 17 -encoding UTF-8 -Xlint:all -d "$BUILD/tawhid" -sourcepath "$SRC" "$SRC/edutech/Main.java" "$HERE/tests/PartBTawhidChecks.java" || exit 1
javac --release 17 -encoding UTF-8 -Xlint:all -d "$BUILD/system" "$HERE/tests/PartBSystemCases.java" || exit 1
echo "Compiled: system under test + 4 Java test programs (no warnings)"

printf 'case_id,status,evidence_file,checks\n' > "$OUT/results.csv"
fail=0
record() { # case status file
  local n; n=$(grep -oE '(assertions|checks)=[0-9]+' "$OUT/$3" 2>/dev/null | tail -1 | cut -d= -f2)
  [ -z "$n" ] && n=$(grep -c -E '^AUTH-0[79][a-h] \| PASS' "$OUT/$3" 2>/dev/null; true)
  printf '%s,%s,%s,%s\n' "$1" "$2" "$3" "$n" >> "$OUT/results.csv"
  printf '%-10s %s  (%s checks)\n' "$1" "$2" "$n"
  [ "$2" = PASS ] || fail=$((fail + 1))
}
runcase() { # case file command...
  local id=$1 file=$2; shift 2
  if "$@" > "$OUT/$file" 2>&1; then record "$id" PASS "$file"; else record "$id" FAIL "$file"; fi
}

echo; echo "== Authentication (design: Kaium) =="
for t in AUTH-01 AUTH-02 AUTH-03 AUTH-04 AUTH-05 AUTH-06; do
  runcase "$t" "$t.txt" java -cp "$BUILD/system" PartBSystemCases "$t" "$BUILD/sut"
done
if java -cp "$BUILD/kaium" KaiumAuthBoundaryAndAuth09Checks > "$OUT/AUTH-07_AUTH-09_harness.txt" 2>&1; then st=PASS; else st=FAIL; fi

hdr() { printf 'CASE_ID: %s\nSUT: integrated EduTech LMS build (see RUN_HEADER.txt)\nTest program: Kaium'"'"'s KaiumAuthBoundaryAndAuth09Checks.java, unchanged; full output in AUTH-07_AUTH-09_harness.txt\n' "$1"; }
{ hdr AUTH-07; grep '^AUTH-07' "$OUT/AUTH-07_AUTH-09_harness.txt"; } > "$OUT/AUTH-07.txt"
{ hdr AUTH-09; sed -n '/^AUTH-09a/,/^SUMMARY/p' "$OUT/AUTH-07_AUTH-09_harness.txt" | grep -v '^SUMMARY'; } > "$OUT/AUTH-09.txt"

grep -q '^AUTH-07[a-h] | FAIL' "$OUT/AUTH-07.txt" && record AUTH-07 FAIL AUTH-07.txt || record AUTH-07 "$st" AUTH-07.txt
runcase AUTH-08 AUTH-08.txt java -cp "$BUILD/system" PartBSystemCases AUTH-08 "$BUILD/sut"
grep -q '^AUTH-09[a-h] | FAIL' "$OUT/AUTH-09.txt" && record AUTH-09 FAIL AUTH-09.txt || record AUTH-09 "$st" AUTH-09.txt
echo; echo "== Courses and enrolment (design: Rahul; COURSE-07, ENROL-05: Tawhid) =="
for t in COURSE-01 COURSE-02 COURSE-03 COURSE-04 COURSE-05 COURSE-06; do
  runcase "$t" "$t.txt" java -cp "$BUILD/rahul" edutech.devtest.PartBRahulCaseRunner "$t"
done
runcase COURSE-07 COURSE-07.txt java -cp "$BUILD/tawhid" PartBTawhidChecks COURSE-07
for t in ENROL-01 ENROL-02 ENROL-03 ENROL-04; do
  runcase "$t" "$t.txt" java -cp "$BUILD/rahul" edutech.devtest.PartBRahulCaseRunner "$t"
done
runcase ENROL-05 ENROL-05.txt java -cp "$BUILD/tawhid" PartBTawhidChecks ENROL-05

echo; echo "== Integration, regression, white-box and non-functional (Tawhid) =="
for t in INT-01 INT-02 INT-03 INT-04; do
  runcase "$t" "$t.txt" java -cp "$BUILD/system" PartBSystemCases "$t" "$BUILD/sut"
done
runcase REG-01 REG-01.txt python3 "$HERE/tests/comp_reg_checks.py" REG-01 "$SRC" "$OUT/work"
cp "$OUT/work/REG-01_session.txt" "$OUT/" 2>/dev/null
for t in REG-02 REG-03; do
  runcase "$t" "$t.txt" java -cp "$BUILD/system" PartBSystemCases "$t" "$BUILD/sut"
done
for t in WB-01 WB-02 PERF-01; do
  runcase "$t" "$t.txt" java -cp "$BUILD/tawhid" PartBTawhidChecks "$t"
done
runcase USE-01 USE-01.txt java -cp "$BUILD/system" PartBSystemCases USE-01 "$BUILD/sut"
runcase COMP-01 COMP-01.txt python3 "$HERE/tests/comp_reg_checks.py" COMP-01 "$SRC" "$OUT/work"
cp "$OUT/work/COMP-01_session_jdk17.txt" "$OUT/work/COMP-01_session_jdk21.txt" "$OUT/" 2>/dev/null
runcase UAT-01 UAT-01.txt java -cp "$BUILD/system" PartBSystemCases UAT-01 "$BUILD/sut"
rm -rf "$OUT/work"

total=$(($(wc -l < "$OUT/results.csv") - 1))
checks=$(awk -F, 'NR>1{s+=$4} END{print s}' "$OUT/results.csv")
echo
echo "Cases run: $total | Passed: $((total - fail)) | Failed: $fail | Individual checks: $checks" | tee "$OUT/SUMMARY.txt"
echo "Run finished: $(date '+%Y-%m-%d %H:%M:%S %Z')" | tee -a "$OUT/SUMMARY.txt"
echo "Evidence folder: $(basename "$OUT")"
exit $(( fail > 0 ))
