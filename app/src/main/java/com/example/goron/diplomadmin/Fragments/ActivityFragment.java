package com.example.goron.diplomadmin.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;


import com.example.goron.diplomadmin.Adapters.AdapterActivity;
import com.example.goron.diplomadmin.Interface.Service;
import com.example.goron.diplomadmin.Manager.SerializableManager;
import com.example.goron.diplomadmin.Model.AccountInformation;
import com.example.goron.diplomadmin.Model.Activities;
import com.example.goron.diplomadmin.Model.DatesFestival;
import com.example.goron.diplomadmin.Model.Setting;
import com.example.goron.diplomadmin.Model.Users;
import com.example.goron.diplomadmin.R;
import com.example.goron.diplomadmin.Service.ServiceGenerator;
import com.example.goron.diplomadmin.StartActivity;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;


public class ActivityFragment extends Fragment {

    // Объект с настройками приложения
    Setting setting;
    // Объект хранящий информацию об акаунте
    AccountInformation accountInformation;

    // Адаптер для активности
    AdapterActivity adapterActivity;

    // Элементы интерфейса
    RecyclerView recyclerView;
    Spinner spinner;
    LinearLayout linearLayoutTop;
    RelativeLayout relativeBottom;
    FloatingActionButton addFlBut;

    // статический Объект хранящий дату фестивалей
    DatesFestival datesFestival;


    private Call<List<Activities>> callActivities;
    public  Call<DatesFestival> callDate;

    private ShimmerFrameLayout mShimmerViewContainer;


    private String name, password;

    // Путь к файлу где хранится информация по акаунту
    private final  String FileNameAccountInformation = "AccountInformation.ser";



    public ActivityFragment() {
        // Required empty public constructor
    }




    public static ActivityFragment newInstance(String name, String password) {
        ActivityFragment fragment = new ActivityFragment();
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
        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        //Инициализируем элементы:
        linearLayoutTop = view.findViewById(R.id.linearLayoutTop);
        relativeBottom = view.findViewById(R.id.relativeBottom);
        recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //layout_stub = view.findViewById(R.id.layout_stub);
        mShimmerViewContainer = view.findViewById(R.id.shimmer_view_container);
        spinner = view.findViewById(R.id.spinner);
        addFlBut = view.findViewById(R.id.addFlBut);

        // Устанавливаем настройки
        setSetting();
        // Если информации об аккаунте нет заполняем ее из файла
        getAccountInformation();
        // Вернуть все даты фестиваля (Каждый раз делать не разумно наверное стоит сохранять в файл)
        getDateToShedule();
        // Добавляем слушателя при выборе в спинере даты
        spinnerSetListener();


        //
        addFlBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showRegistrationDialog();
            }
        });



        // Возвращаем представление
        return view;
    }//onCreateView


    // Добавляем слушателя при выборе в спинере даты
    private void spinnerSetListener() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String date = (String) parent.getItemAtPosition(position);

                // Получить активности
                getActivity(date);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }


    // Установка настроек приложения
    private void setSetting() {
        // Заполняем объект с настройками из файла
        setting = SerializableManager.readSerializableObject(getContext(), "Setting.ser");

        if(setting == null){
            setting = new Setting(0);
        }

            linearLayoutTop.setBackgroundResource(setting.getColorId());
            relativeBottom.setBackgroundResource(setting.getColorId());


            getActivity().findViewById(R.id.toolBar).setBackgroundResource(setting.getColorId());





    }


    // Получить активности
    private void getActivity(String date){

       callActivities = getService().getActivities(date);

        callActivities.enqueue(new Callback<List<Activities>>() {
                @Override
                public void onResponse(Call<List<Activities>> call, retrofit2.Response<List<Activities>> response) {
                    if (response.isSuccessful()) {

                        if(response.code() == 200) {

                            adapterActivity = new AdapterActivity(response.body(), getActivity(), accountInformation, setting, name, password);

                            recyclerView.setAdapter(adapterActivity);
                        }

                        // Stopping Shimmer Effect's animation after data is loaded to ListView
                        mShimmerViewContainer.stopShimmerAnimation();
                        mShimmerViewContainer.setVisibility(View.GONE);

                    } else {
                        Toast.makeText(getActivity(), "error response, no access to resource?", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Activities>> callActivities, Throwable t) {
                    Toast.makeText(getActivity(), t.toString(), Toast.LENGTH_LONG).show();
                }
            });

    }


    // Получить сервис для работы с сервером
    private Service getService(){
        return ServiceGenerator.createService(Service.class, name, password);
    }


    // Если информации об аккаунте нет заполняем ее из файла
    private void getAccountInformation(){

        if(accountInformation == null){

            try{
                accountInformation =  SerializableManager.readSerializableObject(getActivity(), FileNameAccountInformation);
            }catch (Exception ex){
                Toast.makeText(getActivity(), ex.getMessage().toString(), Toast.LENGTH_LONG).show();
            }
        }//if
    }// getAccountInformation


    // Вернуть все даты фестиваля (Каждый раз делать не разумно наверное стоит сохранять в файл)
    private void getDateToShedule(){

        if(datesFestival == null){
            callDate = getService().getDatesFestival();


            callDate.enqueue(new Callback<DatesFestival>() {
                @Override
                public void onResponse(Call<DatesFestival> call, retrofit2.Response<DatesFestival> response) {
                    if (response.isSuccessful()) {


                        // Получить даты фестиваля
                        datesFestival = response.body();

                        // адаптер для спинера
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, datesFestival.getStringDate());
                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                        spinner.setAdapter(adapter);

                    } else {
                        Toast.makeText(getContext(), "error response, no access to resource?", Toast.LENGTH_LONG).show();

                    }
                }

                @Override
                public void onFailure(Call<DatesFestival> call, Throwable t) {
                    //Toast.makeText(getContext(), t.toString(), Toast.LENGTH_LONG).show();
                }
            });
        }else{
            // адаптер для спинера


            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, datesFestival.getStringDate());
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }

    }



    private void showRegistrationDialog(){

        //Получаем вид с файла dialog_registration_user.xml, который применим для диалогового окна:
        LayoutInflater li = LayoutInflater.from(getContext());
        View promptsView = li.inflate(R.layout.dialog_registration_user, null);
        //Создаем AlertDialog
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(getContext());



        //Настраиваем prompt.xml для нашего AlertDialog:
        mDialogBuilder.setView(promptsView);

        //Настраиваем отображение поля для ввода текста в открытом диалоге:
        final TextInputEditText textInputName = promptsView.findViewById(R.id.textInputName);
        final TextInputEditText textInputSurName = promptsView.findViewById(R.id.textInputSurName);
        final TextInputEditText textInputNumber = promptsView.findViewById(R.id.textInputNumber);
        final TextInputEditText textInputPassport = promptsView.findViewById(R.id.textInputPassport);


            //Настраиваем сообщение в диалоговом окне:
            mDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    Users users = new Users(textInputName.getText().toString(),textInputSurName.getText().toString(), textInputNumber.getText().toString(), textInputPassport.getText().toString() );
                                    registration(users);
                                }
                            })
                    .setNegativeButton("Отмена",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    dialog.cancel();
                                }
                            });

            //Создаем AlertDialog:
            AlertDialog alertDialog = mDialogBuilder.create();

             alertDialog.getWindow().setGravity(Gravity.TOP);
            //и отображаем его:
            alertDialog.show();

    }//showRegistrationDialog


    // Метод для регистрации пользователя
    private void registration(Users users){

        Call<Void> call = getService().registrationUser(users);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {

                    if(response.code() == 200) {
                        Toast.makeText(getActivity(), "Регистрация прошла успешно", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(getActivity(), "error response, no access to resource?", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getActivity(), t.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

}
