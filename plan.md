# LocalLife - Step-by-Step Coding Instructions

## Project Setup (Day 1)

### 1. Initialize React Native Project
```bash
npx react-native init LocalLife --template react-native-template-typescript
cd LocalLife
```

### 2. Install Core Dependencies
```bash
# Navigation
npm install @react-navigation/native @react-navigation/bottom-tabs @react-navigation/stack
npm install react-native-screens react-native-safe-area-context

# State Management & Database
npm install zustand
npm install @nozbe/watermelondb @nozbe/with-observables

# Device APIs
npm install react-native-background-fetch
npm install react-native-background-geolocation
npm install @react-native-community/geolocation
npm install react-native-device-info
npm install @react-native-camera-roll/camera-roll

# UI Components
npm install react-native-calendars
npm install react-native-svg react-native-svg-charts
npm install react-native-linear-gradient

# Utilities
npm install date-fns
npm install axios
```

### 3. iOS Setup
```bash
cd ios && pod install
```

Add to `Info.plist`:
```xml
<key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
<string>LocalLife tracks your locations to create your life map</string>
<key>NSLocationWhenInUseUsageDescription</key>
<string>LocalLife needs location to show weather and places</string>
<key>NSMotionUsageDescription</key>
<string>LocalLife counts your steps to track activity</string>
<key>NSPhotoLibraryUsageDescription</key>
<string>LocalLife analyzes photo dates and locations for your timeline</string>
```

## Core Implementation (Days 2-3)

### 4. Database Schema Setup
Create `src/database/schema.ts`:
```typescript
import { appSchema, tableSchema } from '@nozbe/watermelondb'

export default appSchema({
  version: 1,
  tables: [
    tableSchema({
      name: 'day_records',
      columns: [
        { name: 'date', type: 'string', isIndexed: true },
        { name: 'steps', type: 'number' },
        { name: 'screen_time', type: 'number' },
        { name: 'places_visited', type: 'string' }, // JSON
        { name: 'weather_data', type: 'string' }, // JSON
        { name: 'photos_count', type: 'number' },
        { name: 'activity_score', type: 'number' },
        { name: 'created_at', type: 'number' },
        { name: 'updated_at', type: 'number' }
      ]
    }),
    tableSchema({
      name: 'locations',
      columns: [
        { name: 'latitude', type: 'number' },
        { name: 'longitude', type: 'number' },
        { name: 'timestamp', type: 'number', isIndexed: true },
        { name: 'accuracy', type: 'number' },
        { name: 'day_record_id', type: 'string', isIndexed: true }
      ]
    })
  ]
})
```

### 5. State Management
Create `src/store/useDataStore.ts`:
```typescript
import { create } from 'zustand'

interface DataStore {
  currentDate: Date
  todayStats: {
    steps: number
    screenTime: number
    places: number
  }
  isTracking: boolean
  setCurrentDate: (date: Date) => void
  updateTodayStats: (stats: Partial<DataStore['todayStats']>) => void
  toggleTracking: () => void
}

export const useDataStore = create<DataStore>((set) => ({
  currentDate: new Date(),
  todayStats: {
    steps: 0,
    screenTime: 0,
    places: 0
  },
  isTracking: true,
  setCurrentDate: (date) => set({ currentDate: date }),
  updateTodayStats: (stats) => set((state) => ({
    todayStats: { ...state.todayStats, ...stats }
  })),
  toggleTracking: () => set((state) => ({ isTracking: !state.isTracking }))
}))
```

## Device Data Collection (Days 4-5)

### 6. Step Counter Service
Create `src/services/StepCounterService.ts`:
```typescript
import { NativeModules, NativeEventEmitter } from 'react-native'

class StepCounterService {
  private pedometer: any
  private eventEmitter: NativeEventEmitter

  constructor() {
    // For iOS, use CoreMotion
    this.pedometer = NativeModules.CMPedometer
    this.eventEmitter = new NativeEventEmitter(this.pedometer)
  }

  async getTodaySteps(): Promise<number> {
    const start = new Date()
    start.setHours(0, 0, 0, 0)
    
    try {
      const data = await this.pedometer.queryPedometerData(
        start.getTime(),
        new Date().getTime()
      )
      return data.numberOfSteps || 0
    } catch (error) {
      console.error('Error getting steps:', error)
      return 0
    }
  }

  startRealtimeStepCounting(callback: (steps: number) => void) {
    return this.eventEmitter.addListener('pedometerDataDidUpdate', (data) => {
      callback(data.numberOfSteps)
    })
  }
}

export default new StepCounterService()
```

### 7. Location Tracking
Create `src/services/LocationService.ts`:
```typescript
import BackgroundGeolocation from 'react-native-background-geolocation'

class LocationService {
  async configure() {
    BackgroundGeolocation.ready({
      desiredAccuracy: BackgroundGeolocation.DESIRED_ACCURACY_HIGH,
      distanceFilter: 50, // meters
      stopTimeout: 5, // minutes
      debug: false,
      logLevel: BackgroundGeolocation.LOG_LEVEL_VERBOSE,
      stopOnTerminate: false,
      startOnBoot: true,
      batchSync: true,
      maxBatchSize: 20,
    }, (state) => {
      console.log('BackgroundGeolocation configured')
      if (!state.enabled) {
        BackgroundGeolocation.start()
      }
    })
  }

  onLocation(callback: (location: any) => void) {
    return BackgroundGeolocation.onLocation(callback)
  }

  async getCurrentLocation() {
    return BackgroundGeolocation.getCurrentPosition({
      timeout: 30,
      maximumAge: 5000,
      desiredAccuracy: 10
    })
  }
}

export default new LocationService()
```

### 8. Weather API Service
Create `src/services/WeatherService.ts`:
```typescript
import axios from 'axios'

class WeatherService {
  private cache = new Map()

  async getWeatherForLocation(lat: number, lon: number): Promise<any> {
    const cacheKey = `${lat.toFixed(2)},${lon.toFixed(2)}-${new Date().toDateString()}`
    
    if (this.cache.has(cacheKey)) {
      return this.cache.get(cacheKey)
    }

    try {
      // Using weather.gov (US) or open-meteo (global) - no API key needed
      const response = await axios.get(
        `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current_weather=true&daily=temperature_2m_max,temperature_2m_min,precipitation_sum&timezone=auto`
      )
      
      const weatherData = {
        temperature: response.data.current_weather.temperature,
        condition: this.getConditionFromCode(response.data.current_weather.weathercode),
        maxTemp: response.data.daily.temperature_2m_max[0],
        minTemp: response.data.daily.temperature_2m_min[0],
        precipitation: response.data.daily.precipitation_sum[0]
      }
      
      this.cache.set(cacheKey, weatherData)
      return weatherData
    } catch (error) {
      console.error('Weather fetch error:', error)
      return null
    }
  }

  private getConditionFromCode(code: number): string {
    // WMO Weather interpretation codes
    const conditions: { [key: number]: string } = {
      0: 'Clear',
      1: 'Mainly Clear',
      2: 'Partly Cloudy',
      3: 'Overcast',
      45: 'Foggy',
      48: 'Depositing Rime Fog',
      51: 'Light Drizzle',
      61: 'Light Rain',
      71: 'Light Snow',
      95: 'Thunderstorm'
    }
    return conditions[code] || 'Unknown'
  }
}

export default new WeatherService()
```

## UI Implementation (Days 6-7)

### 9. Calendar View Component
Create `src/screens/CalendarScreen.tsx`:
```typescript
import React, { useEffect, useState } from 'react'
import { View, StyleSheet } from 'react-native'
import { Calendar } from 'react-native-calendars'
import { useDataStore } from '../store/useDataStore'
import { database } from '../database'

export const CalendarScreen = () => {
  const [markedDates, setMarkedDates] = useState({})
  const { setCurrentDate } = useDataStore()

  useEffect(() => {
    loadMonthData()
  }, [])

  const loadMonthData = async () => {
    // Query database for this month's data
    const records = await database.collections
      .get('day_records')
      .query()
      .fetch()

    const marked = records.reduce((acc, record) => {
      const score = record.activityScore
      const color = getColorForScore(score)
      
      acc[record.date] = {
        selected: true,
        selectedColor: color,
        customStyles: {
          container: {
            backgroundColor: color,
            borderRadius: 6
          },
          text: {
            color: 'white'
          }
        }
      }
      return acc
    }, {})

    setMarkedDates(marked)
  }

  const getColorForScore = (score: number) => {
    // 0-100 score to color gradient
    if (score > 80) return '#22c55e'
    if (score > 60) return '#84cc16'
    if (score > 40) return '#eab308'
    if (score > 20) return '#f97316'
    return '#ef4444'
  }

  return (
    <View style={styles.container}>
      <Calendar
        markedDates={markedDates}
        markingType="custom"
        onDayPress={(day) => {
          setCurrentDate(new Date(day.dateString))
          // Navigate to day detail
        }}
        theme={{
          backgroundColor: '#ffffff',
          calendarBackground: '#ffffff',
          textSectionTitleColor: '#b6c1cd',
          selectedDayBackgroundColor: '#00adf5',
          selectedDayTextColor: '#ffffff',
          todayTextColor: '#00adf5',
          dayTextColor: '#2d4150'
        }}
      />
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5'
  }
})
```

### 10. Background Data Collection
Create `src/services/BackgroundTaskService.ts`:
```typescript
import BackgroundFetch from 'react-native-background-fetch'
import StepCounterService from './StepCounterService'
import LocationService from './LocationService'
import WeatherService from './WeatherService'
import { database } from '../database'

class BackgroundTaskService {
  async configure() {
    BackgroundFetch.configure({
      minimumFetchInterval: 15, // 15 minutes
      forceAlarmManager: false,
      stopOnTerminate: false,
      startOnBoot: true,
      enableHeadless: true
    }, async (taskId) => {
      console.log('[BackgroundFetch] Task running')
      await this.performBackgroundWork()
      BackgroundFetch.finish(taskId)
    }, (error) => {
      console.error('[BackgroundFetch] Failed to configure:', error)
    })
  }

  private async performBackgroundWork() {
    try {
      // Get current data
      const steps = await StepCounterService.getTodaySteps()
      const location = await LocationService.getCurrentLocation()
      const weather = await WeatherService.getWeatherForLocation(
        location.coords.latitude,
        location.coords.longitude
      )

      // Update today's record
      const today = new Date().toISOString().split('T')[0]
      await database.action(async () => {
        const existing = await database.collections
          .get('day_records')
          .query(Q.where('date', today))
          .fetch()

        if (existing.length > 0) {
          await existing[0].update((record) => {
            record.steps = steps
            record.weatherData = JSON.stringify(weather)
            record.updatedAt = Date.now()
          })
        } else {
          await database.collections.get('day_records').create((record) => {
            record.date = today
            record.steps = steps
            record.weatherData = JSON.stringify(weather)
            record.activityScore = this.calculateActivityScore(steps)
          })
        }
      })
    } catch (error) {
      console.error('Background task error:', error)
    }
  }

  private calculateActivityScore(steps: number): number {
    // Simple scoring: 10k steps = 100 score
    return Math.min(100, (steps / 10000) * 100)
  }
}

export default new BackgroundTaskService()
```

## Final Steps (Day 8)

### 11. App Entry Point
Update `App.tsx`:
```typescript
import React, { useEffect } from 'react'
import { NavigationContainer } from '@react-navigation/native'
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs'
import { CalendarScreen } from './src/screens/CalendarScreen'
import { DashboardScreen } from './src/screens/DashboardScreen'
import { SettingsScreen } from './src/screens/SettingsScreen'
import BackgroundTaskService from './src/services/BackgroundTaskService'
import LocationService from './src/services/LocationService'

const Tab = createBottomTabNavigator()

export default function App() {
  useEffect(() => {
    // Initialize services
    BackgroundTaskService.configure()
    LocationService.configure()
  }, [])

  return (
    <NavigationContainer>
      <Tab.Navigator>
        <Tab.Screen name="Calendar" component={CalendarScreen} />
        <Tab.Screen name="Dashboard" component={DashboardScreen} />
        <Tab.Screen name="Settings" component={SettingsScreen} />
      </Tab.Navigator>
    </NavigationContainer>
  )
}
```

## Testing & Launch

### 12. Test on Device
```bash
# iOS
npx react-native run-ios --device

# Android
npx react-native run-android
```

### 13. Key Testing Points
1. Request all permissions on first launch
2. Verify step counting updates in real-time
3. Check background location tracking
4. Ensure weather data loads for current location
5. Verify data persists between app launches
6. Test calendar color coding
7. Check background fetch is working

### 14. Performance Optimization
- Implement data pagination for calendar
- Cache weather API responses
- Aggregate old data (daily → weekly → monthly)
- Use React.memo for expensive components
- Lazy load images from camera roll

### 15. Next Features to Add
- Air quality API integration
- Photo metadata extraction
- Screen time tracking
- Export data functionality
- Widget for home screen
- Correlation insights
- Year in pixels view

## Additional Features to Implement

### Extended Environmental APIs
- **Moon Phase API**: Calculate lunar cycles
- **Public Holidays API**: Get local holidays
- **News Headlines**: Major events for context
- **Earthquake Data**: USGS seismic activity
- **UV Index**: Historical UV exposure

### Advanced Device Metrics
- **Battery Patterns**: Charging habits and health
- **Network Usage**: WiFi networks and data consumption
- **Storage Patterns**: Media creation rates
- **App Usage**: Which apps used when
- **Phone Unlocks**: Frequency and patterns

### Enhanced Visualizations
- **Environmental Timeline**: Weather overlay on activity
- **Photo Memory Map**: Geographic photo visualization
- **Correlation Dashboard**: Weather vs activity insights
- **Year in Pixels**: Full year color-coded view
- **Activity Rings**: Apple Watch style daily goals

### Data Export & Sharing
- **CSV Export**: Full data dump
- **JSON Export**: Structured data export
- **PDF Reports**: Monthly/yearly summaries
- **Share Images**: Calendar screenshots
- **Backup/Restore**: Local backup system

## Media Tracking Extension

### New Data Sources

#### Device-Based Media Tracking
- **Browser History**: Access local browser history (with permission) to detect YouTube, streaming sites
- **App Usage Stats**: Track time spent in Netflix, Hulu, Spotify, etc. using Screen Time API
- **Media Control Center**: Capture "Now Playing" info from iOS/Android media controls

#### Public APIs for Media
- **TVMaze API**: Free TV schedule and show information
- **TMDB API**: Movie/TV metadata (free tier, requires API key but no user auth)
- **Radio Browser API**: Global radio station directory
- **Podcast Index API**: Open podcast database

#### Implementation Approach

##### 1. Manual Media Logging with Smart Suggestions
```typescript
interface MediaTracker {
  // Pre-populated lists
  popularShows: Show[] // From TVMaze
  trendingMovies: Movie[] // From TMDB
  localTVChannels: Channel[] // Based on location
  
  // Quick-add functionality
  quickAddShow(showId: string, episode?: EpisodeInfo)
  quickAddMovie(movieId: string)
  logCustomMedia(title: string, type: MediaType)
  
  // Smart features
  suggestNextEpisode(showId: string) // Auto-increment episode
  detectBingeSession() // If multiple episodes logged in sequence
}
```

##### 2. App Usage Detection
```typescript
// iOS: Screen Time API
const getMediaAppUsage = async () => {
  const appUsage = await ScreenTime.getAppUsage([
    'com.netflix.Netflix',
    'com.google.android.youtube',
    'com.spotify.music',
    'tv.twitch',
    'com.hulu.plus'
  ])
  return appUsage.map(app => ({
    app: app.bundleId,
    duration: app.screenTime,
    sessions: app.numberOfPickups
  }))
}
```

##### 3. Browser History Analysis
```typescript
// Detect streaming from browser history
const analyzeStreamingHistory = async () => {
  const history = await getBrowserHistory() // Requires permission
  const streamingDomains = [
    'youtube.com/watch',
    'netflix.com/watch',
    'twitch.tv/',
    'disneyplus.com/video'
  ]
  
  return history.filter(entry => 
    streamingDomains.some(domain => entry.url.includes(domain))
  ).map(entry => ({
    platform: detectPlatform(entry.url),
    timestamp: entry.visitTime,
    title: extractTitle(entry.title) // Parse page title
  }))
}
```

##### 4. TV Schedule Integration
```typescript
// Pre-populate with local TV channels
const getTVSchedule = async (zipCode: string) => {
  // Use TVMaze or XMLTV feeds
  const schedule = await fetch(`http://api.tvmaze.com/schedule?country=US&date=${date}`)
  return schedule.filter(show => 
    show.airtime >= '20:00' && show.airtime <= '23:00' // Prime time
  )
}
```

##### 5. Smart UI for Quick Logging
```typescript
// Recent/Favorite Shows Component
<QuickAddMedia>
  <RecentlyWatched /> {/* Last 5 logged items */}
  <Favorites /> {/* User's favorite shows/channels */}
  <CurrentlyAiring /> {/* What's on TV now */}
  <TrendingToday /> {/* Popular on streaming */}
</QuickAddMedia>

// One-tap logging
<ShowCard 
  onTap={() => logEpisode(showId, nextEpisode)}
  onLongPress={() => showEpisodeSelector()}
/>
```

##### 6. Media Consumption Insights
```typescript
// Analytics
const getMediaInsights = () => ({
  bingeStreaks: detectBingeWatching(mediaLog),
  genreDistribution: analyzeGenres(mediaLog),
  platformBalance: getPlatformUsage(mediaLog),
  mediaVsActivity: correlateWithSteps(mediaLog, activityLog),
  primeTimeHabits: analyzePrimeTimeUsage(mediaLog)
})
```

#### Data Schema Addition
```typescript
interface MediaLog {
  id: string
  timestamp: Date
  mediaType: 'tv' | 'movie' | 'youtube' | 'podcast' | 'music'
  title: string
  platform?: string
  duration?: number
  metadata?: {
    showId?: string
    season?: number
    episode?: number
    channel?: string
    genre?: string[]
    tmdbId?: string
  }
  source: 'manual' | 'appUsage' | 'browserHistory' | 'nowPlaying'
}
```

This approach combines:
- Automatic detection where possible (app usage, browser history)
- Easy manual logging with pre-populated options
- Smart suggestions based on viewing patterns
- No authentication required (uses free APIs and local data)

## Development Order
1. **Phase 1**: Core app structure, permissions, basic calendar
2. **Phase 2**: Step counter, location tracking, weather API
3. **Phase 3**: Database, background tasks, data persistence
4. **Phase 4**: Calendar visualization, activity scoring
5. **Phase 5**: Additional sensors (battery, screen time)
6. **Phase 6**: More APIs (air quality, UV, moon phases)
7. **Phase 7**: Advanced visualizations and insights
8. **Phase 8**: Export features and polish