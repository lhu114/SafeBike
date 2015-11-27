package com.safering.safebike.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.safering.safebike.R;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.property.MyApplication;
import com.safering.safebike.property.PropertyManager;

/**
 * Created by Tacademy on 2015-10-30.
 */
public class FriendItemView extends RelativeLayout {
    ImageView friendImage;
    TextView friendId;
    TextView friendRank;
    ImageView imagePlus;
    FriendItem fData;
    DisplayImageOptions options;
    public interface OnButtonClickListener {
        public void onButtonClick(FriendItemView view, FriendItem data);
    }

    OnButtonClickListener mListener;

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        mListener = listener;
    }

    public FriendItemView(Context context) {
        super(context);
        init();

    }

    public void init() {
        inflate(getContext(), R.layout.friend_item_view, this);
        friendImage = (ImageView) findViewById(R.id.image_friend);
        friendId = (TextView) findViewById(R.id.text_friend_id);
        imagePlus = (ImageView) findViewById(R.id.image_plus_friend);

        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .showImageOnLoading(R.mipmap.profile_img)
                .showImageForEmptyUri(R.mipmap.profile_img)


                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(1000))
                .build();
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(MyApplication.getContext()));


        setFont();


        imagePlus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onButtonClick(FriendItemView.this, fData);
            }
        });


    }

    public void setFriendData(FriendItem data) {
        fData = data;
        friendId.setText(data.pname);
        if(data.photo.equals("null")) {
            Log.i("friend photo", data.photo + "");
        }else {

            ImageLoader.getInstance().displayImage(data.photo,friendImage, options);
            Log.i("setData",data.photo);
        /*if(!data.photo.equals("null") && !TextUtils.isEmpty(data.photo)){
            Toast.makeText(getContext(), "photo url : " + data.photo, Toast.LENGTH_SHORT).show();
            ImageLoader.getInstance().displayImage(data.photo,friendImage, options);
        }*/
        }
    }

    public void setAddButtonVisible(boolean isVisible) {
        if (isVisible) {
            imagePlus.setVisibility(View.VISIBLE);

        } else
            imagePlus.setVisibility(View.INVISIBLE);

    }

    public void setFont() {
        friendId.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));

    }
}
