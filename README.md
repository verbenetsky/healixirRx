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
- Address picker based on the official **Polish TERYT registry** (TERC / SIMC / ULIC)
- Nationwide coverage of **cities, towns, villages**, and (for cities) **street-level autocomplete**
- Fast suggestions while typing (e.g., selecting a city like *Lublin* enables searching and selecting streets available in that city)
(More about it at: )

[Go to Features](#features)
[Go to Tech Stack](#tech-stack)
[Go to Address autocomplete](#address-autocomplete-teryt-powered)


