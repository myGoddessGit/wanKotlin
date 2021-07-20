package com.xll.wanstudy.ext

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.xll.wanstudy.R

/**
 * @author cyl
 * @date 2021/7/12
 */
fun ImageView.loadImageView(url : String, placeholder : Int = R.mipmap.ic_launcher){
    Glide.with(context).load(url).apply(getRequestOption(placeholder)).into(this)
}

fun ImageView.loadImageView(url: String, width: Int, height: Int, placeholder: Int = R.mipmap.ic_launcher){
    Glide.with(context).load(url).apply(getRequestOption(width, height, placeholder)).into(this)
}

fun getRequestOption(placeholder: Int) =
    RequestOptions().centerCrop().placeholder(placeholder).error(R.mipmap.ic_launcher).
    diskCacheStrategy(DiskCacheStrategy.ALL)

fun getRequestOption(width : Int, height : Int, placeholder: Int) =
    RequestOptions().centerCrop().override(width, height).placeholder(placeholder)
        .error(R.mipmap.ic_launcher).diskCacheStrategy(DiskCacheStrategy.ALL)
