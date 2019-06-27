package com.alteration.parent;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.alteration.R;

import java.util.ArrayList;
import java.util.List;

class ShareListAdapter extends ArrayAdapter {

    private List<Object> list = new ArrayList<>();

    ShareListAdapter(@NonNull Context context, @LayoutRes int resource) {
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
        TextView dName;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final DataHandler handler = new DataHandler();

        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.al_device_list_share_items, parent, false);
        handler.dName = convertView.findViewById(R.id.dNameShare);
        convertView.setTag(handler);

        final ShareListHandler listHandler;
        listHandler = (ShareListHandler) this.getItem(position);

        assert listHandler != null;
        handler.dName.setText(listHandler.getDevName());

        return convertView;
    }
}
