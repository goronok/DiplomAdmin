package com.example.goron.diplomadmin;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.KeyEventDispatcher;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.goron.diplomadmin.Fragments.ActivityFragment;
import com.example.goron.diplomadmin.Fragments.ScheduleFragment;
import com.example.goron.diplomadmin.Fragments.SettingFragment;
import com.example.goron.diplomadmin.Manager.SerializableManager;
import com.example.goron.diplomadmin.Model.Setting;

public class StartActivity extends AppCompatActivity {

    FrameLayout frameLayout;
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    ViewPager viewpager;


    String name, password;
    Setting setting;



    FragmentTransaction fragmentTransaction;

    // Фрагмент с активностями
    ActivityFragment activityfragment;

    // Фрагмент с расписанием
    ScheduleFragment scheduleFragment;

    // Фрагмент с настройками
    SettingFragment settingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);



        Bundle arguments = getIntent().getExtras();
        name = arguments.get("name").toString();
        password = arguments.get("password").toString();

        Toast.makeText(getApplicationContext(), "Добро пожаловать", Toast.LENGTH_LONG).show();



        frameLayout = findViewById(R.id.content_frame);
        drawerLayout = findViewById(R.id.drawer);
        toolbar = findViewById(R.id.toolBar);
        navigationView = findViewById(R.id.navView);
        viewpager = findViewById(R.id.viewpager);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(null);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);


        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        activityfragment= ActivityFragment.newInstance(name, password);
        showFragment(activityfragment);


        // Заполняем объект с настройками из файла
        setting = SerializableManager.readSerializableObject(getApplicationContext(), "Setting.ser");



        if (setting != null){
            toolbar.setBackgroundResource(setting.getColorId());
        }

        navigationMenu();


    }


    private void showFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack(null).commit();

    }//showFragment





    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();


        // Если открыто боковое меню - закрываем его, Если в стеке последний фрагмент закрываем активность иначе возвращаемся к предыдущему фрагменту
        if (drawerLayout.isDrawerOpen(GravityCompat.START))  drawerLayout.closeDrawer(GravityCompat.START);
        else if (count == 1) {
          super.onBackPressed();
          if(activityfragment.callDate != null){
              activityfragment.callDate.cancel();
          }

            moveTaskToBack(true);
            finish();
        }else {
            getSupportFragmentManager().popBackStack();
        }

    }


    // Переход по NavigationView
    private void navigationMenu() {

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                int id = menuItem.getItemId();
                Intent intent;
                switch (id) {

                    case R.id.settings:
                        settingFragment = new SettingFragment();
                        showFragment(settingFragment);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.schedule:
                          scheduleFragment = ScheduleFragment.newInstance(name,password);
                          showFragment(scheduleFragment);
                          drawerLayout.closeDrawer(GravityCompat.START);
                          break;

                    case R.id.exit:

                        intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("CloseApp", true);
                        startActivity(intent);

                        break;
                }
                return false;
            }
        });
    }
}
