package com.example.moneymate.controller.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.moneymate.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TransactionFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        // Set Adapter for ViewPager2
        viewPager.setAdapter(new TabPagerAdapter(this));

        // Attach TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Inflate custom tab layout
            View customTab = LayoutInflater.from(getContext()).inflate(R.layout.custom_tab, null);
            TextView tabTitle = customTab.findViewById(R.id.custom_tab);
            if (position == 0) {
                tabTitle.setText("Tiền chi");
            } else {
                tabTitle.setText("Tiền thu");
            }
            tab.setCustomView(customTab);
        }).attach();

        return view;
    }

    private class TabPagerAdapter extends FragmentStateAdapter {
        public TabPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new Tab1Fragment();
            } else {
                return new Tab2Fragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2; // Số lượng tab
        }
    }
}
