package com.example.moneymate.controller.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.example.moneymate.R;
import com.example.moneymate.controller.fragment.CalendarFragment;
import com.example.moneymate.controller.fragment.ChartFragment;
import com.example.moneymate.controller.fragment.MoreFragment;
import com.example.moneymate.controller.fragment.TransactionFragment;
import com.example.moneymate.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;


    @SuppressLint({"MissingInflatedId", "NonConstantResourceId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new TransactionFragment());
        // xử lí click bottom nav
        binding.bottomNav.setOnItemSelectedListener(item ->{
            if (item.getItemId() == R.id.transaction) {
                replaceFragment(new TransactionFragment());
            } else if (item.getItemId() == R.id.calendar) {
                replaceFragment(new CalendarFragment());
            } else if (item.getItemId() == R.id.chart) {
                replaceFragment(new ChartFragment());
            } else if (item.getItemId() == R.id.more) {
                replaceFragment(new MoreFragment());

            }

            return true;
        });

    }
    // thay đổi nội dung trong view group mà không cần thay đổi toàn bộ Main Activity
    private  void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}