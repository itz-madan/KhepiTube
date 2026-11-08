# Security Policy

## Supported versions

Security fixes land on the latest release line only. Older versions do not
receive backported patches.

| Version | Supported |
| ------- | --------- |
| 1.0.x   | ✅ |

Always update to the newest release before reporting a security issue.

## Reporting a vulnerability

**Do not report security vulnerabilities through public GitHub issues.**

Report privately through [GitHub Security Advisories](https://github.com/itz-madan/KhepiTube/security/advisories/new) or contact the maintainers at <khepilabs@gmail.com>.

Please include:

- Type of issue (for example: credential exposure, path traversal, injection).
- Full paths of the source files involved.
- The affected tag, branch, or commit.
- Any configuration required to reproduce the issue.
- Step-by-step reproduction instructions.
- Proof-of-concept or exploit code, if available.
- Impact, including how an attacker might exploit it.

You will receive an acknowledgement within 48 hours and a timeline for a fix.
Please do not disclose the issue publicly until a fix has shipped.

## Verifying release APKs

Official KhepiTube builds are signed with a single release key. Any APK that does
not match official releases is unverified.

Verify a downloaded APK with the Android SDK build tools:

```bash
apksigner verify --print-certs khepitube-release.apk
```

Official distribution channels are the [GitHub Releases page](https://github.com/itz-madan/KhepiTube/releases).
