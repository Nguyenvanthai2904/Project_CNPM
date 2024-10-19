package com.example.moneymate;

import android.content.Intent;
import android.content.SharedPreferences; // Thêm dòng này
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {
    EditText edt_email, edt_password;
    Button btn_login, btn_facebook, btn_google;
    TextView tv_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edt_email = findViewById(R.id.edt_email);
        edt_password = findViewById(R.id.edt_password);
        btn_facebook = findViewById(R.id.btn_facebook);
        btn_login = findViewById(R.id.btn_login);
        btn_google = findViewById(R.id.btn_google);
        tv_register = findViewById(R.id.tv_register);

        // Thêm OnClickListener cho tv_register
        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_signup = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent_signup);
            }
        });

        // Thêm OnClickListener cho btn_login
//        btn_login.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Giả lập quá trình xác thực (bạn có thể thay đổi theo logic của mình)
//                String email = edt_email.getText().toString();
//                String password = edt_password.getText().toString();
//
//                // Kiểm tra thông tin đăng nhập (thay thế với logic xác thực thực sự)
//                if (email.equals("user@example.com") && password.equals("password")) {
//                    // Khi người dùng đăng nhập thành công
//                    SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putBoolean("isLoggedIn", true); // Lưu trạng thái đã đăng nhập
//                    editor.putString("username", email); // Lưu tên người dùng hoặc thông tin khác (nếu cần)
//                    editor.apply(); // Áp dụng các thay đổi
//
//                    // Chuyển hướng đến MainActivity
//                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                    startActivity(intent);
//                    finish(); // Đóng LoginActivity nếu không cần quay lại
//                } else {
//                    // Hiển thị thông báo lỗi hoặc xử lý đăng nhập không thành công
//                    // Bạn có thể sử dụng Toast hoặc AlertDialog
//                }
//            }
//        });
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
