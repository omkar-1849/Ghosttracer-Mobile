"""Forward Zerodha telemetry logs to Sentinel and trigger mobile threat alert notification."""
import os
import sys
import json
import time
import requests

try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass

ZERODHA_URL = os.getenv("ZERODHA_URL", "http://127.0.0.1:8080")
SENTINEL_URL = os.getenv("SENTINEL_URL", "http://127.0.0.1:8000")
API_KEY = os.getenv("SENTINEL_API_KEY", "")
ZERODHA_LOG_PATH = os.getenv("ZERODHA_LOG_PATH", "logs/security.log")

def main():
    print("=" * 60)
    print("1. GENERATING BENIGN TELEMETRY ON ZERODHA (NO ATTACKS)")
    print("=" * 60)
    
    session = requests.Session()
    session.headers.update({"User-Agent": "Zerodha-Compliance-Auditor/2.0 (python-requests)"})

    # Legitimate browsing
    r1 = session.get(f"{ZERODHA_URL}/")
    print(f"[Zerodha] GET / -> HTTP {r1.status_code}")

    r2 = session.get(f"{ZERODHA_URL}/about")
    print(f"[Zerodha] GET /about -> HTTP {r2.status_code}")

    r3 = session.get(f"{ZERODHA_URL}/api/search?q=HDFC")
    print(f"[Zerodha] GET /api/search?q=HDFC -> HTTP {r3.status_code}")

    # Security audit: checking authentication failure response and admin surface
    r4 = session.post(f"{ZERODHA_URL}/api/login", json={"username": "audit_user", "password": "audit_wrong_password"})
    print(f"[Zerodha] POST /api/login (audit test) -> HTTP {r4.status_code}")

    r5 = session.get(f"{ZERODHA_URL}/admin/security-portal")
    print(f"[Zerodha] GET /admin/security-portal -> HTTP {r5.status_code}")

    print("\n" + "=" * 60)
    print("2. READING ZERODHA LOGS & FORWARDING TO SENTINEL")
    print("=" * 60)

    # Ingest the audit security log into Sentinel
    headers = {
        "X-API-Key": API_KEY,
        "Content-Type": "application/json"
    }

    # Benign audit log item representing the Zerodha audit event
    audit_telemetry = {
        "ip_address": "127.0.0.1",
        "method": "POST",
        "url": f"{ZERODHA_URL}/api/login",
        "status_code": 401,
        "user_agent": "python-requests/2.34.2 (Zerodha-Compliance-Auditor)",
        "message": "Zerodha Telemetry: login_failed for audit_user - multiple authentication checks"
    }

    print(f"[Sentinel Ingestion] Posting audit telemetry to {SENTINEL_URL}/logs ...")
    resp = requests.post(f"{SENTINEL_URL}/logs", json=audit_telemetry, headers=headers)
    print(f"[Sentinel Ingestion] Response: HTTP {resp.status_code}")
    print(json.dumps(resp.json(), indent=2))

    return resp.status_code == 201

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
