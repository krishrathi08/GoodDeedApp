# GoodDeed: Full-Stack Volunteer & Event Management Platform

## Overview

GoodDeed is a full-stack native **Android** application connecting volunteers with NGOs, built entirely in **Kotlin** and **Jetpack Compose** using a strict **MVVM** architecture. The app features a **dual-interface** (Volunteer and Organizer) across **20+ screens**, demonstrating the ability to handle complex, role-based mobile development.

### Technical Foundation
I **engineered the backend** utilizing the full **Firebase** suite: **Cloud Firestore** as the real-time database for **4 interconnected collections**, **Firebase Authentication** for secure login, and **Firebase Storage** for all image hosting.

---
## File Structure

The main application code is located in `app/src/main`.

* **`app/src/main/AndroidManifest.xml`**: Defines API keys, permissions, and app characteristics.
* **`eu.tutorials.gooddeedproject/`**: Core Kotlin source code.
    * **`Auth/`**: Centralized logic for authentication state and user profile management.
    * **`home/`**: Contains the UI for the main user-side features (`HomeScreen`, `ExploreScreen`, `CommunityScreen`).
    * **`organizer/`**: Contains the UI/logic for the **Organizer-specific features** (`OrganizerDashboardScreen`, `ManageEventScreen`).
    * **`models/`**: Defines data models including `UserProfile.kt`, `Event.kt`, and `Post.kt`.
    * **`utils/`**: Contains utility functions (e.g., `LocationUtils.kt` for map coordinates).

---
## Key Features & Functions (Technical Accomplishments)

Based on the implemented features:

* **Architectural Excellence**:
    * Implemented **MVVM** with **Kotlin Flows** for reactive data updates.
    * Built **Nested Navigation Graphs** to manage complex routing (e.g., Organizer bottom tabs) and ensure smooth navigation flow.
* **Data Integrity & Real-Time Sync (My Pride)**:
    * Utilized **Firebase Batch Writes** to guarantee **atomic updates** across user and event documents during critical actions (like event registration).
    * Developed a specialized **real-time data flow** using **`Flow.combine`** and a **`tickerFlow`** to automatically move "Upcoming" events to the "Completed" list when their date passes, eliminating the need for manual updates.
* **Advanced Features**:
    * **Gamification Engine**: Implemented a dynamic system that live-queries user participation stats to award **5+ unique badges** in real-time.
    * **Geospatial Discovery**: Integrated the **Google Maps API** into the **Explore Screen** to display event locations as live, clickable markers.
    * **Organizer Management**: Full **CRUD** functionality for event management (Create, Edit, Delete) and **Volunteer Oversight**.

---
## Dependencies

The project utilizes several modern Android development libraries, emphasizing stability and modern best practices:

* **Jetpack Compose**: Core UI (Material 3).
* **Navigation**: `androidx.navigation:navigation-compose`.
* **State/ViewModel**: `androidx.lifecycle:lifecycle-viewmodel-compose` (for stable state management across process death).
* **Image Loading**: **Coil** (for efficient image loading from URLs).
* **APIs**: **Google Maps Compose** (Geospatial functionality).
* **Firebase**: **BOM**, Auth, Firestore, and Storage (full serverless backend).
