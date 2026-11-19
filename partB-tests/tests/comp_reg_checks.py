"""ICT602 A3 Part B: REG-01 (regression replay) and COMP-01 (compatibility) checks.

Usage: python3 comp_reg_checks.py REG-01|COMP-01 <project-src> <work-dir>
Runs the real program in a pseudo-terminal (as a person at a keyboard would), so the
password prompt behaves exactly as in a terminal. Test code only; no source file is changed.
"""
import difflib
import os
import shutil
import subprocess
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
JDK17 = Path(os.environ.get("JDK17_HOME", "/usr/lib/jvm/java-17-openjdk-amd64")) / "bin"
JDK21 = Path("/usr/lib/jvm/java-21-openjdk-amd64/bin")
INPUTS = HERE / "inputs" / "partA_e2e.in"
EXPECTED = HERE / "inputs" / "partA_e2e_expected_session.txt"
checks = 0


def check(statement, ok):
    global checks
    checks += 1
    print(("PASS" if ok else "FAIL") + " CHECK | " + statement, flush=True)
    if not ok:
        print("RESULT: FAIL | checks=%d" % checks)
        sys.exit(1)


def env():
    e = dict(os.environ)
    e.pop("JAVA_TOOL_OPTIONS", None)
    return e


def compile_with(jdk_bin, src, out):
    shutil.rmtree(out, ignore_errors=True)
    cmd = [str(jdk_bin / "javac"), "--release", "17", "-encoding", "UTF-8", "-Xlint:all",
           "-d", str(out), "-sourcepath", str(src), str(Path(src) / "edutech" / "Main.java")]
    p = subprocess.run(cmd, capture_output=True, text=True, env=env())
    print("$ " + " ".join(cmd).replace(str(src), "<src>").replace(str(out), "<out>"))
    print("  javac exit code %d; compiler messages: %s" % (p.returncode, p.stderr.strip() or "none"))
    return p


def version(jdk_bin, tool):
    p = subprocess.run([str(jdk_bin / tool), "-version"], capture_output=True, text=True, env=env())
    return (p.stdout + p.stderr).strip().splitlines()[0]


def pty_session(classpath, java_bin, out_file):
    subprocess.run([sys.executable, str(HERE / "pty_drive.py"), str(classpath), str(INPUTS),
                    str(out_file), str(java_bin)], check=True, env=env())
    return Path(out_file).read_text()


def normalise(text):
    return [line.rstrip() for line in text.strip("\n").split("\n")]


def compare_to_partA(label, session):
    expected = normalise(EXPECTED.read_text())
    actual = normalise(session)
    diff = list(difflib.unified_diff(expected, actual, "Part A recorded session", label, lineterm=""))
    print("  %s: %d lines; differences from the Part A recording: %d"
          % (label, len(actual), len([d for d in diff if d[:1] in "+-" and d[:3] not in ("+++", "---")])))
    for line in diff[:40]:
        print("    " + line)
    return not diff, actual


def masking(label, lines):
    pw = [l for l in lines if l.startswith("Password")]
    hidden = all(l in ("Password:", "Password (minimum 8 characters):") for l in pw)
    print("  %s: %d password prompts; typed text shown after any of them: %s"
          % (label, len(pw), "no" if hidden else "YES"))
    return hidden, len(pw)


def reg01(src, work):
    print("PURPOSE: replay the 80-step Part A end-to-end session on the final build and compare it,")
    print("line by line, with the session recorded on 12 Nov 2026 (any difference = regression).")
    out = work / "reg01_build"
    p = compile_with(JDK21, src, out)
    check("final build compiles cleanly", p.returncode == 0 and not p.stderr.strip())
    session = pty_session(out, JDK21 / "java", work / "REG-01_session.txt")
    same, _ = compare_to_partA("final build replay", session)
    check("replayed session is identical to the Part A recording (80 inputs, all screens)", same)
    print("The full replayed session is saved as REG-01_session.txt beside this log.")


def comp01(src, work):
    print("PURPOSE: the module behaves the same on Java 17 and Java 21, from classes and from a JAR,")
    print("and passwords stay hidden in a real terminal on both. Windows Command Prompt behaviour is")
    print("covered by Kaium's screenshots (Environment 2), not re-run here.")
    print("JDK 17: " + version(JDK17, "java") + " | " + version(JDK17, "javac"))
    print("JDK 21: " + version(JDK21, "java") + " | " + version(JDK21, "javac"))

    print("\n(a) compile with each JDK, targeting Java 17")
    p17 = compile_with(JDK17, src, work / "comp_out17")
    check("JDK 17 javac --release 17 -Xlint:all: exit 0, no warnings",
          p17.returncode == 0 and not p17.stderr.strip())
    p21 = compile_with(JDK21, src, work / "comp_out21")
    check("JDK 21 javac --release 17 -Xlint:all: exit 0, no warnings",
          p21.returncode == 0 and not p21.stderr.strip())

    print("\n(b) same 80-step terminal session on each runtime")
    s17 = pty_session(work / "comp_out21", JDK17 / "java", work / "COMP-01_session_jdk17.txt")
    s21 = pty_session(work / "comp_out21", JDK21 / "java", work / "COMP-01_session_jdk21.txt")
    ok17, lines17 = compare_to_partA("Java 17 runtime", s17)
    ok21, lines21 = compare_to_partA("Java 21 runtime", s21)
    check("Java 17 runtime session matches the reference session exactly", ok17)
    check("Java 21 runtime session matches the reference session exactly", ok21)

    print("\n(c) password masking in a real terminal")
    h17, n17 = masking("Java 17", lines17)
    h21, n21 = masking("Java 21", lines21)
    check("Java 17: every password prompt hides the typed text (%d prompts)" % n17, h17 and n17 > 0)
    check("Java 21: every password prompt hides the typed text (%d prompts)" % n21, h21 and n21 > 0)

    print("\n(d) packaged JAR on each runtime")
    jar = work / "edutech-lms.jar"
    if jar.exists():
        jar.unlink()
    subprocess.run([str(JDK21 / "jar"), "--create", "--file", str(jar), "--main-class", "edutech.Main",
                    "-C", str(work / "comp_out21"), "."], check=True, env=env())
    print("$ jar --create --file edutech-lms.jar --main-class edutech.Main -C <out> .")
    for name, jdk in (("Java 17", JDK17), ("Java 21", JDK21)):
        r = subprocess.run([str(jdk / "java"), "-jar", str(jar)], input="2\n", capture_output=True,
                           text=True, env=env(), timeout=60)
        print("  %s: java -jar edutech-lms.jar < '2' -> exit %d, last line: %s"
              % (name, r.returncode, r.stdout.strip().splitlines()[-1]))
        check("%s runs the JAR: banner shown, clean exit" % name,
              r.returncode == 0 and "EduTech LMS: Secure Login and Course Enrolment" in r.stdout
              and r.stdout.strip().endswith("Goodbye from EduTech LMS."))

    print("\n(e) piped input (no terminal): documented, not a pass/fail item")
    print("  With piped input System.console() is unavailable, so InputHelper.readPassword falls back to")
    print("  readNonEmptyText. Nothing is echoed by a pipe, but an IDE console would show the password.")
    print("  Recorded as risk R-03 / Kaium's SEC-OBS-02.")


def main():
    case, src, work = sys.argv[1], Path(sys.argv[2]).resolve(), Path(sys.argv[3]).resolve()
    work.mkdir(parents=True, exist_ok=True)
    print("CASE_ID: " + case)
    print("SUT: integrated EduTech LMS source (see RUN_HEADER.txt for commit and JDK)")
    {"REG-01": reg01, "COMP-01": comp01}[case](src, work)
    print("RESULT: PASS | checks=%d" % checks)


if __name__ == "__main__":
    main()
