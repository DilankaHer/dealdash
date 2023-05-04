package com.example.dealdash

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import kotlin.properties.Delegates


private const val ARG_PARAM1 = "productID"
private const val ARG_PARAM2 = "param2"

class ProductFragment : Fragment(), FirebaseCallBacks.FireBaseGetUserProductsCallback, FirebaseCallBacks.FireBaseCommonCallback, OnProductClickListener {
    private var productID: String? = null
    private var param2: String? = null
    private var mAuth = FirebaseAuth.getInstance()
    private var currentUser = mAuth.currentUser
    private val paddingDp = 20
    private var paddingPx by Delegates.notNull<Int>()
    private lateinit var activity: Activity
    private lateinit var firebaseServices : FirebaseServices
    private lateinit var products: ArrayList<ProductAccount>
    private lateinit var viewF: View
    private lateinit var detailsLayout: FrameLayout
    private lateinit var listLayout: LinearLayout
    private lateinit var productName: TextView
    private lateinit var productPrice: TextView
    private lateinit var productDesc: TextView
    private lateinit var emptyProduct: TextView
    private lateinit var pageCounter: Button
    private lateinit var uploadFab: FloatingActionButton
    private lateinit var deleteFab: FloatingActionButton
    private lateinit var productImagePager: ViewPager
    private lateinit var productNameEdit: MaterialButton
    private lateinit var productPriceEdit: MaterialButton
    private lateinit var productDescEdit: MaterialButton
    private lateinit var productImageUpload: MaterialButton
    private lateinit var backButtonProduct: MaterialButton
    private lateinit var removeButtonProduct: MaterialButton
    private lateinit var getContent: ActivityResultLauncher<String>
    private var productDetails = ProductAccount()
    private lateinit var imageAdapter: ImagePagerAdapter
    private var imagePosition  = 0
    private var fromFab = false
    private lateinit var dialog: AlertDialog
    private lateinit var editTextDialog: EditText
    private var notDisable = false
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: AdapterProductDetails

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            productID = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity = requireActivity()
        paddingPx = (paddingDp * resources.displayMetrics.density).toInt()
        viewF = inflater.inflate(R.layout.fragment_product, container, false)
        listLayout = viewF.findViewById(R.id.bodyLayoutProducts)
        emptyProduct = viewF.findViewById(R.id.emptyProduct)
        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val imageID = productDetails.images[imagePosition].imageID
                if (!fromFab) {
                    firebaseServices.addImage(currentUser?.uid!!, productDetails.productID, uri, this)
                } else {
                    firebaseServices.updateProduct(currentUser?.uid!!, productDetails.productID, imageID, "images", uri.toString(), 0.0, this)
                }
            }
        }
        detailsLayout = viewF.findViewById(R.id.detailsLayout)
        val productDetailsView = inflater.inflate(R.layout.fragment_product_specific, container, false)
        detailsLayout.addView(productDetailsView)
        recyclerView = viewF.findViewById<View>(R.id.recyclerView) as RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(activity, 1)
        return viewF
    }

    companion object {
        @JvmStatic
        fun newInstance(productID: String?, param2: String) =
            ProductFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, productID)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onStart() {
        super.onStart()
        if (isAdded) {
            firebaseServices = FirebaseServices(activity)
            if (productID != null) {
                firebaseServices.getUserProducts(this, currentUser?.uid!!, productID!!)
            } else {
                firebaseServices.getUserProducts(this, currentUser?.uid!!)
            }
        }
    }

    private fun addProductDetail() {
        productName.text = productDetails.name
        productPrice.text = productDetails.price.toString()
        if (productDetails.description == "") {
            productDesc.text = getString(R.string.no_desc)
        } else {
            productDesc.text = productDetails.description
        }
        imageAdapter.setData(productDetails.images)
        imageAdapter.notifyDataSetChanged()
    }

    override fun onGetUserProductsComplete(resultCode: Int, products: ArrayList<ProductAccount>) {
        if (resultCode == Activity.RESULT_OK) {
            if (productID != null) {
                productDetails = products[0]
                createProductDetailView()
                addProductDetail()
                productID = null
            } else if (listLayout.visibility == View.GONE) {
                productDetails = products.filter { it.productID == productDetails.productID }[0]
                pageCounter.text = getString(R.string.page_counter_image,(imagePosition + 1).toString(),productDetails.images.size.toString())
                addProductDetail()
            } else {
                if(products.size == 0) {
                    emptyProduct.visibility = View.VISIBLE
                } else {
                    this.products = products
                    if(::recyclerViewAdapter.isInitialized){
                        recyclerViewAdapter.updateData(this.products)
                    } else {
                        recyclerViewAdapter = AdapterProductDetails(this.products, activity, this)
                        recyclerView.adapter = recyclerViewAdapter
                    }
                }
            }
        }
    }

    override fun onProductClick(resultCode: Int, productId: String) {
        if(resultCode == Activity.RESULT_OK) {
            productDetails = products.filter { it.productID == productId }[0]
            createProductDetailView()
            addProductDetail()
        }
    }

    private fun createProductDetailView() {
        productName = detailsLayout.findViewById(R.id.nameProduct)
        productPrice = detailsLayout.findViewById(R.id.priceProduct)
        productDesc = detailsLayout.findViewById(R.id.descProduct)
        productImagePager = detailsLayout.findViewById(R.id.imagePagerProduct)
        backButtonProduct = detailsLayout.findViewById(R.id.backButtonProductDetails)
        removeButtonProduct = detailsLayout.findViewById(R.id.removeButtonProductDetails)
        pageCounter = detailsLayout.findViewById(R.id.pageViewPagers)
        uploadFab = detailsLayout.findViewById(R.id.uploadFabProduct)
        deleteFab = detailsLayout.findViewById(R.id.deleteFabProduct)
        listLayout.visibility = View.GONE
        detailsLayout.visibility = View.VISIBLE

        productNameEdit = detailsLayout.findViewById(R.id.nameProductEdit)
        productPriceEdit = detailsLayout.findViewById(R.id.priceProductEdit)
        productDescEdit = detailsLayout.findViewById(R.id.descProductEdit)
        productImageUpload = detailsLayout.findViewById(R.id.uploadImageProduct)

        pageCounter.text = getString(R.string.page_counter_image,(imagePosition + 1).toString(),productDetails.images.size.toString())
        imageAdapter = ImagePagerAdapter(productDetails.images, activity)
        productImagePager.adapter = imageAdapter

        productImagePager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageSelected(position: Int) {
                imagePosition = position
                pageCounter.text = getString(R.string.page_counter_image,(imagePosition + 1).toString(),productDetails.images.size.toString())
                if (imagePosition == 0) {
                    deleteFab.visibility = View.GONE
                } else {
                    deleteFab.visibility = View.VISIBLE
                }
            }
        })

        productNameEdit.setOnClickListener{
            handleEditDialog("name", "Product Name", productName.text.toString())
        }
        productPriceEdit.setOnClickListener{
            handleEditDialog("price", "Price", productPrice.text.toString())
        }
        productDescEdit.setOnClickListener{
            if(productDesc.text.toString() != getString(R.string.no_desc)) {
                handleEditDialog("description", "More Info", productDesc.text.toString())
            } else {
                handleEditDialog("description", "More Info", "")
            }
        }
        productImageUpload.setOnClickListener{
            fromFab = false
            getContent.launch("image/*")
        }
        uploadFab.setOnClickListener {
            fromFab = true
            getContent.launch("image/*")
        }
        deleteFab.setOnClickListener {
            val imageID = productDetails.images[imagePosition].imageID
            productDetails.images.removeAt(imagePosition)
            imageAdapter.setData(productDetails.images)
            imageAdapter.notifyDataSetChanged()
            firebaseServices.deleteImage(currentUser?.uid!!, productDetails.productID, imageID, this)
        }
        backButtonProduct.setOnClickListener{
            listLayout.visibility = View.VISIBLE
            detailsLayout.visibility = View.GONE
            imagePosition = 0
            firebaseServices.removeValueListenerUserProducts()
            firebaseServices.getUserProducts(this, currentUser?.uid!!,"")
        }
        removeButtonProduct.setOnClickListener {
            MaterialAlertDialogBuilder(activity, R.style.CustomMaterialDialog)
                .setTitle("Delete Confirmation")
                .setMessage("This action will permanently remove the item")
                .setPositiveButton("Confirm") { dialog, _ ->
                    firebaseServices.removeValueListenerUserProducts()
                    firebaseServices.removeProduct(currentUser!!, productDetails.productID, this)
                    listLayout.visibility = View.VISIBLE
                    detailsLayout.visibility = View.GONE
                    imagePosition = 0
                    firebaseServices.getUserProducts(this, currentUser?.uid!!,"")
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
    }

    private fun handleEditDialog(fieldName: String, title: String, initialText: String) {
        val layout = requireActivity().layoutInflater.inflate(R.layout.edit_dialog, null)
        val emailLayout = layout.findViewById<LinearLayout>(R.id.layoutEditEmail)
        emailLayout.visibility = View.GONE
        editTextDialog = layout.findViewById(R.id.editTextED)
        editTextDialog.setText(initialText)
        editTextDialog.addTextChangedListener(textWatcher)
        dialog = MaterialAlertDialogBuilder(activity, R.style.CustomMaterialDialog)
            .setTitle("Edit $title")
            .setView(layout)
            .setPositiveButton("Confirm") { _, _ ->
                val userInput = editTextDialog.text.toString()
                currentUser?.let {
                    if (fieldName == "price") {
                        firebaseServices.updateProduct(it.uid, productDetails.productID, "", fieldName, "", userInput.trim().toDouble(), this)
                    } else {
                        firebaseServices.updateProduct(it.uid, productDetails.productID, "", fieldName, userInput.trim(), 0.0, this) }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { _, _ ->
                dialog.cancel()
            }
            .show()

        if (fieldName == "description") {
            notDisable = true
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
        } else {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
            notDisable = false
        }
    }

    private var textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if(::dialog.isInitialized && !notDisable) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = editTextDialog.text.toString().trim().isNotEmpty()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).invalidate()
            }
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onStop() {
        super.onStop()
        firebaseServices.removeValueListenerUserProducts()
    }

    override fun onComplete(resultCode: Int, message: String) {
        if(resultCode == Activity.RESULT_OK) {
            if(message != "") {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
        return
    }
}