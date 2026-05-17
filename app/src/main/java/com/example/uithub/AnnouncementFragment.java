package com.example.uithub;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.uithub.adapter.AnnouncementPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AnnouncementFragment extends Fragment {

    public AnnouncementFragment() {
        super(R.layout.fragment_announcement);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        AnnouncementPagerAdapter adapter = new AnnouncementPagerAdapter(this);
        viewPager.setAdapter(adapter);

        final String[] tabTitles = {
            getString(R.string.announcement_tab_general),
            getString(R.string.announcement_tab_registration),
            getString(R.string.announcement_tab_schedule),
            getString(R.string.announcement_tab_tuition),
            getString(R.string.announcement_tab_other)
        };

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }
}