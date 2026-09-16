# Trade Risk Calculator

Android app built with Kotlin + Jetpack Compose.

## Features
- Persistent account balance
- Risk percentage
- Long / Short
- Entry price
- Stop Loss
- Leverage
- Position size
- Position value
- Required margin
- Potential profit for R:R 1:1 through 1:5
- Automatic TP price for each R:R
- Potential loss at Stop Loss

## Open
Open the project folder in Android Studio and let Gradle sync.

## Calculation
Risk Amount = Balance × Risk %
Position Size = Risk Amount / |Entry - Stop|
Position Value = Position Size × Entry
Required Margin = Position Value / Leverage

Leverage changes required margin; it does not multiply the stop-loss dollar risk.
