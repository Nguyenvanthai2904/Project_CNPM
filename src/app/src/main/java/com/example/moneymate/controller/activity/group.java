package com.example.moneymate.controller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moneymate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class group extends AppCompatActivity {

    private EditText groupNameEditText, groupIdEditText, joinGroupIdEditText;
    private Button createGroupButton, joinGroupButton;
    private ListView groupListView;
    private ProgressBar loadingProgressBar;
    private LinearLayout inputLayout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private List<String> groupNameList;
    private List<String> groupIDList;
    private List<Double> monthlyAmount;
    private ArrayAdapter<String> groupAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        groupNameEditText = findViewById(R.id.edt_Name);
        groupIdEditText = findViewById(R.id.edt_IDtao);
        createGroupButton = findViewById(R.id.btn_Taonhom);
        joinGroupIdEditText = findViewById(R.id.edt_IDthamgia);
        joinGroupButton = findViewById(R.id.btn_Thamgia);
        groupListView = findViewById(R.id.lv_Danhsachnhom);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        inputLayout = findViewById(R.id.inputLayout);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        groupNameList = new ArrayList<>();
        groupIDList = new ArrayList<>();

        groupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupNameList);
        groupListView.setAdapter(groupAdapter);

        createGroupButton.setOnClickListener(v -> createGroup());
        joinGroupButton.setOnClickListener(v -> joinGroup());

        groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedGroupID = groupIDList.get(position);
                String selectedGroupName = groupNameList.get(position);

                Intent intent = new Intent(group.this, GroupDetailActivity.class);
                intent.putExtra("groupID", selectedGroupID);
                intent.putExtra("groupName", selectedGroupName);
                startActivity(intent);
            }
        });

        loadGroups();
    }

    private void showLoadingIndicator() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        inputLayout.setVisibility(View.GONE);
        groupListView.setVisibility(View.GONE);
    }

    private void hideLoadingIndicator() {
        loadingProgressBar.setVisibility(View.GONE);
        inputLayout.setVisibility(View.VISIBLE);
        groupListView.setVisibility(View.VISIBLE);
    }

    private void loadGroups() {
        showLoadingIndicator();
        FirebaseUser currentUser = mAuth.getCurrentUser();


        String uid = currentUser.getUid();
        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists() && document.contains("groups")) {
                    List<String> groupIds = (List<String>) document.get("groups");
                    if (groupIds != null && !groupIds.isEmpty()) {
                        groupNameList.clear();
                        groupIDList.clear();
                        AtomicInteger groupCount = new AtomicInteger(groupIds.size());
                        for (String groupId : groupIds) {
                            loadGroupName(groupId, groupCount);
                        }
                    } else {
                        showToast("Người dùng chưa có nhóm nào.");
                        hideLoadingIndicator();
                    }
                } else {
                    showToast("Không tìm thấy dữ liệu người dùng hoặc không có nhóm nào tồn tại.");
                    hideLoadingIndicator();
                }
            } else {
                showToast("Không thể tải nhóm: " + task.getException().getMessage());
                hideLoadingIndicator();
            }
        });
    }

    private void loadGroupName(String groupId, AtomicInteger groupCount) {
        db.collection("groups").document(groupId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists() && document.contains("name")) {
                    String groupName = document.getString("name");
                    groupNameList.add(groupName);
                    groupIDList.add(groupId);

                    if (groupCount.decrementAndGet() == 0) {
                        runOnUiThread(() -> groupAdapter.notifyDataSetChanged());
                    }
                } else {
                    showToast("Không tìm thấy tên nhóm cho ID nhóm: " + groupId);
                }
            } else {
                showToast("Không thể tải tên nhóm: " + task.getException().getMessage());
            }
            hideLoadingIndicator();
        });
    }

    private void createGroup() {
        String groupName = groupNameEditText.getText().toString().trim();
        String groupId = groupIdEditText.getText().toString().trim();


        if (TextUtils.isEmpty(groupName) || TextUtils.isEmpty(groupId)) {
            showToast("Vui lòng điền tất cả các trường.");
            return;
        }


        FirebaseUser currentUser = mAuth.getCurrentUser();

        String uid = currentUser.getUid();

        Map<String, Object> groupData = new HashMap<>();
        groupData.put("name", groupName);
        List<String> members = new ArrayList<>();
        members.add(uid);
        groupData.put("members", members);

        showLoadingIndicator();
        db.collection("groups").document(groupId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            hideLoadingIndicator();
                            showToast("ID nhóm đã tồn tại.");
                        } else {
                            db.collection("groups").document(groupId)
                                    .set(groupData)
                                    .addOnSuccessListener(aVoid -> {
                                        showToast("Nhóm đã được tạo thành công.");
                                        addGroupToUser(groupId, uid);
                                        clearInputFields();
                                        loadGroups();
                                    })
                                    .addOnFailureListener(e -> {
                                        hideLoadingIndicator();
                                        showToast("Tạo nhóm thất bại: " + e.getMessage());
                                    });
                        }
                    } else {
                        hideLoadingIndicator();
                        showToast("Kiểm tra ID nhóm thất bại: " + task.getException().getMessage());
                    }
                });
    }

    private void joinGroup() {
        String groupIdToJoin = joinGroupIdEditText.getText().toString().trim();

        if (TextUtils.isEmpty(groupIdToJoin)) {
            showToast("Vui lòng nhập ID nhóm.");
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            showToast("Bạn chưa đăng nhập.");
            return;
        }
        String uid = currentUser.getUid();

        showLoadingIndicator();
        db.collection("groups").document(groupIdToJoin)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Add user to group's members
                            db.collection("groups").document(groupIdToJoin)
                                    .update("members", FieldValue.arrayUnion(uid))
                                    .addOnSuccessListener(aVoid -> {
                                        // Add group to user's groups
                                        addGroupToUser(groupIdToJoin, uid);
                                        showToast("Tham gia nhóm thành công.");
                                        joinGroupIdEditText.setText("");
                                        loadGroups();
                                    })
                                    .addOnFailureListener(e -> {
                                        hideLoadingIndicator();
                                        showToast("Tham gia nhóm thất bại: " + e.getMessage());
                                    });
                        } else {
                            hideLoadingIndicator();
                            showToast("ID nhóm không tồn tại.");
                        }
                    } else {
                        hideLoadingIndicator();
                        showToast("Kiểm tra ID nhóm thất bại: " + task.getException().getMessage());
                    }
                });
    }

    private void addGroupToUser(String groupId, String userId) {
        db.collection("users").document(userId)
                .update("groups", FieldValue.arrayUnion(groupId))
                .addOnSuccessListener(aVoid -> {
                    Log.d("group", "Nhóm đã được thêm vào người dùng thành công");
                    hideLoadingIndicator();
                })
                .addOnFailureListener(e -> {
                    showToast("Thêm nhóm vào người dùng thất bại: " + e.getMessage());
                    hideLoadingIndicator();
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void clearInputFields() {
        groupNameEditText.setText("");
        groupIdEditText.setText("");

    }
}