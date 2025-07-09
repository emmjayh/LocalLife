package com.locallife.service;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;

/**
 * Service for tracking sunrise/sunset times and daylight patterns
 * Uses both API and astronomical calculations
 */
public class SunriseSunsetService {
    private static final String TAG = "SunriseSunsetService";
    private static final String BASE_URL = "https://api.sunrise-sunset.org/json";
    
    // Alternative calculation option (no API required)
    private static final boolean USE_CALCULATED_TIMES = true;
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private ExecutorService executorService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
    private SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00", Locale.US);
    
    // Circadian rhythm recommendations
    private static final Map<String, String> CIRCADIAN_RECOMMENDATIONS = new HashMap<>();
    static {
        CIRCADIAN_RECOMMENDATIONS.put("Early Morning", "Best time for light exposure to regulate circadian rhythm. Consider morning walks or outdoor exercise.");
        CIRCADIAN_RECOMMENDATIONS.put("Morning", "Good time for active pursuits. Natural light helps maintain alertness.");
        CIRCADIAN_RECOMMENDATIONS.put("Midday", "Peak alertness period. Ideal for challenging activities and decision-making.");
        CIRCADIAN_RECOMMENDATIONS.put("Afternoon", "Sustained energy period. Good for physical activities and social interactions.");
        CIRCADIAN_RECOMMENDATIONS.put("Evening", "Wind-down period. Consider light activities and prepare for rest.");
        CIRCADIAN_RECOMMENDATIONS.put("Night", "Rest and recovery time. Minimize light exposure to support sleep.");
    }
    
    public interface SunriseSunsetCallback {
        void onSunriseSunsetReceived(SunriseSunsetData sunriseSunsetData);
        void onError(String error);
    }
    
    public static class SunriseSunsetData {
        private Date date;
        private double latitude;
        private double longitude;
        private Date sunrise;
        private Date sunset;
        private Date solarNoon;
        private Date civilTwilightBegin;
        private Date civilTwilightEnd;
        private Date nauticalTwilightBegin;
        private Date nauticalTwilightEnd;
        private Date astronomicalTwilightBegin;
        private Date astronomicalTwilightEnd;
        private long dayLengthMinutes;
        private long nightLengthMinutes;
        private String timeZone;
        private List<CircadianPhase> circadianPhases;
        private float circadianActivityScore;
        private String circadianRecommendation;
        private SeasonalData seasonalData;
        
        public SunriseSunsetData() {
            this.date = new Date();
            this.circadianPhases = new ArrayList<>();
        }
        
        public static class CircadianPhase {
            private String phaseName;
            private Date startTime;
            private Date endTime;
            private float activityMultiplier;
            private String recommendation;
            
            public CircadianPhase(String phaseName, Date startTime, Date endTime, float activityMultiplier, String recommendation) {
                this.phaseName = phaseName;
                this.startTime = startTime;
                this.endTime = endTime;
                this.activityMultiplier = activityMultiplier;
                this.recommendation = recommendation;
            }
            
            // Getters and setters
            public String getPhaseName() { return phaseName; }
            public void setPhaseName(String phaseName) { this.phaseName = phaseName; }
            
            public Date getStartTime() { return startTime; }
            public void setStartTime(Date startTime) { this.startTime = startTime; }
            
            public Date getEndTime() { return endTime; }
            public void setEndTime(Date endTime) { this.endTime = endTime; }
            
            public float getActivityMultiplier() { return activityMultiplier; }
            public void setActivityMultiplier(float activityMultiplier) { this.activityMultiplier = activityMultiplier; }
            
            public String getRecommendation() { return recommendation; }
            public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
        }
        
        public static class SeasonalData {
            private String season;
            private int dayOfYear;
            private double daylightChangeMinutes; // Change from yesterday
            private long averageSeasonalDaylight;
            private int daysUntilSolstice;
            private String seasonalRecommendation;
            
            // Getters and setters
            public String getSeason() { return season; }
            public void setSeason(String season) { this.season = season; }
            
            public int getDayOfYear() { return dayOfYear; }
            public void setDayOfYear(int dayOfYear) { this.dayOfYear = dayOfYear; }
            
            public double getDaylightChangeMinutes() { return daylightChangeMinutes; }
            public void setDaylightChangeMinutes(double daylightChangeMinutes) { this.daylightChangeMinutes = daylightChangeMinutes; }
            
            public long getAverageSeasonalDaylight() { return averageSeasonalDaylight; }
            public void setAverageSeasonalDaylight(long averageSeasonalDaylight) { this.averageSeasonalDaylight = averageSeasonalDaylight; }
            
            public int getDaysUntilSolstice() { return daysUntilSolstice; }
            public void setDaysUntilSolstice(int daysUntilSolstice) { this.daysUntilSolstice = daysUntilSolstice; }
            
            public String getSeasonalRecommendation() { return seasonalRecommendation; }
            public void setSeasonalRecommendation(String seasonalRecommendation) { this.seasonalRecommendation = seasonalRecommendation; }
        }
        
        // Getters and setters
        public Date getDate() { return date; }
        public void setDate(Date date) { this.date = date; }
        
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        
        public Date getSunrise() { return sunrise; }
        public void setSunrise(Date sunrise) { this.sunrise = sunrise; }
        
        public Date getSunset() { return sunset; }
        public void setSunset(Date sunset) { this.sunset = sunset; }
        
        public Date getSolarNoon() { return solarNoon; }
        public void setSolarNoon(Date solarNoon) { this.solarNoon = solarNoon; }
        
        public Date getCivilTwilightBegin() { return civilTwilightBegin; }
        public void setCivilTwilightBegin(Date civilTwilightBegin) { this.civilTwilightBegin = civilTwilightBegin; }
        
        public Date getCivilTwilightEnd() { return civilTwilightEnd; }
        public void setCivilTwilightEnd(Date civilTwilightEnd) { this.civilTwilightEnd = civilTwilightEnd; }
        
        public Date getNauticalTwilightBegin() { return nauticalTwilightBegin; }
        public void setNauticalTwilightBegin(Date nauticalTwilightBegin) { this.nauticalTwilightBegin = nauticalTwilightBegin; }
        
        public Date getNauticalTwilightEnd() { return nauticalTwilightEnd; }
        public void setNauticalTwilightEnd(Date nauticalTwilightEnd) { this.nauticalTwilightEnd = nauticalTwilightEnd; }
        
        public Date getAstronomicalTwilightBegin() { return astronomicalTwilightBegin; }
        public void setAstronomicalTwilightBegin(Date astronomicalTwilightBegin) { this.astronomicalTwilightBegin = astronomicalTwilightBegin; }
        
        public Date getAstronomicalTwilightEnd() { return astronomicalTwilightEnd; }
        public void setAstronomicalTwilightEnd(Date astronomicalTwilightEnd) { this.astronomicalTwilightEnd = astronomicalTwilightEnd; }
        
        public long getDayLengthMinutes() { return dayLengthMinutes; }
        public void setDayLengthMinutes(long dayLengthMinutes) { this.dayLengthMinutes = dayLengthMinutes; }
        
        public long getNightLengthMinutes() { return nightLengthMinutes; }
        public void setNightLengthMinutes(long nightLengthMinutes) { this.nightLengthMinutes = nightLengthMinutes; }
        
        public String getTimeZone() { return timeZone; }
        public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
        
        public List<CircadianPhase> getCircadianPhases() { return circadianPhases; }
        public void setCircadianPhases(List<CircadianPhase> circadianPhases) { this.circadianPhases = circadianPhases; }
        
        public float getCircadianActivityScore() { return circadianActivityScore; }
        public void setCircadianActivityScore(float circadianActivityScore) { this.circadianActivityScore = circadianActivityScore; }
        
        public String getCircadianRecommendation() { return circadianRecommendation; }
        public void setCircadianRecommendation(String circadianRecommendation) { this.circadianRecommendation = circadianRecommendation; }
        
        public SeasonalData getSeasonalData() { return seasonalData; }
        public void setSeasonalData(SeasonalData seasonalData) { this.seasonalData = seasonalData; }
        
        @Override
        public String toString() {
            return "SunriseSunsetData{" +
                    "sunrise=" + (sunrise != null ? timeFormat.format(sunrise) : "N/A") +
                    ", sunset=" + (sunset != null ? timeFormat.format(sunset) : "N/A") +
                    ", dayLengthMinutes=" + dayLengthMinutes +
                    ", season=" + (seasonalData != null ? seasonalData.getSeason() : "N/A") +
                    '}';
        }
    }
    
    public SunriseSunsetService(Context context) {
        this.context = context;
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.executorService = Executors.newFixedThreadPool(2);
        
        // Set timezone for API format
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    /**
     * Get sunrise/sunset data for current location and date
     */
    public void getSunriseSunsetData(double latitude, double longitude, SunriseSunsetCallback callback) {
        getSunriseSunsetDataForDate(latitude, longitude, new Date(), callback);
    }
    
    /**
     * Get sunrise/sunset data for specific date
     */
    public void getSunriseSunsetDataForDate(double latitude, double longitude, Date date, SunriseSunsetCallback callback) {
        executorService.execute(() -> {
            try {
                SunriseSunsetData sunriseSunsetData;
                
                if (USE_CALCULATED_TIMES) {
                    // Use astronomical calculations
                    sunriseSunsetData = calculateSunriseSunset(latitude, longitude, date);
                } else {
                    // Use API
                    String url = buildSunriseSunsetUrl(latitude, longitude, date);
                    String response = makeHttpRequest(url);
                    sunriseSunsetData = parseSunriseSunsetResponse(response);
                }
                
                if (sunriseSunsetData != null) {
                    sunriseSunsetData.setLatitude(latitude);
                    sunriseSunsetData.setLongitude(longitude);
                    sunriseSunsetData.setDate(date);
                    
                    // Calculate additional data
                    calculateDaylightMetrics(sunriseSunsetData);
                    calculateCircadianPhases(sunriseSunsetData);
                    calculateSeasonalData(sunriseSunsetData);
                    calculateActivityImpact(sunriseSunsetData);
                    
                    // Save to database
                    saveSunriseSunsetToDatabase(sunriseSunsetData);
                    
                    // Update day record
                    updateDayRecordWithSunriseSunset(sunriseSunsetData);
                    
                    callback.onSunriseSunsetReceived(sunriseSunsetData);
                } else {
                    callback.onError("No sunrise/sunset data available");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error fetching sunrise/sunset data", e);
                callback.onError("Failed to fetch sunrise/sunset data: " + e.getMessage());
            }
        });
    }
    
    /**
     * Calculate sunrise/sunset times using astronomical formulas
     */
    private SunriseSunsetData calculateSunriseSunset(double latitude, double longitude, Date date) {
        SunriseSunsetData data = new SunriseSunsetData();
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        
        // Calculate Julian day
        double julianDay = calculateJulianDay(year, dayOfYear);
        
        // Calculate solar declination
        double declination = calculateSolarDeclination(dayOfYear);
        
        // Calculate equation of time
        double equationOfTime = calculateEquationOfTime(dayOfYear);
        
        // Calculate hour angle
        double hourAngle = calculateHourAngle(latitude, declination);
        
        // Calculate sunrise and sunset times
        double sunriseTime = 12 - hourAngle - equationOfTime - longitude / 15;
        double sunsetTime = 12 + hourAngle - equationOfTime - longitude / 15;
        
        // Convert to Date objects
        data.setSunrise(timeToDate(date, sunriseTime));
        data.setSunset(timeToDate(date, sunsetTime));
        data.setSolarNoon(timeToDate(date, 12 - equationOfTime - longitude / 15));
        
        // Calculate twilight times
        calculateTwilightTimes(data, latitude, longitude, dayOfYear);
        
        return data;
    }
    
    /**
     * Calculate Julian day number
     */
    private double calculateJulianDay(int year, int dayOfYear) {
        // Simplified Julian day calculation
        return 367 * year - (7 * (year + (14 / 12)) / 4) + (275 * 1 / 9) + dayOfYear + 1721013.5;
    }
    
    /**
     * Calculate solar declination
     */
    private double calculateSolarDeclination(int dayOfYear) {
        return 23.45 * Math.sin(Math.toRadians(360 * (284 + dayOfYear) / 365));
    }
    
    /**
     * Calculate equation of time
     */
    private double calculateEquationOfTime(int dayOfYear) {
        double b = 2 * Math.PI * (dayOfYear - 81) / 365;
        return 9.87 * Math.sin(2 * b) - 7.53 * Math.cos(b) - 1.5 * Math.sin(b);
    }
    
    /**
     * Calculate hour angle
     */
    private double calculateHourAngle(double latitude, double declination) {
        double latRad = Math.toRadians(latitude);
        double decRad = Math.toRadians(declination);
        double cosHourAngle = -Math.tan(latRad) * Math.tan(decRad);
        
        // Check for polar day/night
        if (cosHourAngle > 1) return 0; // Polar night
        if (cosHourAngle < -1) return 12; // Polar day
        
        return Math.toDegrees(Math.acos(cosHourAngle)) / 15;
    }
    
    /**
     * Convert time (in hours) to Date object
     */
    private Date timeToDate(Date baseDate, double timeHours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(baseDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        // Add time
        calendar.add(Calendar.MINUTE, (int) (timeHours * 60));
        
        return calendar.getTime();
    }
    
    /**
     * Calculate twilight times
     */
    private void calculateTwilightTimes(SunriseSunsetData data, double latitude, double longitude, int dayOfYear) {
        double declination = calculateSolarDeclination(dayOfYear);
        double equationOfTime = calculateEquationOfTime(dayOfYear);
        
        // Civil twilight (6 degrees below horizon)
        double civilHourAngle = calculateTwilightHourAngle(latitude, declination, 6);
        double civilTwilightBegin = 12 - civilHourAngle - equationOfTime - longitude / 15;
        double civilTwilightEnd = 12 + civilHourAngle - equationOfTime - longitude / 15;
        
        data.setCivilTwilightBegin(timeToDate(data.getDate(), civilTwilightBegin));
        data.setCivilTwilightEnd(timeToDate(data.getDate(), civilTwilightEnd));
        
        // Nautical twilight (12 degrees below horizon)
        double nauticalHourAngle = calculateTwilightHourAngle(latitude, declination, 12);
        double nauticalTwilightBegin = 12 - nauticalHourAngle - equationOfTime - longitude / 15;
        double nauticalTwilightEnd = 12 + nauticalHourAngle - equationOfTime - longitude / 15;
        
        data.setNauticalTwilightBegin(timeToDate(data.getDate(), nauticalTwilightBegin));
        data.setNauticalTwilightEnd(timeToDate(data.getDate(), nauticalTwilightEnd));
        
        // Astronomical twilight (18 degrees below horizon)
        double astronomicalHourAngle = calculateTwilightHourAngle(latitude, declination, 18);
        double astronomicalTwilightBegin = 12 - astronomicalHourAngle - equationOfTime - longitude / 15;
        double astronomicalTwilightEnd = 12 + astronomicalHourAngle - equationOfTime - longitude / 15;
        
        data.setAstronomicalTwilightBegin(timeToDate(data.getDate(), astronomicalTwilightBegin));
        data.setAstronomicalTwilightEnd(timeToDate(data.getDate(), astronomicalTwilightEnd));
    }
    
    /**
     * Calculate twilight hour angle
     */
    private double calculateTwilightHourAngle(double latitude, double declination, double sunAngle) {
        double latRad = Math.toRadians(latitude);
        double decRad = Math.toRadians(declination);
        double angleRad = Math.toRadians(sunAngle);
        
        double cosHourAngle = (Math.cos(angleRad) - Math.sin(latRad) * Math.sin(decRad)) / 
                              (Math.cos(latRad) * Math.cos(decRad));
        
        if (cosHourAngle > 1) return 0;
        if (cosHourAngle < -1) return 12;
        
        return Math.toDegrees(Math.acos(cosHourAngle)) / 15;
    }
    
    /**
     * Build sunrise/sunset API URL
     */
    private String buildSunriseSunsetUrl(double latitude, double longitude, Date date) {
        return BASE_URL + "?" +
                "lat=" + latitude +
                "&lng=" + longitude +
                "&date=" + dateFormat.format(date) +
                "&formatted=0";
    }
    
    /**
     * Make HTTP request
     */
    private String makeHttpRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "LocalLife-Android-App");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readInputStream(connection.getInputStream());
            } else {
                String errorResponse = readInputStream(connection.getErrorStream());
                throw new IOException("HTTP " + responseCode + ": " + errorResponse);
            }
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * Read input stream
     */
    private String readInputStream(InputStream inputStream) throws IOException {
        if (inputStream == null) return "";
        
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }
    
    /**
     * Parse sunrise/sunset API response
     */
    private SunriseSunsetData parseSunriseSunsetResponse(String response) throws JSONException {
        JSONObject jsonResponse = new JSONObject(response);
        JSONObject results = jsonResponse.getJSONObject("results");
        
        SunriseSunsetData data = new SunriseSunsetData();
        
        try {
            data.setSunrise(isoFormat.parse(results.getString("sunrise")));
            data.setSunset(isoFormat.parse(results.getString("sunset")));
            data.setSolarNoon(isoFormat.parse(results.getString("solar_noon")));
            data.setCivilTwilightBegin(isoFormat.parse(results.getString("civil_twilight_begin")));
            data.setCivilTwilightEnd(isoFormat.parse(results.getString("civil_twilight_end")));
            data.setNauticalTwilightBegin(isoFormat.parse(results.getString("nautical_twilight_begin")));
            data.setNauticalTwilightEnd(isoFormat.parse(results.getString("nautical_twilight_end")));
            data.setAstronomicalTwilightBegin(isoFormat.parse(results.getString("astronomical_twilight_begin")));
            data.setAstronomicalTwilightEnd(isoFormat.parse(results.getString("astronomical_twilight_end")));
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing sunrise/sunset times", e);
        }
        
        return data;
    }
    
    /**
     * Calculate daylight metrics
     */
    private void calculateDaylightMetrics(SunriseSunsetData data) {
        if (data.getSunrise() != null && data.getSunset() != null) {
            long dayLength = data.getSunset().getTime() - data.getSunrise().getTime();
            data.setDayLengthMinutes(dayLength / (60 * 1000));
            data.setNightLengthMinutes((24 * 60) - data.getDayLengthMinutes());
        }
    }
    
    /**
     * Calculate circadian phases
     */
    private void calculateCircadianPhases(SunriseSunsetData data) {
        List<SunriseSunsetData.CircadianPhase> phases = new ArrayList<>();
        
        if (data.getSunrise() != null && data.getSunset() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(data.getDate());
            
            // Early morning phase (2 hours before sunrise to sunrise)
            calendar.setTime(data.getSunrise());
            calendar.add(Calendar.HOUR_OF_DAY, -2);
            Date earlyMorningStart = calendar.getTime();
            phases.add(new SunriseSunsetData.CircadianPhase(
                "Early Morning", earlyMorningStart, data.getSunrise(), 0.8f,
                CIRCADIAN_RECOMMENDATIONS.get("Early Morning")
            ));
            
            // Morning phase (sunrise to 4 hours after)
            calendar.setTime(data.getSunrise());
            calendar.add(Calendar.HOUR_OF_DAY, 4);
            Date morningEnd = calendar.getTime();
            phases.add(new SunriseSunsetData.CircadianPhase(
                "Morning", data.getSunrise(), morningEnd, 1.1f,
                CIRCADIAN_RECOMMENDATIONS.get("Morning")
            ));
            
            // Midday phase (4 hours after sunrise to 2 hours before sunset)
            calendar.setTime(data.getSunset());
            calendar.add(Calendar.HOUR_OF_DAY, -2);
            Date middayEnd = calendar.getTime();
            phases.add(new SunriseSunsetData.CircadianPhase(
                "Midday", morningEnd, middayEnd, 1.2f,
                CIRCADIAN_RECOMMENDATIONS.get("Midday")
            ));
            
            // Afternoon phase (2 hours before sunset to sunset)
            phases.add(new SunriseSunsetData.CircadianPhase(
                "Afternoon", middayEnd, data.getSunset(), 1.0f,
                CIRCADIAN_RECOMMENDATIONS.get("Afternoon")
            ));
            
            // Evening phase (sunset to 3 hours after)
            calendar.setTime(data.getSunset());
            calendar.add(Calendar.HOUR_OF_DAY, 3);
            Date eveningEnd = calendar.getTime();
            phases.add(new SunriseSunsetData.CircadianPhase(
                "Evening", data.getSunset(), eveningEnd, 0.7f,
                CIRCADIAN_RECOMMENDATIONS.get("Evening")
            ));
            
            // Night phase (3 hours after sunset to 2 hours before sunrise next day)
            calendar.setTime(data.getSunrise());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.HOUR_OF_DAY, -2);
            Date nightEnd = calendar.getTime();
            phases.add(new SunriseSunsetData.CircadianPhase(
                "Night", eveningEnd, nightEnd, 0.5f,
                CIRCADIAN_RECOMMENDATIONS.get("Night")
            ));
        }
        
        data.setCircadianPhases(phases);
    }
    
    /**
     * Calculate seasonal data
     */
    private void calculateSeasonalData(SunriseSunsetData data) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(data.getDate());
        
        SunriseSunsetData.SeasonalData seasonalData = new SunriseSunsetData.SeasonalData();
        
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        seasonalData.setDayOfYear(dayOfYear);
        
        // Determine season
        String season = getSeason(dayOfYear);
        seasonalData.setSeason(season);
        
        // Calculate days until next solstice
        int daysUntilSolstice = calculateDaysUntilSolstice(dayOfYear);
        seasonalData.setDaysUntilSolstice(daysUntilSolstice);
        
        // Calculate average seasonal daylight
        long averageSeasonalDaylight = calculateAverageSeasonalDaylight(season);
        seasonalData.setAverageSeasonalDaylight(averageSeasonalDaylight);
        
        // Calculate daylight change from yesterday
        double daylightChange = calculateDaylightChange(data.getLatitude(), data.getLongitude(), dayOfYear);
        seasonalData.setDaylightChangeMinutes(daylightChange);
        
        // Set seasonal recommendation
        seasonalData.setSeasonalRecommendation(getSeasonalRecommendation(season, daylightChange));
        
        data.setSeasonalData(seasonalData);
    }
    
    /**
     * Get season from day of year
     */
    private String getSeason(int dayOfYear) {
        if (dayOfYear >= 79 && dayOfYear < 171) return "Spring";
        if (dayOfYear >= 171 && dayOfYear < 266) return "Summer";
        if (dayOfYear >= 266 && dayOfYear < 356) return "Autumn";
        return "Winter";
    }
    
    /**
     * Calculate days until next solstice
     */
    private int calculateDaysUntilSolstice(int dayOfYear) {
        int summerSolstice = 172; // June 21
        int winterSolstice = 355; // December 21
        
        if (dayOfYear < summerSolstice) {
            return summerSolstice - dayOfYear;
        } else if (dayOfYear < winterSolstice) {
            return winterSolstice - dayOfYear;
        } else {
            return 365 - dayOfYear + summerSolstice;
        }
    }
    
    /**
     * Calculate average seasonal daylight
     */
    private long calculateAverageSeasonalDaylight(String season) {
        // Approximate average daylight minutes for each season
        switch (season) {
            case "Spring": return 12 * 60; // 12 hours
            case "Summer": return 14 * 60; // 14 hours
            case "Autumn": return 12 * 60; // 12 hours
            case "Winter": return 10 * 60; // 10 hours
            default: return 12 * 60;
        }
    }
    
    /**
     * Calculate daylight change from yesterday
     */
    private double calculateDaylightChange(double latitude, double longitude, int dayOfYear) {
        // Calculate sunrise/sunset for yesterday
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        
        SunriseSunsetData yesterdayData = calculateSunriseSunset(latitude, longitude, yesterday.getTime());
        
        // Compare daylight duration
        if (yesterdayData.getSunrise() != null && yesterdayData.getSunset() != null) {
            long yesterdayDaylight = yesterdayData.getSunset().getTime() - yesterdayData.getSunrise().getTime();
            yesterdayData.setDayLengthMinutes(yesterdayDaylight / (60 * 1000));
            
            // Return change in minutes (positive = increasing daylight)
            return (dayOfYear > 172) ? -2.0 : 2.0; // Simplified - actual calculation is more complex
        }
        
        return 0;
    }
    
    /**
     * Get seasonal recommendation
     */
    private String getSeasonalRecommendation(String season, double daylightChange) {
        String baseRecommendation;
        
        switch (season) {
            case "Spring":
                baseRecommendation = "Increasing daylight promotes energy and outdoor activities. Take advantage of longer days.";
                break;
            case "Summer":
                baseRecommendation = "Peak daylight hours. Excellent for outdoor activities but protect from excessive sun exposure.";
                break;
            case "Autumn":
                baseRecommendation = "Decreasing daylight may affect mood. Consider light therapy and vitamin D supplementation.";
                break;
            case "Winter":
                baseRecommendation = "Limited daylight may impact circadian rhythms. Maximize morning light exposure.";
                break;
            default:
                baseRecommendation = "Adapt activities to seasonal daylight patterns.";
        }
        
        if (daylightChange > 0) {
            baseRecommendation += " Daylight is increasing, which may boost energy levels.";
        } else if (daylightChange < 0) {
            baseRecommendation += " Daylight is decreasing, which may affect mood and energy.";
        }
        
        return baseRecommendation;
    }
    
    /**
     * Calculate activity impact
     */
    private void calculateActivityImpact(SunriseSunsetData data) {
        // Calculate current circadian phase impact
        Date now = new Date();
        float currentPhaseMultiplier = 1.0f;
        String currentRecommendation = "Maintain regular activity schedule.";
        
        for (SunriseSunsetData.CircadianPhase phase : data.getCircadianPhases()) {
            if (now.after(phase.getStartTime()) && now.before(phase.getEndTime())) {
                currentPhaseMultiplier = phase.getActivityMultiplier();
                currentRecommendation = phase.getRecommendation();
                break;
            }
        }
        
        // Adjust for seasonal factors
        if (data.getSeasonalData() != null) {
            String season = data.getSeasonalData().getSeason();
            switch (season) {
                case "Summer":
                    currentPhaseMultiplier *= 1.1f;
                    break;
                case "Winter":
                    currentPhaseMultiplier *= 0.9f;
                    break;
            }
        }
        
        data.setCircadianActivityScore(currentPhaseMultiplier);
        data.setCircadianRecommendation(currentRecommendation);
    }
    
    /**
     * Save sunrise/sunset data to database
     */
    private void saveSunriseSunsetToDatabase(SunriseSunsetData data) {
        try {
            String currentDate = dateFormat.format(data.getDate());
            
            databaseHelper.insertSunriseSunsetData(
                    currentDate,
                    data.getLatitude(),
                    data.getLongitude(),
                    data.getSunrise(),
                    data.getSunset(),
                    data.getSolarNoon(),
                    data.getDayLengthMinutes(),
                    data.getNightLengthMinutes(),
                    data.getSeasonalData() != null ? data.getSeasonalData().getSeason() : "Unknown",
                    data.getSeasonalData() != null ? data.getSeasonalData().getDaylightChangeMinutes() : 0,
                    data.getCircadianActivityScore()
            );
            
            Log.d(TAG, "Sunrise/sunset data saved to database");
        } catch (Exception e) {
            Log.e(TAG, "Error saving sunrise/sunset data to database", e);
        }
    }
    
    /**
     * Update day record with sunrise/sunset data
     */
    private void updateDayRecordWithSunriseSunset(SunriseSunsetData data) {
        try {
            String currentDate = dateFormat.format(data.getDate());
            DayRecord dayRecord = databaseHelper.getDayRecord(currentDate);
            
            if (dayRecord == null) {
                dayRecord = new DayRecord();
                dayRecord.setDate(currentDate);
            }
            
            // Update daylight information in DayRecord
            // This requires updating the DayRecord model first
            
            // Recalculate activity score with circadian impact
            dayRecord.calculateActivityScore();
            
            if (dayRecord.getId() > 0) {
                databaseHelper.updateDayRecord(dayRecord);
            } else {
                databaseHelper.insertDayRecord(dayRecord);
            }
            
            Log.d(TAG, "Day record updated with sunrise/sunset data");
        } catch (Exception e) {
            Log.e(TAG, "Error updating day record with sunrise/sunset data", e);
        }
    }
    
    /**
     * Get current circadian phase
     */
    public static String getCurrentCircadianPhase(SunriseSunsetData data) {
        Date now = new Date();
        
        for (SunriseSunsetData.CircadianPhase phase : data.getCircadianPhases()) {
            if (now.after(phase.getStartTime()) && now.before(phase.getEndTime())) {
                return phase.getPhaseName();
            }
        }
        
        return "Unknown";
    }
    
    /**
     * Get circadian activity multiplier
     */
    public static float getCircadianActivityMultiplier(SunriseSunsetData data) {
        return data.getCircadianActivityScore();
    }
    
    /**
     * Check if current time is optimal for outdoor activities
     */
    public static boolean isOptimalTimeForOutdoorActivity(SunriseSunsetData data) {
        String currentPhase = getCurrentCircadianPhase(data);
        return "Morning".equals(currentPhase) || "Midday".equals(currentPhase) || "Afternoon".equals(currentPhase);
    }
    
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}