package com.gachon.wifi_positioning_iot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class recyclerViewAdapter extends RecyclerView.Adapter<recyclerViewAdapter.ViewHolder> {

private ArrayList<RssiItem> mRssiItem;

@NonNull
@Override
public recyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
        return new ViewHolder(view);
        }

    @Override
    public void onBindViewHolder(@NonNull recyclerViewAdapter.ViewHolder holder, int position) {
            holder.onBind(mRssiItem.get(position));
            }

    public void setRssiList(ArrayList<RssiItem> list){
            this.mRssiItem = list;
            notifyDataSetChanged();
            }

    @Override
    public int getItemCount() {
            return mRssiItem.size();
            }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView bssid;
        TextView rssi;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.name);
            bssid = (TextView) itemView.findViewById(R.id.bssid);
            rssi = (TextView) itemView.findViewById(R.id.rssiValue);
        }

        void onBind(RssiItem item){
            name.setText(item.getName());
            bssid.setText(item.getBssid());
            rssi.setText(item.getRssi());

        }
    }
}
