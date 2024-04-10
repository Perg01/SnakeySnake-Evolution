package com.example.snakeysnake.java;

import android.content.Context;
import android.graphics.Typeface;

public class Font {
    private static Typeface customTypeface;

    public static Typeface getCustomTypeface(Context context) {
        if (customTypeface == null) {
            customTypeface = Typeface.createFromAsset(context.getAssets(), "ChrustyRock-ORLA.ttf");
        }
        return customTypeface;
    }
}
