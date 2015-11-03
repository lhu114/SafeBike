package com.safering.safebike.navigation;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.safering.safebike.R;

/**
 * Created by lhu on 2015-11-03.
 */
public class FavoriteItemView extends RelativeLayout {
    ImageView iconView;
    TextView fvPOINameVIew;
    FavoriteItem mItemData;

    public FavoriteItemView(Context context) {
        super(context);

        init();
    }

    public FavoriteItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        inflate(getContext(), R.layout.favorite_item_view, this);
        iconView = (ImageView) findViewById(R.id.image_favorite);
        fvPOINameVIew = (TextView) findViewById(R.id.text_fv_poi_name);
    }

    public void setItemData(FavoriteItem itemData) {
        mItemData = itemData;

        if (mItemData.poiIcon != null) {
            iconView.setImageDrawable(mItemData.poiIcon);
        }

        fvPOINameVIew.setText(mItemData.fvPOIName);
    }
}
