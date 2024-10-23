package com.example.moneymate;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class ChartFragment extends Fragment {
    //khai bao cac view trong layout
    BarChart bar_chart;
    PieChart pie_chart;;

    public ChartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        //gan cac view trong layout vao bien
        bar_chart = view.findViewById(R.id.bar_chart);
        pie_chart = view.findViewById(R.id.pie_chart);
        // xu li bieu do thi can database đã rồi thực hiện sau này
        //khoi tao cac mang du lieu
        ArrayList< BarEntry> barEntries = new ArrayList<>();
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        //use for loop
                //Khởi tạo mục nhập biểu đồ cột
                //khởi tạo mục nhaapj biểu đồ tròn
        //khoi tao du lieu cho bieu do
        //        https://www.youtube.com/watch?v=dzZQNsHhqQs&t=217s



        return view;
    }
}
