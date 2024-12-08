package com.example.moneymate;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GroupDetailActivity extends AppCompatActivity {

    private TextView tvGroupName, tvGroupID, tvDeadline;
    private EditText edit_monthlyAmount; // Đặt tên biến chính xác

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        // Liên kết các View với XML
        tvGroupName = findViewById(R.id.tvGroupName);
        tvGroupID = findViewById(R.id.tvGroupID);
        edit_monthlyAmount = findViewById(R.id.edit_monthlyAmount); // Sử dụng đúng tên biến
        tvDeadline = findViewById(R.id.tvDeadline);

        // Lấy dữ liệu từ Intent
        String groupName = getIntent().getStringExtra("groupName");
        String groupID = getIntent().getStringExtra("groupID");
        double monthlyAmountDouble = getIntent().getDoubleExtra("monthlyAmount", 0.0); // Lấy kiểu double
        int monthlyAmount = (int) monthlyAmountDouble; // Ép kiểu sang int
        String deadline = getIntent().getStringExtra("deadline");

        // Hiển thị dữ liệu
        tvGroupName.setText("Tên nhóm: " + groupName);
        tvGroupID.setText("ID nhóm: " + groupID);
        edit_monthlyAmount.setText( String.valueOf(monthlyAmount)); // Set giá trị số nguyên
        tvDeadline.setText("Hạn đóng quỹ: " + deadline);
    }
}
