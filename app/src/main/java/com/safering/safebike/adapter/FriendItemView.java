package com.safering.safebike.adapter;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.safering.safebike.R;
import com.safering.safebike.manager.FontManager;

/**
 * Created by Tacademy on 2015-10-30.
 */
public class FriendItemView extends RelativeLayout {
    ImageView friendImage;
    TextView friendId;
    TextView friendRank;
    ImageView imagePlus;
    FriendItem fData;


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
        if (data != null) {
            // friendImage.setImageDrawable(data.friendImage);
        }
        friendId.setText(data.pname);

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
