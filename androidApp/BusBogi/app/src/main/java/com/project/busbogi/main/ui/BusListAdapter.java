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
    private ArrayList<Integer> busNumberList;

    @Override
    public int getCount() {
        return busNumberList.size();
    }

    @Override
    public Object getItem(int position) {
        return busNumberList.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
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

        busNumberView.setText(busNumberList.get(position)+"");
        return convertView;
    }


    public void setBusNumberList(ArrayList<Integer> arrayList){
        this.busNumberList = arrayList;
    }
}

