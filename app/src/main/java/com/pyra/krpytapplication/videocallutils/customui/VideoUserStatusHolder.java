package com.pyra.krpytapplication.videocallutils.customui;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.pyra.krpytapplication.R;


public class VideoUserStatusHolder extends RecyclerView.ViewHolder {
    public final RelativeLayout mMaskView;

    public final ImageView mAvatar;
    public final ImageView mIndicator;

    public VideoUserStatusHolder(View v) {
        super(v);

        mMaskView = (RelativeLayout) v.findViewById(R.id.user_control_mask);
        mAvatar = (ImageView) v.findViewById(R.id.default_avatar);
        mIndicator = (ImageView) v.findViewById(R.id.indicator);


    }
}
