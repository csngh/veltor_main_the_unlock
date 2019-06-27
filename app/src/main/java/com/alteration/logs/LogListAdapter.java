package com.alteration.logs;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alteration.R;

import java.util.ArrayList;
import java.util.List;

class
LogListAdapter extends ArrayAdapter {

    private List<Object> list = new ArrayList<>();

    LogListAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }

    @Override
    public void add(@Nullable Object object) {
        super.add(object);
        list.add(object);
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return this.list.get(position);
    }

    private static class DataHandler {
        private TextView tvName, tvDate;
        private ImageView imgStat;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final DataHandler handler = new DataHandler();

        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        convertView = inflater.inflate(R.layout.al_log_list_view_items, parent, false);
        handler.tvName = convertView.findViewById(R.id.lNickName);
        handler.tvDate = convertView.findViewById(R.id.lTime);
        handler.imgStat = convertView.findViewById(R.id.imgLockStat);
        convertView.setTag(handler);

        final LogsHandler logsHandler;
        logsHandler = (LogsHandler) this.getItem(position);

        assert logsHandler != null;
        String dataLog = logsHandler.getNickName();
        String nickName = dataLog.substring(dataLog.indexOf("%") + 1, dataLog.indexOf("$"));
        String isLock = dataLog.substring(dataLog.indexOf("$") + 1, dataLog.indexOf("#"));
        String dateTime = dataLog.substring(dataLog.indexOf("#") + 1);

        handler.tvName.setText(nickName + " " + isLock);
        handler.tvDate.setText(dateTime);
        if (isLock.equalsIgnoreCase("LOCKED")) {
            handler.imgStat.setImageResource(R.drawable.log_lock);
            handler.tvName.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));
        } else if (isLock.equalsIgnoreCase("UNLOCKED")) {
            handler.imgStat.setImageResource(R.drawable.log_unlock);
            handler.tvName.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark));
        }

        return convertView;
    }
}
