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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;

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
    String groupCreatorId; // Store the creator's ID

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
        edtDategroup = findViewById(R.id.edtDate_ngaygroupexpenses);
        edtTienChigroup = findViewById(R.id.edtNb_tienchigroup);
        gridViewgroup = findViewById(R.id.gv_groupchi);
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
        Intent intent2 = getIntent();
        groupId = intent2.getStringExtra("groupID");
        if (groupId == null) {
            Toast.makeText(this, "Error: Group ID not provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize expenses list and adapter
        expensesList = new ArrayList<>();
        expensesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, expensesList);
        lvGroupExpenses.setAdapter(expensesAdapter);

        // Load expenses, total amount and update totals
        loadExpenses();
        updateTotals();
        loadGroupCreator(groupId);

        // Button click event
        btnthem.setOnClickListener(v -> {
            // Check if the current user is the group creator
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String currentUserId = currentUser.getUid();
                if (currentUserId.equals(groupCreatorId)) {
                    // User is the creator, proceed to add expense
                    saveDataToFirestore();
                } else {
                    // User is not the creator, show error message
                    Toast.makeText(GroupExpensesActivity.this, "Chỉ trưởng nhóm mới có thể thêm chi tiêu.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // No user is logged in
                Toast.makeText(GroupExpensesActivity.this, "Bạn phải đăng nhập để thực hiện hành động này.", Toast.LENGTH_SHORT).show();
            }
        });

        // Open DatePicker when clicking the EditText for the date
        edtDategroup.setOnClickListener(v -> openDatePicker());

        // Set item click listener for GridView
        gridViewgroup.setOnItemClickListener((parent, view, position, id) -> {
            // Deselect all items
            for (int i = 0; i < serviceList.size(); i++) {
                serviceList.get(i).setSelected(false);
            }

            // Select the clicked item
            Service selectedService = serviceList.get(position);
            selectedService.setSelected(true);

            // Update selectedServicegroup
            selectedServicegroup = selectedService.getName();

            // Notify the adapter to update the view
            adapter.notifyDataSetChanged();

            Toast.makeText(this, "Dịch vụ đã chọn: " + selectedServicegroup, Toast.LENGTH_SHORT).show();
        });
    }

    private void loadGroupCreator(String groupId) {
        dbgroup.collection("groups").document(groupId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // Get the list of members
                            List<String> members = (List<String>) document.get("members");
                            if (members != null && !members.isEmpty()) {
                                // The first member is the group creator
                                groupCreatorId = members.get(0);
                            } else {
                                Log.e("FirestoreError", "Group members list is null or empty");
                            }
                        } else {
                            Log.e("FirestoreError", "Group document does not exist");
                        }
                    } else {
                        Log.e("FirestoreError", "Error getting group document: " + task.getException().getMessage());
                    }
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
                        edtDategroup.setText(""); // Clear the EditText
                    } else {
                        edtDategroup.setText(formattedDate); // Update EditText with selected date
                    }
                }, year, month, day);

        // Set the maximum date to the current date
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void saveDataToFirestore() {
        String dategroup = edtDategroup.getText().toString().trim();
        String moneygroup = edtTienChigroup.getText().toString().trim();

        // Check if date, money, and service are filled
        if (TextUtils.isEmpty(dategroup)) {
            Toast.makeText(this, "Vui lòng chọn ngày!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(moneygroup)) {
            Toast.makeText(this, "Vui lòng nhập số tiền chi!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(selectedServicegroup)) {
            Toast.makeText(this, "Vui lòng chọn dịch vụ!", Toast.LENGTH_SHORT).show();
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

        // Reset input fields and selected service
        edtDategroup.setText("");
        edtTienChigroup.setText("");
        selectedServicegroup = null;
        for (Service service : serviceList) {
            service.setSelected(false);
        }
        adapter.notifyDataSetChanged();
    }

    private void loadExpenses() {
        dbgroup.collection("groups").document(groupId).collection("expenses")
                .orderBy("date", Query.Direction.DESCENDING) // Order by date in descending order
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> expensesData = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        expensesData.add(document.getData());
                    }

                    // Sort the expenses by date in descending order
                    Collections.sort(expensesData, new Comparator<Map<String, Object>>() {
                        final SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                        @Override
                        public int compare(Map<String, Object> map1, Map<String, Object> map2) {
                            try {
                                Date date1 = f.parse((String) map1.get("date"));
                                Date date2 = f.parse((String) map2.get("date"));
                                return date2.compareTo(date1); // Descending order
                            } catch (ParseException e) {
                                Log.e("ParseError", "Error parsing date", e);
                                return 0;
                            }
                        }
                    });

                    List<String> newExpensesList = new ArrayList<>();
                    for (Map<String, Object> data : expensesData) {
                        String date = (String) data.get("date");
                        Double money = (Double) data.get("money");
                        String service = (String) data.get("service");

                        if (date != null && money != null && service != null) {
                            String formattedMoney = String.format("%.0f", money);
                            String expense = "Date: " + date + " - Money: " + formattedMoney + "VNĐ" + " - Service: " + service;
                            newExpensesList.add(expense);
                        } else {
                            Log.w("FirestoreError", "Missing data in document");
                        }
                    }

                    runOnUiThread(() -> {
                        expensesList.clear();
                        expensesList.addAll(newExpensesList);
                        expensesAdapter.notifyDataSetChanged();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading expenses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", "Error loading expenses", e);
                });
    }

    private void updateTotals() {
        final double[] values = {0.0, 0.0}; // [0]: totalIncome, [1]: totalExpenses

        // First, get total expenses
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

                    // Then, get total income
                    dbgroup.collection("groups").document(groupId).collection("incomes")
                            .get()
                            .addOnSuccessListener(incomeQuerySnapshot -> {
                                double totalIncome = 0;
                                for (QueryDocumentSnapshot incomeDoc : incomeQuerySnapshot) {
                                    Double incomeMoney = incomeDoc.getDouble("money");
                                    if (incomeMoney != null) {
                                        totalIncome += incomeMoney;
                                    }
                                }
                                values[0] = totalIncome; // Update totalIncome in the array

                                // Calculate remaining amount
                                double remainingAmount = values[0] - values[1];

                                // Update UI with retrieved values
                                runOnUiThread(() -> {
                                    edtTongTienGroup.setText(String.format("%.0f", values[0]) + "VNĐ");
                                    edtTongTienChiGroup.setText(String.format("%.0f", values[1]) + "VNĐ");
                                    edtTienThuaGroup.setText(String.format("%.0f", remainingAmount) + "VNĐ");
                                });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FirestoreError", "Error getting total income", e);
                                // Update UI with error values
                                runOnUiThread(() -> {
                                    edtTongTienGroup.setText("Error");
                                    edtTongTienChiGroup.setText(String.format("%.0f", values[1]) + "VNĐ");
                                    edtTienThuaGroup.setText("Error");
                                });
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error getting total expenses", e);
                    // Update UI with error values
                    runOnUiThread(() -> {
                        edtTongTienGroup.setText("Error");
                        edtTongTienChiGroup.setText("Error");
                        edtTienThuaGroup.setText("Error");
                    });
                });
    }
}