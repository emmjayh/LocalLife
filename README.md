# LocalLife

A comprehensive Android app for tracking and visualizing your daily life activities with privacy-focused local data storage.

## Overview

LocalLife is a personal activity tracking application that helps you understand your daily patterns through comprehensive data collection and visualization. The app focuses on privacy by storing all data locally on your device while providing rich insights into your lifestyle.

## Key Features

### Activity Tracking
- **Step Counter**: Monitor daily step counts and activity levels
- **Location History**: Track visited locations and movement patterns
- **Weather Integration**: Record weather conditions for each day
- **Photo Analysis**: Analyze photos to identify activity patterns and locations

### Data Visualization
- **Calendar Heat Map**: Visual representation of activity levels over time
- **Real-time Dashboard**: Live insights into daily metrics and trends
- **Historical Analytics**: Long-term pattern analysis and progress tracking

### Privacy & Storage
- **Local Data Storage**: All personal data remains on your device
- **No Cloud Sync**: Complete privacy with offline-first approach
- **Secure Database**: SQLite database with proper data protection

## Technical Stack

- **Platform**: Android (API 24+)
- **Language**: Java
- **Database**: SQLite with custom ORM
- **UI Framework**: Android Views with Material Design
- **Architecture**: MVVM pattern with LiveData and ViewModel
- **Services**: Background services for continuous tracking

## Project Structure

```
app/src/main/
├── java/com/locallife/
│   ├── activity/          # UI activities and fragments
│   ├── database/          # Database helper and DAOs
│   ├── model/             # Data models and entities
│   ├── service/           # Background services
│   └── utils/             # Utility classes
├── res/                   # Resources (layouts, strings, etc.)
└── AndroidManifest.xml    # App configuration
```

## Development

Built with Android Gradle Plugin 7.4.2 and targeting Android 14 (API 34). The app uses modern Android development practices including:

- View Binding for type-safe UI interactions
- WorkManager for background tasks
- Material Design components
- Lifecycle-aware components