package com.example.uithub.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.uithub.ScheduleDayFragment;
import com.example.uithub.models.ScheduleItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchedulePagerAdapter extends FragmentStateAdapter {

    private List<String> days = new ArrayList<>();
    private Map<String, List<ScheduleItem>> data = new HashMap<>();

    public SchedulePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public void setData(Map<String, List<ScheduleItem>> map) {
        data.clear();
        data.putAll(map);

        days.clear();
        days.addAll(map.keySet());

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String day = days.get(position);
        return ScheduleDayFragment.newInstance(day, data.get(day));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }
}