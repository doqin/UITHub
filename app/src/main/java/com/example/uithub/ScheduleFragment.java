package com.example.uithub;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.uithub.adapter.SchedulePagerAdapter;
import com.example.uithub.models.ScheduleItem;
import com.example.uithub.utils.JSONParser;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ScheduleFragment extends Fragment {

    private SchedulePagerAdapter adapter;
    private Map<String, List<ScheduleItem>> map;

    public ScheduleFragment() {
        super(R.layout.fragment_schedule);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        adapter = new SchedulePagerAdapter(this);
        viewPager.setAdapter(adapter);

        loadData(tabLayout, viewPager);
    }

    private void loadData(TabLayout tabLayout, ViewPager2 viewPager) {
        try {
//            thay TEST_JSON bang response cua API
            String json = "{\n" +
                    "    \"success\": true,\n" +
                    "    \"count\": 7,\n" +
                    "    \"data\": [\n" +
                    "        {\n" +
                    "            \"day\": \"Thứ 2\",\n" +
                    "            \"period\": \"Tiết 1-3\",\n" +
                    "            \"time\": \"07:30-09:45\",\n" +
                    "            \"start_time\": \"07:30\",\n" +
                    "            \"end_time\": \"09:45\",\n" +
                    "            \"start_date\": \"26/01/26\",\n" +
                    "            \"end_date\": \"30/05/26\",\n" +
                    "            \"code\": \"SE101.Q21\",\n" +
                    "            \"name\": \"Phương pháp mô hình hóa - VN\",\n" +
                    "            \"room\": \"P. C302\",\n" +
                    "            \"teacher\": \"80056 - Nguyễn Công Hoan\",\n" +
                    "            \"date\": \"26/01/26 -> 30/05/26\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"day\": \"Thứ 4\",\n" +
                    "            \"period\": \"Tiết 1-3\",\n" +
                    "            \"time\": \"07:30-09:45\",\n" +
                    "            \"start_time\": \"07:30\",\n" +
                    "            \"end_time\": \"09:45\",\n" +
                    "            \"start_date\": \"26/01/26\",\n" +
                    "            \"end_date\": \"25/04/26\",\n" +
                    "            \"code\": \"SS004.Q25\",\n" +
                    "            \"name\": \"Kỹ năng nghề nghiệp - VN\",\n" +
                    "            \"room\": \"P. B3.14\",\n" +
                    "            \"teacher\": \"80209 - Lê Thanh Trọng\",\n" +
                    "            \"date\": \"26/01/26 -> 25/04/26\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"day\": \"Thứ 5\",\n" +
                    "            \"period\": \"Tiết 1-3\",\n" +
                    "            \"time\": \"07:30-09:45\",\n" +
                    "            \"start_time\": \"07:30\",\n" +
                    "            \"end_time\": \"09:45\",\n" +
                    "            \"start_date\": \"26/01/26\",\n" +
                    "            \"end_date\": \"30/05/26\",\n" +
                    "            \"code\": \"SE104.Q26\",\n" +
                    "            \"name\": \"Nhập môn Công nghệ phần mềm - VN\",\n" +
                    "            \"room\": \"P. C202\",\n" +
                    "            \"teacher\": \"80198 - Huỳnh Ngọc Tín\",\n" +
                    "            \"date\": \"26/01/26 -> 30/05/26\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"day\": \"Thứ 2\",\n" +
                    "            \"period\": \"Tiết 4-5\",\n" +
                    "            \"time\": \"10:00-11:30\",\n" +
                    "            \"start_time\": \"10:00\",\n" +
                    "            \"end_time\": \"11:30\",\n" +
                    "            \"start_date\": \"26/01/26\",\n" +
                    "            \"end_date\": \"30/05/26\",\n" +
                    "            \"code\": \"SS003.Q22\",\n" +
                    "            \"name\": \"Tư tưởng Hồ Chí Minh - VN\",\n" +
                    "            \"room\": \"P. B1.14\",\n" +
                    "            \"teacher\": \"10072 - Phạm Thị Thu Hương\",\n" +
                    "            \"date\": \"26/01/26 -> 30/05/26\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"day\": \"Thứ 4\",\n" +
                    "            \"period\": \"Tiết 4-5\",\n" +
                    "            \"time\": \"10:00-11:30\",\n" +
                    "            \"start_time\": \"10:00\",\n" +
                    "            \"end_time\": \"11:30\",\n" +
                    "            \"start_date\": \"26/01/26\",\n" +
                    "            \"end_date\": \"30/05/26\",\n" +
                    "            \"code\": \"SS008.Q22\",\n" +
                    "            \"name\": \"Kinh tế chính trị Mác – Lênin - VN\",\n" +
                    "            \"room\": \"P. B4.14\",\n" +
                    "            \"teacher\": \"10774 - Hà Thị Việt Thúy\",\n" +
                    "            \"date\": \"26/01/26 -> 30/05/26\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"day\": \"Thứ 3\",\n" +
                    "            \"period\": \"Tiết 6-9\",\n" +
                    "            \"time\": \"13:00-16:15\",\n" +
                    "            \"start_time\": \"13:00\",\n" +
                    "            \"end_time\": \"16:15\",\n" +
                    "            \"start_date\": \"26/01/26\",\n" +
                    "            \"end_date\": \"06/06/26\",\n" +
                    "            \"code\": \"SE114.Q21\",\n" +
                    "            \"name\": \"Nhập môn ứng dụng di động - VN\",\n" +
                    "            \"room\": \"P. C302  - Cách 2 tuần\",\n" +
                    "            \"teacher\": \"80320 - Nguyễn Tấn Toàn\",\n" +
                    "            \"date\": \"26/01/26 -> 06/06/26\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"day\": \"Thứ 4\",\n" +
                    "            \"period\": \"Tiết 6-7\",\n" +
                    "            \"time\": \"13:00-14:30\",\n" +
                    "            \"start_time\": \"13:00\",\n" +
                    "            \"end_time\": \"14:30\",\n" +
                    "            \"start_date\": \"26/01/26\",\n" +
                    "            \"end_date\": \"30/05/26\",\n" +
                    "            \"code\": \"SS009.Q25\",\n" +
                    "            \"name\": \"Chủ nghĩa xã hội khoa học - VN\",\n" +
                    "            \"room\": \"P. B4.14\",\n" +
                    "            \"teacher\": \"10917 - Nguyễn Thị Bích Cần\",\n" +
                    "            \"date\": \"26/01/26 -> 30/05/26\"\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}";

            List<ScheduleItem> list = JSONParser.parseSchedule(json);

            map = JSONParser.groupByDay(list);

            adapter.setData(map);

            List<String> days = new ArrayList<>(map.keySet());

            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                tab.setText(days.get(position));
            }).attach();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}