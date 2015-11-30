package com.jj.mysimpleplayer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jj.mysimpleplayer.R;
import com.jj.mysimpleplayer.models.Playlist;

import java.util.ArrayList;

public class DrawerAdapter extends BaseAdapter {
    private ArrayList<String> menuItems;
    private LayoutInflater drawerInflater;

    static class DrawerItem {
        TextView drawerItemNameView;
    }

    public DrawerAdapter(Context c, String[] menu) {
        menuItems = new ArrayList<>();
        for (String item : menu) {
            menuItems.add(item);
        }

        drawerInflater = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return menuItems.size();
    }

    @Override
    public Object getItem(int id) {
        return null;
    }

    @Override
    public long getItemId(int id) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DrawerItem drawerItem = new DrawerItem();

        if (convertView == null) {
            convertView = drawerInflater.inflate(R.layout.drawer_item, parent, false);

            drawerItem.drawerItemNameView = (TextView) convertView.findViewById(R.id.item_text);

            convertView.setTag(drawerItem);
        } else {
            drawerItem = (DrawerItem) convertView.getTag();
        }

        String currentItem = menuItems.get(position);

        if (currentItem != null) {
            drawerItem.drawerItemNameView.setTag(position);
            drawerItem.drawerItemNameView.setText(currentItem);
        }
        return convertView;
    }
}
