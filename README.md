# 🍏 Food Expiry Reminder

**A modern Android application designed to help you track food expiration dates, reduce waste, and save money. Built entirely with Kotlin and the latest Android development practices.**

## About This Project

Everyone has forgotten about food in the back of the fridge, only to find it has expired. This application was built to solve that exact problem in a simple and efficient way. This project demonstrates the end-to-end creation of a functional Android app, with a focus on clean code, a great user experience, and a solid, understandable architecture.

## ✨ Key Features

-   📲 **Full Product Management (CRUD)**: Easily add, edit, and delete products from your inventory.
-   🚦 **Visual Status Indicators**: Instantly see a product's status with clear color codes: **Safe** (Green), **Nearing Expiry** (Yellow), and **Expired** (Red).
-   🔔 **Smart & Customizable Notifications**: Get reliable reminders a few days before a product expires. The notification lead time and time of day are fully customizable in the settings.
-   🔍 **Instant Search & Filtering**: Quickly find any product by name or filter your entire list by category (`Dry`, `Wet`, `Frozen`).
-   🎨 **Dynamic Theming**: Enjoy the app in **Light Mode**, **Dark Mode**, or let it sync with your **System** settings for optimal comfort.
-   💾 **Offline-First Local Storage**: All your data is securely saved on your device, ensuring the app is fully functional even without an internet connection.

## 🛠️ Tech Stack & Architecture

This project is built using modern components that are standard in the Android development industry.

| Category              | Technology                                                                                                                                                                                                                               |
| --------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Language**          | [**Kotlin**](https://kotlinlang.org/)                                                                                                                                                                                                     |
| **UI & Design**       | [**Material Design 3**](https://m3.material.io/)<br/>[**AndroidX Libraries**](https://developer.android.com/jetpack/androidx)<br/>[**View Binding**](https://developer.android.com/topic/libraries/view-binding)                                 |
| **Core Components**   | [**RecyclerView**](https://developer.android.com/guide/topics/ui/layout/recyclerview)<br/>[**Activity & Fragment**](https://developer.android.com/guide/components/activities/activity-lifecycle)<br/>[**Preference-KTX**](https://developer.android.com/jetpack/androidx/releases/preference) |
| **Notifications**     | [**AlarmManager**](https://developer.android.com/reference/android/app/AlarmManager) (for precise scheduling)<br/>[**BroadcastReceiver**](https://developer.android.com/guide/components/broadcasts)                                         |
| **Data Storage**      | [**SharedPreferences**](https://developer.android.com/training/data-storage/shared-preferences) (for settings)<br/>[**Gson**](https://github.com/google/gson) (for product data serialization to JSON)          |

## 🚀 Getting Started

Want to run this project on your local machine? Follow these simple steps.

1.  **Clone the repository:**
