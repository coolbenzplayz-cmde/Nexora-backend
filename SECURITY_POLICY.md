# Security Policy

## Scope
This policy applies to Nexora backend, frontend, mobile clients, infrastructure, CI/CD, and third-party integrations.

## 1. Authentication & Session Security
- All passwords must be hashed using BCrypt/Argon2; no plaintext storage.
- JWT access tokens must be short-lived.
- Refresh tokens must be revocable and rotated.
- OTP must expire quickly and be rate-limited.
- 2FA should be available for privileged accounts.

## 2. Authorization
- Enforce role-based access control (RBAC) server-side.
- Never trust client-side authorization checks.
- Users may only access resources they own unless explicit admin permissions are granted.
- All sensitive admin actions must be auditable.

## 3. API Security
- Require authentication for private endpoints.
- Validate and sanitize all request payloads.
- Apply endpoint rate limiting and anti-abuse throttling.
- Return minimal error details in production.

## 4. Data Protection
- Encrypt sensitive data at rest and in transit.
- Do not log passwords, raw tokens, OTP codes, or payment secrets.
- Use parameterized queries/ORM protections to prevent SQL injection.
- Apply data minimization and retention policies.

## 5. Messaging/Call/Media Security
- Enforce conversation-level access checks.
- Use signed or time-limited URLs for private media.
- Validate upload MIME type, extension, and size.
- Block executable and dangerous file types.

## 6. Payment Security
- Never store PAN/card CVV in Nexora systems.
- Use PCI-compliant payment providers.
- Verify payment webhooks with signatures.
- Monitor for anomalies and trigger fraud workflows.

## 7. Infrastructure Security
- Store secrets in environment variables or secret managers.
- Keep container images minimal and patched.
- Restrict ingress/egress and exposed ports.
- Enforce least-privilege access in cloud resources.

## 8. Monitoring & Incident Response
- Collect logs for auth events, payments, admin actions, and API abuse.
- Alert on suspicious login behavior and fraud signals.
- Maintain incident response runbooks and postmortems.

## 9. Backup & Recovery
- Automate encrypted backups and test restores regularly.
- Define RPO/RTO targets and disaster recovery ownership.

## 10. Compliance & Review
- Review this policy every quarter.
- Run dependency and image vulnerability scans continuously.
- Validate security controls in staging before production rollout.
