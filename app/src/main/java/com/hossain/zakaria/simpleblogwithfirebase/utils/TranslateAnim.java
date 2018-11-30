package com.hossain.zakaria.simpleblogwithfirebase.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.hossain.zakaria.simpleblogwithfirebase.R;

public class TranslateAnim {

    public static void setAnimation(View view, Context context, String animType) {
        switch (animType) {
            case "left":
                view.setAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_from_left));
                break;

            case "right":
                view.setAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_from_right));
                break;

            case "top":
                view.setAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_from_top));
                break;

            case "bottom":
                view.setAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_from_bottom));
                break;

            default:
                break;
        }
    }
}
