package com.jj.mysimpleplayer;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.jj.mysimpleplayer.constants.Constants;

public class PlaylistsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_playlists, container, false);

        LinearLayout addNewPlaylist = (LinearLayout) rootView.findViewById(R.id.add_new_playlist_layout);
        addNewPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewPlaylistClicked(view);
            }
        });

        return rootView;
    }

    private void addNewPlaylistClicked(View view) {
        Intent addNewPlaylistIntent = new Intent(view.getContext(), NewPlaylistActivity.class);
        startActivity(addNewPlaylistIntent);
    }
}
