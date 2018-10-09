package com.example.alik.musicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Custom adapter that is used to project ListView
 * and inflate elements in it.
 *
 * @author Alikhan Tagybergen
 * @version 1.0
 * @date 24/11/2017
 * @since 1.0
 */

public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songList;
    private LayoutInflater songInf;

    /**
     * Constructor of SongAdapter class
     *
     * @param songList list of songs
     * @param context  application context
     */
    public SongAdapter(ArrayList<Song> songList, Context context) {
        this.songList = songList;
        songInf = LayoutInflater.from(context);
    }

    /**
     * Get how many items are in ListView
     *
     * @return the amount of songs in ListView
     */
    @Override
    public int getCount() {
        return songList.size();
    }

    //Default override
    @Override
    public Object getItem(int arg0) {
        return null;
    }

    // Default override
    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    /**
     * Used to project content with songs in the ListView.
     *
     * @param position    Position of the item in the adapter
     * @param convertView View of the activity
     * @param parent      The parent that the view in the adaptor is attached to
     * @return the RelativeLayout view that is used in ListView to be projected in parent
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout songLay = (RelativeLayout) songInf.inflate
                (R.layout.song, parent, false);
        TextView songView = (TextView) songLay.findViewById(R.id.song_title);
        TextView artistView = (TextView) songLay.findViewById(R.id.song_desc);
        Song currSong = songList.get(position);
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist() + " Duration: " + Tools.durationString(currSong.getDuration()));
        songLay.setTag(position);
        return songLay;
    }

}