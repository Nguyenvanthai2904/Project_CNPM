package com.example.moneymate;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

public class MoreFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    int image[] = {R.drawable.caidat, R.drawable.thaydoimau, R.drawable.baocao, R.drawable.dangxuat};
    String name[] = {"Cài đặt", "Thay đổi màu", "Báo cáo", "Đăng xuất"};
    ArrayList<item> mylist;
    MoreMyArrayAdapter moremyarrrayadapter;
    ListView lv;

    public MoreFragment() {
        // Required empty public constructor
    }

    public static MoreFragment newInstance(String param1, String param2) {
        MoreFragment fragment = new MoreFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

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

        return view;
    }
}
