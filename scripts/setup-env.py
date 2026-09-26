
from pathlib import Path
import os
import secrets


def main():
    root = Path(__file__).resolve().parent.parent
    target = root / ".env"
    template = (root / ".env.example").read_text(encoding="utf-8")
    for key in ("DB_PASSWORD", "MYSQL_ROOT_PASSWORD", "JWT_SECRET"):
        template = template.replace(f"{key}=\n", f"{key}={secrets.token_hex(32)}\n")
    try:
        fd = os.open(target, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
    except FileExistsError:
        raise SystemExit(".env already exists; left unchanged. Edit it manually if needed.")
    with os.fdopen(fd, "w", encoding="utf-8") as output:
        output.write(template)
    print("Created private .env with unique local secrets. Do not commit this file.")


if __name__ == "__main__":
    main()
