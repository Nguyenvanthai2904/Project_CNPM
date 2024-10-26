package com.example.moneymate;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class Tab2Fragment extends Fragment {
    int image[] = {R.drawable.tienluong,R.drawable.phucap,R.drawable.dautu,R.drawable.tienthuong,R.drawable.tichgop};
    String name[] ={"Tiền lương","Phụ cấp","Đầu tư","Tiền thưởng","Tích lũy"};

    ArrayList<Service> serviceList;
    MyArrayAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_tab2, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khai báo GridView từ layout_tab1
        GridView gridView = view.findViewById(R.id.gv_tab2); // Thay thế "grid_view_id" bằng ID thực tế của GridView trong layout_tab1

        // Khởi tạo danh sách Service
        serviceList = new ArrayList<>();
        for (int i = 0; i < image.length; i++) {
            serviceList.add(new Service(image[i], name[i]));
        }

        // Thiết lập Adapter cho GridView
        adapter = new MyArrayAdapter((Activity) requireContext(), R.layout.layout_service, serviceList);
        gridView.setAdapter(adapter);
    }
}
