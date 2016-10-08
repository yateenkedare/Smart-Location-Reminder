package com.rozrost.www.smartlocationreminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;


import java.util.ArrayList;

/**
 * Created by Yateen Kedare on 5/13/2016.
 */
public class MyListAdapter extends BaseAdapter {
    private ArrayList<SingleRow> list;
    private Context context;
    DatabaseHelper mDatabaseHelper;
    GeofenceFunctions mGeofenceFunctions;
    public MyListAdapter(ArrayList<SingleRow> list, Context context, GeofenceFunctions GFF) {
        this.list = new ArrayList<SingleRow>();
        this.list = list;
        this.context = context;
        mGeofenceFunctions = GFF;
        mDatabaseHelper = new DatabaseHelper(context);
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
    public long getItemId(int position) {
        return 0;
    }

    class MyViewHolder {
        TextView title,time;
        final Switch mSwitch;
        ImageView imageView ;

        MyViewHolder(View v) {
            mSwitch = (Switch) v.findViewById(R.id.switch1);
            title = (TextView) v.findViewById(R.id.textView);
            imageView = (ImageView) v.findViewById(R.id.imageView);
            time = (TextView) v.findViewById(R.id.timeTextView);
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        MyViewHolder holder = null;
        if(row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.my_list_view, parent, false);
            holder = new MyViewHolder(row);
            row.setTag(holder);
        }
        else {
            holder = (MyViewHolder) row.getTag();
        }


        final SingleRow temp = list.get(position);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(temp.status){
                    mGeofenceFunctions.removeGeofence(temp.PrimaryKey, false);
                }
                mDatabaseHelper.deleteData(temp.PrimaryKey);
                list.remove(position);
                notifyDataSetChanged();
            }
        });

        holder.title.setText(temp.name);
        holder.time.setText(temp.time);
        holder.mSwitch.setChecked(temp.status);

        holder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    mGeofenceFunctions.addExistingGeofence(temp.PrimaryKey, true);
                }
                else {
                    mGeofenceFunctions.removeGeofence(temp.PrimaryKey, true);
                }
            }
        });
        return row;
    }

}