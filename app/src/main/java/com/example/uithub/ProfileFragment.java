package com.example.uithub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uithub.utils.PreferenceManager;

public class ProfileFragment extends Fragment {

    private PreferenceManager preferenceManager;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferenceManager = new PreferenceManager(requireContext());

        // Set student info
        TextView tvStudentName = view.findViewById(R.id.tvStudentName);
        TextView tvStudentId = view.findViewById(R.id.tvStudentId);

        // TODO: Load real student info from API
        String mssv = preferenceManager.getMssv();
        if (mssv != null && !mssv.isEmpty()) {
            tvStudentId.setText("MSSV: " + mssv);
        }

        // Menu click handlers
        view.findViewById(R.id.btnProfileInfo).setOnClickListener(v ->
                Toast.makeText(getContext(), "Thông tin sinh viên", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btnAccount).setOnClickListener(v ->
                Toast.makeText(getContext(), "Tài khoản", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btnSettings).setOnClickListener(v ->
                Toast.makeText(getContext(), "Cài đặt", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            preferenceManager.clear();
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
        });
    }
}