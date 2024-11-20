package com.example.moneymate;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;
import android.app.DatePickerDialog;

import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;



public class Tab1Fragment extends Fragment {

    int image[] = {R.drawable.yte, R.drawable.giaoduc, R.drawable.dilai, R.drawable.anuong, R.drawable.dautu,R.drawable.quanao,R.drawable.dulich, R.drawable.tiendien,R.drawable.lamdep, R.drawable.giaoluu};
    String name[] ={"Y tế","Giáo dục","Đi lại","Ăn uống","Đầu tư","Quần áo","Du lịch","Tiền điện","Làm đẹp","Giao lưu"};

   ArrayList<Service> serviceList;
   MyArrayAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_tab1, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khai báo GridView từ layout_tab1
        GridView gridView = view.findViewById(R.id.gv_tab1); // Thay thế "grid_view_id" bằng ID thực tế của GridView trong layout_tab1

        // Khởi tạo danh sách Service
        serviceList = new ArrayList<>();
        for (int i = 0; i < image.length; i++) {
            serviceList.add(new Service(image[i], name[i]));
        }

        // Thiết lập Adapter cho GridView
        adapter = new MyArrayAdapter((Activity) requireContext(), R.layout.layout_service, serviceList);
        gridView.setAdapter(adapter);


        // Hiển thị ngày hiện tại trên EditText
        EditText edtDateNgay = view.findViewById(R.id.edtDate_ngay);

        // Định dạng ngày
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        String currentDate = dateFormat.format(Calendar.getInstance().getTime());

        // Gán ngày hiện tại vào EditText
        edtDateNgay.setText(currentDate);


        // Mở DatePickerDialog khi nhấn vào EditText
        edtDateNgay.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(), (datePicker, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                edtDateNgay.setText(dateFormat.format(calendar.getTime()));
            },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }
}