package com.example.moneymate;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Date;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private ListView eventListView;
    private EditText incomeEditText, expenseEditText, totalEditText;

    private FirebaseFirestore firestore;
    private ArrayAdapter<String> adapter;
    private List<String> eventList;
    private int totalIncome = 0;
    private int totalExpenses = 0;
    public CalendarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Connect views
        calendarView = view.findViewById(R.id.calendarView);
        eventListView = view.findViewById(R.id.eventListView);
        incomeEditText = view.findViewById(R.id.editText_Tthu);
        expenseEditText = view.findViewById(R.id.editText_Tchi);
        totalEditText = view.findViewById(R.id.editText_Tong);

        // Initialize event list and adapter
        eventList = new ArrayList<>();
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, eventList);

        eventListView.setAdapter(adapter);

        // Get the current date
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Fetch events and totals for the current date initially
        fetchEventsAndTotalsForDate(currentDate);

        // Set CalendarView date change listener
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String selectedDate = year + "-" + String.format("%02d", (month + 1)) + "-" + String.format("%02d", dayOfMonth);

            fetchEventsAndTotalsForDate(selectedDate);
        });

        return view;
    }

    private void fetchEventsAndTotalsForDate(String date) {
        // Reset total values
        totalIncome = 0;
        totalExpenses = 0;
        eventList.clear(); // Clear the event list

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch incomes
        firestore.collection("users")
                .document(userId)
                .collection("incomes")
                .whereEqualTo("date", date)
                .get()
                .addOnCompleteListener(incomeTask -> {
                    if (incomeTask.isSuccessful()) {
                        for (DocumentSnapshot document : incomeTask.getResult()) {
                            String service = document.getString("service");
                            String note = document.getString("note");
                            int amount = Integer.parseInt(document.getString("money"));

                            // Update total income
                            totalIncome += amount;

                            // Add income event to the list
                            eventList.add("Tiền thu: " + service + "-" + amount +"VNĐ"+ " : " + note);
                        }

                        // Update income EditText
                        incomeEditText.setText(String.valueOf(totalIncome)+"VNĐ");

                        // Fetch expenses after incomes
                        fetchExpensesForDate(date);
                    } else {
                        Toast.makeText(getContext(), "Failed to fetch incomes: " + incomeTask.getException(), Toast.LENGTH_SHORT).show();
                        Log.e("CalendarFragment", "Error fetching incomes", incomeTask.getException());
                    }
                });
    }

    private void fetchExpensesForDate(String date) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("users")
                .document(userId)
                .collection("expenses")
                .whereEqualTo("date", date)
                .get()
                .addOnCompleteListener(expenseTask -> {
                    if (expenseTask.isSuccessful()) {
                        for (DocumentSnapshot document : expenseTask.getResult()) {
                            String service = document.getString("service");
                            String note = document.getString("note");
                            int amount = Integer.parseInt(document.getString("money"));

                            // Update total expenses
                            totalExpenses += amount;

                            // Add expense event to the list
                            eventList.add("Tiền chi: " + service + "-" + amount +"VNĐ" + " : " + note);
                        }

                        // Update expense EditText
                        expenseEditText.setText(String.valueOf(totalExpenses)+"VNĐ");

                        // Calculate and update total (income - expenses)
                        int total = totalIncome - totalExpenses;
                        totalEditText.setText(String.valueOf(total)+"VNĐ");

                        // Notify adapter to update ListView
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Failed to fetch expenses: " + expenseTask.getException(), Toast.LENGTH_SHORT).show();
                        Log.e("CalendarFragment", "Error fetching expenses", expenseTask.getException());
                    }
                });
    }
}