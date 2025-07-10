package com.locallife.model;

import java.util.Date;

/**
 * Model for tracking media consumption (TV, Movies, Music, YouTube, etc.)
 */
public class MediaConsumption {
    private int id;
    private String date;
    private String mediaType; // tv, movie, music, youtube, podcast, livestream
    private String title;
    private String platform; // netflix, youtube, spotify, etc.
    private int durationMinutes;
    private String genre;
    private String source; // manual, appUsage, browserHistory, nowPlaying
    private Date startTime;
    private Date endTime;
    private String metadata; // JSON string for additional data
    private Date createdAt;
    private Date updatedAt;
    
    // Media metadata details
    private String showId;
    private int season;
    private int episode;
    private String channel;
    private String director;
    private String artist;
    private String album;
    private boolean isRewatch;
    private int rating; // 1-5 star rating
    private String notes;
    
    // Constructors
    public MediaConsumption() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    
    public MediaConsumption(String date, String mediaType, String title, String platform) {
        this();
        this.date = date;
        this.mediaType = mediaType;
        this.title = title;
        this.platform = platform;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    public String getShowId() { return showId; }
    public void setShowId(String showId) { this.showId = showId; }
    
    public int getSeason() { return season; }
    public void setSeason(int season) { this.season = season; }
    
    public int getEpisode() { return episode; }
    public void setEpisode(int episode) { this.episode = episode; }
    
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    
    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }
    
    public boolean isRewatch() { return isRewatch; }
    public void setRewatch(boolean rewatch) { isRewatch = rewatch; }
    
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    // Utility methods
    public boolean isVideo() {
        return "tv".equals(mediaType) || "movie".equals(mediaType) || "youtube".equals(mediaType) || "livestream".equals(mediaType);
    }
    
    public boolean isAudio() {
        return "music".equals(mediaType) || "podcast".equals(mediaType);
    }
    
    public boolean isBingeWatching() {
        return "tv".equals(mediaType) && episode > 1;
    }
    
    public String getFormattedDuration() {
        if (durationMinutes < 60) {
            return durationMinutes + " minutes";
        } else {
            int hours = durationMinutes / 60;
            int minutes = durationMinutes % 60;
            return hours + "h " + minutes + "m";
        }
    }
    
    public String getDisplayTitle() {
        if ("tv".equals(mediaType) && season > 0 && episode > 0) {
            return title + " S" + season + "E" + episode;
        }
        return title;
    }
    
    @Override
    public String toString() {
        return "MediaConsumption{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", title='" + title + '\'' +
                ", platform='" + platform + '\'' +
                ", durationMinutes=" + durationMinutes +
                ", source='" + source + '\'' +
                '}';
    }
}