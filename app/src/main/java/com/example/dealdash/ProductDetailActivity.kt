package com.example.dealdash

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import java.io.Serializable
import java.util.*

class ProductDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val thaiLocale = Locale("", "TH")
        val currency: Currency = Currency.getInstance(thaiLocale)

        val product : ProductDisplay? = intent.serializable("ProductDetails")
        val sellerDetails = product?.sellerDetails

        val productName = findViewById<TextView>(R.id.productDetailsName)
        val productPrice = findViewById<TextView>(R.id.productDetailsPrice)
        val productDesc = findViewById<TextView>(R.id.productDetailsDescription)
        val productImagePager = findViewById<ViewPager>(R.id.productDetailImagePager)
        val sellerName = findViewById<TextView>(R.id.productDetailsSellerName)
        val sellerMobileNo = findViewById<TextView>(R.id.productDetailsSellerMobile)
        val sellerFacebook = findViewById<TextView>(R.id.productDetailsSellerFacebook)
        val sellerLine = findViewById<TextView>(R.id.productDetailsSellerLine)
        val pageCounter = findViewById<Button>(R.id.pageViewPagers)
        var imagePosition = 0

        productName.text = product?.name.toString()
        productPrice.text = getString(R.string.display_price, product?.price,currency.symbol)
        sellerName.text = getString(R.string.seller_name, sellerDetails?.firstName,sellerDetails?.lastName)
        sellerMobileNo.text = sellerDetails?.mobileNumber.toString()

        if (product?.description.toString() == "") {
            productDesc.text = getString(R.string.no_desc)
        } else {
            productDesc.text = product?.description.toString()
        }

        if (sellerDetails?.facebook.toString() == "") {
            sellerFacebook.visibility = View.GONE
        } else {
            sellerFacebook.text = sellerDetails?.facebook.toString()
        }

        if (sellerDetails?.line.toString() == "") {
            sellerLine.visibility = View.INVISIBLE
        } else {
            sellerLine.text = sellerDetails?.line.toString()
        }

        productImagePager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageSelected(position: Int) {
                imagePosition = position
                pageCounter.text = getString(R.string.page_counter_image,(imagePosition + 1).toString(),product?.images?.size.toString())
            }
        })

        pageCounter.text = getString(R.string.page_counter_image,(imagePosition + 1).toString(),product?.images?.size.toString())
        val imageAdapter = ImagePagerAdapter(product?.images!!, this)
        productImagePager.adapter = imageAdapter
    }

    private inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}