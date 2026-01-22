# healixirRx

> Project status:
> Pharmacy is a prototype (PoC) built to explore and demonstrate specific engineering solutions (architecture, paging, caching, DI, service integrations). It is not intended to be a production deployment. Some features may be incomplete, and the data/behaviour may be simplified for demonstration purposes.

## Tech Stack

### Android (Language & Async)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Coroutines](https://img.shields.io/badge/Coroutines-0095D5?logo=kotlin&logoColor=white)
![Flow](https://img.shields.io/badge/Flow-0095D5?logo=kotlin&logoColor=white)

### Architecture
![Repository Pattern](https://img.shields.io/badge/Repository-Pattern-6E40C9?logoColor=white)
![MVVM](https://img.shields.io/badge/MVVM-1F6FEB?logoColor=white)

### UI
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Material Design 3](https://img.shields.io/badge/Material%20Design%203-757575?logo=materialdesign&logoColor=white)

### Dependency Injection & Data
![Hilt](https://img.shields.io/badge/Hilt-34A853?logo=android&logoColor=white)
![Room](https://img.shields.io/badge/Room-3DDC84?logo=android&logoColor=white)
![Paging%203](https://img.shields.io/badge/Paging%203-3DDC84?logo=android&logoColor=white)
![DataStore](https://img.shields.io/badge/DataStore-3DDC84?logo=android&logoColor=white)

### Maps & Location
![Google Maps API](https://img.shields.io/badge/Google%20Maps%20API-4285F4?logo=googlemaps&logoColor=white)
![OpenStreetMap](https://img.shields.io/badge/OpenStreetMap-7EBC6F?logo=openstreetmap&logoColor=white)

### Backend APIs
![OpenFDA API](https://img.shields.io/badge/OpenFDA%20API-0A0A0A?logo=databricks&logoColor=white)
![REST API](https://img.shields.io/badge/REST%20API-005571?logo=fastapi&logoColor=white)

### Backend
![Ktor](https://img.shields.io/badge/Ktor-0095D5?logo=kotlin&logoColor=white)

A separate backend service built with **Ktor** (Kotlin) is available here: https://github.com/goofydoog/pharmacy-backend-ktor.git

### Firebase
![Firebase Authentication](https://img.shields.io/badge/Firebase%20Authentication-FFCA28?logo=firebase&logoColor=black)
![Two--Factor Authentication](https://img.shields.io/badge/Two--Factor%20Authentication-1F6FEB?logo=auth0&logoColor=white)
![Cloud Firestore](https://img.shields.io/badge/Cloud%20Firestore-FFCA28?logo=firebase&logoColor=black)


## Features

### Authentication & Account
- Sign in with **email + password** or **phone number**
- Optional **Two-Factor Authentication (2FA)** enabled by the user
- Profile setup after login (basic personal details)

### Address & Location (TERYT-powered)
- Address picker based on the official Polish **TERYT** registry (TERC / SIMC / ULIC)
- Nationwide coverage of **cities, towns, villages**, with street-level autocomplete for cities
- Fast suggestions while typing (locality selection enables street suggestions where available)
- More details: [docs/teryt-address.md](docs/teryt-address.md)

### Nearby Pharmacies (range + sorting + filters)
- Find pharmacies within a user-defined radius (**1–30 km**) based on the saved profile address
- Sorting options:
  - by **distance** (nearest → farthest / farthest → nearest)
  - by **name** (alphabetical)
- Filter pharmacies by **“open now”** status

### Pharmacy Details
- Pharmacy detail page with:
  - opening hours and basic contact information
  - quick actions: **call**, **send email**, and **navigate** (opens Google Maps navigation)
  - map location view (Google Maps)

### Pharmacy Stock & Add-to-Cart
- View the full **medicine stock** for a selected pharmacy
- Add items directly from pharmacy stock to the cart (quantity selection)

### Drugs Search (OpenFDA-backed)
- Search medicines by **name** or **barcode**
- View medicine details and **availability across pharmacies** (including price per pharmacy)
- Deep link from a medicine result to:
  - the pharmacy details page
  - the pharmacy stock page

### Cart & Checkout (PoC)
- Cart items are **grouped by pharmacy** for clarity when products come from multiple sources
- Edit cart content: change quantities / remove items
- Order summary screen:
  - pre-filled with profile data (editable)
  - payment method selection (prototype-level)
- “Place order” completes a demo flow (shows confirmation and clears the cart)

### Orders History
- Orders screen showing previously placed orders (prototype order tracking)

### Reservation (in progress)
- During checkout (Order Summary step), selected items will be **temporarily reserved**.
- Reserved stock will **not be shown as available to other users** until the reservation expires or the order is completed/cancelled.

## Quick navigation
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Architecture](#architecture)
- [Setup](#setup)
- [Documentation](#documentation)
- [Screenshots](#screenshots)
- [Roadmap](#roadmap)

## Architecture

- **MVVM** with reactive UI state (**Flow/StateFlow**) and **Kotlin Coroutines**
- **Repository pattern** separating data sources:
  - **Local** cache: Room
  - **Remote**: REST API / Firebase (depending on the feature)
- **Paging 3** for scalable lists and efficient data loading
- **DataStore** for persisted preferences and lightweight app state
- UI built with **Jetpack Compose** + Material 3, driven by immutable state

### Key implementation details
- Search and filtering trigger a controlled refresh to avoid double clicks and inconsistent list states
- Location-based pharmacy discovery is based on the user’s saved profile address (TERYT-backed)
- Reservation flow is designed as a temporary stock hold during checkout (in progress)

## Development environment

- **Android Studio:** Otter | 2025.2.1
- **Kotlin:** 2.2.20
- **Android Gradle Plugin (AGP):** 8.7.3
- **Gradle:** 8.9
- **Compile SDK:** 35
- **Min SDK:** 28
- **Target SDK:** 34

## Dataset (TERYT-based)

The app uses official Polish **TERYT** exports to provide a nationwide address dictionary with **full locality coverage** (cities, towns, and even the smallest villages) and street-level autocomplete where applicable.  
Dataset downloads and detailed file descriptions are available in **Releases → Assets**.


