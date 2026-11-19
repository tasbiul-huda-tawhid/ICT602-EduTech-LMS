"""Run the EduTech LMS in a pseudo-terminal, typing inputs after each prompt.

Usage: python3 pty_drive.py <classpath> <inputs-file> <transcript-out> [java-binary]
The transcript contains exactly what a user would see, including typed input.
"""
import os
import pty
import select
import sys
import time

PROMPT_ENDINGS = (b"Choose: ", b"Choice: ", b"Username: ", b"Password: ", b"Title: ",
                  b"Description: ", b"Capacity: ", b"Course ID: ", b"Choose role: ",
                  b"User ID: ", b"Full name: ", b"Email: ", b"Login username: ",
                  b"Password (minimum 8 characters): ", b"corporate trainee): ")


def read_until_prompt(fd, buf, timeout=20.0):
    """Read output until the screen ends with a known prompt or the program exits."""
    end = time.time() + timeout
    while time.time() < end:
        r, _, _ = select.select([fd], [], [], 0.2)
        if r:
            try:
                chunk = os.read(fd, 4096)
            except OSError:
                return buf, False
            if not chunk:
                return buf, False
            buf += chunk
            if buf.endswith(PROMPT_ENDINGS):
                return buf, True
    return buf, True


def main():
    classpath, inputs_file, out_file = sys.argv[1:4]
    java_bin = sys.argv[4] if len(sys.argv) > 4 else "java"
    inputs = open(inputs_file).read().splitlines()
    pid, fd = pty.fork()
    if pid == 0:
        env = dict(os.environ)
        env.pop("JAVA_TOOL_OPTIONS", None)  # keep the JVM banner out of the screen
        os.execvpe(java_bin, [java_bin, "-cp", classpath, "edutech.Main"], env)
    buf = b""
    for line in inputs:
        buf, alive = read_until_prompt(fd, buf)
        if not alive:
            break
        time.sleep(0.05)
        os.write(fd, (line + "\n").encode())
    # collect the rest of the output
    while True:
        r, _, _ = select.select([fd], [], [], 1.0)
        if not r:
            break
        try:
            chunk = os.read(fd, 4096)
        except OSError:
            break
        if not chunk:
            break
        buf += chunk
    os.waitpid(pid, 0)
    text = buf.decode("utf-8", "replace").replace("\r\n", "\n").replace("\r", "")
    open(out_file, "w").write(text)


if __name__ == "__main__":
    main()
