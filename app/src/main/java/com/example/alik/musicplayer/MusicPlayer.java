package com.example.alik.musicplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * MusicPlayer is the main application class
 * In this application a simple music player is made
 *
 * @author Alikhan Tagybergen
 * @version 1.0
 * @date 24/11/2017
 * @since 1.0
 */

public class  MusicPlayer extends AppCompatActivity {

    public static boolean playButtonEnable = false;
    protected PlayerService playerService;
    protected SeekBar playerBar;
    private Intent mediaIntent;
    private ArrayList<Song> songList = new ArrayList<Song>();
    private Tools appTools = new Tools();
    private boolean serviceConnected = false;
    private ImageButton playButton;
    private TextView songInfo, currTime, totalTime;
    private Handler mHandler = new Handler();
    /**
     * Used to run and update the SeekBar
     */
    public final Runnable seekBarRun = new Runnable() {
        @Override
        public void run() {
            if (serviceConnected && playerService.isPlaying()) {
                playerBar.setProgress(playerService.getCurrentPosition() / 1000);
                String time = appTools.durationString(playerService.getCurrentPosition());
                currTime.setText(time);

            } else if (!serviceConnected) {

            }
            mHandler.postDelayed(this, 500);
        }
    };
    // Sort = 0 is alphabetical sort, sort = 1 is artist.
    private int sort = 0;
    // source = 0 is sd card music files, source = 1 is all audio files from sd card,
    // source = 3 is resource raw folder.
    private int source = 0;
    private SongAdapter songAdt;
    private ListView songViewer;
    /**
     * Used to connect to the service
     */
    private ServiceConnection playerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
            playerService = binder.getBinder();
            playerService.setSongs(songList);
            serviceConnected = true;
            playerService.setMainClass(MusicPlayer.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceConnected = false;
        }
    };

    /**
     * On create method where application is run ( similar to main class in java)
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        permissionCheck();
        songViewer = (ListView) findViewById(R.id.song_display);
        playButton = (ImageButton) findViewById(R.id.play);
        songInfo = (TextView) findViewById(R.id.song_info);
        totalTime = (TextView) findViewById(R.id.total_time);
        playerBar = (SeekBar) findViewById(R.id.seek_bar);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        seekBarListener();
        currTime = (TextView) findViewById(R.id.currentTime);
        songAdt = new SongAdapter(songList, this);
        songViewer.setAdapter(songAdt);
        loadSongs();
    }

    /**
     * When applicaiton creates or resumes its called
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (mediaIntent == null) {
            mediaIntent = new Intent(this, PlayerService.class);
            bindService(mediaIntent, playerConnection, Context.BIND_AUTO_CREATE);
            startService(mediaIntent);
        }
    }

    /**
     * Invoked when application is destroyed
     */
    @Override
    protected void onDestroy() {
        serviceConnected = false;
        mHandler.removeCallbacks(seekBarRun);
        mHandler.removeCallbacksAndMessages(seekBarRun);
        stopService(mediaIntent);
        playerService = null;
        unbindService(playerConnection);
        super.onDestroy();
    }

    /**
     * Creates the toolbar menu
     *
     * @param menu takes the menu
     * @return returns inflated menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    /**
     * Listener when user presses menu buttons
     *
     * @param item menu item
     * @return boolean if action was successful
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_source:
                songList.clear();
                //Get music from SD card
                if (source == 0) {
                    // checks for permission.
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        permissionCheck();
                        loadSongs();
                    } else {
                        appTools.getSongs(songList, getApplicationContext(), source);
                        source++;
                        Toast.makeText(getApplicationContext(), "Loaded Music Files from SD card", Toast.LENGTH_LONG).show();
                    }
                    // Get all audio files from SD card
                } else if (source == 1) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        permissionCheck();
                        loadSongs();
                    } else {

                        appTools.getSongs(songList, getApplicationContext(), source);
                        source++;
                        Toast.makeText(getApplicationContext(), "Loaded All Audio Files from SD card", Toast.LENGTH_LONG).show();
                    }
                    // Get audio files from resource raw file
                } else if (source == 2) {
                    appTools.getSongs(songList, getApplicationContext(), source);
                    source = 0;
                    Toast.makeText(getApplicationContext(), "Application Audio Files", Toast.LENGTH_LONG).show();
                }
                playerService.setSongs(songList);
                songAdt.notifyDataSetChanged();
                return true;

            case R.id.action_sort:
                //sort alphabetical
                if (sort == 0) {
                    appTools.sortArtist(songList);
                    Toast.makeText(getApplicationContext(), "Sorted by Artist", Toast.LENGTH_SHORT).show();
                    sort = 1;

                    // sort by artist
                } else if (sort == 1) {
                    appTools.sortAlphabetical(songList);
                    Toast.makeText(getApplicationContext(), "Sorted by Title", Toast.LENGTH_SHORT).show();
                    sort = 0;
                }
                playerService.setSongs(songList);
                songAdt.notifyDataSetChanged();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }


    /**
     * Check if permission was granted, if not requests them.
     */
    public void permissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }


    /**
     * Loads the songs into the ListView,
     * if permission was not granted it loads from resource raw.
     */
    public void loadSongs() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            source = 2;
            appTools.getSongs(songList, this, source);
            Toast.makeText(getApplicationContext(), "Loaded Application Audio Files", Toast.LENGTH_LONG).show();
            source = 0;
        } else {
            appTools.getSongs(songList, this, source);
            source = 1;
        }
    }

    /**
     * Updates values of TextView and other views to match the song information.
     */
    public void updateValues() {
        String dur = appTools.durationString(playerService.getCurrSong().getDuration());
        totalTime.setText(dur);
        String currPlaying = playerService.getCurrSong().getTitle() + " By: " + playerService.getCurrSong().getArtist();
        songInfo.setText(currPlaying);
        playerBar.setMax(playerService.getCurrSong().getDuration());
        updatePlayerBar();
    }

    /**
     * Updates the SeekBar values by running runnable in UiThread and
     * uses Handler to handle the thread.
     */
    public void updatePlayerBar() {
        MusicPlayer.this.runOnUiThread(seekBarRun);
        mHandler.postDelayed(seekBarRun, 500);
    }

    /**
     * SeekBar listener which runs on thread, it updates SeekBar values as
     * MediaPlayer is playing.
     */
    public void seekBarListener() {
        playerBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean user) {

                seekBar.setMax(playerService.getCurrSong().getDuration() / 1000);
                mHandler.removeCallbacks(seekBarRun);
                if (user) {
                    playerService.seekTo(progress * 1000);
                    currTime.setText(appTools.durationString(progress));
                    updatePlayerBar();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /**
     * When song was selected and clicked, loads its position and then
     * loads the song into MediaPlayer which runs in service.
     *
     * @param view associated with the click
     */
    public void songClicked(View view) {
        playerService.setSong(Integer.parseInt(view.getTag().toString()));
        playerService.play();
        playButtonEnable = true;
        playButton.setImageResource(R.drawable.pause);
        updateValues();
    }

    /**
     * Play button functionality, it can pause and resume if song was loaded.
     *
     * @param view of playButton
     */
    public void playButton(View view) {
        if (playButtonEnable == true) {
            if (playerService.isPlaying()) {
                playerService.pause();
                playButton.setImageResource(R.drawable.play);
                updateValues();
            } else if (!playerService.isPlaying()) {
                playerService.start();
                playButton.setImageResource(R.drawable.pause);
                updateValues();
            }
        } else if (playButtonEnable == false) {
            Toast.makeText(getApplicationContext(), "No Song Selected", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Skips to next song using skipforw button
     *
     * @param view associated with skipForw button
     */
    public void skipForw(View view) {
        playerService.playNext();
        updateValues();
    }

    /**
     * Skips to previous song using skipBack button
     *
     * @param view associated with skipBack button
     */
    public void skipBack(View view) {
        playerService.playPrev();
        updateValues();
    }

    /**
     * Replay button that will loop current song
     *
     * @param view associated with replay button
     */
    public void replay(View view) {
        if (playerService.isLooping()) {
            playerService.setLooping(false);
            view.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        } else if (!playerService.isLooping()) {
            playerService.setLooping(true);
            view.setBackgroundColor(Color.rgb(255, 0, 0));
        }
    }

    /**
     * Shuffle button that will shuffle the order the songs are played
     *
     * @param view associated with shuffle button
     */
    public void shuffle(View view) {
        if (playerService.isShuffleToggled()) {
            playerService.setShuffleToggle(false);
            view.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        } else if (!playerService.isShuffleToggled()) {
            playerService.shuffleSongs();
            playerService.setShuffleToggle(true);
            view.setBackgroundColor(Color.rgb(255, 0, 0));
        }

    }

}
