package com.safering.safebike;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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

    private static final String SERVICE_FINISH = "finish";
    private static final String SERVICE_RUNNING = "running";

    public static final int MESSAGE_BACK_KEY = 1;
    public static final int TIME_BACK_TIMEOUT = 2000;
    private boolean isBackPressed = false;
//    String serviceCondition = "";

    Fragment mainFragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(MainActivity.this, "MainActivity.onCreate", Toast.LENGTH_SHORT).show();

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

//        serviceCondition = PropertyManager.getInstance().getServiceCondition();

//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction().add(R.id.container, MainFragment.newInstance(serviceCondition), TAG_MAIN).commit();
//        }


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new MainFragment(), TAG_MAIN).commit();
        }

//        if (savedInstanceState == null) {
//            mainFragment = new MainFragment();
//            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//            ft.add(R.id.container, mainFragment, TAG_MAIN);
//            ft.commit();
//        }

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
//        emptyBackStack();
//        Fragment old = getSupportFragmentManager().findFragmentByTag(TAG_MAIN);
//        getSupportFragmentManager().popBackStack(TAG_MAIN, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Toast.makeText(MainActivity.this, "MainActivity.onNewIntent : " + PropertyManager.getInstance().getServiceCondition(), Toast.LENGTH_SHORT).show();

        Fragment old = getSupportFragmentManager().findFragmentByTag(TAG_NAVIGATION);

        if (old != null) {
            getSupportFragmentManager().popBackStack();
        }

//        if (intent != null) {
//            if (intent.getStringExtra("d"));
//            String serviceCondition = PropertyManager.getInstance().getServiceCondition();
//
//            Toast.makeText(MainActivity.this, serviceCondition, Toast.LENGTH_SHORT).show();
//        }

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

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_BACK_KEY :
                    isBackPressed = false;

                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (PropertyManager.getInstance().getServiceCondition().equals(SERVICE_FINISH)) {
            if (isBackPressed) {
                mHandler.removeMessages(MESSAGE_BACK_KEY);
                super.onBackPressed();
            } else {
                isBackPressed = true;
                Toast.makeText(this, "뒤로 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                mHandler.sendEmptyMessageDelayed(MESSAGE_BACK_KEY, TIME_BACK_TIMEOUT);
            }
        } else if (PropertyManager.getInstance().getServiceCondition().equals(SERVICE_RUNNING)) {
            onMainFinishNavigationDialog();


//            getSupportFragmentManager().beginTransaction().add(R.id.container, new MainFragment(), TAG_MAIN).commit();
//            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//            ft.detach(mainFragment);
//            ft.attach(mainFragment);
//            ft.commit();

        }

        /*
         *  SharedPreferences Service Condition 값이 Running 이면 네비게이션 안내를 종료하겠습니까? 다이얼로그 나오고 예 누르면 Service Condition -> finish ,
         *  SharedPreferences 값 다 날리기
         *
         *  SharedPreferences Service Condition 값이 finish 이면 두번 눌렀을 때 종료
         */
    }

    public void onMainFinishNavigationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("내비게이션 안내종료");
        builder.setMessage("현재 내비게이션 안내 중입니다. 정말로 종료하시겠습니까");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PropertyManager.getInstance().setServiceCondition(SERVICE_FINISH);

                /*
                 *   오늘 아침에 처리할 부분(UI 변경 위해 replace 맞는지 여쭤보기
                 */
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new MainFragment(), TAG_MAIN).commit();

//                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//                ft.detach(mainFragment);
//                ft.attach(mainFragment);
//                ft.commit();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
//        builder.setCancelable(false);

        builder.create().show();
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
