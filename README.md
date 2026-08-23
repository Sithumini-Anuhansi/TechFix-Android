# TechFix Android

![TechFix Logo](app/src/main/res/drawable-nodpi/img_home.jpg)

**TechFix** — a professional computer and mobile phone repair service with branches in Colombo and Galle. This application streamlines repair bookings, staff management, and stock tracking for a seamless customer experience.

**Current Version:** 2.0 (Stable)

Built with **Java**, **XML layouts**, and **SQLite** (fully offline). Features GPS-based branch mapping, role-based access control, and repair progress tracking.

GitHub: https://github.com/Sithumini-Anuhansi/TechFix-Android

Demo Video: https://drive.google.com/file/d/1KQxS2-IwemlbO3pVl6_1TCKI7OUYGrzC/view?usp=drive_link

## Team Responsibility
Each member in the team was responsible for handling at a UI in the application.

| Member                                | Responsible Module / UI |
|---------------------------------------| --- |
| **Member 1:**<br/>Sithumini Anuhansi  | **Customer Module** (Home, Booking, History, Interactive Maps) |
| **Member 2:**<br/>Pabasara Ranasinghe | **Admin Module** (Staff, Branch, Service, and User Management) |
| **Member 3:**<br/>Binusha Fernando    | **Branch Manager Module** (Branch Dashboard, Inventory Stock Requests) |
| **Member 4:**<br/>Sadeepa Gunarathna  | **Staff/Technician Module** (Repair Tasks, Status Updates, Payment Handling) |

## Features

### 🛡️ Admin (System Owner)
- **Comprehensive CRUD Management:** Create, update, and manage Staff, Branches, Services, and global Category definitions.
- **Inventory Oversight:** Authorize or decline inventory stock requests submitted by branch managers.
- **Business Intelligence:** Monitor system-wide statistics including total revenue, daily earnings, and repair volume.
- **User Role Management:** Control access levels and maintain system integrity.

### 🏢 Branch Manager
- **Branch-Specific Dashboard:** Track repair statuses (Pending, Ongoing, Completed) within their specific branch.
- **Smart Inventory Requests:** Identify stock shortages and submit automated requests for spare parts.
- **Financial Monitoring:** View branch-level revenue and daily transaction summaries.
- **Technician Supervision:** Monitor technician availability and assigned workloads.

### 🛠️ Staff / Technician
- **Personalized Task List:** Access a dedicated list of assigned repairs.
- **Status Lifecycle Management:** Move repairs from `PENDING` through `IN_PROGRESS` to `COMPLETED`.
- **Visual Repair Proof:** Capture and upload high-quality photos of repaired hardware as verification.
- **Payment Handling:** Process and record service payments (Cash/Card) with automated timestamps.

### 👤 Customer
- **Dynamic Repair Booking:** Select services and book appointments with integrated camera support for device diagnosis.
- **Intelligent Branch Locator:** GPS-based automatic assignment to the nearest eligible branch.
- **Real-Time Tracking:** Receive notifications and track repair progress from "Assigned" to "Ready for Pickup".
- **Interactive Mapping:** Locate physical TechFix branches on a Google Maps interface.
- **Self-Service Profile:** Manage account details, contact information, and secure password resets.

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

| Role                 | Email                      | Password      |
|----------------------|----------------------------|---------------|
| **Admin/Owner**      | `admin@techfix.lk`         | `admin123`    |
| **Branch Manager**   | `nimal.manager@techfix.lk` | `manager123`  |
| **Staff/Technician** | `kamal.tech@techfix.lk`    | `tech123`     |
| **Customer**         | `dilini@gmail.com`         | `customer123` |

## Technical Deliverables

This application satisfies the following specialized technical requirements:

- **📍 Locations / Map GPS:** Integrated **Google Maps SDK** to visualize branch locations and **FusedLocationProviderClient** for real-time user positioning. Uses the **Haversine formula** in `LocationHelper` to calculate the nearest branch for automatic repair assignment.
- **🌐 Web Services & Remote Data:** Architected using the **DAO (Data Access Object) Pattern**. While local-first for reliability, the data layer is decoupled and "Web-Ready," allowing for an easy transition to remote REST APIs without modifying the UI.
- **📊 Complex Data Model & Adaptors:** Utilizes advanced **SQL JOINs** to populate complex Java objects (Models). Custom **RecyclerView Adaptors** (e.g., `ImageAdapter`, `SimpleAdapter`) handle multi-type data displays for repair logs and inventory.
- **📷 Camera & Image Integrations:** Implements the modern **ActivityResultLauncher** with the `TakePicture()` contract. Captures high-resolution device photos for diagnosis, saved to internal storage and managed via **FileProvider**.
- **💾 SQLite, Content Providers & Offline:** Features a robust relational schema with 10+ tables using `SQLiteOpenHelper`. A **ContentProvider** (`TechFixProvider`) provides a structured interface to the data, ensuring 100% offline functionality.

## Data Schema & Workflow

The SQLite database `techfix.db` is initialized on the first launch. 

**Repair Status Workflow:**
`PENDING` → `ASSIGNED` → `IN_PROGRESS` → `COMPLETED` / `CANCELLED`.

## Notes

- Passwords are stored in plain text for this coursework demo only.
- No remote backend: the app works fully offline after install.
