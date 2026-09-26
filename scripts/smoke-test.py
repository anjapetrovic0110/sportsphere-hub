from datetime import date, timedelta
from pathlib import Path
import json
import os
import re
import secrets
import socket
import subprocess
import tempfile
import time
import urllib.error
import urllib.request

ROOT = Path(__file__).resolve().parent.parent


def free_port():
    with socket.socket() as sock:
        sock.bind(("127.0.0.1", 0))
        return str(sock.getsockname()[1])


def request(url, method="GET", body=None, token=None, expected=200, headers=None):
    headers = dict(headers or {})
    if body is not None:
        headers["Content-Type"] = "application/json"
    if token:
        headers["Authorization"] = "Bearer " + token
    req = urllib.request.Request(url, method=method, headers=headers,
        data=None if body is None else json.dumps(body).encode())
    try:
        response = urllib.request.urlopen(req, timeout=10)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        status = response.status
        raw = response.read()
    if status != expected:
        # Do not print response bodies: they can contain login/reset tokens.
        raise AssertionError(f"{method} {url.split('?')[0]}: expected {expected}, got {status}")
    return json.loads(raw) if raw else None


def check_flows(api, inbox):
    sports = request(api + "/javno/sportovi")
    assert len(sports) == 5, "Fresh schema and demo seed were not loaded"

    def login(username, password="DemoPass1!"):
        return request(api + "/prijava", "POST",
            {"korisnickoIme": username, "lozinka": password})["token"]

    athlete, employee, admin = (login(name) for name in ("sportista", "zaposleni", "admin"))
    request(api + "/sportista/status", token=athlete)
    request(api + "/zaposleni/status", token=employee)
    request(api + "/admin/korisnici", token=admin)
    request(api + "/admin/korisnici", token=athlete, expected=403)
    request(api + "/sportista/status", expected=403)
    print("PASS: fresh database, demo logins and role restrictions", flush=True)

    booking = {"identifikatorResursa": 1, "identifikatorSporta": 1,
        "datum": (date.today() + timedelta(days=14)).isoformat(), "satOd": 22, "satDo": 24}
    request(api + "/sportista/rezervacije", "POST", booking, athlete, expected=400)
    booking.update(satOd=20, satDo=21)
    request(api + "/sportista/rezervacije", "POST", booking, athlete)
    request(api + "/sportista/rezervacije", "POST", booking, athlete, expected=400)
    print("PASS: closing time and overlapping reservations", flush=True)

    def stock():
        return next(item["zaliha"] for item in request(api + "/sportista/prodavnica", token=athlete)
            if item["id"] == 1)
    before = stock()
    order = request(api + "/sportista/porudzbine", "POST",
        {"stavke": [{"identifikatorOpreme": 1, "kolicina": 2}]}, athlete)[0]
    assert stock() == before - 2
    cancel = api + f"/sportista/porudzbine/{order['id']}/otkazivanje"
    request(cancel, "POST", {}, athlete)
    assert stock() == before
    request(cancel, "POST", {}, athlete, expected=400)
    assert stock() == before
    print("PASS: order stock decrement and single cancellation refund", flush=True)

    known = request(api + "/javno/lozinka/zahtev", "POST", {"identifikator": "anja"})
    unknown = request(api + "/javno/lozinka/zahtev", "POST", {"identifikator": "missing"})
    assert known == unknown and "link" not in known
    messages = request(inbox + "/api/v1/messages")["messages"]
    assert len(messages) == 1
    mail = request(inbox + "/api/v1/message/" + messages[0]["ID"])
    assert mail["To"][0]["Address"] == "anja@sportsphere.local"
    match = re.search(r"/nova-lozinka\?token=([A-Za-z0-9_-]+)", mail["Text"])
    assert match, "Reset email did not contain the expected link"
    reset = {"token": match[1], "lozinka": "Changed123!"}
    request(api + "/javno/lozinka/reset", "POST", reset)
    request(api + "/javno/lozinka/reset", "POST", reset, expected=400)
    login("anja", "Changed123!")
    request(api + "/prijava", "POST",
        {"korisnickoIme": "anja", "lozinka": "DemoPass1!"}, expected=400)
    print("PASS: SMTP reset delivery, generic responses, password change and token reuse rejection", flush=True)


def main():
    project = "sportsphere-test-" + secrets.token_hex(4)
    with tempfile.TemporaryDirectory(prefix="sportsphere-test-") as directory:
        temp = Path(directory)
        (temp / "uploads").mkdir()
        env_file = temp / ".env"
        env_file.write_text("\n".join(f"{key}={secrets.token_hex(32)}" for key in
            ("DB_PASSWORD", "MYSQL_ROOT_PASSWORD", "JWT_SECRET")) + "\n")
        env_file.chmod(0o600)
        override = temp / "override.json"
        override.write_text(json.dumps({"services": {"backend": {
            "volumes": [f"{temp / 'uploads'}:/app/uploads"]}}}))
        env = os.environ.copy()
        # Use only generated credentials, even if the calling shell has app secrets.
        for key in ("DB_PASSWORD", "MYSQL_ROOT_PASSWORD", "JWT_SECRET"):
            env.pop(key, None)
        for key in ("MYSQL_PORT", "MAIL_PORT", "MAIL_UI_PORT", "BACKEND_PORT"):
            env[key] = free_port()
        env["CORS_ALLOWED_ORIGIN"] = "http://localhost:4200"
        env["FRONTEND_URL"] = "http://localhost:4200"
        env["MAIL_FROM"] = "noreply@sportsphere.local"
        command = ["docker", "compose", "--env-file", str(env_file), "-p", project,
            "-f", str(ROOT / "compose.yml"), "-f", str(override)]

        def compose(*args, check=True):
            return subprocess.run(command + list(args), cwd=ROOT, env=env, check=check)

        try:
            compose("up", "-d", "--build")
            api = "http://127.0.0.1:" + env["BACKEND_PORT"]
            inbox = "http://127.0.0.1:" + env["MAIL_UI_PORT"]
            for attempt in range(60):
                try:
                    request(api + "/actuator/health")
                    break
                except (OSError, AssertionError):
                    time.sleep(1)
            else:
                raise AssertionError("Backend did not become healthy within 60 seconds")
            check_flows(api, inbox)
            print("All integration checks passed.", flush=True)
        finally:
            compose("down", "--volumes", "--remove-orphans", check=False)


if __name__ == "__main__":
    main()
