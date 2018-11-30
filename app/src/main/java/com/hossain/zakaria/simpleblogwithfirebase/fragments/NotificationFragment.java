package com.hossain.zakaria.simpleblogwithfirebase.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hossain.zakaria.simpleblogwithfirebase.R;

public class NotificationFragment extends Fragment {

    // Required empty public constructor
    public NotificationFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }
}
