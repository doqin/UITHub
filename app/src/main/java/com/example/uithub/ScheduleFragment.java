package com.example.uithub;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.uithub.adapter.SchedulePagerAdapter;
import com.example.uithub.api.RetrofitClient;
import com.example.uithub.models.ScheduleItem;
import com.example.uithub.utils.JSONParser;
import com.example.uithub.utils.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ScheduleFragment extends Fragment {

    private SchedulePagerAdapter adapter;
    private Map<String, List<ScheduleItem>> map;
    private PreferenceManager preferenceManager;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private View progressBar;
    private MaterialButton btnReload;
    private TabLayoutMediator tabLayoutMediator;
    private Call<ResponseBody> scheduleCall;

    public ScheduleFragment() {
        super(R.layout.fragment_schedule);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferenceManager = new PreferenceManager(requireContext());

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        progressBar = view.findViewById(R.id.progressBar);
        btnReload = view.findViewById(R.id.btnReload);

        adapter = new SchedulePagerAdapter(this);
        viewPager.setAdapter(adapter);

        btnReload.setOnClickListener(v -> fetchSchedule(true));

        if (!renderCachedSchedule()) {
            fetchSchedule(false);
        }
    }

    private boolean renderCachedSchedule() {
        String cachedJson = preferenceManager.getScheduleJson();
        if (cachedJson == null || cachedJson.isEmpty()) {
            return false;
        }

        try {
            renderSchedule(cachedJson);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void fetchSchedule(boolean forceReload) {
        String token = preferenceManager.getToken();
        if (token == null) {
            Toast.makeText(getContext(), getString(R.string.please_login_first), Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("ScheduleFragment", "Token length: " + token.length() + ", Full token: " + token);
        android.util.Log.d("ScheduleFragment", "Authorization header: Bearer " + token);

        if (scheduleCall != null) {
            scheduleCall.cancel();
        }

        setLoading(true);
        scheduleCall = RetrofitClient.getApiService().getSchedule("Bearer " + token);
        scheduleCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        renderSchedule(json);
                        preferenceManager.saveScheduleJson(json);
                    } catch (Exception e) {
                        e.printStackTrace();
                        showToast(getString(R.string.unable_to_load_schedule));
                    }
                } else if (response.code() == 401) {
                    android.util.Log.e("ScheduleFragment", "401 Unauthorized - Full token: " + token);
                    android.util.Log.e("ScheduleFragment", "401 Unauthorized - Header sent: Bearer " + token);
                    if (getActivity() != null) {
                        preferenceManager.clear();
                        showToast(getString(R.string.please_login_again));
                    }
                } else {
                    showToast(getString(R.string.failed_to_load_schedule, response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                setLoading(false);

                if (call.isCanceled()) {
                    return;
                }

                if (!forceReload && renderCachedSchedule()) {
                    showToast(getString(R.string.showing_cached_schedule));
                    return;
                }

                showToast(getString(R.string.network_error_generic));
            }
        });
    }

    private void renderSchedule(String json) throws Exception {
        List<ScheduleItem> list = JSONParser.parseSchedule(json);
        map = JSONParser.groupByDay(list);

        if (!isAdded()) {
            return;
        }

        if (tabLayoutMediator != null) {
            tabLayoutMediator.detach();
        }

        adapter = new SchedulePagerAdapter(this);
        viewPager.setAdapter(adapter);
        adapter.setData(map);

        List<String> days = new ArrayList<>(map.keySet());
        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(days.get(position));
        });
        tabLayoutMediator.attach();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnReload.setEnabled(!loading);
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        if (scheduleCall != null) {
            scheduleCall.cancel();
        }
        if (tabLayoutMediator != null) {
            tabLayoutMediator.detach();
            tabLayoutMediator = null;
        }
        super.onDestroyView();
    }
}
