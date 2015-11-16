package com.safering.safebike.manager;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by dongja94 on 2015-10-14.
 */
public class FontManager {

    private static FontManager instance;

    private static Object obj = new Object();

    public static FontManager getInstance() {
        synchronized (obj) {
            if (instance == null) {
                instance = new FontManager();
            }
        }
        return instance;
    }

    Typeface nanum, noto, noto_m,noto_s,noto_r;

    public static final String BMJUA = "nanum";
    public static final String NOTOSANS = "noto";
    public static final String NOTOSANS_M = "noto_m";
    public static final String NOTOSANS_S = "noto_s";
    public static final String NOTOSANS_R = "noto_r";



    private FontManager() {

    }

    public Typeface getTypeface(Context context, String fontName) {
        if (BMJUA.equals(fontName)) {
            if (nanum == null) {
                nanum = Typeface.createFromAsset(context.getAssets(), "BMJUA_ttf.ttf");
            }
            return nanum;
        }
        if (NOTOSANS.equals(fontName)) {
            if (noto == null) {
                noto = Typeface.createFromAsset(context.getAssets(), "NotoSansKR-Regular.otf");
            }
            return noto;
        }
        if (NOTOSANS_M.equals(fontName)) {
            if (noto_m == null) {
                noto_m = Typeface.createFromAsset(context.getAssets(), "NotoSansKR-Medium.otf");
            }
            return noto_m;
        }
        if (NOTOSANS_S.equals(fontName)) {
            if (noto_s == null) {
                noto_s = Typeface.createFromAsset(context.getAssets(), "NotoSansKR-Light.otf");
            }
            return noto_s;

        }
        if(NOTOSANS_R.equals(fontName)){
            if (noto_r == null) {
                noto_r = Typeface.createFromAsset(context.getAssets(), "NotoSansKR-Regular.otf");
            }
            return noto_r;

        }


        return null;
    }
}
