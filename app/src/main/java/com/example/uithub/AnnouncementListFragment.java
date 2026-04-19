package com.example.uithub;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uithub.adapter.AnnouncementAdapter;
import com.example.uithub.models.Announcement;
import com.example.uithub.models.AnnouncementResponse;
import com.example.uithub.repository.MainRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnouncementListFragment extends Fragment {

    private static final String ARG_CATEGORY = "topic";

    private RecyclerView recyclerView;
    private AnnouncementAdapter adapter;
    private MainRepository repository;

    public AnnouncementListFragment() {
        super(R.layout.fragment_announcement_list);
    }

    public static AnnouncementListFragment newInstance(String category) {
        AnnouncementListFragment fragment = new AnnouncementListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_CATEGORY, category);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String topic = null;

        if (getArguments() != null) {
            topic = getArguments().getString(ARG_CATEGORY);
        }

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AnnouncementAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        repository = new MainRepository();

        callAPI(topic);
    }

    private void callAPI(String topic) {
        repository.getAnnouncements(topic)
                .enqueue(new Callback<AnnouncementResponse>() {
                    @Override
                    public void onResponse(Call<AnnouncementResponse> call,
                                           Response<AnnouncementResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            List<Announcement> list = response.body().getData();
                            adapter.setData(list);
                        }
                    }

                    @Override
                    public void onFailure(Call<AnnouncementResponse> call, Throwable t) {
                        Log.e("API", t.getMessage());
                    }
                });
    }
}