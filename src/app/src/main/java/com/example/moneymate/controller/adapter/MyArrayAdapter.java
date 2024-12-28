package com.example.moneymate.controller.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.moneymate.R;
import com.example.moneymate.model.Service;

import java.util.ArrayList;

public class MyArrayAdapter extends ArrayAdapter<Service> {
    private final Activity context;
    private final int id_layout;
    private final ArrayList<Service> services;

    public MyArrayAdapter(Activity context, int id_layout, ArrayList<Service> services) {
        super(context, id_layout, services);
        this.context = context;
        this.id_layout = id_layout;
        this.services = services;
    }

    @NonNull
    @Override
    public View getView(int position, @NonNull View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        // Kiểm tra convertView có rỗng không
        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(id_layout, parent, false);

            // Khởi tạo ViewHolder và lưu trữ các view con
            holder = new ViewHolder();
            holder.imgService = convertView.findViewById(R.id.img_service);
            holder.txtService = convertView.findViewById(R.id.txt_service);

            convertView.setTag(holder);
        } else {
            // Lấy ViewHolder từ convertView đã lưu
            holder = (ViewHolder) convertView.getTag();
        }

        // Gán dữ liệu cho các view trong ViewHolder
        Service service = services.get(position);
        holder.imgService.setImageResource(service.getImage());
        holder.txtService.setText(service.getName());

        return convertView;
    }

    // Lớp ViewHolder để lưu trữ view con
    static class ViewHolder {
        ImageView imgService;
        TextView txtService;
    }
}
