package com.example.alik.musicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * PlayerService class will play MediaPLayer in a service
 * Service is used so that MediaPlayer is able to run when
 * the application is in background.
 *
 * @author Alikhan Tagybergen
 * @version 1.0
 * @date 25/11/2017
 * @since 1.0
 */

public class PlayerService extends Service {

    private final IBinder bind = new PlayerBinder();
    private MediaPlayer mediaPlayer;
    /**
     * Listener to ignore errors when media player  encounters an error.
     */
    MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int a, int b) {
            return false;
        }
    };
    private ArrayList<Song> songList;
    private ArrayList<Integer> shuffledList;
    private int listPos, shufflePos;
    private Song currSong;
    /**
     * Listener when the MediaPlayer is prepared.
     * Starts playing the selected song and
     * makes a notification using a builder.
     */

    MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
            Intent notifIntent = new Intent(getApplicationContext(), MusicPlayer.class);
            notifIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendInt = PendingIntent.getActivity(getApplicationContext(), 0,
                    notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Notification.Builder builder = new Notification.Builder(getApplicationContext());
            builder.setContentIntent(pendInt)
                    .setSmallIcon(R.drawable.music_player)
                    .setTicker(currSong.getTitle())
                    .setContentTitle("Currently Playing")
                    .setContentText(currSong.getTitle())
                    .setOngoing(true);
            Notification notif = builder.build();
            startForeground(1, notif);
        }
    };
    private boolean shuffleToggle = false;
    private Tools appTools = new Tools();
    private MusicPlayer mainClass;
    /**
     * Listener when the song completes playback.
     * Plays next if replay is not enabled (not looping).
     * Plays song again if replay is enabled.
     */
    MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (!mp.isLooping()) {
                if (mediaPlayer.getCurrentPosition() > 0) {
                    mediaPlayer.reset();
                    playNext();
                }
            } else if (mp.isLooping()) {
                play();
            }
            mainClass.updateValues();
        }
    };

    /**
     * Set the ArrayList of songs from the musicplayer class
     *
     * @param listSongs
     */
    public void setSongs(ArrayList<Song> listSongs) {
        songList = listSongs;
    }

    /**
     * Return communication channel to the service
     *
     * @param intent The intent that is used to bind to that service
     * @return communication channel
     */
    @Override
    public IBinder onBind(Intent intent) {
        return bind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaPlayer.stop();
        mediaPlayer.release();
        return false;
    }

    /**
     * Initialise service on creation
     */
    public void onCreate() {
        super.onCreate();
        listPos = 0;
        mediaPlayer = new MediaPlayer();
        initMediaPlayer();
    }

    /**
     * Remove notification when service is destroyed
     */
    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    /**
     * Play the music file ASynchronously in the service  using MediaPlayer Class
     */
    public void play() {
        mediaPlayer.reset();
        currSong = songList.get(listPos);
        Uri pathUri = Uri.parse(currSong.getPath());
        try {
            mediaPlayer.setDataSource(getApplicationContext(), pathUri);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "File Path Error", Toast.LENGTH_LONG).show();
        }
        mediaPlayer.prepareAsync();
    }

    /**
     * Play next song if last song pause and reset the player
     */
    public void playNext() {
        if (shuffleToggle == true) {
            if (shufflePos < shuffledList.size()) {
                shufflePos++;
                listPos = shuffledList.get(shufflePos);
                play();
            } else {
                mediaPlayer.pause();
                mediaPlayer.stop();
                mediaPlayer.reset();
                Toast.makeText(getApplicationContext(), "Last Song", Toast.LENGTH_LONG).show();
            }
        } else if (shuffleToggle == false) {
            if (listPos < songList.size() - 1) {
                listPos++;
                play();
            } else {
                mediaPlayer.pause();
                mediaPlayer.stop();
                mediaPlayer.reset();
                Toast.makeText(getApplicationContext(), "Last Song", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Shuffle songs using the method in Tool class.
     */
    public void shuffleSongs() {
        shuffledList = appTools.shuffleSongs(listPos, songList);
        shufflePos = 0;
    }

    /**
     * Plays the previous song in order
     * if first song plays the first song again.
     */
    public void playPrev() {
        if (shuffleToggle == true) {
            if (shufflePos > 0) {
                shufflePos--;
                listPos = shuffledList.get(shufflePos);
                play();
            } else if (shufflePos == 0) {
                listPos = shuffledList.get(shufflePos);
                play();
            }
        } else if (shuffleToggle == false) {
            if (listPos > 0) {
                listPos--;
                play();
            } else if (listPos == 0) {
                listPos = 0;
                play();
            }
        }
    }

    /**
     * Used to initialise the media player.
     */
    public void initMediaPlayer() {
        mediaPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaPlayer.setOnPreparedListener(onPreparedListener);
        mediaPlayer.setOnCompletionListener(onCompletionListener);
        mediaPlayer.setOnErrorListener(onErrorListener);
        mediaPlayer.setLooping(false);
    }

    /**
     * Check if the MediaPlayer is looping the song file
     *
     * @return boolean if it is looping
     */
    public boolean isLooping() {
        return mediaPlayer.isLooping();
    }

    /**
     * Set looping of the MediaPlayer
     *
     * @param looping
     */
    public void setLooping(boolean looping) {
        mediaPlayer.setLooping(looping);
    }

    /**
     * Get song that is currently playing
     *
     * @return the song that is currently playing
     */
    public Song getCurrSong() {
        return currSong;
    }

    /**
     * Get the timer position of the song that is currently playing
     *
     * @return int of time in milliseconds
     */
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * Set the MainActivity class MusicPlayer pointer in this class.
     *
     * @param mainClass takes the pointer of MainActivity lass
     */
    public void setMainClass(MusicPlayer mainClass) {
        this.mainClass = mainClass;
    }

    /**
     * Check if the song is playing in MediaPlayer
     *
     * @return boolean if it is playing
     */
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    /**
     * Method to pause the MediaPlayer
     */
    public void pause() {
        mediaPlayer.pause();
    }

    /**
     * Go to specific time in MediaPlayer
     *
     * @param pos int position in milliseconds
     */
    public void seekTo(int pos) {
        mediaPlayer.seekTo(pos);
    }

    /**
     * Resume playing the song in MediaPlayer
     */
    public void start() {
        mediaPlayer.start();
    }

    /**
     * Set the song to play in the media player.
     *
     * @param songIndex list of songs
     */
    public void setSong(int songIndex) {
        listPos = songIndex;
    }

    /**
     * Boolean method to check whether shuffle is toggled by user
     *
     * @return boolean of shuffle toggle
     */
    public boolean isShuffleToggled() {
        return shuffleToggle;
    }

    /**
     * Set the shuffle toggle
     *
     * @param shuffleToggle boolean to set shuffle on or off.
     */
    public void setShuffleToggle(boolean shuffleToggle) {
        this.shuffleToggle = shuffleToggle;
    }

    /**
     * Class of the binder
     */
    public class PlayerBinder extends Binder {
        PlayerService getBinder() {
            return PlayerService.this;
        }
    }
}
