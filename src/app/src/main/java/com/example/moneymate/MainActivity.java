package com.example.moneymate;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.moneymate.R;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.example.moneymate.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    // truy cập các View trong layout XML của mình mà không cần phải sử dụng findViewById()
    //bật viewBinding trong gradle module
    ActivityMainBinding binding;
//    private BottomNavigationView bottomNavigationView;
//    private FrameLayout frame_layout;


    @SuppressLint({"MissingInflatedId", "NonConstantResourceId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

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