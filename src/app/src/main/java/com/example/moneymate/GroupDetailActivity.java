package com.example.moneymate;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import android.content.Intent;
public class GroupDetailActivity extends AppCompatActivity {

    private TextView tvGroupName, tvGroupID;
    private EditText edit_monthlyAmount, edt_IDthhanhvien;
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
        edit_monthlyAmount = findViewById(R.id.edit_monthlyAmount);
        edt_IDthhanhvien = findViewById(R.id.edt_IDthhanhvien);
        buttonAdd = findViewById(R.id.button2);
        tvCreatorName = findViewById(R.id.tvt_Tentruongnhom);
        listViewMembers = findViewById(R.id.listViewMembers);
        TextView tvGroupExpensesInfo = findViewById(R.id.tv_viecchinhom);

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
                Intent intent = new Intent(GroupDetailActivity.this, GroupExpensesActivity.class);
                intent.putExtra("groupID", currentGroupID);
                startActivity(intent);
            }
        });
    }

    private void loadGroupData(String groupID) {
        Log.d(TAG, "loadGroupData called for groupID: " + groupID);
        db.collection("groups").document(groupID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d(TAG, "loadGroupData: Document exists: " + documentSnapshot.exists());
                        if (documentSnapshot.exists()) {
                            // Get group creator ID
                            if (documentSnapshot.contains("members")) {
                                List<String> members = (List<String>) documentSnapshot.get("members");
                                if (members != null && !members.isEmpty()) {
                                    groupCreatorId = members.get(0); // Store the creator ID here
                                    Log.d(TAG, "loadGroupData: Creator ID: " + groupCreatorId);
                                    fetchAndDisplayCreatorName(groupCreatorId);
                                } else {
                                    Log.d(TAG, "loadGroupData: Members list is empty or null");
                                }
                            } else {
                                Log.d(TAG, "loadGroupData: Document does not contain 'members' field");
                            }

                            // Get monthly amount
                            if (documentSnapshot.contains("totalAmount")) {
                                Long monthlyAmount = documentSnapshot.getLong("totalAmount");
                                Log.d(TAG, "loadGroupData: Monthly amount: " + monthlyAmount);
                                if (monthlyAmount != null) {
                                    edit_monthlyAmount.setText(String.valueOf(monthlyAmount));
                                } else {
                                    edit_monthlyAmount.setText("0");
                                    Log.d(TAG, "loadGroupData: Monthly amount is null");
                                }
                            } else {
                                edit_monthlyAmount.setText("0");
                                Log.d(TAG, "loadGroupData: Document does not contain 'totalAmount' field");
                            }
                        } else {
                            Toast.makeText(GroupDetailActivity.this, "Group not found", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "loadGroupData: Group not found");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupDetailActivity.this, "Error loading group data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error loading group data", e);
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
                        Log.d(TAG, "fetchAndDisplayCreatorName: Document exists: " + documentSnapshot.exists());
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.contains("name")) {
                                String creatorName = documentSnapshot.getString("name");
                                Log.d(TAG, "fetchAndDisplayCreatorName: Creator name: " + creatorName);
                                if (creatorName != null) {
                                    tvCreatorName.setText("Trưởng nhóm: " + creatorName);
                                } else {
                                    tvCreatorName.setText("Trưởng nhóm: N/A");
                                    Log.d(TAG, "fetchAndDisplayCreatorName: Creator name is null");
                                }
                            } else {
                                tvCreatorName.setText("Trưởng nhóm: N/A");
                                Log.d(TAG, "fetchAndDisplayCreatorName: Document does not contain 'username' field");
                            }
                        } else {
                            tvCreatorName.setText("Trưởng nhóm: N/A");
                            Log.d(TAG, "fetchAndDisplayCreatorName: User document not found");
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
                        Log.d(TAG, "loadGroupMembers: Current data: " + documentSnapshot.getData());
                        if (documentSnapshot.contains("members")) {
                            List<String> members = (List<String>) documentSnapshot.get("members");
                            if (members != null && !members.isEmpty()) {
                                fetchMemberNames(members);
                            } else {
                                Log.d(TAG, "loadGroupMembers: members is null or empty");
                                membersList.clear();
                                runOnUiThread(() -> membersAdapter.notifyDataSetChanged());
                            }
                        } else {
                            Log.d(TAG, "loadGroupMembers: Document does not contain 'members' field");
                            membersList.clear();
                            runOnUiThread(() -> membersAdapter.notifyDataSetChanged());
                        }
                    } else {
                        Log.d(TAG, "loadGroupMembers: No such document or snapshot is null");
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
                    Log.d(TAG, "fetchMemberNames: Member document exists: " + document.getId());
                    if (document.contains("name")) {
                        String memberName = document.getString("name");
                        Log.d(TAG, "fetchMemberNames: Member name: " + memberName);
                        if (memberName != null) {
                            membersList.add(memberName);
                        } else {
                            Log.d(TAG, "fetchMemberNames: Member name is null");
                            membersList.add("Unknown User");
                        }
                    } else {
                        Log.d(TAG, "fetchMemberNames: Member document does not contain 'username' field");
                        membersList.add("Unknown User");
                    }
                } else {
                    Log.d(TAG, "fetchMemberNames: Member document does not exist: " + document.getId());
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
            Log.d(TAG, "checkIfUserIsCreatorAndAddMember: Current User ID: " + currentUserId);
            Log.d(TAG, "checkIfUserIsCreatorAndAddMember: Group Creator ID: " + groupCreatorId);

            if (currentUserId.equals(groupCreatorId)) {
                // User is the creator, proceed to add member
                String memberID = edt_IDthhanhvien.getText().toString().trim();
                if (!memberID.isEmpty()) {
                    addMemberToGroup(currentGroupID, memberID);
                } else {
                    Toast.makeText(GroupDetailActivity.this, "Please enter a User ID", Toast.LENGTH_SHORT).show();
                }
            } else {
                // User is not the creator, show error message
                Log.d(TAG, "checkIfUserIsCreatorAndAddMember: User is not the creator");
                Toast.makeText(GroupDetailActivity.this, "Only the group creator can add members.", Toast.LENGTH_SHORT).show();
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
                        Log.d(TAG, "addMemberToGroup: User exists: " + memberID);

                        db.collection("groups").document(groupID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> groupTask) {
                                if (groupTask.isSuccessful()) {
                                    DocumentSnapshot groupDocument = groupTask.getResult();
                                    if (groupDocument.exists()) {
                                        List<String> members = (List<String>) groupDocument.get("members");
                                        if (members != null && members.contains(memberID)) {
                                            Log.d(TAG, "addMemberToGroup: User is already a member of the group");
                                            Toast.makeText(GroupDetailActivity.this, "User is already a member of the group", Toast.LENGTH_SHORT).show();
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
                                                        Toast.makeText(GroupDetailActivity.this, "Member added successfully", Toast.LENGTH_SHORT).show();
                                                        Log.d(TAG, "addMemberToGroup: Member added successfully");
                                                        edt_IDthhanhvien.setText("");
                                                    } else {
                                                        Toast.makeText(GroupDetailActivity.this, "Failed to add member", Toast.LENGTH_SHORT).show();
                                                        Log.e(TAG, "addMemberToGroup: Failed to add member", task.getException());
                                                    }
                                                }
                                            });
                                        }
                                    } else {
                                        Log.d(TAG, "addMemberToGroup: Group document does not exist");
                                        Toast.makeText(GroupDetailActivity.this, "Group does not exist", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.e(TAG, "addMemberToGroup: Error checking group membership", groupTask.getException());
                                    Toast.makeText(GroupDetailActivity.this, "Error checking group membership", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Log.d(TAG, "addMemberToGroup: User does not exist: " + memberID);
                        Toast.makeText(GroupDetailActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "addMemberToGroup: Error checking user", task.getException());
                    Toast.makeText(GroupDetailActivity.this, "Error checking user", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (membersListener != null) {
            membersListener.remove();
            Log.d(TAG, "onDestroy: membersListener removed");
        }
    }
}