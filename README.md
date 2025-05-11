# 📸 BanterBox - Android Social Media App

**BanterBox** is a real-time, image-focused social media app developed in **Kotlin** using **Firebase** and **Supabase**, built as part of a team project. Developed over **4 weeks** using agile sprints, BanterBox enables users to register, create and share image posts, interact with others via likes and comments, and explore user-generated content through a clean, responsive interface.

---

## 🚀 Project Overview

BanterBox was designed and developed to simulate a lightweight social media platform tailored for mobile. It allows users to:

- Register and log in using email or Google accounts.
- Create posts with images and captions.
- Like and comment on posts.
- View and manage their profiles.
- Search users and posts by tags.
- Enjoy a responsive, modern UI with light/dark mode options.

The application reflects core social platform functionality in a clean, material-inspired layout.

---

## 👥 Team Members

| Name                        | Student Number |
|-----------------------------|----------------|
| Sashveer Lakhan Ramjathan   | ST10361554     |
| Blaise Mikka de Gier        | ST10249838     |

---

## 🧩 Features by Sprint

### 🏁 Sprint 1: Project Setup & Authentication (Week 1)

- ✅ Initialized GitHub repository and Actions workflow.
- ✅ Set up Android Studio project using Kotlin and XML.
- ✅ Integrated Firebase Authentication and Firestore.
- ✅ Implemented email and Google sign-in functionality.
- ✅ Created user registration and login UI.
- ✅ Stored user metadata (name, email, profile picture) in Firestore.

---

### 🖼️ Sprint 2: Posting & Image Upload (Week 2)

- ✅ Designed post creation UI.
- ✅ Built bottom navigation for app-wide navigation.
- ✅ Implemented image upload via camera or gallery.
- ✅ Stored uploaded images in **Supabase (banterbox-posts bucket)**.
- ✅ Saved post metadata (caption, image URL, userID, timestamp) in Firestore.
- ✅ Displayed posts in a scrollable RecyclerView feed.

---

### 💬 Sprint 3: Likes, Comments & User Profiles (Week 3)

- ✅ Implemented like system (increment count in Firestore).
- ✅ Built comment functionality with nested Firestore structure.
- ✅ Created profile screen showing user's posts.
- ✅ Enabled profile updates (username, password, profile image).
- ✅ Enhanced post UI with like/comment buttons.
- ✅ Added comment input and scrollable comment section.

---

### 🔍 Sprint 4: Navigation & UI Improvements (Week 4)

- ✅ Implemented user search by username.
- ✅ Enabled post search via hashtags/tags.
- ✅ Finalized bottom navigation with Profile, Home, and Search.
- ✅ Added FAB (Floating Action Button) for new posts.
- ✅ Improved UI using **Material Design Components**.
- ✅ Implemented light/dark mode toggle.
- ✅ Used **SharedPreferences** to persist theme settings.
- ✅ Added pull-to-refresh to update the feed.
- ✅ Ranked posts for relevance using Firestore query logic.

---

## 🧪 Technologies Used

| Layer         | Tools / Libraries                                |
|---------------|--------------------------------------------------|
| Language      | Kotlin                                           |
| UI Framework  | XML, Material Design Components                  |
| Back-end       | Firebase Authentication, Firestore              |
| Media Storage | Supabase Buckets (for images)                   |
| Auth          | Firebase Auth (Email Sign-in)                   |
| Versioning    | Git, GitHub                                      |
| IDE           | Android Studio                                   |

---

## 🧠 What We Learned

Through this project, we gained practical experience in:

- 📲 Mobile app architecture and UI/UX design using Kotlin.
- 🔐 Integrating Firebase Authentication and Firestore databases.
- 🖼️ Working with external storage (Supabase) for media handling.
- 💬 Building interactive features like comments and likes.
- 🎨 Designing responsive, user-friendly Android interfaces.
- 🧪 Testing features in incremental sprints using agile principles.
- ☁️ Managing app state and preferences via SharedPreferences.

---

## 📈 Key Takeaways

- ✅ Full-stack mobile app development requires cohesive design across UI, backend, and cloud services.
- ✅ Firebase + Supabase provides a powerful stack for rapid prototyping.
- ✅ Sprint-based development helps organize deliverables and improve focus.
- ✅ User engagement features (like comments and likes) significantly increase interactivity.

---
