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

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.login.LoginActivity;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.property.MyApplication;
import com.safering.safebike.property.PropertyManager;
import com.safering.safebike.service.RouteService;

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
    TextView textProfileName;
    TextView textProfileJoin;
    TextView textProfileEmail;
    TextView textSafeBikeMainTitle, textMainTitle;
    ImageView imageProfileUser;
    RelativeLayout userProfile;


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
        textSafeBikeMainTitle = (TextView) ((MainActivity) getActivity()).findViewById(R.id.text_safebike_main_title);
        textMainTitle = (TextView) ((MainActivity) getActivity()).findViewById(R.id.text_main_title);
        textSafeBikeMainTitle.setVisibility(View.GONE);
        textMainTitle.setVisibility(View.VISIBLE);

        textLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (PropertyManager.getInstance().getServiceCondition().equals(SERVICE_RUNNING)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

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

                            Intent serviceIntent = new Intent(getContext(), RouteService.class);
                            getActivity().stopService(serviceIntent);

                            PropertyManager.getInstance().setUserId("");
                            PropertyManager.getInstance().setUserPassword("");
                            PropertyManager.getInstance().setUserEmail("");
                            PropertyManager.getInstance().setUserJoin("");
                            PropertyManager.getInstance().setUserImagePath("");
                            PropertyManager.getInstance().setFacebookUser(0);

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
                    PropertyManager.getInstance().setUserId("");
                    PropertyManager.getInstance().setUserPassword("");
                    PropertyManager.getInstance().setUserEmail("");
                    PropertyManager.getInstance().setUserJoin("");
                    PropertyManager.getInstance().setUserImagePath("");
                    PropertyManager.getInstance().setFacebookUser(0);
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

        setProfile();
        setFont();

        return view;
    }

    public void setFont(){
        textMainTitle.setText(R.string.text_account);
        textMainTitle.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        textHelp.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS_M));
        textLogout.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS_M));
        textProfileName.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        textProfileJoin.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        textProfileEmail.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
    }


    public String getDateFormat(String date){
        String resultDate = "";
        if(!date.equals("")) {
            StringTokenizer tokenizer = new StringTokenizer(date, "-");
            resultDate += tokenizer.nextToken() + "년 ";
            resultDate += tokenizer.nextToken() + "월 ";
            resultDate += tokenizer.nextToken() + "일 가입";
        }
        return resultDate;
    }

    public void setProfile(){
        textProfileName.setText(PropertyManager.getInstance().getUserId());
        textProfileEmail.setText(PropertyManager.getInstance().getUserEmail());
        textProfileJoin.setText(getDateFormat((PropertyManager.getInstance().getUserJoin())));
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
            ImageLoader.getInstance().displayImage(PropertyManager.getInstance().getUserImagePath(),imageProfileUser, options);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setProfile();
        textSafeBikeMainTitle.setVisibility(View.GONE);
        textMainTitle.setVisibility(View.VISIBLE);

    }
}
