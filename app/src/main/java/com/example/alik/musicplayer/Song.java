package com.example.alik.musicplayer;

/**
 * Song Class to represent song information
 *
 * @author Alikhan Tagybergen
 * @version 1.0
 * @date 24/11/2017
 * @since 1.0
 */

public class Song {
    /**
     * Title of the song
     */
    private String title;
    /**
     * Song artist
     */
    private String artist;
    /**
     * Total duration of the song
     */
    private int duration;
    /**
     * Song path to be used by MediaPlayer
     */
    private String path;

    /**
     * Constructior that is used to build the song object
     *
     * @param title    title of the song
     * @param artist   artist of the song
     * @param duration duration of the song
     */
    public Song(String title, String artist, String path, int duration) {
        this.title = title;
        this.artist = artist;
        this.path = path;
        this.duration = duration;
    }

    /**
     * Get the title of the song
     *
     * @return title of the song
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the song
     *
     * @param title of the song
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the artist of the song
     *
     * @return artist string
     */
    public String getArtist() {
        return artist;
    }

    /**
     * Set the artist of the song
     *
     * @param artist of the song
     */
    public void setArtist(String artist) {
        this.artist = artist;
    }

    /**
     * Get the duration of the song
     *
     * @return duration int
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Set duration of the song
     *
     * @param duration of the song
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Get uri path in string
     *
     * @return path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets uri path in string format
     *
     * @param path of the song
     */
    public void setPath(String path) {
        this.path = path;
    }
}

