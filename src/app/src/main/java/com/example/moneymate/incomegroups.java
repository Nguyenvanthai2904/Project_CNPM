package com.example.moneymate;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Intent;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class incomegroups extends AppCompatActivity {

    private FirebaseFirestore dbgroupin;

    // UI components
    private EditText edtDategroupin, edtTienthugroup, edtTongTienGroupin;
    private Button btnnap;
    private ListView lvGroupincomein;

    private String groupId;
    private ArrayAdapter<String> incomeAdapter;
    private List<String> incomeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomegroups);

        // Initialize Firestore
        dbgroupin = FirebaseFirestore.getInstance();

        // Link UI components
        edtDategroupin = findViewById(R.id.edtDate_ngaygroupincome);
        edtTienthugroup = findViewById(R.id.edtNb_tienthugroup);
        btnnap = findViewById(R.id.btn_tienthugroup);
        lvGroupincomein = findViewById(R.id.lv_group_income);
        edtTongTienGroupin = findViewById(R.id.edt_tongtiengroupin);

        // Get the groupId from the intent
        Intent intent1 = getIntent();
        groupId = intent1.getStringExtra("groupID");
        if (groupId == null) {
            Toast.makeText(this, "Lỗi: ID nhóm chưa được cung cấp.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize incomes list and adapter
        incomeList = new ArrayList<>();
        incomeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, incomeList);
        lvGroupincomein.setAdapter(incomeAdapter);

        // Load incomes and total amount (Initial load)
        loadIncomes();

        // Button click event
        btnnap.setOnClickListener(v -> saveDataToFirestore());

        // Open DatePicker when clicking the EditText for the date
        edtDategroupin.setOnClickListener(v -> openDatePicker());
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format the selected date
                    String formattedDate = selectedYear + "-" + String.format("%02d", (selectedMonth + 1)) + "-" + String.format("%02d", selectedDay);

                    // Get the current date
                    Calendar today = Calendar.getInstance();
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    today.set(Calendar.MILLISECOND, 0);

                    // Create a Calendar object for the selected date
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);

                    // Check if the selected date is in the future
                    if (selectedDate.after(today)) {
                        Toast.makeText(this, "Ngày không thể là trong tương lai!", Toast.LENGTH_SHORT).show();
                        edtDategroupin.setText(""); // Clear the EditText
                    } else {
                        edtDategroupin.setText(formattedDate); // Update EditText with selected date
                    }
                }, year, month, day);

        // Set the maximum date to the current date
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis()+1000);
        datePickerDialog.show();
    }

    private void saveDataToFirestore() {
        String date = edtDategroupin.getText().toString();
        String moneyString = edtTienthugroup.getText().toString();

        if (date.isEmpty() || moneyString.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền tất cả các trường", Toast.LENGTH_SHORT).show();
            return;
        }

        double money = Double.parseDouble(moneyString);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        // Fetch user info
        dbgroupin.collection("users").document(userId).get()
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        DocumentSnapshot userDocument = userTask.getResult();
                        String username1 = userDocument != null && userDocument.exists() ?
                                userDocument.getString("name") : "Unknown";

                        // Prepare data
                        Map<String, Object> incomeData = new HashMap<>();
                        incomeData.put("date", date);
                        incomeData.put("userId", userId);
                        incomeData.put("money", money);
                        incomeData.put("userName", username1);

                        // Save to Firestore
                        dbgroupin.collection("groups").document(groupId).collection("incomes")
                                .add(incomeData)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(incomegroups.this, "Thu nhập đã được thêm thành công", Toast.LENGTH_SHORT).show();
                                    edtDategroupin.setText("");
                                    edtTienthugroup.setText("");
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(incomegroups.this, "Lỗi khi thêm thu nhập: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("FirestoreError", "Lỗi khi thêm thu nhập", e);
                                });
                    } else {
                        Log.e("FirestoreError", "Lỗi khi lấy dữ liệu người dùng: ", userTask.getException());
                        Toast.makeText(this, "Lấy thông tin người dùng thất bại.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadIncomes() {
        dbgroupin.collection("groups").document(groupId).collection("incomes")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("FirestoreError", "Listen failed.", e);
                        return;
                    }

                    incomeList.clear();
                    double totalIncome = 0;

                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots) {
                            String date = doc.getString("date");
                            String userName = doc.getString("userName");
                            double money = doc.getDouble("money");
                            totalIncome += money;

                            String incomeInfo = "Date: " + date + "\nUser: " + userName + "\nMoney: " + String.format("%.0f", money) + "VNĐ";
                            incomeList.add(incomeInfo);
                        }
                    }

                    incomeAdapter.notifyDataSetChanged();
                    edtTongTienGroupin.setText(String.format("%.0f", totalIncome) + "VNĐ");
                });
    }
}