package com.example.uithub.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.uithub.AnnouncementListFragment;

public class AnnouncementPagerAdapter extends FragmentStateAdapter {

    private final String[] topics = {
            null, "ĐKHP", "Schedule", "HP", "Misc"
    };

    public AnnouncementPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return AnnouncementListFragment.newInstance(topics[position]);
    }

    @Override
    public int getItemCount() {
        return topics.length;
    }
}