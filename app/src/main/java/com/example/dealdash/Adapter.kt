package com.example.dealdash

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import java.util.*


class HomeAdapter(private var productsList: ArrayList<ProductDisplay>, private val activity: Activity) :
    RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    private var currentUserID: String? = null

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewName: TextView
        val textViewPrice : TextView
        val imageView: ImageView
        val editButton: MaterialButton

        init {
            textViewName = view.findViewById(R.id.textViewNameRecycler)
            textViewPrice = view.findViewById(R.id.textViewPriceRecycler)
            imageView = view.findViewById(R.id.imageViewRecycler)
            editButton = view.findViewById(R.id.editButton)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.card_layout, viewGroup, false)

        return ViewHolder(view).apply {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val product = productsList[position]
                    val productDetailActivity = Intent(activity, ProductDetailActivity::class.java)
                    productDetailActivity.putExtra("ProductDetails", product)
                    activity.startActivity(productDetailActivity)
                }
            }
            editButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val productId = productsList[position].productID
                    val productAccountActivity = Intent(activity, AccountTabbedActivity::class.java)
                    productAccountActivity.putExtra("ProductID", productId)
                    activity.startActivity(productAccountActivity)
                }
            }
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val thaiLocale = Locale("", "TH")
        val currency: Currency = Currency.getInstance(thaiLocale)
        viewHolder.itemView.tag = productsList[position].productID
        viewHolder.textViewName.text = productsList[position].name
        viewHolder.textViewPrice.text = activity.getString(R.string.display_price, productsList[position].price, currency.symbol)
        Glide.with(activity)
            .load(productsList[position].images[0].imageUri)
            .into(viewHolder.imageView)

        if (currentUserID == productsList[position].userId) {
            viewHolder.editButton.visibility = View.VISIBLE
        } else {
            viewHolder.editButton.visibility = View.GONE
        }
    }

    override fun getItemCount() = productsList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(products: ArrayList<ProductDisplay>) {
        this.productsList = products
        notifyDataSetChanged()
    }

    fun setCurrentUserID(userID: String?) {
        currentUserID = userID
    }
}