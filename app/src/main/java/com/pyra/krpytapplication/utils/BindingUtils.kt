package com.pyra.krpytapplication.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.pyra.krpytapplication.R

object BindingUtils {


    @BindingAdapter("loadUser")
    @JvmStatic
    fun loadImage(view: ImageView?, url: String?) {

        url?.let {
            view?.let {
                Glide.with(view.context)
                    .load(url)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.user)
                            .error(R.drawable.user)
                    )
                    .into(view)
            }

        }

    }
}