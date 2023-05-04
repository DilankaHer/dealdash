package com.example.dealdash

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide

class ImagePagerAdapter(private var images: ArrayList<ImageDisplay>, private val activity: Activity) :
    PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(container.context)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        Glide.with(activity)
            .load(images[position].imageUri)
            .into(imageView)

        imageView.setOnClickListener{
            val fullScreenIntent = Intent(activity, ImageFullScreenActivity::class.java)
            fullScreenIntent.putExtra("position", images[position].imageUri)
            activity.startActivity(fullScreenIntent)
        }
        imageView.tag = position
        container.addView(imageView)
        return imageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ImageView)
    }

    override fun getCount(): Int {
        return images.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getItemPosition(obj: Any): Int {
        return POSITION_NONE
    }

    fun setData(images: ArrayList<ImageDisplay>) {
        this.images = images
    }
}
