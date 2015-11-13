package com.safering.safebike.property;

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

    Typeface nanum, noto, roboto;

    public static final String BMJUA = "nanum";
    public static final String NOTOSANS = "noto";
    public static final String ROBOTO = "roboto";

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
                noto = Typeface.createFromAsset(context.getAssets(), "NotoSansCJKkr-Regular.otf");
            }
            return noto;
        }
        if (ROBOTO.equals(fontName)) {
            if (roboto == null) {
                roboto = Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf");
            }
            return roboto;
        }
        return null;
    }
}
