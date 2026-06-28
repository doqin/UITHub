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
import com.example.uithub.utils.PreferenceManager;
import com.google.gson.Gson;

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
    private PreferenceManager preferenceManager;
    private ProgressBar spinner;

    private int currentSkip = 0;
    private boolean isLoading = false;
    private boolean hasMore = true;
    private boolean apiPagingEnabled = false;
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
        preferenceManager = new PreferenceManager(requireContext());

        spinner = view.findViewById(R.id.loadingSpinner);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) { // check for scroll down
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (apiPagingEnabled && !isLoading && hasMore) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            callAPI(currentTopic, currentSkip, PAGE_SIZE);
                        }
                    }
                }
            }
        });

        loadFromCache();
    }

    public void reloadFromApi() {
        currentSkip = 0;
        hasMore = true;
        apiPagingEnabled = true;
        callAPI(currentTopic, currentSkip, PAGE_SIZE);
    }

    private void loadFromCache() {
        String cachedJson = preferenceManager.getAnnouncementsJson(currentTopic);
        if (cachedJson == null || cachedJson.isEmpty()) {
            return;
        }

        try {
            AnnouncementResponse response = new Gson().fromJson(cachedJson, AnnouncementResponse.class);
            List<Announcement> list = response != null ? response.getData() : null;
            adapter.setData(list);
            int loadedCount = list != null ? list.size() : 0;
            currentSkip = loadedCount;
            hasMore = loadedCount == PAGE_SIZE;
        } catch (Exception e) {
            Log.e("AnnouncementList", "Error loading cached announcements", e);
        }
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
                                preferenceManager.saveAnnouncementsJson(topic, new Gson().toJson(response.body()));
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
