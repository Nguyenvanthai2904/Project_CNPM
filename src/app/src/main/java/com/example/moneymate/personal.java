package com.example.moneymate;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class personal extends AppCompatActivity {

    private EditText edtNameuser;
    private EditText edtIDcanhan;
    private ImageButton btnCopyId;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String TAG = "personal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        edtNameuser = findViewById(R.id.edt_Nameuser);
        edtIDcanhan = findViewById(R.id.edt_IDcanhan);
        btnCopyId = findViewById(R.id.btn_copy_id); // Get the ImageButton

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            edtIDcanhan.setText(userId);

            fetchUserData(userId);

            // Set click listener for the copy button
            btnCopyId.setOnClickListener(view -> {
                String idToCopy = edtIDcanhan.getText().toString();
                if (!idToCopy.isEmpty()) {
                    copyToClipboard(idToCopy);
                    Toast.makeText(personal.this, "ID đã được sao chép vào bảng tạm", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(this, "Chưa có người dùng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchUserData(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String name = document.getString("name");
                                edtNameuser.setText(name);

                                Log.d(TAG, "Name: " + name);
                                Log.d(TAG, "User ID: " + userId);
                            } else {
                                Log.d(TAG, "Không có tài liệu như vậy");
                            }
                        } else {
                            Log.w(TAG, "Lỗi khi lấy tài liệu", task.getException());
                            Toast.makeText(personal.this, "Lỗi khi lấy dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("User ID", text);
        clipboard.setPrimaryClip(clip);
    }
}