package com.example.moneymate;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class ChartFragment extends Fragment {
    private BarChart barChart;

    public ChartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        barChart = view.findViewById(R.id.bar_chart);
        setupBarChart();
        return view;
    }

    private void setupBarChart() {
        ArrayList<BarEntry> incomeEntries = new ArrayList<>();
        ArrayList<BarEntry> expenseEntries = new ArrayList<>();

        // Dữ liệu mẫu cho từng tháng (tháng 1 đến tháng 12)
        for (int i = 0; i < 12; i++) {
            incomeEntries.add(new BarEntry(i, (float) Math.random() * 1000));
            expenseEntries.add(new BarEntry(i, (float) Math.random() * 800));
        }

        BarDataSet incomeDataSet = new BarDataSet(incomeEntries, "Tiền Thu");
        incomeDataSet.setColor(ColorTemplate.COLORFUL_COLORS[0]);  // Đặt màu cho cột Tiền Thu
        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "Tiền Chi");
        expenseDataSet.setColor(ColorTemplate.COLORFUL_COLORS[1]); // Đặt màu cho cột Tiền Chi

        BarData data = new BarData(incomeDataSet, expenseDataSet);
        data.setBarWidth(0.3f); // Đặt chiều rộng cột
        barChart.setData(data);

        // Điều chỉnh khoảng cách giữa các cột kép
        barChart.groupBars(0f, 0.4f, 0.1f); // (groupSpace, barSpace, barWidth)
        barChart.invalidate(); // Cập nhật biểu đồ
    }
}
