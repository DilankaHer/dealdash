package com.example.dealdash

import android.net.Uri

data class ProductDisplay(var productID: String = "", var userId: String = "", var name: String = "", var price: Double = 0.0, var description: String = "", var images: ArrayList<ImageDisplay> = ArrayList(), var sold: Boolean = false, var sellerDetails: User = User()): java.io.Serializable

data class Product(val name: String, val price: Double, val description: String, var images: MutableMap<String, Any>?, var sold: Boolean = false)

data class ProductAccount(var productID: String = "", var name: String = "", var price: Double = 0.0, var description: String = "", var images: ArrayList<ImageDisplay> = ArrayList(), var sold: Boolean = false): java.io.Serializable

data class ImageDisplay(var imageID: String = "", var imageUri: String = "", var isMainImage: Boolean = false): java.io.Serializable

data class Image(var imageURL: String, var isMainImage: Boolean)

data class ImageUri(var imageUri: Uri, var isMainImage: Boolean)