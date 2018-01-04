package com.andyisdope.headsuppoker;

/*
  Created by Andy on 9/11/2017.
 */

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

class ListViewAdapter extends BaseAdapter {

    private final ArrayList<HashMap<String, String>> list;
    private final Activity activity;
    private TextView Table;
    private TextView Game;
    private TextView Stakes;
    private TextView Players;

    public ListViewAdapter(Activity activity, ArrayList<HashMap<String, String>> list) {
        super();
        this.activity = activity;
        this.list = list;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub


        LayoutInflater inflater = activity.getLayoutInflater();

        if (convertView == null) {

            convertView = inflater.inflate(R.layout.columns, null);

            Table = convertView.findViewById(R.id.Table);
            Game = convertView.findViewById(R.id.Game);
            Stakes = convertView.findViewById(R.id.Stakes);
            Players = convertView.findViewById(R.id.Players);

        }

        HashMap<String, String> map = list.get(position);
        Table.setText(map.get("Table"));
        Game.setText(map.get("Game"));
        Stakes.setText(map.get("Stakes"));
        Players.setText(map.get("Players"));

        return convertView;
    }

}