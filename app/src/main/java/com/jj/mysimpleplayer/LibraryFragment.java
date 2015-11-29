package com.jj.mysimpleplayer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.jj.mysimpleplayer.adapters.SongAdapter;


public class LibraryFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_library, container, false);

        ListView libraryView = (ListView)rootView.findViewById(R.id.library_list);

        // Populate library list with songs
        SongAdapter songAdapter = new SongAdapter(getActivity(), MainActivity.songLibrary, false);
        libraryView.setAdapter(songAdapter);

        return rootView;
    }
}
