package com.choice.nearlocation;
import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.amap.api.services.core.PoiItem;

import java.util.HashMap;
import java.util.List;

class LocationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<PoiItem> list;
    private Activity activity;
    private static HashMap<String, Boolean> hashMap;
    LocationAdapter(Activity activity, List<PoiItem> list,HashMap<String, Boolean> hashMap) {
        this.activity = activity;
        this.list = list;
        this.hashMap=hashMap;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        boolean b = ((LocationActivity) activity).isFirst();
        if (b && position == 0) {
            hashMap.put("0", true);
        }
        ViewHolder viewHolder=  (ViewHolder)holder;
        viewHolder.mTextView.setText(list.get(position).getTitle());
        viewHolder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LocationActivity)activity).itemOnclick(list.get(position),false);
                for (String key : hashMap.keySet()) {
                    hashMap.put(key, false);
                }
                hashMap.put(String.valueOf(position), true);
                notifyDataSetChanged();
            }
        });

        int flag;
        if (hashMap.get(String.valueOf(position)) == null || !hashMap.get(String.valueOf(position))) {
            flag = View.GONE;
            hashMap.put(String.valueOf(position), false);
        } else {
            flag = View.VISIBLE;
        }
        viewHolder.mImageView.setVisibility(flag);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

     static class ViewHolder extends RecyclerView.ViewHolder {
          TextView mTextView;
         LinearLayout mLinearLayout;
         ImageView mImageView;
        ViewHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.tv_item_location);
            mLinearLayout=(LinearLayout)view.findViewById(R.id.ll_item_location);
            mImageView= (ImageView) view.findViewById(R.id.img_item_location);
        }
    }
}
