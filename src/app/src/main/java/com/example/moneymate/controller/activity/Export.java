package com.example.moneymate.controller.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.moneymate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class Export extends AppCompatActivity {

    private Spinner monthSpinner_report;
    private Spinner yearSpinner_report;
    private Button btn_report;
    private FirebaseFirestore db;
    private String currentUserId;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        monthSpinner_report = findViewById(R.id.monthSpinner_report);
        yearSpinner_report = findViewById(R.id.yearSpinner_report);
        btn_report = findViewById(R.id.btn_report);
        setUpSpinners();

        btn_report.setOnClickListener(v -> generateReport());
    }

    private void setUpSpinners() {
        ArrayList<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(String.format(Locale.getDefault(), "%02d", i));
        }

        ArrayList<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear - 10; i <= currentYear; i++) {
            years.add(String.valueOf(i));
        }

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner_report.setAdapter(monthAdapter);

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner_report.setAdapter(yearAdapter);

        String currentMonth = String.format(Locale.getDefault(), "%02d", Calendar.getInstance().get(Calendar.MONTH) + 1);
        monthSpinner_report.setSelection(months.indexOf(currentMonth));
        yearSpinner_report.setSelection(years.indexOf(String.valueOf(currentYear)));
    }

    private void generateReport() {
        String selectedMonth = (String) monthSpinner_report.getSelectedItem();
        String selectedYear = (String) yearSpinner_report.getSelectedItem();

        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder reportContent = new StringBuilder();
        reportContent.append("Báo cáo chi tiêu tháng ").append(selectedMonth).append("/").append(selectedYear).append("\n\n");

        CountDownLatch latch = new CountDownLatch(2);

        fetchAndAppendData(reportContent, "incomes", selectedMonth, selectedYear, true, latch);
        fetchAndAppendData(reportContent, "expenses", selectedMonth, selectedYear, false, latch);

        new Thread(() -> {
            try {
                latch.await();
                saveFile("report_" + selectedMonth + "_" + selectedYear + ".txt", reportContent.toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void fetchAndAppendData(StringBuilder reportContent, String collectionName, String selectedMonth, String selectedYear, boolean isIncome, CountDownLatch latch) {
        reportContent.append(isIncome ? "Thu nhập:\n" : "Chi tiêu:\n");

        db.collection("users").document(currentUserId).collection(collectionName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            double money = getMoneyValue(document, selectedMonth, selectedYear);
                            String date = document.getString("date");
                            String note = document.getString("note");
                            String service = document.getString("service");
                            reportContent.append(date != null ? date : "N/A").append(" | ")
                                    .append(money).append(" | ")
                                    .append(note != null ? note : "N/A")
                                    .append(" | ").append(service != null ? service : "N/A")
                                    .append("\n");
                        }
                    } else {
                        handleError(task.getException());
                    }
                    latch.countDown();

                });
    }

    private double getMoneyValue(QueryDocumentSnapshot document, String selectedMonth, String selectedYear) {
        String dateString = document.getString("date");
        try {
            Date date = dateFormat.parse(dateString);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            String docMonth = String.format(Locale.getDefault(), "%02d", cal.get(Calendar.MONTH) + 1);
            String docYear = String.valueOf(cal.get(Calendar.YEAR));
            if (docMonth.equals(selectedMonth) && docYear.equals(selectedYear)) {
                String moneyStr = document.getString("money");
                if (moneyStr != null) {
                    return Double.parseDouble(moneyStr);
                }
            }
        } catch (ParseException e) {
            Log.e("Report", "Lỗi khi phân tích ngày", e);
        }
        return 0;
    }



    private void saveFile(String fileName, String content) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri fileUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            try (OutputStream outputStream = resolver.openOutputStream(fileUri)) {
                if (outputStream != null) {
                    outputStream.write(content.getBytes());
                    runOnUiThread(() -> Toast.makeText(this, "Tệp đã lưu vào thư mục Downloads", Toast.LENGTH_LONG).show());
                }
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Lỗi khi lưu tệp: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        } else {
            // Handle older android versions here if needed.
        }
    }
    private void handleError(Exception e) {
        Toast.makeText(this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
        Log.e("Report", "Lỗi truy vấn Firestore", e);
    }


}