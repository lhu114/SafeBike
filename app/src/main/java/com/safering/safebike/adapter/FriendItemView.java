package com.safering.safebike.adapter;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.safering.safebike.R;

/**
 * Created by Tacademy on 2015-10-30.
 */
public class FriendItemView extends RelativeLayout{
    ImageView friendImage;
    TextView friendId;
    Button btn;
    public FriendItemView(Context context) {
        super(context);
        init();
    }

    public void init(){
        inflate(getContext(), R.layout.friend_item_view, this);
        friendImage = (ImageView)findViewById(R.id.image_friend);
        friendId = (TextView)findViewById(R.id.text_friend_id);
        btn = (Button)findViewById(R.id.btn_add_friend);

    }

    public void setFriendData(FriendItem data){
        if(data.friendImage != null){
            friendImage.setImageDrawable(data.friendImage);
        }
        friendId.setText(data.friendId);

    }

    public void setAddButtonVisible(boolean isVisible){
        if(isVisible){
            btn.setVisibility(View.VISIBLE);
        }
        else
            btn.setVisibility(View.INVISIBLE);

    }
}
