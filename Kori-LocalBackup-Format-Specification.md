# Kori Local Backup Format Specification (v1)

## Overview
The Kori Local Backup format is a versioned, integrity-protected JSON envelope designed for secure local data portability. It allows users to export their financial diary data (income, expenses, categories) for backup or transfer purposes.

## Structure
The format is a JSON object with the following top-level keys:
- `version`: String (e.g., "1.0")
- `timestamp`: Long (Epoch milliseconds)
- `sha256`: String (Hexadecimal representation of the payload's SHA-256 hash)
- `payload`: String (The actual serialized JSON string of the user data)

## Security
- Data is hashed using SHA-256 for integrity verification upon restore.
- Sensitive fields (like PINs, user credentials) are explicitly excluded from the export.
