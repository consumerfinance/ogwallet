🛡️ Security Policy
===================

We take the security of your financial data and the integrity of our open-source code seriously. Because this app handles sensitive information via SMS and MBOX, we are committed to a "Zero-Knowledge" architecture and a transparent security model.

1\. Supported Versions
----------------------

We only provide security updates for the latest stable release. Please ensure you are running the most recent version of the app before reporting a vulnerability.

2\. Reporting a Vulnerability
-----------------------------

**Do not open a public GitHub Issue for security vulnerabilities.**

If you discover a security flaw related to our Biometric locking, local database encryption, or the Regex parsing engine, please follow these steps:

1.  **Private Disclosure:** Email us at security@your-domain.com with the subject line Vulnerability Report: \[Short Description\].

2.  **Details to Include:**

    *   A description of the vulnerability and its potential impact.

    *   Step-by-step instructions to reproduce the issue.

    *   The platform (Android/iOS/Desktop/Web) and version affected.

    *   (Optional) Any suggested fix or mitigation.

3.  **Response Time:** We will acknowledge your report within **48 hours** and provide a timeline for a fix.


3\. Our Security Model
----------------------

To help researchers, here is a summary of our core security assumptions:

*   **Zero-Knowledge:** No transaction data should ever leave the device. Any network call sending personal spend data is considered a high-priority bug.

*   **Hardware-Backed Encryption:** We use **SQLCipher** and the **Android Keystore / iOS Keychain**. The app assumes that if the hardware's "Trusted Execution Environment" (TEE) is compromised, the data is at risk.

*   **Biometric Integrity:** We rely on native system prompts. Any bypass that allows viewing spend data without a successful biometric/PIN challenge is a critical vulnerability.

*   **Submodule Sanitization:** Our Python-based data pipeline is designed to catch "troll" data. Any exploit that allows a malicious user to inject executable code or crash-inducing strings into the app via the rewards submodule is a valid report.


4\. Coordinated Disclosure
--------------------------

We follow the principle of **Coordinated Vulnerability Disclosure**. We ask that you give us a reasonable amount of time to fix the issue before making it public. In return, we promise to:

*   Keep you updated on our progress.

*   Credit you in our \[CHANGELOG.md\] and **TechnoFino** updates (unless you wish to remain anonymous).


### Why this is vital for your project:

*   **Protects Your Users:** If someone finds a way to bypass your Biometric lock, you want to fix it _before_ the public knows about it.

*   **Professionalism:** A SECURITY.md file is a hallmark of a mature open-source project. It tells your contributors that you have a plan for when things go wrong.

*   **Researcher Friendly:** Most "white hat" hackers look for this file first to see if you are open to responsible disclosure.