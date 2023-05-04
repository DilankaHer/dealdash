package com.example.dealdash

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class CreateProduct : AppCompatActivity(), FirebaseCallBacks.FireBaseCommonCallback {
    private lateinit var getContent: ActivityResultLauncher<String>
    private val firebaseServices = FirebaseServices(this)
    private var images = ArrayList<ImageDisplay>()
    private lateinit var imageAdapter: ImagePagerAdapter
    private lateinit var frameLayoutPager: FrameLayout
    private var imagePosition  = 0
    private var fromFab = false
    private lateinit var pageCounter: Button
    private lateinit var submitButton: Button
    private lateinit var productNameEdt: EditText
    private lateinit var productPriceEdt: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_product)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        productNameEdt = findViewById(R.id.productNameCreate)
        productPriceEdt = findViewById(R.id.productPriceCreate)
        val productDescEdt = findViewById<EditText>(R.id.productDescCreate)
        val uploadButton = findViewById<Button>(R.id.uploadButtonCreate)
        val imagePager = findViewById<ViewPager>(R.id.imagePagerCreate)
        val uploadFab = findViewById<FloatingActionButton>(R.id.uploadButtonViewPager)
        val deleteFab = findViewById<FloatingActionButton>(R.id.deleteButtonViewPager)
        pageCounter = findViewById(R.id.pageViewPagers)
        frameLayoutPager = findViewById(R.id.frameLayoutCreate)
        submitButton = findViewById(R.id.submitButtonCreate)

        productNameEdt.addTextChangedListener(textWatcher)
        productPriceEdt.addTextChangedListener(textWatcher)

        imageAdapter = ImagePagerAdapter(images, this)
        imagePager.adapter = imageAdapter

        imagePager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageSelected(position: Int) {
                imagePosition = position
                pageCounter.text = getString(R.string.page_counter_image,(imagePosition + 1).toString(),images.size.toString())
                if (imagePosition == 0) {
                    deleteFab.visibility = View.GONE
                } else {
                    deleteFab.visibility = View.VISIBLE
                }
            }
        })

        uploadFab.setOnClickListener {
            fromFab = true
            getContent.launch("image/*")
        }

        deleteFab.setOnClickListener {
            images.removeAt(imagePosition)
            imageAdapter.setData(images)
            imageAdapter.notifyDataSetChanged()
        }

        val userID = intent.getStringExtra("UserID")

        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                frameLayoutPager.visibility = View.VISIBLE
                if (images.size == 0) {
                    images.add(ImageDisplay("", uri.toString(), true))
                } else if (fromFab && imagePosition == 0) {
                        images.removeAt(0)
                        images.add(0,ImageDisplay("", uri.toString(), true))
                } else if (fromFab) {
                    images.removeAt(imagePosition)
                    images.add(imagePosition,ImageDisplay("", uri.toString(), false))
                } else {
                    images.add(ImageDisplay("", uri.toString(), false))

                }
                pageCounter.text = getString(R.string.page_counter_image,(imagePosition + 1).toString(),images.size.toString())
                uploadButton.text = getString(R.string.add_more_image)
                imageAdapter.setData(images)
                imageAdapter.notifyDataSetChanged()
                checkSubmitButtonEnable()
            }
        }

        uploadButton.setOnClickListener {
            fromFab = false
            getContent.launch("image/*")
        }

        submitButton.setOnClickListener {
            val imageUriList = ArrayList<ImageUri>()
            for(image in images) {
                imageUriList.add(ImageUri(image.imageUri.toUri(), image.isMainImage))
            }
            firebaseServices.addProduct(Product(productNameEdt.text.trim().toString(), productPriceEdt.text.trim().toString().toDouble(), productDescEdt.text.trim().toString(), null),
                userID.toString(), UUID.randomUUID().toString(), imageUriList, this
            )
        }
    }

    fun checkSubmitButtonEnable() {
        submitButton.isEnabled = productNameEdt.text.trim().isNotEmpty() && productPriceEdt.text.trim().isNotEmpty() && images.size != 0
    }

    private var textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkSubmitButtonEnable()
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onComplete(resultCode: Int, message: String) {
        if(resultCode == Activity.RESULT_OK) {
            finish()
        } else {
            Toast.makeText(this@CreateProduct, "Product creation failed", Toast.LENGTH_SHORT).show()
        }
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