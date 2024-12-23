package com.example.moneymate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText edt_name, edt_email2, edt_password2;
    private TextView textViewHaveAccount;
    private Button btn_signup;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Link UI elements
        edt_name = findViewById(R.id.edt_name);
        edt_email2 = findViewById(R.id.edt_email2);
        edt_password2 = findViewById(R.id.edt_password2);
        btn_signup = findViewById(R.id.btn_signup);
        textViewHaveAccount = findViewById(R.id.textViewHaveAccount);
        // Apply window insets for edge-to-edge design
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // Set button click listener for signup
        btn_signup.setOnClickListener(v -> createUser());
        textViewHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_haveAccount = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent_haveAccount);
            }
        });
    }


    private void createUser() {
        String name = edt_name.getText().toString().trim();
        String email = edt_email2.getText().toString().trim();
        String password = edt_password2.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = auth.getCurrentUser().getUid();

                        // Create a user profile in Firestore
                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("email", email);

                        db.collection("users").document(userId).set(user)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Tài khoản đã được tạo thành công!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Lưu dữ liệu người dùng không thành công", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
