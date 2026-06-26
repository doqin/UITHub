package com.example.uithub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uithub.adapter.ScheduleItemAdapter;
import com.example.uithub.models.ScheduleItem;

import java.util.ArrayList;
import java.util.List;


public class ScheduleDayFragment extends Fragment {

    private RecyclerView recyclerView;

    public static ScheduleDayFragment newInstance(String day, List<ScheduleItem> list) {
        ScheduleDayFragment fragment = new ScheduleDayFragment();

        Bundle args = new Bundle();
        args.putString("day", day);
        args.putSerializable("data", new ArrayList<>(list));

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_schedule_day, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);

        List<ScheduleItem> list =
                (List<ScheduleItem>) getArguments().getSerializable("data");

        ScheduleItemAdapter adapter = new ScheduleItemAdapter(list);
        adapter.setOnCalendarSyncListener((title, location, description,
                                            vietnameseDay, dateStr, endDateStr,
                                            beginTime, endTime) -> {
            ScheduleFragment parent = (ScheduleFragment) getParentFragment();
            if (parent != null) {
                parent.addCalendarEvent(title, location, description,
                        vietnameseDay, dateStr, endDateStr, beginTime, endTime);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }
}