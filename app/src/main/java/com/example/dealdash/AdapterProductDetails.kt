package com.example.dealdash

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import java.util.*

interface OnProductClickListener {
    fun onProductClick(resultCode: Int, productId: String)
}

class AdapterProductDetails(private var productsList: ArrayList<ProductAccount>, private val activity: Activity, private val listener: OnProductClickListener) :
    RecyclerView.Adapter<AdapterProductDetails.ViewHolder>(), FirebaseCallBacks.FireBaseCommonCallback {
    private val firebaseServices = FirebaseServices(activity)
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var viewHolderGlobal: ViewHolder

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewName: TextView
        val textViewPrice : TextView
        val textViewDesc: TextView
        val imageView: ImageView
        val markAsSoldButton: MaterialButton

        init {
            textViewName = view.findViewById(R.id.nameRecyclerAccount)
            textViewPrice = view.findViewById(R.id.priceRecyclerAccount)
            textViewDesc = view.findViewById(R.id.descRecyclerAccount)
            imageView = view.findViewById(R.id.imageRecyclerAccount)
            markAsSoldButton = view.findViewById(R.id.markAsSoldButton)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.card_layout_account, viewGroup, false)

        return ViewHolder(view).apply {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val product = productsList[position]
                    listener.onProductClick(Activity.RESULT_OK, product.productID)
                }
            }
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolderGlobal = viewHolder
        val thaiLocale = Locale("", "TH")
        val currency: Currency = Currency.getInstance(thaiLocale)
        viewHolderGlobal.itemView.tag = productsList[position].productID
        viewHolderGlobal.textViewName.text = productsList[position].name
        viewHolderGlobal.textViewPrice.text = activity.getString(R.string.display_price,productsList[position].price, currency.symbol)
        if (productsList[position].description == "") {
            viewHolderGlobal.textViewDesc.text = activity.getString(R.string.no_desc)
        } else {
            viewHolderGlobal.textViewDesc.text = productsList[position].description
        }
        Glide.with(activity)
            .load(productsList[position].images[0].imageUri)
            .into(viewHolderGlobal.imageView)

        val message: String
        if(productsList[position].sold) {
            message = "This action will change the status of your product as NOT SOLD"
            viewHolderGlobal.markAsSoldButton.text = activity.getString(R.string.mark_as_not_sold)
        } else {
            message = "This action will change the status of your product as SOLD"
            viewHolderGlobal.markAsSoldButton.text = activity.getString(R.string.mark_as_sold)
        }

        viewHolderGlobal.markAsSoldButton.setOnClickListener {
                MaterialAlertDialogBuilder(activity, R.style.CustomMaterialDialog)
                    .setTitle("Confirmation Dialog")
                    .setMessage(message)
                    .setPositiveButton("Confirm") { dialog, _ ->
                        firebaseServices.updateProduct(currentUser?.uid!!, productsList[position].productID, "", "sold",
                            (!productsList[position].sold).toString(), 0.0, this)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.cancel()
                    }
                    .show()

        }

    }

    override fun getItemCount() = productsList.size

    override fun onComplete(resultCode: Int, message: String) {
        if(resultCode == Activity.RESULT_OK) {
            if(viewHolderGlobal.markAsSoldButton.text == activity.getString(R.string.mark_as_sold)) {
                viewHolderGlobal.markAsSoldButton.text = activity.getString(R.string.mark_as_not_sold)
            } else {
                viewHolderGlobal.markAsSoldButton.text = activity.getString(R.string.mark_as_sold)
            }
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, "Failed to update", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(products: ArrayList<ProductAccount>) {
        productsList = products
        notifyDataSetChanged()
    }

}