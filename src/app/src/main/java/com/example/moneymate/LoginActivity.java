package com.example.moneymate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.CheckBox;
import android.text.InputType;

public class LoginActivity extends AppCompatActivity {
    private EditText edt_email, edt_password;
    private Button btn_login, btn_facebook, btn_google;
    private TextView tv_register;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // Khởi tạo Firebase Auth và Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Liên kết các view
        edt_email = findViewById(R.id.edt_email); // Đảm bảo ID đúng
        edt_password = findViewById(R.id.edt_password); // Đảm bảo ID đúng
        btn_facebook = findViewById(R.id.btn_facebook);
        btn_login = findViewById(R.id.btn_login);
        btn_google = findViewById(R.id.btn_google);
        tv_register = findViewById(R.id.tv_register);

        // OnClickListener cho TextView đăng ký
        tv_register.setOnClickListener(view -> {
            Intent intent_signup = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent_signup);
        });

        // OnClickListener cho nút đăng nhập
        btn_login.setOnClickListener(view -> loginUser());

        // thêm OnClickListener cho các nút Facebook và Google nếu đã tích hợp
        // btn_facebook.setOnClickListener(...);
        // btn_google.setOnClickListener(...);

        CheckBox cbShowpassword = findViewById(R.id.cbshowpassword);

        // Lắng nghe sự kiện thay đổi của CheckBox
        cbShowpassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Hiển thị mật khẩu
                edt_password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                // Ẩn mật khẩu
                edt_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            // Đặt con trỏ tại cuối văn bản
            edt_password.setSelection(edt_password.getText().length());
        });
        // Ẩn mật khẩu mặc định khi bắt đầu
        edt_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    private void loginUser() {
        String email = edt_email.getText().toString().trim();
        String password = edt_password.getText().toString().trim();

        // Kiểm tra các trường nhập liệu
        if (TextUtils.isEmpty(email)) {
            edt_email.setError("Vui lòng nhập email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edt_password.setError("Vui lòng nhập mật khẩu");
            return;
        }

        // Hiển thị thông báo đang đăng nhập
        Toast.makeText(this, "Đang đăng nhập...", Toast.LENGTH_SHORT).show();

        // Thực hiện đăng nhập với Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Đăng nhập thành công, chuyển hướng đến MainActivity
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        // Đăng nhập thất bại, hiển thị thông báo lỗi
                        Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Kiểm tra nếu người dùng đã đăng nhập, tự động chuyển hướng đến MainActivity
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}