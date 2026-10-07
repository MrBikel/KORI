# KORI

A clean Kotlin-based finance and budgeting starter built to help track spending, analyze categories, and monitor budget health.

## Overview

KORI is designed as a simple but polished finance tool for personal budgeting. It focuses on:

- tracking expenses by category
- calculating total monthly spending
- estimating remaining budget
- identifying risk when spending approaches or exceeds the target
- keeping the structure maintainable and easy to extend

## Features

- type-safe Kotlin models
- modular package structure for domain, repository, and analysis logic
- budget summary generation
- category breakdowns for expense analysis
- test coverage for core logic

## Project structure

```text
KORI/
├── src/
│   ├── main/
│   │   └── kotlin/
│   │       └── com/mrbikel/kori/
│   │           ├── Main.kt
│   │           ├── model/
│   │           ├── repository/
│   │           └── service/
│   └── test/
│       └── kotlin/
│           └── com/mrbikel/kori/service/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── .gitignore
├── LICENSE
└── README.md
```

## Quick start

```bash
git clone https://github.com/MrBikel/KORI.git
cd KORI
./gradlew run
```

## Example output

```text
KORI Budget Summary
Monthly budget: $1800.00
Total spent: $1435.00
Remaining: $365.00
Top category: Food - $420.00
Risk level: Moderate
```

## Why this is optimized

This starter uses a clear separation of responsibilities:

- `model` holds the business data
- `repository` manages expense storage
- `service` performs analysis and summary logic
- `Main` keeps the app entry point minimal and readable

This keeps the project scalable and easy to extend for features such as CSV exports, recurring bills, dashboards, or persistence.

## License

This project is licensed under the MIT License.

## Profile tip

To showcase this repo on your GitHub profile:

1. Open your GitHub profile.
2. Click "Customize your pins".
3. Select `KORI` and pin it to your profile.

This makes the project visible for recruiters, collaborators, and visitors.
