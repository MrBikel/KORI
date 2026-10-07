# KORI

KORI is a Kotlin-powered personal finance dashboard designed to help you track money, monitor spending, and stay on top of your monthly budget with a clean, portfolio-ready structure.

## Why this project

This repository was built as a polished, production-style starter for a finance tracker. It balances simplicity and maintainability, making it ideal for a portfolio project, personal budgeting assistant, or base for a richer financial application.

## Features

- monthly budget tracking
- expense and income classification
- category-based spending summaries
- risk level detection for overspending
- clean, modular Kotlin architecture
- test coverage for core financial logic

## Project structure

```text
KORI/
├── src/
│   ├── main/
│   │   └── kotlin/
│   │       └── com/mrbikel/kori/
│   │           ├── Main.kt
│   │           ├── model/
│   │           │   ├── FinancialTransaction.kt
│   │           │   └── BudgetSummary.kt
│   │           ├── repository/
│   │           │   └── FinanceRepository.kt
│   │           └── service/
│   │               └── FinanceService.kt
│   └── test/
│       └── kotlin/
│           └── com/mrbikel/kori/service/
│               └── FinanceServiceTest.kt
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── .gitignore
├── LICENSE
└── README.md
```

## Run locally

Make sure you have Java 17+ and Gradle installed:

```bash
gradle run
```

If your machine already has the Gradle wrapper configured later, this can also be run with:

```bash
./gradlew run
```

## Example output

```text
KORI Finance Dashboard
Month: 2026-10
Monthly budget: $4,200.00
Total income: $5,000.00
Total expenses: $3,470.00
Net: $1,530.00
Remaining: $730.00
Risk level: Healthy

Top spending category: Housing - $1,250.00

Category breakdown:
- Housing: $1,250.00
- Food: $620.00
- Transport: $370.00
- Utilities: $240.00
- Lifestyle: $190.00
```

## Architecture

This project follows a simple but scalable design:

- `model` defines the domain objects
- `repository` stores and retrieves financial records
- `service` performs the budget logic and summaries
- `Main` keeps the app entry point small and readable

## Profile optimization

To showcase this on GitHub, pin the repo from your profile:

1. Go to your GitHub profile.
2. Click "Customize your pins".
3. Select `KORI`.
4. Pin it to the top of your profile.

This keeps the project visible to recruiters and collaborators.

## License

This project is licensed under the MIT License.
