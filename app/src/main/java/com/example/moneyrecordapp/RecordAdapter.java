package com.example.moneyrecordapp;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RecordAdapter extends ArrayAdapter<Record> {
    private static class ViewHolder {
        TextView tvAmount, tvType, tvDate, tvDescription;
    }
    private OnRecordClickListener onRecordClickListener;
    private OnRecordLongClickListener onRecordLongClickListener;

    public RecordAdapter(Context context, List<Record> records) {
        super(context, 0, new ArrayList<>(records));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Record record = getItem(position);
        ViewHolder holder;
        if (record == null) {
            Log.e("RecordAdapter", "Item at position " + position + " is null");
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(android.R.layout.simple_list_item_1, parent, false);
                ((TextView) convertView).setText("空记录");
            }
            return convertView;
        }
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_record, parent, false);

            holder = new ViewHolder();
            holder.tvAmount = convertView.findViewById(R.id.tvAmount);
            holder.tvType = convertView.findViewById(R.id.tvType);
            holder.tvDate = convertView.findViewById(R.id.tvDate);
            holder.tvDescription = convertView.findViewById(R.id.tvDescription);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        boolean isExpense = record.getType().equals("支出");
        String amountText = (isExpense ? "-" : "+") + "¥" + record.getAmount();
        holder.tvAmount.setText(amountText);
        holder.tvAmount.setTextColor(isExpense ? Color.RED : Color.GREEN);//指出红，收入绿
        holder.tvType.setText(record.getType());
        holder.tvDate.setText(record.getDate());
        holder.tvDescription.setText(record.getDescription());
        // 点击和长按事件
        final int pos = position;
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onRecordClickListener != null) {
                    onRecordClickListener.onRecordClick(pos, record);
                }
            }
        });
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onRecordLongClickListener != null) {
                    onRecordLongClickListener.onRecordLongClick(pos, record);
                    return true;
                }
                return false;
            }
        });

        return convertView;
    }


    public void updateData(List<Record> newData) {
        clear();
        addAll(newData);
        notifyDataSetChanged();
    }
    public interface OnRecordClickListener {
        void onRecordClick(int position, Record record);
    }
    public interface OnRecordLongClickListener {
        void onRecordLongClick(int position, Record record);
    }
    public void setOnRecordClickListener(OnRecordClickListener listener) {
        this.onRecordClickListener = listener;
    }

    public void setOnRecordLongClickListener(OnRecordLongClickListener listener) {
        this.onRecordLongClickListener = listener;
    }
}
