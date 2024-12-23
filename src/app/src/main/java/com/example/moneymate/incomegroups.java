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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
            Toast.makeText(this, "Error: Group ID not provided.", Toast.LENGTH_SHORT).show();
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

                    // Create a Calendar object for the selected date
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);

                    // Set the time of selectedDate to 00:00:00
                    selectedDate.set(Calendar.HOUR_OF_DAY, 0);
                    selectedDate.set(Calendar.MINUTE, 0);
                    selectedDate.set(Calendar.SECOND, 0);
                    selectedDate.set(Calendar.MILLISECOND, 0);

                    // Create a Calendar object for today
                    Calendar today = Calendar.getInstance();
                    // Set the time of today to 00:00:00
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    today.set(Calendar.MILLISECOND, 0);
                    // Check if the selected date is in the future (after today)
                    if (selectedDate.after(today)) {
                        Toast.makeText(this, "Ngày không thể là trong tương lai!", Toast.LENGTH_SHORT).show();
                        edtDategroupin.setText(""); // Clear the EditText
                    } else {
                        edtDategroupin.setText(formattedDate); // Update EditText with selected date
                    }
                }, year, month, day);

        // Set the maximum date to the current date (without modifying time)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void saveDataToFirestore() {
        String date = edtDategroupin.getText().toString();
        String moneyString = edtTienthugroup.getText().toString();

        if (date.isEmpty() || moneyString.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double money = Double.parseDouble(moneyString);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(incomegroups.this, "Income added successfully", Toast.LENGTH_SHORT).show();
                                    edtDategroupin.setText("");
                                    edtTienthugroup.setText("");
                                    loadIncomes(); // Reload incomes to show the updated list
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(incomegroups.this, "Error adding income: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("FirestoreError", "Error adding income", e);
                                });
                    } else {
                        Log.e("FirestoreError", "Error getting user data: ", userTask.getException());
                        Toast.makeText(this, "Failed to retrieve user info.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadIncomes() {
        dbgroupin.collection("groups").document(groupId).collection("incomes")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("FirestoreError", "Listen failed.", e);
                        return;
                    }

                    List<Map<String, Object>> incomesData = new ArrayList<>();
                    double totalIncome = 0;

                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            incomesData.add(doc.getData());
                            Double money = doc.getDouble("money");
                            if (money != null) {
                                totalIncome += money;
                            }
                        }

                        // Sort incomes by date in descending order
                        Collections.sort(incomesData, new Comparator<Map<String, Object>>() {
                            final SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                            @Override
                            public int compare(Map<String, Object> map1, Map<String, Object> map2) {
                                try {
                                    Date date1 = f.parse((String) map1.get("date"));
                                    Date date2 = f.parse((String) map2.get("date"));
                                    return date2.compareTo(date1); // Descending order
                                } catch (ParseException ex) {
                                    Log.e("ParseError", "Error parsing date", ex);
                                    return 0;
                                }
                            }
                        });

                        incomeList.clear();
                        for (Map<String, Object> data : incomesData) {
                            String date = (String) data.get("date");
                            String userName = (String) data.get("userName");
                            double money = (Double) data.get("money");

                            String incomeInfo = "Date: " + date + "\nUser: " + userName + "\nMoney: " + String.format("%.0f", money) + "VNĐ";
                            incomeList.add(incomeInfo);
                        }
                    }

                    incomeAdapter.notifyDataSetChanged();
                    edtTongTienGroupin.setText(String.format("%.0f", totalIncome) + "VNĐ");
                });
    }
}