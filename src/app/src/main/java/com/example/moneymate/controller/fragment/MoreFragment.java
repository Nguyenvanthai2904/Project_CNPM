package com.example.moneymate.controller.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import android.widget.AdapterView;
import android.content.Intent;

import com.example.moneymate.controller.activity.Export;
import com.example.moneymate.controller.activity.LoginActivity;
import com.example.moneymate.controller.activity.Setting;
import com.example.moneymate.controller.adapter.MoreMyArrayAdapter;
import com.example.moneymate.R;
import com.example.moneymate.controller.activity.group;
import com.example.moneymate.controller.activity.personal;
import com.example.moneymate.model.item;
import com.google.firebase.auth.FirebaseAuth;

public class MoreFragment extends Fragment {
    // Khai báo FirebaseAuth
    private FirebaseAuth mAuth;

    int image[] = {R.drawable.thongtincanhan, R.drawable.caidat, R.drawable.thaydoimau, R.drawable.baocao, R.drawable.dangxuat};
    String name[] = {"Thông tin cá nhân", "Cài đặt", "Quỹ chung", "Báo cáo", "Đăng xuất"};
    ArrayList<item> mylist;
    MoreMyArrayAdapter moremyarrrayadapter;
    ListView lv;

    public MoreFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        // Initialize the list of items
        mylist = new ArrayList<>();
        for (int i = 0; i < name.length; i++) {
            mylist.add(new item(image[i], name[i]));
        }

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        // Initialize the ListView and set up the adapter here
        lv = view.findViewById(R.id.lv);
        moremyarrrayadapter = new MoreMyArrayAdapter(getActivity(), R.layout.layout_item, mylist);
        lv.setAdapter(moremyarrrayadapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = mylist.get(position).getName(); // Lấy tên của mục được chọn
                Intent myIntent;

                switch (selectedItem) {
                    case"Thông tin cá nhân":
                        myIntent = new Intent(getActivity(), personal.class);
                        break;
                    case "Cài đặt":
                        myIntent = new Intent(getActivity(), Setting.class);
                        break;
                    case "Quỹ chung":
                        myIntent = new Intent(getActivity(), group.class);
                        break;
                    case "Báo cáo":
                        myIntent = new Intent(getActivity(), Export.class);
                        break;
                    case "Đăng xuất":
                        // Thực hiện đăng xuất
                        mAuth.signOut();
                        // Chuyển hướng đến LoginActivity
                        myIntent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(myIntent);
                        // Kết thúc Fragment để ngăn người dùng quay lại
                        getActivity().finish();
                        break;
                    default:
                        myIntent = new Intent(getActivity(), LoginActivity.class); // Activity mặc định
                        break;
                }
                startActivity(myIntent);
            }
        });
        return view;
    }
}
