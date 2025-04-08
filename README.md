# Photo Gallery Application

**Made by**: Nguyễn Quang Sáng  
**Student's Number**: 22110067

---

## Introduction

This project involves the development of a modern Photo Gallery Application using **Jetpack Compose**. It combines advanced user interaction techniques, gesture handling, and clean architectural practices to deliver a polished and maintainable Android application. The app enables users to browse, view, and manage their photo collections with ease.

---

## Core Features

### 1. Grid-Based Photo Gallery

A responsive grid layout built with `LazyVerticalGrid` displays image thumbnails efficiently. Each photo is fully interactive, allowing users to tap and view the image in full-screen mode.

### 2. Full-Screen Photo Viewer

Users can navigate through photos using **previous** and **next** buttons. This full photo view improves the user experience while reinforcing skills in UI state handling and component reuse.

### 3. Floating Action Button (FAB)

A prominently placed FAB offers quick access to core actions such as uploading photos from the gallery or opening the settings menu. This element enhances usability while showcasing Compose’s floating action pattern.

### 4. Gesture Support

The app incorporates gesture recognition using Compose’s gesture APIs:

- **Swipe**: Move left/right to switch photos in full view
- **Pinch-to-Zoom**: For detailed image inspection -**Press heart icon**: to mark as favorite

---

## Technical Implementation

### Architecture and State Management

The project follows the **Model-View-ViewModel (MVVM)** architecture. It uses `ViewModel` and `LiveData` (or `State`) for reactive UI updates and clean state separation, promoting scalability and code clarity.

### Performance Optimization

- **Lazy loading** of images via Compose's grid system ensures efficient memory usage.
- **Coil** is integrated for asynchronous image loading and caching, resulting in faster rendering and better user experience with large image sets.

### Animations and Responsiveness

Custom animations are used to enhance transitions, image interactions, and feedback for gestures and button presses. The UI is designed to adapt gracefully to different screen sizes and orientations, adhering to modern mobile design standards.

### Optional Advanced Integration

- **Camera Access**: Users can optionally capture new photos directly through the app.
- **Local Storage**: Photo metadata can be stored using **Room**, enabling persistent data management and offline capabilities.
- **Jetpack Compose** – Declarative UI framework
- **ViewModel & LiveData** – For reactive state handling
- **Coil** – Efficient image loading
- **Room (Optional)** – Local database for storing photo metadata

---

## Conclusion

This Photo Gallery Application provides a comprehensive platform for learning modern Android development. It not only demonstrates the practical use of Compose and architectural patterns but also encourages students to explore advanced features like gestures, animations, and real-world device integration.

---
