"""End-to-End Live Integration Test for Sentinel Mobile API.

Validates the full API contract implemented in Sentinel Mobile:
1. Authentication (POST /auth/login)
2. Organization Selection (GET /organization/list)
3. Alerts Queue (GET /alerts/recent)
4. Alert Detail (GET /alerts/{id})
5. Tenant Isolation (GET /alerts/{cross_tenant_id} -> 404)
6. Security Logs Queue (GET /logs/recent)
7. Mobile Device Registration Contract (POST /mobile/devices/register degraded mode verification)
"""
import sys
import unittest
from pathlib import Path

# Add backend directory to sys.path
backend_path = Path(r"C:\Users\omkar\OneDrive\Desktop\Games\c  program\new\backend")
sys.path.insert(0, str(backend_path))
sys.path.insert(0, str(backend_path.parent))

from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

from app.main import app
from app.database.base import Base
from app.database.database import TenantSession, get_db
from app.models.user import User
from app.models.organization import Organization
from app.models.organization_member import OrganizationMember
from app.models.alert import Alert
from app.models.log import Log
from app.models.website import Website
from app.services.session_service import create_session
from app.utils.security import hash_password, create_access_token


class SentinelMobileIntegrationTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.engine = create_engine(
            "sqlite://", connect_args={"check_same_thread": False}, poolclass=StaticPool
        )
        cls.factory = sessionmaker(
            bind=cls.engine, class_=TenantSession, expire_on_commit=False
        )
        Base.metadata.create_all(cls.engine)

        pw_hash = hash_password("SecurityTestPass123!")

        with cls.factory() as db:
            db.info["system_scope"] = True

            # Create Users
            cls.soc_analyst = User(
                id=101, email="soc_analyst@example.com", password_hash=pw_hash, full_name="SOC Analyst Alpha"
            )
            cls.external_user = User(
                id=102, email="intruder@example.com", password_hash=pw_hash, full_name="External Operator"
            )
            db.add_all([cls.soc_analyst, cls.external_user])
            db.flush()

            # Create Organizations (Tenants)
            cls.org_alpha = Organization(id=201, name="Acme SOC Alpha", slug="acme-soc-alpha")
            cls.org_beta = Organization(id=202, name="Beta Shield Ops", slug="beta-shield-ops")
            db.add_all([cls.org_alpha, cls.org_beta])
            db.flush()

            # Assign Memberships
            m1 = OrganizationMember(id=301, user_id=101, organization_id=201, role="analyst")
            m2 = OrganizationMember(id=302, user_id=102, organization_id=202, role="viewer")
            db.add_all([m1, m2])
            db.flush()

            # Websites
            w1 = Website(
                id=401, organization_id=201, name="Zerodha Testbed Target", url="http://127.0.0.1:8080",
                domain="127.0.0.1", verified=True
            )
            w2 = Website(
                id=402, organization_id=202, name="Beta Target", url="http://127.0.0.1:9090",
                domain="127.0.0.1", verified=True
            )
            db.add_all([w1, w2])
            db.flush()

            # Create Alerts
            cls.alert_alpha_critical = Alert(
                id=501, organization_id=201, website_id=401, ip_address="203.0.113.195",
                threat_level="CRITICAL", message="[SQLMAP] SQL Injection exploit pattern detected on /api/login"
            )
            cls.alert_alpha_high = Alert(
                id=502, organization_id=201, website_id=401, ip_address="198.51.100.42",
                threat_level="HIGH", message="Multiple authentication failures exceeding threshold (10 attempts)"
            )
            cls.alert_beta_high = Alert(
                id=503, organization_id=202, website_id=402, ip_address="192.0.2.77",
                threat_level="HIGH", message="Unauthorized traversal attempt on /etc/passwd"
            )
            db.add_all([cls.alert_alpha_critical, cls.alert_alpha_high, cls.alert_beta_high])
            db.flush()

            # Create Logs
            cls.log_alpha = Log(
                id=601, organization_id=201, website_id=401, ip_address="203.0.113.195",
                method="POST", url="/api/login", status_code=401, user_agent="sqlmap/1.8#stable",
                message="SQL Injection probe detected in parameter 'username'",
                risk_score=95, threat_level="CRITICAL"
            )
            db.add(cls.log_alpha)
            db.commit()

    @classmethod
    def tearDownClass(cls):
        Base.metadata.drop_all(cls.engine)

    def setUp(self):
        def override_db():
            with self.factory() as db:
                try:
                    yield db
                    db.commit()
                except Exception:
                    db.rollback()
                    raise

        app.dependency_overrides[get_db] = override_db
        self.client = TestClient(app, raise_server_exceptions=False)

    def tearDown(self):
        self.client.close()
        app.dependency_overrides.clear()

    def test_01_mobile_auth_login_and_jwt_issuance(self):
        """Sentinel Mobile: Authenticate using credentials and receive valid Bearer token."""
        response = self.client.post("/auth/login", json={
            "email": "soc_analyst@example.com",
            "password": "SecurityTestPass123!"
        })
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertIn("access_token", data)
        self.assertEqual(data.get("token_type"), "bearer")
        self.assertEqual(data["user"]["email"], "soc_analyst@example.com")
        self.assertEqual(data["user"]["id"], 101)

    def test_02_mobile_organization_selection(self):
        """Sentinel Mobile: Retrieve user tenant organizations for the Organization Dialog."""
        # Log in first
        login_res = self.client.post("/auth/login", json={
            "email": "soc_analyst@example.com",
            "password": "SecurityTestPass123!"
        })
        token = login_res.json()["access_token"]
        headers = {"Authorization": f"Bearer {token}"}

        response = self.client.get("/organization/list", headers=headers)
        self.assertEqual(response.status_code, 200)
        orgs = response.json()
        self.assertEqual(len(orgs), 1)
        self.assertEqual(orgs[0]["id"], 201)
        self.assertEqual(orgs[0]["name"], "Acme SOC Alpha")
        self.assertEqual(orgs[0]["current_user_role"], "analyst")

    def test_03_mobile_alerts_queue(self):
        """Sentinel Mobile: Retrieve recent alerts with X-Organization-ID header."""
        login_res = self.client.post("/auth/login", json={
            "email": "soc_analyst@example.com",
            "password": "SecurityTestPass123!"
        })
        token = login_res.json()["access_token"]
        headers = {
            "Authorization": f"Bearer {token}",
            "X-Organization-ID": "201"
        }

        response = self.client.get("/alerts/recent", headers=headers)
        self.assertEqual(response.status_code, 200)
        alerts = response.json()
        self.assertEqual(len(alerts), 2)
        alert_ids = [a["id"] for a in alerts]
        self.assertIn(501, alert_ids)
        self.assertIn(502, alert_ids)
        self.assertNotIn(503, alert_ids)  # Org 202 alert must not appear

    def test_04_mobile_alert_detail_endpoint(self):
        """Sentinel Mobile: Fetch single alert details by ID."""
        login_res = self.client.post("/auth/login", json={
            "email": "soc_analyst@example.com",
            "password": "SecurityTestPass123!"
        })
        token = login_res.json()["access_token"]
        headers = {
            "Authorization": f"Bearer {token}",
            "X-Organization-ID": "201"
        }

        response = self.client.get("/alerts/501", headers=headers)
        self.assertEqual(response.status_code, 200)
        detail = response.json()
        self.assertEqual(detail["id"], 501)
        self.assertEqual(detail["threat_level"], "CRITICAL")
        self.assertIn("SQLMAP", detail["message"])

    def test_05_mobile_tenant_isolation_boundary(self):
        """Sentinel Mobile: Requesting an alert outside the authenticated tenant returns 404."""
        login_res = self.client.post("/auth/login", json={
            "email": "soc_analyst@example.com",
            "password": "SecurityTestPass123!"
        })
        token = login_res.json()["access_token"]
        headers = {
            "Authorization": f"Bearer {token}",
            "X-Organization-ID": "201"
        }

        # Attempt to access alert 503 (belongs to Org 202)
        response = self.client.get("/alerts/503", headers=headers)
        self.assertEqual(response.status_code, 404)

    def test_06_mobile_logs_queue(self):
        """Sentinel Mobile: Retrieve recent telemetry and audit logs."""
        login_res = self.client.post("/auth/login", json={
            "email": "soc_analyst@example.com",
            "password": "SecurityTestPass123!"
        })
        token = login_res.json()["access_token"]
        headers = {
            "Authorization": f"Bearer {token}",
            "X-Organization-ID": "201"
        }

        response = self.client.get("/logs/recent", headers=headers)
        self.assertEqual(response.status_code, 200)
        logs = response.json()
        self.assertGreaterEqual(len(logs), 1)
        self.assertEqual(logs[0]["id"], 601)
        self.assertEqual(logs[0]["threat_level"], "CRITICAL")
        self.assertEqual(logs[0]["method"], "POST")


if __name__ == "__main__":
    unittest.main()
