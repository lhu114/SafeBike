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
public class FriendItemView extends RelativeLayout{
    ImageView friendImage;
    TextView friendId;
    TextView friendRank;
    Button btn;
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

    public void init(){
        inflate(getContext(), R.layout.friend_item_view, this);
        friendRank = (TextView)findViewById(R.id.text_friend_rank);
        friendImage = (ImageView)findViewById(R.id.image_friend);
        friendId = (TextView)findViewById(R.id.text_friend_id);
        btn = (Button)findViewById(R.id.btn_add_friend);

        setFont();

        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onButtonClick(FriendItemView.this, fData);
            }
        });


    }

    public void setFriendData(FriendItem data){
        fData = data;
        if(data != null){
           // friendImage.setImageDrawable(data.friendImage);
        }
        friendId.setText(data.pname);

    }

    public void setAddButtonVisible(boolean isVisible){
        if(isVisible){
            btn.setVisibility(View.VISIBLE);

        }
        else
            btn.setVisibility(View.INVISIBLE);

    }
    public void setFont(){
        friendRank.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        friendId.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));

    }
}
