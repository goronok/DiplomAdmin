package com.example.goron.diplomadmin.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.goron.diplomadmin.Adapters.AdapterActivities;
import com.example.goron.diplomadmin.Interface.Service;
import com.example.goron.diplomadmin.Model.Schedule;
import com.example.goron.diplomadmin.R;
import com.example.goron.diplomadmin.Service.ServiceGenerator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;


public class ActivitiesFragment extends Fragment {
    RecyclerView recyclerView;
    AdapterActivities activitiesAdapter;
    private String name, password;

    private Call<List<Schedule>> callSchedule;

    public ActivitiesFragment() {
        // Required empty public constructor
    }


    public static ActivitiesFragment newInstance(String name, String password) {
        ActivitiesFragment fragment = new ActivitiesFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("password", password);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString("name");
            password = getArguments().getString("password");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activities, container, false);
        recyclerView = view.findViewById(R.id.recyclerActivities);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
        getAllActivities();
        return view;
    }


    // Получить все активности
    private void getAllActivities() {

        callSchedule = getService().getSchedule();
        callSchedule.enqueue(new Callback<List<Schedule>>() {
            @Override
            public void onResponse(Call<List<Schedule>> call, retrofit2.Response<List<Schedule>> response) {
                if (response.isSuccessful()) {
                    if(response.code() == 200) {
                        activitiesAdapter = new AdapterActivities(response.body(), getActivity(), name, password);
                        recyclerView.setAdapter(activitiesAdapter);
                    }
                } else {
                    Toast.makeText(getActivity(), "error response, no access to resource?", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Schedule>> callActivities, Throwable t) {
                Toast.makeText(getActivity(), t.toString(), Toast.LENGTH_LONG).show();
            }
        });

    }


    // Получить сервис для работы с сервером
    private Service getService(){
        return ServiceGenerator.createService(Service.class, name, password);
    }
}
