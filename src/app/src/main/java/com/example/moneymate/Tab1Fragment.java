package com.example.moneymate;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Tab1Fragment extends Fragment {

    // Firebase Firestore
    private FirebaseFirestore db;
    private String selectedService;

    // UI components
    private EditText edtDate, edtGhiChu, edtTienChi;
    private GridView gridView;
    private Button btnSubmit;

    // Data for GridView
    int[] image = {
            R.drawable.yte, R.drawable.giaoduc, R.drawable.dilai,
            R.drawable.anuong, R.drawable.dautu, R.drawable.quanao,
            R.drawable.dulich, R.drawable.tiendien, R.drawable.lamdep,
            R.drawable.giaoluu
    };
    String[] name = {
            "Y tế", "Giáo dục", "Đi lại", "Ăn uống", "Đầu tư", "Quần áo",
            "Du lịch", "Tiền điện", "Làm đẹp", "Giao lưu"
    };

    ArrayList<Service> serviceList;
    MyArrayAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout
        return inflater.inflate(R.layout.layout_tab1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Link UI components
        edtDate = view.findViewById(R.id.edtDate_ngay);
        edtGhiChu = view.findViewById(R.id.edt_ghichu);
        edtTienChi = view.findViewById(R.id.edtNb_tienchi);
        gridView = view.findViewById(R.id.gv_tab1);
        btnSubmit = view.findViewById(R.id.btn_tienchi);

        // Initialize GridView
        serviceList = new ArrayList<>();
        for (int i = 0; i < image.length; i++) {
            serviceList.add(new Service(image[i], name[i]));
        }

        adapter = new MyArrayAdapter((Activity) requireContext(), R.layout.layout_service, serviceList);
        gridView.setAdapter(adapter);

        // Button click event
        btnSubmit.setOnClickListener(v -> saveDataToFirestore());

        // Open DatePicker when clicking the EditText for the date
        edtDate.setOnClickListener(v -> openDatePicker());


        gridView.setOnItemClickListener((parent, v, position, id) -> {
            // Get the selected service name
             selectedService = name[position];
            // Store it globally or use it when saving data
            // You can use this to show a Toast or save the selected service
            Toast.makeText(requireContext(), "Dịch vụ đã chọn: " + selectedService, Toast.LENGTH_SHORT).show();
        });
    }

    // Open DatePickerDialog
    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = selectedYear + "-" + String.format("%02d", (selectedMonth + 1)) + "-" + String.format("%02d", selectedDay);
                    edtDate.setText(formattedDate);
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);

                    //so sánh ngày được chọn với ngày hiện tại
                    Calendar currentDate = Calendar.getInstance();
                    if (selectedDate.after(currentDate)) {
                        Toast.makeText(requireContext(), "Ngày không thể là trong tương lai!", Toast.LENGTH_SHORT).show();
                        edtDate.setText("");
                    }
                }, year, month, day);

        datePickerDialog.show();
    }

    private void saveDataToFirestore() {
        String date = edtDate.getText().toString().trim();
        String note = edtGhiChu.getText().toString().trim();
        String money = edtTienChi.getText().toString().trim();


        // Validate inputs
        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(note) || TextUtils.isEmpty(money)) {
            Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();



        // Prepare data to save
        Map<String, Object> data = new HashMap<>();
        data.put("date", date);
        data.put("note", note);
        data.put("money", money);
        data.put("service", selectedService);  // Add the selected service

        // Save to Firestore

        db.collection("users").document(userId)
                .collection("expenses")
                .add(data)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(requireContext(), "Chi tiêu thêm thành công!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


}

