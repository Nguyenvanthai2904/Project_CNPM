package com.example.moneymate;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class ChartFragment extends Fragment {

    private BarChart barChart;
    private FirebaseFirestore db;
    private Spinner monthSpinner;
    private Spinner yearSpinner;
    private String currentUserId;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public ChartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chart, container, false);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        barChart = rootView.findViewById(R.id.barChart);
        monthSpinner = rootView.findViewById(R.id.monthSpinner);
        yearSpinner = rootView.findViewById(R.id.yearSpinner);

        setupChartAxis();
        setUpSpinners();

        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                fetchData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }
        };
        monthSpinner.setOnItemSelectedListener(spinnerListener);
        yearSpinner.setOnItemSelectedListener(spinnerListener);

        return rootView;
    }

    private void setupChartAxis() {
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Thu", "Chi"}));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        barChart.getDescription().setEnabled(false); // Disable chart description
    }

    private void setUpSpinners() {
        // Create a list for months
        ArrayList<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(String.format(Locale.getDefault(), "%02d", i));
        }

        // Create a list for years (for example, from 2020 to current year)
        ArrayList<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear - 10; i <= currentYear; i++) {
            years.add(String.valueOf(i));
        }

        // Set adapter for month spinner
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        // Set adapter for year spinner
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        // Set default selection to current month and year
        String currentMonth = String.format(Locale.getDefault(), "%02d", Calendar.getInstance().get(Calendar.MONTH) + 1);
        monthSpinner.setSelection(months.indexOf(currentMonth));
        yearSpinner.setSelection(years.indexOf(String.valueOf(currentYear)));
    }

    private void fetchData() {
        String selectedMonth = (String) monthSpinner.getSelectedItem();
        String selectedYear = (String) yearSpinner.getSelectedItem();

        // Fetch income and expense data from Firestore
        fetchMonthlyTotals(selectedMonth, selectedYear);
    }


    private void fetchMonthlyTotals(String selectedMonth, String selectedYear) {
        if (currentUserId == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        AtomicReference<Double> totalIncomes = new AtomicReference<>((double) 0);
        AtomicReference<Double> totalExpenses = new AtomicReference<>((double) 0);

        db.collection("users").document(currentUserId).collection("incomes")
                .get().addOnCompleteListener(incomeTask -> {
                    if (incomeTask.isSuccessful()) {
                        for (QueryDocumentSnapshot document : incomeTask.getResult()) {
                            totalIncomes.updateAndGet(v -> new Double((double) (v + getMoneyValue(document))));
                        }
                        db.collection("users").document(currentUserId).collection("expenses")
                                .get().addOnCompleteListener(expenseTask -> {
                                    if (expenseTask.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : expenseTask.getResult()) {
                                            totalExpenses.updateAndGet(v -> new Double((double) (v + getMoneyValue(document))));
                                        }
                                        updateChart(totalIncomes.get(), totalExpenses.get());
                                    } else {
                                        handleError(expenseTask.getException());
                                    }
                                });

                    } else {
                        handleError(incomeTask.getException());
                    }
                });
    }

    private double getMoneyValue(QueryDocumentSnapshot document) {
        String dateString = document.getString("date");
        String selectedMonth = (String) monthSpinner.getSelectedItem();
        String selectedYear = (String) yearSpinner.getSelectedItem();
        try {
            Date date = dateFormat.parse(dateString);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            String docMonth = String.format(Locale.getDefault(), "%02d", cal.get(Calendar.MONTH) + 1);
            String docYear = String.valueOf(cal.get(Calendar.YEAR));
            if (docMonth.equals(selectedMonth) && docYear.equals(selectedYear)) {
                String moneyStr = document.getString("money");
                if (moneyStr != null) {
                    try {
                        return Double.parseDouble(moneyStr);
                    } catch (NumberFormatException e) {
                        Log.w("ChartFragment","Invalid number",e);
                    }
                }

            }

        } catch (ParseException e) {
            Log.w("ChartFragment", "Invalid date format", e);
        }
        return 0; // or handle the error as needed
    }



    private void handleError(Exception e) {
        Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
        Log.e("ChartFragment", "Firestore query error", e);
    }

    private void updateChart(double totalIncomes, double totalExpenses) {
        // Prepare data entries for the chart
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, (float) totalIncomes));   // Entry for income
        entries.add(new BarEntry(1f, (float) totalExpenses));  // Entry for expense

        // Set up BarDataSet
        BarDataSet dataSet = new BarDataSet(entries, "Thu chi trong tháng");
        dataSet.setColors(new int[] {getResources().getColor(R.color.colorIncome), getResources().getColor(R.color.colorExpense)});
        BarData data = new BarData(dataSet);
        data.setValueTextSize(12f);
        // Set data to the chart and refresh it
        barChart.setData(data);
        barChart.invalidate(); // Refresh the chart view

        TextView emotionIcon = getView().findViewById(R.id.emotionIcon);
        TextView emotionMessage = getView().findViewById(R.id.emotionMessage);
        if (totalIncomes > totalExpenses) {
            emotionIcon.setText("😃 😃 😃");
            emotionMessage.setText("Tháng này thật tuyệt vời! Bạn đã tiết kiệm được một khoản đáng kể.");
        } else if (totalIncomes < totalExpenses) {
            emotionIcon.setText("😢 😢 😢");
            emotionMessage.setText("Cẩn thận hơn trong chi tiêu nhé! Tháng này bạn đã tiêu nhiều hơn thu.");
        } else {
            emotionIcon.setText("😐 😐 😐");
            emotionMessage.setText("Tài chính tháng này cân bằng. Hãy thử tiết kiệm nhiều hơn trong tháng sau!");
        }
    }
}