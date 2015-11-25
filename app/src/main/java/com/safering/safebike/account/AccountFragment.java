package com.safering.safebike.account;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.login.LoginActivity;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.property.MyApplication;
import com.safering.safebike.property.PropertyManager;

import java.util.StringTokenizer;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {
    private static final String SERVICE_FINISH = "finish";
    private static final String SERVICE_RUNNING = "running";
    private static final int BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION = 3;
    TextView textHelp;
    TextView textLogout;

    ImageView imageProfileUser;
    TextView textProfileName;
    TextView textProfileJoin;
    TextView textProfileEmail;

    RelativeLayout userProfile;
    Button btnLogout;


    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        userProfile = (RelativeLayout)view.findViewById(R.id.layout_user_profile);
        textHelp = (TextView)view.findViewById(R.id.text_btn_help);
        textLogout = (TextView)view.findViewById(R.id.text_btn_logout);
        textProfileName = (TextView)view.findViewById(R.id.text_id_profile);
        textProfileJoin = (TextView)view.findViewById(R.id.text_join_profile);
        textProfileEmail = (TextView)view.findViewById(R.id.text_email_profile);
        imageProfileUser = (ImageView)view.findViewById(R.id.image_user_profile_account);
        Toast.makeText(getContext(),"AccountFragmentOncREATE",Toast.LENGTH_SHORT).show();
        setProfile();
        setFont();


        textLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PropertyManager.getInstance().setUserId("");
                PropertyManager.getInstance().setUserPassword("");
                PropertyManager.getInstance().setUserEmail("");
                PropertyManager.getInstance().setUserJoin("");
                PropertyManager.getInstance().setUserImagePath("");

                if (PropertyManager.getInstance().getServiceCondition().equals(SERVICE_RUNNING)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setIcon(android.R.drawable.ic_dialog_info);
                    builder.setTitle("내비게이션 안내종료");
                    builder.setMessage("현재 내비게이션 안내 중입니다. 정말로 종료하시겠습니까");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        /*
                         *  목적지 위도, 경도, searchoption 날리기
                         */
                            PropertyManager.getInstance().setServiceCondition(SERVICE_FINISH);
                            PropertyManager.getInstance().setDestinationLatitude(null);
                            PropertyManager.getInstance().setDestinationLongitude(null);
                            PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION);

                            Intent intent = new Intent(((MainActivity) getActivity()), LoginActivity.class);
                            startActivity(intent);
                            ((MainActivity) getActivity()).finish();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    builder.create().show();
                } else {
                    Intent intent = new Intent(((MainActivity) getActivity()), LoginActivity.class);
                    startActivity(intent);
                    ((MainActivity) getActivity()).finish();
                }
            }
        });

        userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(((MainActivity)getActivity()),ProfileActivity.class);
                startActivity(intent);
            }
        });

        textHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(((MainActivity) getActivity()), AccountHelpActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

    public void setFont(){
        textHelp.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS_M));
        textLogout.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS_M));
        textProfileName.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        textProfileJoin.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        textProfileEmail.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
    }


    public String getDateFormat(String date){
        Log.i("date",date);
        String resultDate = "";
        StringTokenizer tokenizer = new StringTokenizer(date,"-");
        resultDate += tokenizer.nextToken() + "년 ";
        resultDate += tokenizer.nextToken() + "월 ";
        resultDate += tokenizer.nextToken() + "일 가입";
        return resultDate;
    }

    public void setProfile(){
        textProfileName.setText(PropertyManager.getInstance().getUserId());
        textProfileEmail.setText(PropertyManager.getInstance().getUserEmail());
       //getDateFormat(PropertyManager.getInstance().getUserJoin());
       textProfileJoin.setText(getDateFormat((PropertyManager.getInstance().getUserJoin())));
        if(!PropertyManager.getInstance().getUserImagePath().equals("")){
            DisplayImageOptions options;
            options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisc(true)
                    .showImageOnLoading(R.mipmap.profile_img)
                    .showImageForEmptyUri(R.mipmap.profile_img)


                    .considerExifParams(true)
                    .displayer(new RoundedBitmapDisplayer(1000))
                    .build();

            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(MyApplication.getContext()));
            ImageLoader.getInstance().displayImage(PropertyManager.getInstance().getUserImagePath(),imageProfileUser, options);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(getContext(),"AccountFragmentonResume",Toast.LENGTH_SHORT).show();
        setProfile();
    }
}
