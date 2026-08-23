# TechFix Android

![TechFix Logo](app/src/main/res/drawable-nodpi/img_home.jpg)

**TechFix** — a professional computer and mobile phone repair service with branches in Colombo and Galle. This application streamlines repair bookings, staff management, and stock tracking for a seamless customer experience.

Built with **Java**, **XML layouts**, and **SQLite** (fully offline). Features GPS-based branch mapping, role-based access control, and repair progress tracking.

GitHub: https://github.com/Sithumini-Anuhansi/TechFix-Android

## Brand Identity
The app uses a premium **Navy Blue & Orange** theme, reflecting the TechFix logo:
- **Primary Color:** Navy Blue (`#003366`)
- **Accent Color:** Orange (`#F7941D`)
- **Secondary Color:** Teal (`#00A99D`)

## Features

### 👤 Customer
- **Register / Log in:** Secure access to personalized services.
- **Search Services:** Browse computer and mobile repair options with real-time pricing.
- **Book Appointments:** Integrated camera support for device diagnosis.
- **Smart Branch Mapping:** Automatic assignment to the nearest eligible branch using GPS.
- **Repair History:** Track current jobs and view past completions.
- **Interactive Map:** View all branch locations relative to your current position.
- **Profile Management:** Update personal details and manage passwords.

### 🛠️ Staff
- **Dashboard:** Personalized overview of repair tasks.
- **Job Management:** Update status from `PENDING` to `COMPLETED`.
- **Inventory & Techs:** Manage spare parts stock and technician availability.
- **Payments:** Record cash/card transactions with timestamping.
- **Repair Proof:** Capture and store photos of repaired devices.

### 🛡️ Admin
- **Full CRUD Management:** Manage Staff, Branches, Services, and Spare Parts.
- **System Oversight:** Monitor all appointments and payments across all branches.
- **Role Control:** Elevated privileges to maintain system integrity.

## Available Services

| Service | Price (LKR) | Category |
| --- | --- | --- |
| Phone Screen Replacement | 14,500.00 | Mobile Phone |
| Laptop Battery Change | 12,000.00 | Computer |
| OS Re-installation | 3,500.00 | Software |
| Keyboard Replacement | 8,500.00 | Computer |
| Charging Port Repair | 5,500.00 | Mobile Phone |

## Tech Stack
- **Language:** Java 17
- **UI:** Material Components, XML Layouts
- **Database:** SQLite (Offline-first approach)
- **APIs:** Google Maps SDK, Android Location Services, FileProvider (Camera)
- **Minimum SDK:** 24 (Android 7.0)

## Getting Started

1. **Clone the Repo:** `git clone https://github.com/Sithumini-Anuhansi/TechFix-Android.git`
2. **Open in Android Studio:** Open the project folder and allow Gradle to sync.
3. **API Key:** Add your Google Maps API key to `res/values/strings.xml` as `google_maps_key`.
4. **Build & Run:** Deploy to an emulator or physical device.

## Demo Logins

| Role | Email | Password |
| --- | --- | --- |
| **Admin** | `admin@techfix.lk` | `admin123` |
| **Staff** | `staff@techfix.lk` | `staff123` |
| **Customer** | `customer@techfix.lk` | `customer123` |

## Data

SQLite database `techfix.db` is created on first launch (`DatabaseHelper` + `TechFixDao`). A read-only `TechFixProvider` ContentProvider exposes services and branches for the “SQLite, Content Providers & Offline” deliverable.

Statuses: `PENDING` → `ASSIGNED` → `IN_PROGRESS` → `COMPLETED` / `CANCELLED`.

## Notes

- Passwords are stored in plain text for this coursework demo only.
- No remote backend: the app works fully offline after install.
