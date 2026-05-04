package com.example.uithub.models;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

public class DayScheduleFragment extends Fragment {

    public static DayScheduleFragment newInstance(int dayIndex) {
        DayScheduleFragment fragment = new DayScheduleFragment();
        Bundle args = new Bundle();
        args.putInt("day", dayIndex);
        fragment.setArguments(args);
        return fragment;
    }
}