package com.example.uithub;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

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
    private static final int PAGE_SIZE = 15;

    private RecyclerView recyclerView;
    private AnnouncementAdapter adapter;
    private MainRepository repository;
    private ProgressBar spinner;

    private int currentSkip = 0;
    private boolean isLoading = false;
    private boolean hasMore = true;
    private String currentTopic = null;

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

        if (getArguments() != null) {
            currentTopic = getArguments().getString(ARG_CATEGORY);
        }

        recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new AnnouncementAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        repository = new MainRepository();

        spinner = view.findViewById(R.id.loadingSpinner);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) { // check for scroll down
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && hasMore) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            callAPI(currentTopic, currentSkip, PAGE_SIZE);
                        }
                    }
                }
            }
        });

        callAPI(currentTopic, currentSkip, PAGE_SIZE);
    }

    private void callAPI(String topic, int skip, int limit) {
        isLoading = true;
        spinner.setVisibility(View.VISIBLE);
        repository.getAnnouncements(topic, skip, limit)
                .enqueue(new Callback<AnnouncementResponse>() {
                    @Override
                    public void onResponse(Call<AnnouncementResponse> call,
                                           Response<AnnouncementResponse> response) {
                        isLoading = false;
                        spinner.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            List<Announcement> list = response.body().getData();
                            if (skip == 0) {
                                adapter.setData(list);
                            } else {
                                adapter.addData(list);
                            }

                            int loadedCount = list != null ? list.size() : 0;
                            hasMore = loadedCount == limit;
                            if (loadedCount > 0) {
                                currentSkip += loadedCount;
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<AnnouncementResponse> call, Throwable t) {
                        isLoading = false;
                        spinner.setVisibility(View.GONE);
                        Log.e("API", t.getMessage());
                    }
                });
    }
}
