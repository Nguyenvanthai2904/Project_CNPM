package com.example.moneymate.controller.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moneymate.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
public class GroupDetailActivity extends AppCompatActivity {

    private TextView tvGroupName, tvGroupID;
    private EditText edt_IDthhanhvien;
    private Button buttonAdd;
    private TextView tvCreatorName;
    private ListView listViewMembers;

    private ArrayList<String> membersList;
    private ArrayAdapter<String> membersAdapter;

    private FirebaseFirestore db;
    private String currentGroupID;
    private String groupCreatorId; // Store the creator's ID

    private static final String TAG = "GroupDetailActivity";

    private ListenerRegistration membersListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        tvGroupName = findViewById(R.id.tvGroupName);
        tvGroupID = findViewById(R.id.tvGroupID);
        edt_IDthhanhvien = findViewById(R.id.edt_IDthhanhvien);
        buttonAdd = findViewById(R.id.button2);
        tvCreatorName = findViewById(R.id.tvt_Tentruongnhom);
        listViewMembers = findViewById(R.id.listViewMembers);
        TextView tvGroupExpensesInfo = findViewById(R.id.tv_viecchinhom);
        TextView tvGroupIncomeInfo = findViewById(R.id.tv_Dongquy);

        db = FirebaseFirestore.getInstance();

        membersList = new ArrayList<>();
        membersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, membersList);
        listViewMembers.setAdapter(membersAdapter);

        String groupName = getIntent().getStringExtra("groupName");
        String groupID = getIntent().getStringExtra("groupID");
        currentGroupID = groupID;

        tvGroupName.setText("Tên nhóm: " + groupName);
        tvGroupID.setText("ID nhóm: " + groupID);

        loadGroupData(groupID);
        loadGroupMembers(groupID);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIfUserIsCreatorAndAddMember();
            }
        });
        tvGroupExpensesInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(GroupDetailActivity.this, GroupExpensesActivity.class);
                intent1.putExtra("groupID", currentGroupID);
                
                startActivity(intent1);
            }
        });
        tvGroupIncomeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(GroupDetailActivity.this, incomegroups.class);
                intent2.putExtra("groupID", currentGroupID);

                startActivity(intent2);
            }
        });

        // Thêm sự kiện click cho ListView
        listViewMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedMemberName = membersList.get(position);
                showDeleteConfirmationDialog(selectedMemberName);
            }
        });
    }

    // Phương thức hiển thị hộp thoại xác nhận xóa
    private void showDeleteConfirmationDialog(String memberName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xóa thành viên");
        builder.setMessage("Bạn có chắc chắn muốn xóa " + memberName + " khỏi nhóm?");

        builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Xóa thành viên
                deleteMember(memberName);
            }
        });

        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Đóng hộp thoại
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Phương thức xóa thành viên
    private void deleteMember(String memberName) {
        // Lấy ID của thành viên từ membersList dựa trên tên
        getMemberIdFromName(memberName, new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    String memberId = task.getResult();
                    if (memberId != null) {
                        // Kiểm tra xem người dùng hiện tại có phải là trưởng nhóm không
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null && currentUser.getUid().equals(groupCreatorId)) {
                            // Xóa thành viên khỏi nhóm
                            removeMemberFromGroup(currentGroupID, memberId);
                        } else {
                            Toast.makeText(GroupDetailActivity.this, "Chỉ trưởng nhóm mới có thể xóa thành viên.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(GroupDetailActivity.this, "Không tìm thấy ID thành viên.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(GroupDetailActivity.this, "Lỗi khi lấy ID thành viên.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    // Phương thức lấy ID thành viên từ tên
    private void getMemberIdFromName(String memberName, OnCompleteListener<String> onCompleteListener) {
        db.collection("users")
                .whereEqualTo("name", memberName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                onCompleteListener.onComplete(Tasks.forResult(document.getId()));
                                return;
                            }
                            onCompleteListener.onComplete(Tasks.forResult(null)); // Không tìm thấy user
                        } else {
                            // Trả về Task<String> với giá trị null để biểu thị lỗi
                            onCompleteListener.onComplete(Tasks.forResult(null));
                            // Hoặc bạn có thể trả về một exception:
                            // onCompleteListener.onComplete(Tasks.forException(task.getException()));
                            Log.e(TAG, "Lỗi khi lấy ID thành viên", task.getException());
                        }
                    }
                });
    }

    // Phương thức xóa thành viên khỏi nhóm
    private void removeMemberFromGroup(String groupID, String memberID) {
        WriteBatch batch = db.batch();

        // Cập nhật danh sách thành viên trong nhóm
        DocumentReference groupRef = db.collection("groups").document(groupID);
        batch.update(groupRef, "members", FieldValue.arrayRemove(memberID));

        // Cập nhật danh sách nhóm của thành viên
        DocumentReference userRef = db.collection("users").document(memberID);
        batch.update(userRef, "groups", FieldValue.arrayRemove(groupID));

        // Thực hiện batch
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(GroupDetailActivity.this, "Thành viên đã được xóa khỏi nhóm", Toast.LENGTH_SHORT).show();
                    // Cập nhật lại danh sách thành viên
                    loadGroupMembers(groupID);
                } else {
                    Toast.makeText(GroupDetailActivity.this, "Lỗi khi xóa thành viên", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadGroupData(String groupID) {
        Log.d(TAG, "loadGroupData gọi cho groupID: " + groupID);
        db.collection("groups").document(groupID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d(TAG, "loadGroupData:  Tài liệu đã tồn tại: " + documentSnapshot.exists());
                        if (documentSnapshot.exists()) {
                            // Get group creator ID
                            if (documentSnapshot.contains("members")) {
                                List<String> members = (List<String>) documentSnapshot.get("members");
                                if (members != null && !members.isEmpty()) {
                                    groupCreatorId = members.get(0); // Store the creator ID here
                                    Log.d(TAG, "loadGroupData: ID người tạo: " + groupCreatorId);
                                    fetchAndDisplayCreatorName(groupCreatorId);
                                } else {
                                    Log.d(TAG, "loadGroupData: Danh sách thành viên trống");
                                }
                            } else {
                                Log.d(TAG, "loadGroupData: Document does not contain 'members' field");
                            }

                        } else {
                            Toast.makeText(GroupDetailActivity.this, "Không tìm thấy nhóm", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "loadGroupData: Không tìm thấy nhóm");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupDetailActivity.this, "Lỗi khi tải dữ liệu nhóm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Lỗi khi tải dữ liệu nhóm", e);
                    }
                });
    }

    private void fetchAndDisplayCreatorName(String creatorId) {
        Log.d(TAG, "fetchAndDisplayCreatorName called for creatorId: " + creatorId);
        db.collection("users").document(creatorId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d(TAG, "fetchAndDisplayCreatorName: Tài liệu đã tồn tại: " + documentSnapshot.exists());
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.contains("name")) {
                                String creatorName = documentSnapshot.getString("name");
                                Log.d(TAG, "fetchAndDisplayCreatorName: Tên người dùng: " + creatorName);
                                if (creatorName != null) {
                                    tvCreatorName.setText("Trưởng nhóm: " + creatorName);
                                } else {
                                    tvCreatorName.setText("Trưởng nhóm: N/A");
                                    Log.d(TAG, "fetchAndDisplayCreatorName: Tên người dùng trống");
                                }
                            } else {
                                tvCreatorName.setText("Trưởng nhóm: N/A");
                                Log.d(TAG, "fetchAndDisplayCreatorName: Tài liệu không chứa trường 'username'");
                            }
                        } else {
                            tvCreatorName.setText("Trưởng nhóm: N/A");
                            Log.d(TAG, "fetchAndDisplayCreatorName: Không tìm thấy tài liệu người dùng");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error loading creator name: " + e.getMessage(), e);
                        tvCreatorName.setText("Trưởng nhóm: N/A");
                    }
                });
    }

    private void loadGroupMembers(String groupID) {
        Log.d(TAG, "loadGroupMembers called for groupID: " + groupID);

        if (membersListener != null) {
            membersListener.remove();
        }

        membersListener = db.collection("groups").document(groupID)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "loadGroupMembers: Listen failed.", e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Log.d(TAG, "loadGroupMembers: Dữ liệu hiện tại: " + documentSnapshot.getData());
                        if (documentSnapshot.contains("members")) {
                            List<String> members = (List<String>) documentSnapshot.get("members");
                            if (members != null && !members.isEmpty()) {
                                fetchMemberNames(members);
                            } else {
                                Log.d(TAG, "loadGroupMembers: members rỗng");
                                membersList.clear();
                                runOnUiThread(() -> membersAdapter.notifyDataSetChanged());
                            }
                        } else {
                            Log.d(TAG, "loadGroupMembers: Tài liệu không chứa trường 'members");
                            membersList.clear();
                            runOnUiThread(() -> membersAdapter.notifyDataSetChanged());
                        }
                    } else {
                        Log.d(TAG, "loadGroupMembers: Không có tài liệu nào");
                        membersList.clear();
                        runOnUiThread(() -> membersAdapter.notifyDataSetChanged());
                    }
                });
    }

    private void fetchMemberNames(List<String> memberIds) {
        Log.d(TAG, "fetchMemberNames called for memberIds: " + memberIds);
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String memberId : memberIds) {
            Task<DocumentSnapshot> task = db.collection("users").document(memberId).get();
            tasks.add(task);
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(objects -> {
            membersList.clear();
            for (Object object : objects) {
                DocumentSnapshot document = (DocumentSnapshot) object;
                if (document.exists()) {
                    Log.d(TAG, "fetchMemberNames: Tài liệu thành viên đã tồn tại: " + document.getId());
                    if (document.contains("name")) {
                        String memberName = document.getString("name");
                        Log.d(TAG, "fetchMemberNames: Tên thành viên: " + memberName);
                        if (memberName != null) {
                            membersList.add(memberName);
                        } else {
                            Log.d(TAG, "fetchMemberNames: Tên thành viên bị trống");
                            membersList.add("Unknown User");
                        }
                    } else {
                        Log.d(TAG, "fetchMemberNames: Tài liệu thành viên không chứa trường 'username");
                        membersList.add("Unknown User");
                    }
                } else {
                    Log.d(TAG, "fetchMemberNames: Tài liệu thành viên không tồn tại: " + document.getId());
                    membersList.add("Unknown User");
                }
            }
            runOnUiThread(() -> membersAdapter.notifyDataSetChanged());
        });
    }

    private void checkIfUserIsCreatorAndAddMember() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            Log.d(TAG, "checkIfUserIsCreatorAndAddMember: ID người dùng hiện tại: " + currentUserId);
            Log.d(TAG, "checkIfUserIsCreatorAndAddMember: ID người tạo nhóm: " + groupCreatorId);

            if (currentUserId.equals(groupCreatorId)) {
                // User is the creator, proceed to add member
                String memberID = edt_IDthhanhvien.getText().toString().trim();
                if (!memberID.isEmpty()) {
                    addMemberToGroup(currentGroupID, memberID);
                } else {
                    Toast.makeText(GroupDetailActivity.this, "Vui lòng nhập ID người dùng", Toast.LENGTH_SHORT).show();
                }
            } else {
                // User is not the creator, show error message
                Log.d(TAG, "checkIfUserIsCreatorAndAddMember: Người dùng không phải là người tạo");
                Toast.makeText(GroupDetailActivity.this, "Chỉ nhóm trưởng mới thêm được thành viên.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // No user is logged in
            Log.d(TAG, "checkIfUserIsCreatorAndAddMember: No user logged in");
            Toast.makeText(GroupDetailActivity.this, "You must be logged in to perform this action.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addMemberToGroup(String groupID, String memberID) {
        Log.d(TAG, "addMemberToGroup called for groupID: " + groupID + ", memberID: " + memberID);

        db.collection("users").document(memberID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot userDocument = task.getResult();
                    if (userDocument.exists()) {
                        Log.d(TAG, "addMemberToGroup: Người dùng tồn tại: " + memberID);

                        db.collection("groups").document(groupID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> groupTask) {
                                if (groupTask.isSuccessful()) {
                                    DocumentSnapshot groupDocument = groupTask.getResult();
                                    if (groupDocument.exists()) {
                                        List<String> members = (List<String>) groupDocument.get("members");
                                        if (members != null && members.contains(memberID)) {
                                            Log.d(TAG, "addMemberToGroup: Người dùng đã là thành viên của nhóm");
                                            Toast.makeText(GroupDetailActivity.this, "Người dùng đã là thành viên của nhóm", Toast.LENGTH_SHORT).show();
                                        } else {
                                            WriteBatch batch = db.batch();

                                            DocumentReference groupRef = db.collection("groups").document(groupID);
                                            batch.update(groupRef, "members", FieldValue.arrayUnion(memberID));

                                            DocumentReference userRef = db.collection("users").document(memberID);
                                            batch.update(userRef, "groups", FieldValue.arrayUnion(groupID));

                                            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(GroupDetailActivity.this, "Đã thêm thành viên thành công", Toast.LENGTH_SHORT).show();
                                                        Log.d(TAG, "addMemberToGroup: Đã thêm thành viên thành công");
                                                        edt_IDthhanhvien.setText("");
                                                    } else {
                                                        Toast.makeText(GroupDetailActivity.this, "Thêm thành viên không thành công", Toast.LENGTH_SHORT).show();
                                                        Log.e(TAG, "addMemberToGroup: Thêm thành viên không thành công", task.getException());
                                                    }
                                                }
                                            });
                                        }
                                    } else {
                                        Log.d(TAG, "addMemberToGroup: Tài liệu nhóm không tồn tại");
                                        Toast.makeText(GroupDetailActivity.this, "Nhóm không tồn tại", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.e(TAG, "addMemberToGroup: Error checking group membership", groupTask.getException());
                                    Toast.makeText(GroupDetailActivity.this, "Lỗi kiểm tra tư cách thành viên nhóm.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Log.d(TAG, "addMemberToGroup: Người dùng không tồn tại: " + memberID);
                        Toast.makeText(GroupDetailActivity.this, "Người dùng không tồn tại", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "addMemberToGroup: Lỗi kiểm tra người dùng", task.getException());
                    Toast.makeText(GroupDetailActivity.this, "Lỗi kiểm tra người dùng.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (membersListener != null) {
            membersListener.remove();
            Log.d(TAG, "onDestroy: đã gỡ bỏ members Listener.");
        }
    }
}