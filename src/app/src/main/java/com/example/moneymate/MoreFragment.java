package com.example.moneymate;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import android.widget.AdapterView;
import android.content.Intent;
public class MoreFragment extends Fragment {


    int image[] = {R.drawable.caidat, R.drawable.thaydoimau, R.drawable.baocao, R.drawable.dangxuat};
    String name[] = {"Cài đặt", "Thay đổi màu", "Báo cáo", "Đăng xuất"};
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
                    case "Cài đặt":
                        myIntent = new Intent(getActivity(), LoginActivity.class);
                        break;
                    case "Thay đổi màu":
                        myIntent = new Intent(getActivity(), LoginActivity.class);
                        break;
                    case "Báo cáo":
                        myIntent = new Intent(getActivity(), LoginActivity.class);
                        break;
                    case "Đăng xuất":
                        myIntent = new Intent(getActivity(), LoginActivity.class);
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
