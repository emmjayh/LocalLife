# LocalLife - Personal Life Calendar App

## Project Overview
LocalLife is a personal analytics app that automatically gathers data from your device and public sources (no logins required) to create a comprehensive "life calendar" visualization. It combines device sensors, local data, and public APIs to paint a picture of your life journey.

## Core Features

### Automatic Data Collection (No Login Required)

**Device Sensors & Local Data**
- **Step Counter**: Native pedometer API (iOS: CoreMotion, Android: SensorManager)
- **Screen Time**: Device usage statistics API
- **Location**: Background location tracking for places visited
- **Photos**: EXIF data from camera roll (dates, locations, camera settings)
- **Device Activity**: App usage patterns, phone unlock frequency
- **Battery**: Charging patterns, battery health over time
- **Network**: WiFi networks connected, data usage patterns
- **Storage**: Device storage patterns, media creation rates

**Public APIs (No Auth)**
- **Weather**: Historical weather for your location (OpenWeatherMap, NOAA)
- **Air Quality**: AQI data for your locations (AirVisual, PurpleAir)
- **Daylight**: Sunrise/sunset times, daylight hours (Sunrise-Sunset API)
- **Moon Phases**: Lunar calendar data
- **Public Holidays**: Local holidays and observances
- **News Headlines**: Major events on specific dates (NewsAPI free tier)
- **Earthquake Data**: Seismic activity in your area (USGS)
- **UV Index**: Historical UV exposure data

### Visualization Modes
1. **Life Calendar Grid**: Each cell colored by activity level, weather, mood
2. **Environmental Timeline**: Your life overlaid with weather, air quality, daylight
3. **Activity Heatmaps**: Movement patterns, active hours, step goals
4. **Photo Memory Map**: Geographic visualization of photo locations
5. **Correlation Dashboard**: How weather/environment affects your activity

## Technical Architecture

### Frontend
- **Framework**: React Native (TypeScript)
- **State Management**: Zustand for simplicity
- **Local Database**: WatermelonDB for performance
- **Charts**: Victory Native or React Native SVG Charts
- **Maps**: React Native Maps

### Data Processing
- **Background Tasks**: React Native Background Fetch
- **Location**: React Native Background Geolocation
- **Sensors**: React Native Sensors
- **Photo Access**: React Native Camera Roll
- **Device Info**: React Native Device Info

## Data Schema

### Core Models
```typescript
// DayRecord - Core unit of data
interface DayRecord {
  id: string;
  date: Date;
  deviceMetrics: {
    steps: number;
    screenTime: number;
    unlocks: number;
    appsUsed: string[];
    batteryHealth: number;
    photosToken: number;
  };
  locationData: {
    places: Place[];
    distance: number;
    homeTime: number;
    workTime: number;
  };
  environmental: {
    weather: WeatherData;
    airQuality: number;
    uvIndex: number;
    daylight: { sunrise: Date; sunset: Date; hours: number };
    moonPhase: string;
  };
  computed: {
    activityScore: number;
    environmentScore: number;
    overallScore: number;
  };
}

// Place - Location visited
interface Place {
  latitude: number;
  longitude: number;
  arrivalTime: Date;
  departureTime: Date;
  address?: string;
  category?: string; // inferred from location
}

// WeatherData
interface WeatherData {
  temp: { min: number; max: number; avg: number };
  condition: string;
  humidity: number;
  pressure: number;
  windSpeed: number;
}
```

## No-Login API Integration

### Weather Data
```javascript
// OpenWeatherMap - Free tier, no auth required for basic calls
const WEATHER_API = 'https://api.openweathermap.org/data/2.5/weather';
// Use free tier with rate limiting

// NOAA - Completely free, no auth
const NOAA_API = 'https://api.weather.gov/points/{lat},{lon}';
```

### Environmental Data
```javascript
// Air Quality - OpenAQ (free, no auth)
const AIR_QUALITY_API = 'https://api.openaq.org/v2/measurements';

// UV Index - OpenUV (free tier available)
const UV_API = 'https://api.openuv.io/api/v1/uv';

// Sunrise/Sunset - No auth required
const SUNRISE_API = 'https://api.sunrise-sunset.org/json';
```

### Device APIs (iOS Example)
```swift
// CoreMotion - No permissions needed for step count
CMPedometer().queryPedometerData(from: startDate, to: endDate)

// Photos - Read-only access to metadata
PHPhotoLibrary.requestAuthorization(for: .readWrite)

// Screen Time - Available through iOS 12+ API
// Location - Background tracking with user permission
```

## Privacy & Permissions

### Required Permissions
- **Location**: For place tracking and weather data
- **Photos**: Read-only access to EXIF data
- **Motion & Fitness**: For step counting
- **Notifications**: For daily summary reminders

### Privacy Features
- All data stored locally on device
- No account creation required
- No data leaves device except for public API calls
- Export all data anytime
- Clear data with one tap

## Development Phases

### Phase 1: Core Infrastructure (Week 1-2)
- Basic app structure with React Native
- Local database setup
- Permission request flows
- Basic calendar grid view

### Phase 2: Device Data (Week 3-4)
- Implement step counter
- Photos metadata extraction
- Location tracking setup
- Screen time tracking

### Phase 3: Public APIs (Week 5-6)
- Weather data integration
- Air quality monitoring
- Sunrise/sunset calculations
- Historical data backfill

### Phase 4: Visualizations (Week 7-8)
- Calendar view with color coding
- Statistics dashboard
- Activity heatmaps
- Correlation insights

## Key Implementation Details

### Background Processing
```javascript
// React Native Background Fetch
BackgroundFetch.configure({
  minimumFetchInterval: 15, // minutes
  stopOnTerminate: false,
  startOnBoot: true
}, async (taskId) => {
  // Collect sensor data
  // Fetch weather for current location
  // Process and store in database
  BackgroundFetch.finish(taskId);
});
```

### Data Collection Strategy
1. **Real-time**: Step count, location changes
2. **Hourly**: Weather, air quality, screen time
3. **Daily**: Photo analysis, battery stats, app usage
4. **Weekly**: Data aggregation, pattern analysis

### Performance Optimization
- Lazy load calendar cells
- Cache computed values
- Aggregate old data (daily → weekly → monthly)
- Limit API calls with smart caching
- Use device idle time for processing

## Example User Flow
1. User installs app
2. Grants permissions (location, photos, motion)
3. App starts collecting device data immediately
4. Fetches weather/environmental data for current location
5. User sees today's data immediately
6. Historical data fills in over next 24-48 hours
7. Patterns and insights emerge after 1 week of use

## Build Tools Available in Termux
- **Java**: OpenJDK 17
- **Android tools**: aapt, aapt2, d8, apksigner
- **Additional utilities**: zip, android-tools

### APK Build Process
The `build_apk.sh` script handles:
1. Compiles Java to .class files
2. Converts to DEX format using d8
3. Packages resources with aapt
4. Creates and signs the APK