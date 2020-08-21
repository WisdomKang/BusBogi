package com.project.busbogi.main.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.project.busbogi.R;

import java.util.ArrayList;

public class BusListAdapter extends BaseAdapter {
    private ArrayList<String> busNumberList;

    @Override
    public int getCount() {
        return busNumberList.size();
    }

    @Override
    public Object getItem(int i) {
        return busNumberList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        Context context = viewGroup.getContext();

        if( convertView == null ) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.bus_list_layout, viewGroup, false);
        }
        CheckableLinearLayout linearLayout = (CheckableLinearLayout) convertView;
        TextView busNumberView = convertView.findViewById(R.id.busNumber);
        TextView busStatusView = convertView.findViewById(R.id.busStatus);

        busNumberView.setText(busNumberList.get(position));
        return convertView;
    }

    public void setBusNumberList(ArrayList<String> arrayList){
        this.busNumberList = arrayList;
    }
    public void addNumber(String number){
        busNumberList.add(number);
    }
}

