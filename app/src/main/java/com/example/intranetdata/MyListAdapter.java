package com.example.intranetdata;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MyListAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<String> title;
    private final ArrayList<Integer> icon;
    private final ArrayList<String> date;
    private final ArrayList<String> time;

    MyListAdapter(Activity context, ArrayList<String> title, ArrayList<Integer> iconid, ArrayList<String> date, ArrayList<String> time) {
        super(context, R.layout.mylist, title);
        // TODO Auto-generated constructor stub

        this.context = context;
        this.title = title;
        this.icon = iconid;
        this.date = date;
        this.time = time;
    }

    @NonNull
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        @SuppressLint({"ViewHolder", "InflateParams"}) View rowView = inflater.inflate(R.layout.mylist, null,true);

        TextView titleText = (TextView) rowView.findViewById(R.id.myListTitle);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.myListIcon);
        TextView dateText = (TextView) rowView.findViewById(R.id.myListDate);
        TextView timeText = (TextView) rowView.findViewById(R.id.myListTime);

        titleText.setText(title.get(position));
        imageView.setImageResource(icon.get(position));
        dateText.setText(date.get(position));
        timeText.setText(time.get(position));

        return rowView;
    }
}
