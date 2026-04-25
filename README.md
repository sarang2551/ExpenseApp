# ExpenseApp (Kotlin + Firebase Realtime DB)

This project is a starter implementation of your personal budget/spending tracker.

## Included features
- Dashboard with:
  - today's red expense bar
  - today's green income bar
  - add transaction bottom sheet
  - scrollable transaction history
  - recurring transactions highlighted with a distinct tint
- Add transaction popup:
  - swipe left/right to toggle money out vs money in
  - category list changes with selected type
  - create new category inline
- Spending overview:
  - filters for category, day, month, year, and money in/out/both
  - dynamic pie chart for filtered transactions
- Settings:
  - export to Google Drive via Android share sheet (CSV payload)
  - add recurring transactions (daily, weekly, monthly, biannual, annual)

## Firebase placeholders
- `app/google-services.json` includes placeholder values.
- Replace with your real Firebase project config before production use.

## Notes
- This first version uses an in-memory repository with Firebase-ready structure/constants.
- Realtime DB rules are included in `app/src/main/res/xml/realtime_database_rules.json` as a personal-project permissive template.
