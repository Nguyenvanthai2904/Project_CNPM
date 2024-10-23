package com.example.moneymate;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import androidx.fragment.app.Fragment;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;

    public CalendarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        Log.d("CalendarFragment", "onCreateView called");
        // Kết nối CalendarView với XML
        calendarView = view.findViewById(R.id.calendarView);

        // Xử lý sự kiện khi chọn ngày (nếu cần)
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            // Xử lý khi người dùng chọn ngày từ CalendarView
            // Ví dụ: hiển thị ngày được chọn
            String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            // Bạn có thể thực hiện hành động khác tại đây
        });

        return view;
    }
}
