package cn.zheft.www.zheft.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.model.KeyValuePair;

/**
 * Created by Lin on 2017/3/16.
 */

public class OrderDetailAdapter extends BaseAdapter {

    Context mContext;
    private List<KeyValuePair> valueMaps;


    public OrderDetailAdapter(Context context, List<KeyValuePair> valueMap) {
        mContext = context;
        valueMaps = valueMap;
    }

    @Override
    public int getCount() {
        return valueMaps == null ? 0 : valueMaps.size();
    }

    @Override
    public Object getItem(int i) {
        return valueMaps.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(viewHolder == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_order, null);
            viewHolder = new ViewHolder();
            viewHolder.key = (TextView) convertView.findViewById(R.id.tv_item_order_key);
            viewHolder.value = (TextView) convertView.findViewById(R.id.tv_item_order_value);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        KeyValuePair info = (KeyValuePair) getItem(position);
        viewHolder.key.setText(info.getKey());
        viewHolder.value.setText(info.getValue());

        return convertView;
    }

    public static class ViewHolder {
        TextView key;
        TextView value;
    }

}
