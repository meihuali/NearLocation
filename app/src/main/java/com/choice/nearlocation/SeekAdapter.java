package com.choice.nearlocation;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;

import java.util.ArrayList;
import java.util.List;


 class SeekAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<PoiItem> poiItems = new ArrayList<>();
    private Activity activity;

    SeekAdapter(Activity activity, List<PoiItem> poiItems) {
        this.activity = activity;
        this.poiItems = poiItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        ViewHolder viewHolder=(ViewHolder)holder;
        viewHolder.tvName.setText(poiItems.get(position).getTitle());
        viewHolder.tvAddress.setVisibility(View.VISIBLE);
        viewHolder.tvAddress.setText(poiItems.get(position).getCityName()+poiItems.get(position).getAdName()+poiItems.get(position).getSnippet());
        viewHolder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("PoiItem", poiItems.get(position));
                activity.setResult(1002, intent);
                activity.finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return poiItems == null ? 0 : poiItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvAddress;
        LinearLayout mLinearLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            tvName= (TextView) itemView.findViewById(R.id.tv_item_location);
            mLinearLayout=(LinearLayout)itemView.findViewById(R.id.ll_item_location);
            tvAddress= (TextView) itemView.findViewById(R.id.tv_item_seek);
        }
    }
}
