# AbhiBoT 7.1

Android starter project for the AbhiBoT 7.1 advanced virtual/paper trading app.

## Included
- Premium dark mobile trading UI
- Login screen
- Dashboard
- Markets
- Paper trade screen
- Orders & positions
- Profile
- API/Data Connections
- Secure local API-key storage using Android Keystore
- Twelve Data REST `/price` integration
- Provider symbol input so exact NSE/XNSE symbols can be configured after verifying availability
- APK-ready Gradle project
- No real broker order placement in this starter

## Important: live market data
Twelve Data documents REST at `https://api.twelvedata.com` and WebSocket at `wss://ws.twelvedata.com`. WebSocket subscriptions use a `subscribe` message with a comma-separated symbol list. Availability and real-time coverage depend on the Twelve Data plan and the exact instrument/symbol.

This project currently implements REST price refresh and the UI for WebSocket/live streaming. The production version should move API/broker credentials to a backend rather than shipping long-lived secrets inside the APK.

## Build APK locally
1. Open this folder in Android Studio.
2. Let Gradle sync.
3. Run on a physical Android phone/emulator.
4. Build > Build APK(s).

Command line:
`./gradlew assembleDebug`

The debug APK will be under:
`app/build/outputs/apk/debug/app-debug.apk`

## GitHub Actions
A workflow can be added at `.github/workflows/build-apk.yml` to build the debug/release APK automatically on every push.

## Next production work
- Backend authentication and user accounts
- WebSocket market stream + reconnect/heartbeat
- Exact NSE/XNSE symbol mapping
- Candlestick history and chart
- Option chain provider integration
- Paper order matching engine
- P&L/equity curve
- Broker OAuth/token flow
- Release signing and Play Store AAB
