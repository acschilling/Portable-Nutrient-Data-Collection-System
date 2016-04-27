package com.ndca.nutrientdatacollectionapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by polar_cyclone12 on 4/25/2016.
 */
public class ListDataAdapter extends ArrayAdapter {

    List list = new ArrayList();

    public ListDataAdapter(Context context, int resource) {
        super(context, resource);
    }

    static class LayoutHandler{
        TextView name,id,location;
    }

    @Override
    public void add(Object object){
        super.add(object);
        list.add(object);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        LayoutHandler handler;
        if(row == null){
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.activity_row,parent,false);
            handler = new LayoutHandler();
            handler.name = (TextView) row.findViewById(R.id.sample_name);
            handler.id = (TextView) row.findViewById(R.id.sample_id);
            handler.location = (TextView) row.findViewById(R.id.sample_location);
            row.setTag(handler);
        }
        else{
            handler = (LayoutHandler) row.getTag();
        }
        DataProvidor providor = (DataProvidor) this.getItem(position);
        handler.name.setText(providor.getName());
        handler.id.setText(providor.getId());
        handler.location.setText(providor.getLocation());
        return row;
    }
}
