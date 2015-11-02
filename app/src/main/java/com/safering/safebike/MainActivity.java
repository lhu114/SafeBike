package com.safering.safebike;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.safering.safebike.account.AccountFragment;
import com.safering.safebike.exercisereport.ExerciseReportFragment;
import com.safering.safebike.friend.FriendFragment;
import com.safering.safebike.property.PropertyManager;
import com.safering.safebike.setting.SettingFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String DEBUG_TAG = "safebike";

    private static final String TAG_MAIN = "main";
    private static final String TAG_NAVIGATION = "navigation";
    private static final String TAG_EXERCISEREPORT = "exercisereport";
    private static final String TAG_FRIEND = "friend";
    private static final String TAG_SETTING = "setting";

    String serviceCondition = "";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Toast.makeText(MainActivity.this, "MainActivity.onCreate", Toast.LENGTH_SHORT).show();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        serviceCondition = PropertyManager.getInstance().getServiceCondition();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, MainFragment.newInstance(serviceCondition), TAG_MAIN).commit();
        }

        NavigationView nav = (NavigationView)findViewById(R.id.nav_view);
        View header = LayoutInflater.from(MainActivity.this).inflate(R.layout.nav_header_main, nav);
        Button btn = (Button)header.findViewById(R.id.btn_account_setting);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new AccountFragment(), "ACCOUNT").addToBackStack(null).commit();

            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Toast.makeText(MainActivity.this, "MainActivity.onNewIntent", Toast.LENGTH_SHORT).show();

        if (intent != null) {
            String serviceCondition = intent.getStringExtra("change_button");

            Toast.makeText(MainActivity.this, serviceCondition, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Toast.makeText(MainActivity.this, "MainActivity.onPause", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(MainActivity.this, "MainActivity.onResume", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

        /*
         *  SharedPreferences Service Condition 값이 Running 이면 네비게이션 안내를 종료하겠습니까? 다이얼로그 나오고 예 누르면 Service Condition -> finish ,
         *  SharedPreferences 값 다 날리기
         *
         *  SharedPreferences Service Condition 값이 finish 이면 두번 눌렀을 때 종료
         */
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_safebike) {
            emptyBackStack();

        } else if (id == R.id.nav_exercise_report) {
            Fragment old = getSupportFragmentManager().findFragmentByTag(TAG_EXERCISEREPORT);

            if (old == null) {
                emptyBackStack();
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new ExerciseReportFragment(), TAG_EXERCISEREPORT).addToBackStack(null).commit();
            }
        } else if (id == R.id.nav_friend) {
            Fragment old = getSupportFragmentManager().findFragmentByTag(TAG_FRIEND);

            if (old == null) {
                emptyBackStack();
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new FriendFragment(), TAG_FRIEND).addToBackStack(null).commit();
            }
        } else if (id == R.id.nav_setting) {
            Fragment old = getSupportFragmentManager().findFragmentByTag(TAG_SETTING);

            if (old == null) {
                emptyBackStack();
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new SettingFragment(), TAG_SETTING).addToBackStack(null).commit();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void emptyBackStack() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
