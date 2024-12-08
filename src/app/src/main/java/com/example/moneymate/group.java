package com.example.moneymate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class group extends AppCompatActivity {

    private EditText edtName, edtID, edtAmount, edtDeadline;
    private Button btnCreateGroup;
    private ListView listViewGroups;

    private FirebaseFirestore db; // Firestore instance
    private FirebaseAuth auth; // Firebase Auth instance
    private List<String> groupList; // Danh sách nhóm
    private ArrayAdapter<String> adapter; // Adapter cho ListView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        // Liên kết các thành phần giao diện
        edtName = findViewById(R.id.edt_Name);
        edtID = findViewById(R.id.edt_IDtao);
        edtAmount = findViewById(R.id.edt_Tienhangthang);
        edtDeadline = findViewById(R.id.edt_Han);
        btnCreateGroup = findViewById(R.id.btn_Taonhom); // Nút "Tạo nhóm"
        listViewGroups = findViewById(R.id.lv_Danhsachnhom);

        // Khởi tạo Firebase Firestore và Auth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        groupList = new ArrayList<>();

        // Cài đặt adapter cho ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupList);
        listViewGroups.setAdapter(adapter);

        // Xử lý sự kiện nút "Tạo nhóm"
        btnCreateGroup.setOnClickListener(v -> createGroup());

        // Tải danh sách nhóm
        loadGroups();

        listViewGroups.setOnItemClickListener((parent, view, position, id) -> {
            String selectedGroup = groupList.get(position); // Lấy tên nhóm được chọn

            // Lấy ID người dùng đã đăng nhập
            String userId = auth.getCurrentUser().getUid(); // ID người dùng hiện tại

            // Truy vấn Firestore để lấy thông tin chi tiết của nhóm
            db.collection("users")
                    .document(userId) // Lấy dữ liệu từ người dùng hiện tại
                    .collection("groups")
                    .whereEqualTo("name", selectedGroup) // Lọc nhóm theo tên
                    .get()
                    .addOnSuccessListener(DocumentSnapshots -> {
                        if (!DocumentSnapshots.isEmpty()) {
                            // Lấy thông tin chi tiết nhóm
                            DocumentSnapshot document = DocumentSnapshots.getDocuments().get(0);
                            String groupName = document.getString("name");
                            String groupID = document.getString("id");
                            double monthlyAmount = document.getDouble("monthlyAmount");
                            String deadline = document.getString("deadline");

                            // Chuyển sang Activity mới và truyền dữ liệu
                            Intent intent = new Intent(group.this, GroupDetailActivity.class);
                            intent.putExtra("groupName", groupName);
                            intent.putExtra("groupID", groupID);
                            intent.putExtra("monthlyAmount", monthlyAmount);
                            intent.putExtra("deadline", deadline);
                            startActivity(intent);
                        } else {
                            Toast.makeText(group.this, "Không tìm thấy thông tin nhóm!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(group.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void createGroup() {
        // Lấy dữ liệu từ các EditText
        String name = edtName.getText().toString().trim();
        String id = edtID.getText().toString().trim();
        String amount = edtAmount.getText().toString().trim();
        String deadline = edtDeadline.getText().toString().trim();

        // Kiểm tra nếu có trường nào bỏ trống
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(id) || TextUtils.isEmpty(amount) || TextUtils.isEmpty(deadline)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Chuyển đổi số tiền thành Double
            double monthlyAmount = Double.parseDouble(amount);

            // Lấy ID người dùng đã đăng nhập
            String userId = auth.getCurrentUser().getUid(); // ID người dùng hiện tại

            // Chuẩn bị dữ liệu để lưu
            Map<String, Object> groupData = new HashMap<>();
            groupData.put("name", name);
            groupData.put("id", id);
            groupData.put("monthlyAmount", monthlyAmount);
            groupData.put("deadline", deadline);

            // Lưu dữ liệu vào Firestore
            db.collection("users") // Thay đổi nếu cần
                    .document(userId) // Lưu vào ID người dùng hiện tại
                    .collection("groups")
                    .add(groupData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Nhóm đã được tạo thành công!", Toast.LENGTH_SHORT).show();
                        groupList.add(name); // Thêm nhóm vào danh sách
                        adapter.notifyDataSetChanged(); // Cập nhật danh sách hiển thị
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền phải là số hợp lệ!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadGroups() {
        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid(); // Lấy ID người dùng hiện tại

        db.collection("users")
                .document(userId) // Thay USER_ID bằng ID người dùng hiện tại
                .collection("groups")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        groupList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String groupName = document.getString("name");
                            if (groupName != null) {
                                groupList.add(groupName); // Thêm tên nhóm
                            }
                        }
                        adapter.notifyDataSetChanged(); // Cập nhật danh sách hiển thị
                    } else {
                        Toast.makeText(this, "Không thể tải danh sách nhóm.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
