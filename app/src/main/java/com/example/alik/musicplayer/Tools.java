package com.example.alik.musicplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

/**
 * Various tools that will be used by other classes are located in this class
 *
 * @author Alikhan Tagybergen
 * @version 1.0
 * @date 25/11/2017
 * @since 1.0
 */

public class Tools {
    private final ArrayList<Integer> internalMusicID = new ArrayList<Integer>();

    /**
     * Sets from milliseconds to m:ss format to display in views.
     *
     * @param duration duration of the song
     */

    public static String durationString(int duration) {
        String dur = String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        );
        return dur;
    }

    /**
     * Sorts songs in Alphabetical order
     *
     * @param songList the list of songs
     */

    public static void sortAlphabetical(ArrayList<Song> songList) {
        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().toUpperCase().compareTo(b.getTitle().toUpperCase());
            }
        });
    }

    /**
     * Sorts songs by artist Alphabetically
     *
     * @param songList the list of songs
     */
    public static void sortArtist(ArrayList<Song> songList) {
        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getArtist().toUpperCase().compareTo(b.getArtist().toUpperCase());
            }
        });
    }

    /**
     * Used to shuffle the songs when shuffle button is activated.
     *
     * @param listPos  position of the song in ListView
     * @param songList list of songs
     * @return shuffled listPos order of songs to play
     */
    public static ArrayList<Integer> shuffleSongs(int listPos, ArrayList<Song> songList) {
        ArrayList<Integer> songOrder = new ArrayList<>();
        for (int i = 0; i < songList.size(); i++) {
            if (i != listPos) {
                songOrder.add(i);
            }
        }
        Collections.shuffle(songOrder);
        songOrder.add(listPos);
        Collections.reverse(songOrder);
        return songOrder;
    }


    /**
     * Gets the audio or music files from the external storage using a ContentResolver
     * and Cursor to query for the desired audio files
     *
     * @param songList ArrayList where music files from storage are recorded
     * @param context  Application context
     * @param isMusic  Used to vary source from external music files, all audio files
     *                 and local app storage
     */
    public void getSongs(ArrayList<Song> songList, Context context, int isMusic) {
        ContentResolver songResolver = context.getContentResolver();
        Cursor songCursor;
        if (isMusic == 0) {
            // Get only music files from external storage
            songCursor = songResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Audio.Media.IS_MUSIC, null, null);
        } else if (isMusic == 1) {
            // Get all audio files from external storage
            songCursor = songResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, null);
        } else {
            // Get audio files from local Android res/raw folder
            getInternalSongs(songList, context);
            songCursor = null;
        }
        if (songCursor == null) {

        } else if (!songCursor.moveToFirst()) {
            context = context.getApplicationContext();
            Toast toast = Toast.makeText(context, "No Audio Files Found", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            //Get information about audio files
            while (songCursor.moveToNext()) {
                String fullPath = songCursor.getString(songCursor.getColumnIndex
                        (MediaStore.Audio.Media.DATA));
                String title = songCursor.getString(songCursor.getColumnIndex
                        (MediaStore.Audio.Media.TITLE));
                String artist = songCursor.getString(songCursor.getColumnIndex
                        (MediaStore.Audio.Media.ARTIST));
                int duration = songCursor.getInt(songCursor.getColumnIndex
                        (MediaStore.Audio.Media.DURATION));
                songList.add(new Song(title, artist, fullPath, duration));
            }
            sortAlphabetical(songList);
        }
    }

    /**
     * Receives audio files from raw folder and gets information about them using
     * MediaMetadataRetriever.
     *
     * @param songList ArrayList where music files are recorded.
     * @param context  Application context.
     */
    public void getInternalSongs(ArrayList<Song> songList, Context context) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        getRawMusic();
        for (int i = 0; i < internalMusicID.size(); i++) {
            String fullPath = "android.resource://" + context.getPackageName() + "/" + internalMusicID.get(i);
            Uri uri = Uri.parse(fullPath);
            mmr.setDataSource(context, uri);
            String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist;
            // If no data about artist is present set it to unknown.
            if (mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) == null) {
                artist = "<unknown>";
            } else {
                artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            }
            int dur = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            songList.add(new Song(title, artist, fullPath, dur));
        }
        sortAlphabetical(songList);
    }

    /**
     * Gets the music files from res/raw folder of android app
     * and puts them in an ArrayList.
     */
    public void getRawMusic() {
        internalMusicID.clear();
        internalMusicID.add(R.raw.a491_10);
        internalMusicID.add(R.raw.bach_bwv924_breemer);
        internalMusicID.add(R.raw.bagatelle_in_a_minor_woo_59);
        internalMusicID.add(R.raw.harmoniedesdeuxrives_spanishdance);
        internalMusicID.add(R.raw.harmony_of_the_angels_op_100_no_21);
        internalMusicID.add(R.raw.missasanctinicolaigloria);
        internalMusicID.add(R.raw.mozart_eine_kleine06);
    }

}
