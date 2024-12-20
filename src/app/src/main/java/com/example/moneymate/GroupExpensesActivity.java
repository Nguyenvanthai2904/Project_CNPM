package com.example.moneymate;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Intent;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupExpensesActivity extends AppCompatActivity {

    private FirebaseFirestore dbgroup;
    private String selectedServicegroup;

    // UI components
    private EditText edtDategroup, edtTienChigroup, edtTongTienGroup, edtTongTienChiGroup, edtTienThuaGroup;
    private GridView gridViewgroup;
    private Button btnthem;
    private ListView lvGroupExpenses;
    private TextView totalAmountTextView;

    // Data for GridView
    int[] imagegroup = {
            R.drawable.yte, R.drawable.dilai,
            R.drawable.anuong, R.drawable.quanao,
            R.drawable.tiendien, R.drawable.lamdep,
            R.drawable.giaoluu
    };
    String[] namegroup = {
            "Y tế", "Đi lại", "Ăn uống", "Quần áo", "Tiền điện", "Làm đẹp", "Giao lưu"
    };
    String groupId;

    ArrayList<Service> serviceList;
    MyArrayAdapter adapter;
    private ArrayAdapter<String> expensesAdapter;
    private List<String> expensesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_expenses);

        // Initialize Firestore
        dbgroup = FirebaseFirestore.getInstance();

        // Link UI components
        edtDategroup = findViewById(R.id.edtDate_ngaygroup);
        edtTienChigroup = findViewById(R.id.edtNb_tienchigroup);
        gridViewgroup = findViewById(R.id.gv_group);
        btnthem = findViewById(R.id.btn_tienchigroup);
        lvGroupExpenses = findViewById(R.id.lv_group_expenses);
        totalAmountTextView = findViewById(R.id.edt_tongtiengroup);
        edtTongTienGroup = findViewById(R.id.edt_tongtiengroup);
        edtTongTienChiGroup = findViewById(R.id.edt_tongtienchigroup);
        edtTienThuaGroup = findViewById(R.id.edt_tienthuagroup);

        // Initialize GridView
        serviceList = new ArrayList<>();
        for (int i = 0; i < imagegroup.length; i++) {
            serviceList.add(new Service(imagegroup[i], namegroup[i]));
        }

        adapter = new MyArrayAdapter(this, R.layout.layout_service, serviceList);
        gridViewgroup.setAdapter(adapter);

        // Get the groupId from the intent
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupID");
        if (groupId == null) {
            Toast.makeText(this, "Error: Group ID not provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize expenses list and adapter
        expensesList = new ArrayList<>();
        expensesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, expensesList);
        lvGroupExpenses.setAdapter(expensesAdapter);

        // Load expenses and total amount
        loadExpenses();
        loadTotalAmount();

        // Button click event
        btnthem.setOnClickListener(v -> saveDataToFirestore());

        // Open DatePicker when clicking the EditText for the date
        edtDategroup.setOnClickListener(v -> openDatePicker());

        gridViewgroup.setOnItemClickListener((parent, v, position, id) -> {
            selectedServicegroup = namegroup[position];
            Toast.makeText(this, "Dịch vụ đã chọn: " + selectedServicegroup, Toast.LENGTH_SHORT).show();
        });
    }

    // Open DatePickerDialog
    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = selectedYear + "-" + String.format("%02d", (selectedMonth + 1)) + "-" + String.format("%02d", selectedDay);
                    edtDategroup.setText(formattedDate);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void saveDataToFirestore() {
        String dategroup = edtDategroup.getText().toString().trim();
        String moneygroup = edtTienChigroup.getText().toString().trim();

        if (TextUtils.isEmpty(dategroup) || TextUtils.isEmpty(moneygroup)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        double money = Double.parseDouble(moneygroup);

        Map<String, Object> datagroup = new HashMap<>();
        datagroup.put("date", dategroup);
        datagroup.put("money", money);
        datagroup.put("service", selectedServicegroup);

        dbgroup.collection("groups").document(groupId)
                .collection("expenses")
                .add(datagroup)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Chi tiêu thêm thành công!", Toast.LENGTH_SHORT).show();
                    loadExpenses(); // Reload expenses
                    updateTotals(); // Update totals after adding expense
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", "Error saving expense", e);
                });
        edtDategroup.setText("");
        edtTienChigroup.setText("");
        selectedServicegroup = null;
    }

    private void loadExpenses() {
        dbgroup.collection("groups").document(groupId).collection("expenses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Create a new list to store the expenses
                    List<String> newExpensesList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String date = document.getString("date");
                        Double money = document.getDouble("money");
                        String service = document.getString("service");

                        if (date != null && money != null && service != null) {
                            String formattedMoney = String.format("%.0f", money);
                            String expense = "Date: " + date + "- Money: " + formattedMoney+ "VNĐ" + "- Service: " + service;
                            newExpensesList.add(expense); // Add to the new list
                        } else {
                            Log.w("FirestoreError", "Missing data in document: " + document.getId());
                            if (date == null) {
                                newExpensesList.add("Error: Date missing");
                            }
                            if (money == null) {
                                newExpensesList.add("Error: Money missing");
                            }
                            if (service == null) {
                                newExpensesList.add("Error: Service missing");
                            }
                        }
                    }

                    // Update expensesList on the main thread
                    runOnUiThread(() -> {
                        expensesList.clear();
                        expensesList.addAll(newExpensesList);
                        expensesAdapter.notifyDataSetChanged();
                        updateTotals(); // Update totals after loading expenses
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading expenses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", "Error loading expenses", e);
                });
    }

    private void loadTotalAmount() {
        dbgroup.collection("groups").document(groupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double totalAmount = documentSnapshot.getDouble("totalAmount");
                        if (totalAmount != null) {
                            totalAmountTextView.setText(String.format("%.0f", totalAmount)+"VNĐ");
                        } else {
                            totalAmountTextView.setText("0");
                        }
                    } else {
                        Log.d("FirestoreError", "Document does not exist");
                        totalAmountTextView.setText("0");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading total amount: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", "Error loading total amount", e);
                });
    }

    private void updateTotals() {
        final double[] values = {0.0, 0.0}; // [0]: totalAmount, [1]: totalExpenses

        dbgroup.collection("groups").document(groupId).collection("expenses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalExpenses = 0;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Double money = document.getDouble("money");
                        if (money != null) {
                            totalExpenses += money;
                        }
                    }
                    values[1] = totalExpenses; // Update totalExpenses in the array

                    // Get total amount
                    dbgroup.collection("groups").document(groupId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    Double totalAmount = documentSnapshot.getDouble("totalAmount");
                                    if (totalAmount != null) {
                                        values[0] = totalAmount; // Update totalAmount in the array

                                        // Calculate remaining amount
                                        double remainingAmount = values[0] - values[1];

                                        // Update UI
                                        edtTongTienChiGroup.setText(String.format("%.0f", values[1])+"VNĐ");
                                        edtTienThuaGroup.setText(String.format("%.0f", remainingAmount)+"VNĐ");
                                    } else {
                                        edtTongTienChiGroup.setText("0");
                                        edtTienThuaGroup.setText("0");
                                    }
                                }
                            });
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error updating totals", e));
    }
}