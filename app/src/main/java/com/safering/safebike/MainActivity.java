package com.safering.safebike;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
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
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.safering.safebike.account.AccountFragment;
import com.safering.safebike.exercisereport.ExerciseReportFragment;
import com.safering.safebike.friend.FriendFragment;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.navigation.NavigationFragment;
import com.safering.safebike.property.MyApplication;
import com.safering.safebike.property.PropertyManager;
import com.safering.safebike.service.RouteService;
import com.safering.safebike.setting.CustomTypefaceSpan;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String DEBUG_TAG = "safebike";

    public static final String TAG_MAIN = "main";
    private static final String TAG_NAVIGATION = "navigation";
    private static final String TAG_EXERCISEREPORT = "exercisereport";
    private static final String TAG_FRIEND = "friend";
    public static final String TAG_SETTING = "setting";

    private static final String SERVICE_FINISH = "finish";
    private static final String SERVICE_RUNNING = "running";

    public static final int MESSAGE_BACK_KEY = 1;
    public static final int TIME_BACK_TIMEOUT = 2000;
    private boolean isBackPressed = false;

    private static final String KEY_POP_NAVIGATION_FRAGMENT = "popNavigation";
    private static final String VALUE_POP_NAVIGATION_FRAGMENT = "popNavigation";
    private static final String KEY_REPLACE_MAIN_FRAGMENT = "replaceMainFragment";
    private static final String VALUE_REPLACE_MAIN_FRAGMENT = "replaceMainFragment";
    private static final int BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION = 3;

    public static String FABFINDROUTE_ONOFF_FLAG = "off";
    private static String ON = "on";
    private static String OFF = "off";

    TextView textSafeBikeMainTitle, textMainTitle;
    View naviHeaderView;
    ImageView imageAccountSetting;
    ImageView imageUserProfile;
    TextView textUserId;
    TextView textUserEmail;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        toggle.setDrawerIndicatorEnabled(false);
        toggle.setHomeAsUpIndicator(R.drawable.pannel);

        drawer.setDrawerListener(toggle);

        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
                setProfile();
            }
        });

        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        NavigationView nav = (NavigationView)findViewById(R.id.nav_view);

        Menu m = nav.getMenu();
        for (int i=0;i<m.size();i++) {
            MenuItem mi = m.getItem(i);

            //for aapplying a font to subMenu ....
        /*    SubMenu subMenu = mi.getSubMenu();
            if (subMenu!=null && subMenu.size() >0 ) {
                for (int j=0; j <subMenu.size();j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }
*/
            //the method we have create in activity
            applyFontToMenuItem(mi);
        }


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new MainFragment(), TAG_MAIN).commit();
        }

        textSafeBikeMainTitle = (TextView) findViewById(R.id.text_safebike_main_title);
        textMainTitle = (TextView) findViewById(R.id.text_main_title);

        naviHeaderView = LayoutInflater.from(MainActivity.this).inflate(R.layout.nav_header_main, nav);
        imageAccountSetting = (ImageView)naviHeaderView.findViewById(R.id.btn_account_setting);

        imageAccountSetting = (ImageView)naviHeaderView.findViewById(R.id.btn_account_setting);
        imageUserProfile = (ImageView)naviHeaderView.findViewById(R.id.image_join_user);
        textUserId = (TextView)naviHeaderView.findViewById(R.id.text_user_imform_id);
        textUserEmail = (TextView)naviHeaderView.findViewById(R.id.text_user_imform_email);

        textSafeBikeMainTitle.setVisibility(View.VISIBLE);
        textMainTitle.setVisibility(View.GONE);

        imageAccountSetting.setOnClickListener(new View.OnClickListener() {
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
        
        setProfile();
        setFont();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        Log.d("safebike", "MainActivity.onNewIntent : " + PropertyManager.getInstance().getServiceCondition());

        if (intent != null) {
            String popMsg = intent.getStringExtra(KEY_POP_NAVIGATION_FRAGMENT);
            String replaceMsg = intent.getStringExtra(KEY_REPLACE_MAIN_FRAGMENT);

//            Log.d("safebike", "MainActivity.onNewIntent.replaceMsg : " + replaceMsg);
            if (popMsg != null && popMsg.equals(VALUE_POP_NAVIGATION_FRAGMENT)) {
                Fragment old = getSupportFragmentManager().findFragmentByTag(TAG_NAVIGATION);

                if (old != null) {
//                    Log.d("safebike", "MainActivity.onNewIntent.popBackStack");
                    getSupportFragmentManager().popBackStack();
                }

                if (replaceMsg != null && replaceMsg.equals(VALUE_REPLACE_MAIN_FRAGMENT)) {
//                    Log.d("safebike", "MainActivity.onNewIntent.Replace.MainFragment");
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, new MainFragment(), TAG_MAIN).commit();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Log.d("safebike",  "MainActivity.onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.d("safebike", "MainActivity.onResume");

        textSafeBikeMainTitle.setVisibility(View.VISIBLE);
        textMainTitle.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PropertyManager.getInstance().setServiceCondition(SERVICE_FINISH);
        PropertyManager.getInstance().setDestinationLatitude(null);
        PropertyManager.getInstance().setDestinationLongitude(null);
        PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION);
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

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(),"NotoSansKR-Regular.otf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("" , font), 0 , mNewTitle.length(),  Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    @Override
    public void onBackPressed() {
//        Toast.makeText(this, "MainActivity.onBackPressed", Toast.LENGTH_SHORT).show();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (PropertyManager.getInstance().getServiceCondition().equals(SERVICE_FINISH) ) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0 && FABFINDROUTE_ONOFF_FLAG.equals(OFF)) {
                getSupportFragmentManager().popBackStack();

//                Log.d("safebike", "MainActivity.onFabFindRouteOnOffFlag : " + FABFINDROUTE_ONOFF_FLAG);
            } else if (getSupportFragmentManager().getBackStackEntryCount() > 0 && FABFINDROUTE_ONOFF_FLAG.equals(ON)) {
                FABFINDROUTE_ONOFF_FLAG = OFF;

                NavigationFragment old = (NavigationFragment) getSupportFragmentManager().findFragmentByTag(TAG_NAVIGATION);

                if (old != null) {
                    old.setBtnFindRouteChange();
                }
            } else {
                if (isBackPressed) {
                    mHandler.removeMessages(MESSAGE_BACK_KEY);
                    super.onBackPressed();
                } else {
                    isBackPressed = true;
                    Toast.makeText(this, "뒤로 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                    mHandler.sendEmptyMessageDelayed(MESSAGE_BACK_KEY, TIME_BACK_TIMEOUT);
                }
            }
        } else if (PropertyManager.getInstance().getServiceCondition().equals(SERVICE_RUNNING)) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                onMainFinishNavigationDialog();
            }
        }
    }

    public void onFabFindRouteOnOffFlag(String flag) {
//        Log.d("safebike", "MainActivity.onFabFindRouteOnOffFlag : " + flag);
        FABFINDROUTE_ONOFF_FLAG = flag;
    }

    public void onMainFinishNavigationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setIcon(null);
        builder.setTitle("내비게이션 안내종료");
        builder.setMessage("현재 내비게이션 안내 중입니다." + "\n" + "정말로 종료하시겠습니까");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PropertyManager.getInstance().setServiceCondition(SERVICE_FINISH);
                PropertyManager.getInstance().setDestinationLatitude(null);
                PropertyManager.getInstance().setDestinationLongitude(null);
                PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION);

                Intent serviceIntent = new Intent(MainActivity.this, RouteService.class);
                stopService(serviceIntent);

//                Log.d("safebike", "Replace.MainFragment");

                getSupportFragmentManager().beginTransaction().replace(R.id.container, new MainFragment(), TAG_MAIN).commit();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

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
        }/* else if (id == R.id.nav_setting) {
            Fragment old = getSupportFragmentManager().findFragmentByTag(TAG_SETTING);

            if (old == null) {
                emptyBackStack();
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new SettingFragment(), TAG_SETTING).addToBackStack(null).commit();
            }
        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void emptyBackStack() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public void setFont(){
        textSafeBikeMainTitle.setText("Safe Riding");
        textSafeBikeMainTitle.setTypeface(FontManager.getInstance().getTypeface(MainActivity.this, FontManager.BMJUA));
        textUserEmail.setTypeface(FontManager.getInstance().getTypeface(MainActivity.this, FontManager.NOTOSANS));
        textUserId.setTypeface(FontManager.getInstance().getTypeface(MainActivity.this,FontManager.NOTOSANS));
    }

    public void setProfile(){
        textUserId.setText(PropertyManager.getInstance().getUserId());
        textUserEmail.setText(PropertyManager.getInstance().getUserEmail());
        if(!PropertyManager.getInstance().getUserImagePath().equals("")){
            DisplayImageOptions options;
            options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisc(true)
                    .showImageOnLoading(R.drawable.profile_img)
                    .showImageForEmptyUri(R.drawable.profile_img)
                    .considerExifParams(true)
                    .displayer(new RoundedBitmapDisplayer(1000))
                    .build();

            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(MyApplication.getContext()));
            ImageLoader.getInstance().displayImage(PropertyManager.getInstance().getUserImagePath(), imageUserProfile, options);
        }
    }
}
