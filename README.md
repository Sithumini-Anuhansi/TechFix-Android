# TechFix Android

**TechFix** — a computer and mobile phone repair shop with branches in Colombo and Galle.

Built with **Java**, **XML layouts**, and **SQLite** (offline). GPS assigns bookings to the nearest eligible branch. The camera stores device photos through FileProvider.

GitHub: https://github.com/Sithumini-Anuhansi/TechFix-Android

## Open in Android Studio

1. Open this folder as an Android project (Gradle wrapper is included).
2. Let Gradle sync. SDK: compile/target **34**, min **24**, Java **17**.
3. Optional: put a Google Maps API key in `app/src/main/res/values/strings.xml` as `google_maps_key` so the map tiles load. Branch markers still work in code without a key.
4. Run on an emulator or device (enable location and camera for the full demo).

## Demo logins

| Role | Email | Password |
| --- | --- | --- |
| Customer | `customer@techfix.lk` | `customer123` |
| Staff | `staff@techfix.lk` | `staff123` |

A seeded completed phone-screen job appears in the customer **Repair history**.

## Features

**Customer**
- Register / log in
- Search repair services (computers and phones) with prices and sample-photo notes
- Book an appointment (device note + optional camera photo)
- Auto-assign to the nearest branch that has an available technician **and** spare-part stock; otherwise pick a branch
- Track open jobs and view history
- Map of Colombo and Galle plus current location

**Staff**
- Manage appointments (status workflow)
- Technicians, spare-part quantities, branches
- Record cash/card payments
- Capture after-repair photos

## Data

SQLite database `techfix.db` is created on first launch (`DatabaseHelper` + `TechFixDao`). A read-only `TechFixProvider` ContentProvider exposes services and branches for the “SQLite, Content Providers & Offline” deliverable.

Statuses: `PENDING` → `ASSIGNED` → `IN_PROGRESS` → `COMPLETED` / `CANCELLED`.

## Group UI ownership

Each Activity under `ui/customer` and `ui/staff` is a separate screen so team members can own at least one UI for the brief.

## Notes

- Passwords are stored in plain text for this coursework demo only.
- No remote backend: the app works fully offline after install.
