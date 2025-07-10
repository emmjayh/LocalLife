package com.locallife.model;

/**
 * Enum representing different types of activities for prediction and recommendation
 */
public enum ActivityType {
    OUTDOOR_EXERCISE("Outdoor Exercise", "physical", new String[]{"running", "cycling", "hiking", "walking"}),
    INDOOR_EXERCISE("Indoor Exercise", "physical", new String[]{"gym", "yoga", "workout", "fitness"}),
    SOCIAL_ACTIVITY("Social Activity", "social", new String[]{"meeting friends", "dining out", "party", "social gathering"}),
    WORK_PRODUCTIVITY("Work/Productivity", "productivity", new String[]{"work", "study", "office", "meetings"}),
    RECREATIONAL("Recreational", "recreational", new String[]{"shopping", "movies", "entertainment", "hobbies"}),
    RELAXATION("Relaxation", "wellness", new String[]{"meditation", "spa", "reading", "rest"}),
    TRAVEL("Travel", "mobility", new String[]{"commuting", "travel", "transportation", "trip"}),
    PHOTOGRAPHY("Photography", "creative", new String[]{"photo walk", "photography", "sightseeing", "exploration"}),
    INDOOR_ACTIVITIES("Indoor Activities", "indoor", new String[]{"home activities", "indoor entertainment", "cooking", "cleaning"}),
    OUTDOOR_LEISURE("Outdoor Leisure", "outdoor", new String[]{"picnic", "park visit", "outdoor dining", "nature activities"});

    private final String displayName;
    private final String category;
    private final String[] keywords;

    ActivityType(String displayName, String category, String[] keywords) {
        this.displayName = displayName;
        this.category = category;
        this.keywords = keywords;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCategory() {
        return category;
    }

    public String[] getKeywords() {
        return keywords;
    }

    /**
     * Get weather suitability score for this activity type
     * @param temperature Temperature in Celsius
     * @param weatherCondition Weather condition string
     * @param humidity Humidity percentage
     * @param windSpeed Wind speed in km/h
     * @param uvIndex UV index value
     * @return Suitability score from 0.0 to 1.0
     */
    public double getWeatherSuitability(float temperature, String weatherCondition, 
                                       float humidity, float windSpeed, double uvIndex) {
        switch (this) {
            case OUTDOOR_EXERCISE:
                return calculateOutdoorExerciseSuitability(temperature, weatherCondition, humidity, windSpeed, uvIndex);
            case INDOOR_EXERCISE:
                return calculateIndoorExerciseSuitability(temperature, weatherCondition, humidity);
            case SOCIAL_ACTIVITY:
                return calculateSocialActivitySuitability(temperature, weatherCondition, humidity, windSpeed);
            case WORK_PRODUCTIVITY:
                return calculateWorkProductivitySuitability(temperature, weatherCondition, humidity);
            case RECREATIONAL:
                return calculateRecreationalSuitability(temperature, weatherCondition, humidity, windSpeed);
            case RELAXATION:
                return calculateRelaxationSuitability(temperature, weatherCondition, humidity);
            case TRAVEL:
                return calculateTravelSuitability(temperature, weatherCondition, humidity, windSpeed);
            case PHOTOGRAPHY:
                return calculatePhotographySuitability(temperature, weatherCondition, humidity, windSpeed, uvIndex);
            case INDOOR_ACTIVITIES:
                return calculateIndoorActivitiesSuitability(temperature, weatherCondition, humidity);
            case OUTDOOR_LEISURE:
                return calculateOutdoorLeisureSuitability(temperature, weatherCondition, humidity, windSpeed, uvIndex);
            default:
                return 0.5; // Neutral suitability
        }
    }

    private double calculateOutdoorExerciseSuitability(float temperature, String weatherCondition, 
                                                     float humidity, float windSpeed, double uvIndex) {
        double score = 1.0;
        
        // Temperature suitability (optimal: 15-25°C)
        if (temperature >= 15 && temperature <= 25) {
            score *= 1.0;
        } else if (temperature >= 10 && temperature <= 30) {
            score *= 0.8;
        } else if (temperature >= 5 && temperature <= 35) {
            score *= 0.6;
        } else {
            score *= 0.3;
        }
        
        // Weather condition impact
        if (weatherCondition != null) {
            String condition = weatherCondition.toLowerCase();
            if (condition.contains("clear") || condition.contains("sunny")) {
                score *= 1.0;
            } else if (condition.contains("cloudy") || condition.contains("overcast")) {
                score *= 0.9;
            } else if (condition.contains("light rain") || condition.contains("drizzle")) {
                score *= 0.4;
            } else if (condition.contains("rain") || condition.contains("storm")) {
                score *= 0.1;
            }
        }
        
        // Humidity impact (optimal: 40-60%)
        if (humidity >= 40 && humidity <= 60) {
            score *= 1.0;
        } else if (humidity >= 30 && humidity <= 70) {
            score *= 0.9;
        } else {
            score *= 0.7;
        }
        
        // Wind speed impact (optimal: < 15 km/h)
        if (windSpeed < 15) {
            score *= 1.0;
        } else if (windSpeed < 25) {
            score *= 0.8;
        } else {
            score *= 0.5;
        }
        
        // UV index impact (avoid very high UV)
        if (uvIndex <= 6) {
            score *= 1.0;
        } else if (uvIndex <= 8) {
            score *= 0.9;
        } else {
            score *= 0.7;
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }

    private double calculateIndoorExerciseSuitability(float temperature, String weatherCondition, float humidity) {
        double score = 0.8; // Base score for indoor activities
        
        // Indoor exercise becomes more appealing when outdoor conditions are poor
        if (weatherCondition != null) {
            String condition = weatherCondition.toLowerCase();
            if (condition.contains("rain") || condition.contains("storm") || 
                condition.contains("snow") || condition.contains("fog")) {
                score = 1.0;
            } else if (condition.contains("cloudy") || condition.contains("overcast")) {
                score = 0.9;
            }
        }
        
        // Extreme temperatures make indoor exercise more appealing
        if (temperature < 5 || temperature > 35) {
            score = Math.max(score, 0.95);
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }

    private double calculateSocialActivitySuitability(float temperature, String weatherCondition, 
                                                    float humidity, float windSpeed) {
        double score = 0.8; // Base score
        
        // Moderate temperatures are better for social activities
        if (temperature >= 18 && temperature <= 28) {
            score *= 1.0;
        } else if (temperature >= 12 && temperature <= 32) {
            score *= 0.9;
        } else {
            score *= 0.7;
        }
        
        // Weather condition impact
        if (weatherCondition != null) {
            String condition = weatherCondition.toLowerCase();
            if (condition.contains("clear") || condition.contains("sunny")) {
                score *= 1.0;
            } else if (condition.contains("cloudy")) {
                score *= 0.9;
            } else if (condition.contains("rain") || condition.contains("storm")) {
                score *= 0.6;
            }
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }

    private double calculateWorkProductivitySuitability(float temperature, String weatherCondition, float humidity) {
        double score = 0.8; // Base score
        
        // Comfortable indoor temperature range
        if (temperature >= 20 && temperature <= 24) {
            score *= 1.0;
        } else if (temperature >= 18 && temperature <= 26) {
            score *= 0.95;
        } else {
            score *= 0.85;
        }
        
        // Slightly higher score for overcast/rainy days (less outdoor distractions)
        if (weatherCondition != null) {
            String condition = weatherCondition.toLowerCase();
            if (condition.contains("overcast") || condition.contains("cloudy")) {
                score *= 1.05;
            } else if (condition.contains("rain") && !condition.contains("heavy")) {
                score *= 1.1;
            }
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }

    private double calculateRecreationalSuitability(float temperature, String weatherCondition, 
                                                  float humidity, float windSpeed) {
        double score = 0.8; // Base score
        
        // Pleasant conditions for recreational activities
        if (temperature >= 16 && temperature <= 26) {
            score *= 1.0;
        } else if (temperature >= 12 && temperature <= 30) {
            score *= 0.9;
        } else {
            score *= 0.7;
        }
        
        // Weather condition impact
        if (weatherCondition != null) {
            String condition = weatherCondition.toLowerCase();
            if (condition.contains("clear") || condition.contains("sunny")) {
                score *= 1.0;
            } else if (condition.contains("cloudy")) {
                score *= 0.85;
            } else if (condition.contains("rain")) {
                score *= 0.5;
            }
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }

    private double calculateRelaxationSuitability(float temperature, String weatherCondition, float humidity) {
        double score = 0.9; // Base score (relaxation is always good)
        
        // Comfortable temperature range
        if (temperature >= 20 && temperature <= 25) {
            score *= 1.0;
        } else if (temperature >= 18 && temperature <= 28) {
            score *= 0.95;
        } else {
            score *= 0.9;
        }
        
        // Some weather conditions enhance relaxation
        if (weatherCondition != null) {
            String condition = weatherCondition.toLowerCase();
            if (condition.contains("rain") && !condition.contains("heavy")) {
                score *= 1.05; // Light rain can be relaxing
            } else if (condition.contains("cloudy") || condition.contains("overcast")) {
                score *= 1.0;
            }
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }

    private double calculateTravelSuitability(float temperature, String weatherCondition, 
                                            float humidity, float windSpeed) {
        double score = 0.8; // Base score
        
        // Moderate conditions are better for travel
        if (temperature >= 15 && temperature <= 30) {
            score *= 1.0;
        } else if (temperature >= 10 && temperature <= 35) {
            score *= 0.9;
        } else {
            score *= 0.6;
        }
        
        // Weather condition impact
        if (weatherCondition != null) {
            String condition = weatherCondition.toLowerCase();
            if (condition.contains("clear") || condition.contains("sunny")) {
                score *= 1.0;
            } else if (condition.contains("cloudy")) {
                score *= 0.9;
            } else if (condition.contains("rain") || condition.contains("storm")) {
                score *= 0.4;
            } else if (condition.contains("fog")) {
                score *= 0.3;
            }
        }
        
        // High wind speed impacts travel
        if (windSpeed > 25) {
            score *= 0.7;
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }

    private double calculatePhotographySuitability(float temperature, String weatherCondition, 
                                                 float humidity, float windSpeed, double uvIndex) {
        double score = 0.8; // Base score
        
        // Comfortable temperature range for outdoor photography
        if (temperature >= 15 && temperature <= 28) {
            score *= 1.0;
        } else if (temperature >= 10 && temperature <= 32) {
            score *= 0.9;
        } else {
            score *= 0.7;
        }
        
        // Weather condition impact (some conditions can be photogenic)
        if (weatherCondition != null) {
            String condition = weatherCondition.toLowerCase();
            if (condition.contains("clear") || condition.contains("sunny")) {
                score *= 1.0;
            } else if (condition.contains("cloudy")) {
                score *= 1.05; // Clouds can provide nice lighting
            } else if (condition.contains("overcast")) {
                score *= 0.9;
            } else if (condition.contains("light rain") || condition.contains("drizzle")) {
                score *= 0.8; // Can create interesting effects
            } else if (condition.contains("heavy rain") || condition.contains("storm")) {
                score *= 0.3;
            }
        }
        
        // Wind speed impact (affects stability)
        if (windSpeed < 15) {
            score *= 1.0;
        } else if (windSpeed < 25) {
            score *= 0.9;
        } else {
            score *= 0.7;
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }

    private double calculateIndoorActivitiesSuitability(float temperature, String weatherCondition, float humidity) {
        double score = 0.8; // Base score
        
        // Indoor activities become more appealing when outdoor conditions are poor
        if (weatherCondition != null) {
            String condition = weatherCondition.toLowerCase();
            if (condition.contains("rain") || condition.contains("storm") || 
                condition.contains("snow") || condition.contains("fog")) {
                score = 1.0;
            } else if (condition.contains("cloudy") || condition.contains("overcast")) {
                score = 0.9;
            }
        }
        
        // Extreme temperatures make indoor activities more appealing
        if (temperature < 10 || temperature > 30) {
            score = Math.max(score, 0.95);
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }

    private double calculateOutdoorLeisureSuitability(float temperature, String weatherCondition, 
                                                    float humidity, float windSpeed, double uvIndex) {
        double score = 0.8; // Base score
        
        // Pleasant temperature range for outdoor leisure
        if (temperature >= 18 && temperature <= 28) {
            score *= 1.0;
        } else if (temperature >= 15 && temperature <= 32) {
            score *= 0.9;
        } else if (temperature >= 10 && temperature <= 35) {
            score *= 0.7;
        } else {
            score *= 0.4;
        }
        
        // Weather condition impact
        if (weatherCondition != null) {
            String condition = weatherCondition.toLowerCase();
            if (condition.contains("clear") || condition.contains("sunny")) {
                score *= 1.0;
            } else if (condition.contains("cloudy")) {
                score *= 0.9;
            } else if (condition.contains("overcast")) {
                score *= 0.8;
            } else if (condition.contains("rain")) {
                score *= 0.2;
            }
        }
        
        // Humidity impact
        if (humidity >= 40 && humidity <= 65) {
            score *= 1.0;
        } else if (humidity >= 30 && humidity <= 75) {
            score *= 0.9;
        } else {
            score *= 0.8;
        }
        
        // Wind speed impact
        if (windSpeed < 20) {
            score *= 1.0;
        } else if (windSpeed < 30) {
            score *= 0.8;
        } else {
            score *= 0.6;
        }
        
        // UV index impact
        if (uvIndex <= 6) {
            score *= 1.0;
        } else if (uvIndex <= 8) {
            score *= 0.9;
        } else {
            score *= 0.8;
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }
}