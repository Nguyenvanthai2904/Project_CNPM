package com.example.moneymate.controller.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import androidx.annotation.Nullable;

import android.widget.TextView;

import com.example.moneymate.R;
import com.example.moneymate.model.item;

public class MoreMyArrayAdapter extends ArrayAdapter<item>{
    Activity context;
    int Idlayout;
    ArrayList<item> mylist;
    // Tạo cóntructor

    public MoreMyArrayAdapter( Activity context, int idlayout, ArrayList<item> mylist) {
        super(context, idlayout, mylist);
        this.context = context;
        Idlayout = idlayout;
        this.mylist = mylist;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater myflacter = context.getLayoutInflater();
        // Đặt idlayout lên để tạo thành đối tượng view
        convertView = myflacter.inflate(Idlayout, null);
        // lấy phần tử trong mảng;
        item myitem = mylist.get(position);
        // khai báo tham chiếu Id và hiển thịk ảnh lên ImageView
        ImageView imgcaidat = convertView.findViewById(R.id.imgcaidat);
        imgcaidat.setImageResource(myitem.getImage());
        //khai báo, tham chiếu id và hiển thị tên điện thoại lên text view
        TextView txtcaidat = convertView.findViewById(R.id.txtcaidat);
        txtcaidat.setText(myitem.getName());
        return convertView;
    }

}
