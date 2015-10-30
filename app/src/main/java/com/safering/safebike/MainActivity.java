package com.safering.safebike;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.safering.safebike.exercisereport.ExerciseReportFragment;
import com.safering.safebike.friend.FriendFragment;
import com.safering.safebike.setting.SettingFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String DEBUG_TAG = "safebike";

    private static final String TAG_MAIN = "main";
    private static final String TAG_NAVIGATION = "navigation";
    private static final String TAG_EXERCISEREPORT = "exercisereport";
    private static final String TAG_FRIEND = "friend";
    private static final String TAG_SETTING = "setting";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new MainFragment(), TAG_MAIN).commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment oldNaviTag = getSupportFragmentManager().findFragmentByTag(TAG_NAVIGATION);

        if (id == R.id.nav_safebike) {
            if (oldNaviTag != null) {
                getSupportFragmentManager().popBackStack(TAG_NAVIGATION, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new MainFragment(), TAG_MAIN).addToBackStack(null).commit();
            } else {
                emptyBackStack();
            }
        } else if (id == R.id.nav_exercise_report) {
            Fragment old = getSupportFragmentManager().findFragmentByTag(TAG_EXERCISEREPORT);

            if (old == null) {
                if (oldNaviTag != null) {
                    getSupportFragmentManager().popBackStack(TAG_NAVIGATION, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else {
                    emptyBackStack();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new ExerciseReportFragment(), TAG_EXERCISEREPORT).addToBackStack(null).commit();
            }
        } else if (id == R.id.nav_friend) {
            Fragment old = getSupportFragmentManager().findFragmentByTag(TAG_FRIEND);

            if (old == null) {
                if (oldNaviTag != null) {
                    getSupportFragmentManager().popBackStack(TAG_NAVIGATION, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else {
                    emptyBackStack();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new FriendFragment(), TAG_FRIEND).addToBackStack(null).commit();
            }
        } else if (id == R.id.nav_setting) {
            Fragment old = getSupportFragmentManager().findFragmentByTag(TAG_SETTING);

            if (old == null) {
                if (oldNaviTag != null) {
                    getSupportFragmentManager().popBackStack(TAG_NAVIGATION, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else {
                    emptyBackStack();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new SettingFragment(), TAG_SETTING).addToBackStack(null).commit();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void emptyBackStack() {
//        FragmentManager fm = getSupportFragmentManager();
//        fm.popBackStack (TAG_NAVIGATION, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
