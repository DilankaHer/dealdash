package com.example.dealdash

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.net.Uri
import androidx.core.net.toUri
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.util.*


class FirebaseServices(private val activity: Activity) {

    private lateinit var databaseRefProducts: DatabaseReference
    private lateinit var databaseRefUserProducts: DatabaseReference
    private lateinit var valueEventListener: ValueEventListener
    private lateinit var valueEventListenerUserProducts: ValueEventListener

    private val dbURL: String = "https://dealdash-ebabe-default-rtdb.asia-southeast1.firebasedatabase.app/"

    fun signIn(email: String, password: String, callback: FirebaseCallBacks.FireBaseCommonCallback) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        callback.onComplete(Activity.RESULT_OK, "Authentication Successful")
                    } else{
                        callback.onComplete(Activity.RESULT_CANCELED, task.exception?.message.toString())
                    }
                }
    }

    fun signUp(user: User, email: String, password: String, callback: FirebaseCallBacks.FireBaseCommonCallback) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                    firebaseUser?.let {
                        val databaseRef = FirebaseDatabase.getInstance(dbURL).getReference("users")
                        databaseRef.child(firebaseUser.uid).setValue(user)
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener {
                                firebaseUser.delete()
                                    .addOnCompleteListener {
                                        callback.onComplete(Activity.RESULT_CANCELED)
                                    }
                        }
                        firebaseUser.sendEmailVerification()
                            .addOnCompleteListener { task1 ->
                                if (task1.isSuccessful) {
                                    MaterialAlertDialogBuilder(activity, R.style.CustomMaterialDialog)
                                    .setTitle("Verify your email address")
                                    .setMessage("A verification email has been sent to your email address. Please verify your email address and sign-in")
                                    .setPositiveButton(
                                        "OK"
                                    ) { dialog: DialogInterface, _: Int ->
                                        dialog.dismiss()
                                        callback.onComplete(Activity.RESULT_OK)
                                    }.show()
                                } else {
                                    callback.onComplete(Activity.RESULT_CANCELED, task1.exception?.message.toString())
                                }
                            }
                    }
                } else {
                    callback.onComplete(Activity.RESULT_CANCELED, task.exception?.message.toString())
                }
            }
    }

    fun updateUser(userId: String, fieldName: String, stringValue: String) {
        val databaseRef = FirebaseDatabase.getInstance(dbURL).getReference("users").child(userId)
        databaseRef.child(fieldName).setValue(stringValue)

    }

    fun addProduct(product: Product, userId: String, productID: String, images: ArrayList<ImageUri>, callback: FirebaseCallBacks.FireBaseCommonCallback) {
        val databaseRef = FirebaseDatabase.getInstance(dbURL).getReference("users")
        val storage = FirebaseStorage.getInstance().reference
        val imagesMap = mutableMapOf<String, Any>()
        for(image in images) {
            val imageUUID = UUID.randomUUID()
            val imageRef = storage.child("${userId}/${productID}/${imageUUID}.jpg")
            val uploadTask = imageRef.putFile(image.imageUri)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    callback.onComplete(Activity.RESULT_CANCELED, task.exception?.message.toString())
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val imageUrl = task.result.toString()
                    imagesMap[imageUUID.toString()] = mapOf("imageURL" to imageUrl, "isMainImage" to image.isMainImage)
                    if (imagesMap.size == images.size) {
                        product.images= imagesMap
                        databaseRef.child(userId).child("products").child(productID).setValue(product)
                            .addOnSuccessListener {
                                callback.onComplete(Activity.RESULT_OK)
                            }
                            .addOnFailureListener {
                                callback.onComplete(Activity.RESULT_CANCELED, task.exception?.message.toString())
                            }
                    }
                } else {
                    callback.onComplete(Activity.RESULT_CANCELED, task.exception?.message.toString())
                }
            }
        }
    }

    fun getProducts(callback: FirebaseCallBacks.FireBaseGetProductsCallback) {
        val products: ArrayList<ProductDisplay> = ArrayList()
        databaseRefProducts = FirebaseDatabase.getInstance(dbURL).reference.child("users")
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                products.clear()
                for (userSnapshot in dataSnapshot.children) {
                    val sellerDetails = User()
                    val userId = userSnapshot.key.toString()
                    sellerDetails.firstName = userSnapshot.child("firstName").getValue(String::class.java).toString()
                    sellerDetails.lastName = userSnapshot.child("lastName").getValue(String::class.java).toString()
                    sellerDetails.mobileNumber = userSnapshot.child("mobileNumber").getValue(String::class.java).toString()
                    sellerDetails.facebook = userSnapshot.child("facebook").getValue(String::class.java).toString()
                    sellerDetails.line = userSnapshot.child("line").getValue(String::class.java).toString()
                    val productsList = userSnapshot.child("products")
                    for (productSnapshot in productsList.children) {
                        val product = ProductDisplay()
                        product.sold = productSnapshot.child("sold").getValue(Boolean::class.java) == true
                        if (!product.sold) {
                            product.userId = userId
                            product.productID = productSnapshot.key.toString()
                            product.name = productSnapshot.child("name").getValue(String::class.java).toString()
                            product.price = productSnapshot.child("price").getValue(Double::class.java)?.toDouble()!!
                            product.description = productSnapshot.child("description").getValue(String::class.java).toString()

                            val images = productSnapshot.child("images")
                            for (imageSnapshot in images.children) {
                                val image = ImageDisplay()
                                image.imageID = imageSnapshot.key.toString()
                                image.imageUri = imageSnapshot.child("imageURL").getValue(String::class.java).toString()
                                image.isMainImage = imageSnapshot.child("isMainImage").getValue(Boolean::class.java) == true
                                if (image.isMainImage && product.images.size != 0) {
                                    product.images.add(0, image)
                                } else {
                                    product.images.add(image)
                                }
                            }
                            product.sellerDetails = sellerDetails
                            products.add(product)
                        }
                    }
                }
                callback.onGetProductsComplete(Activity.RESULT_OK, products)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback.onGetProductsComplete(Activity.RESULT_OK, products)
            }
        }

        databaseRefProducts.addValueEventListener(valueEventListener)
    }

    fun getUserProducts(callback: FirebaseCallBacks.FireBaseGetUserProductsCallback, userId: String, productID: String = "") {
        val products: ArrayList<ProductAccount> = ArrayList()
        databaseRefUserProducts = if (productID != "") {
            FirebaseDatabase.getInstance(dbURL).getReference("users").child(userId).child("products").child(productID)
        } else {
            FirebaseDatabase.getInstance(dbURL).getReference("users").child(userId).child("products")
        }
        valueEventListenerUserProducts = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                products.clear()
                if (productID.isNotEmpty()){
                    val product = ProductAccount()
                    product.productID = dataSnapshot.key.toString()
                    product.name = dataSnapshot.child("name").getValue(String::class.java).toString()
                    product.price = dataSnapshot.child("price").getValue(Double::class.java)?.toDouble()!!
                    product.description = dataSnapshot.child("description").getValue(String::class.java).toString()
                    product.sold = dataSnapshot.child("sold").getValue(Boolean::class.java) == true
                    val images = dataSnapshot.child("images")
                    for (imageSnapshot in images.children) {
                        val image = ImageDisplay()
                        image.imageID = imageSnapshot.key.toString()
                        image.imageUri = imageSnapshot.child("imageURL").getValue(String::class.java).toString()
                        image.isMainImage = imageSnapshot.child("isMainImage").getValue(Boolean::class.java) == true
                        if (image.isMainImage && product.images.size != 0) {
                            product.images.add(0, image)
                        } else {
                            product.images.add(image)
                        }
                    }
                    products.add(product)
                } else {
                    for (productSnapshot in dataSnapshot.children) {
                        val product = ProductAccount()
                        product.productID = productSnapshot.key.toString()
                        product.name = productSnapshot.child("name").getValue(String::class.java).toString()
                        product.price = productSnapshot.child("price").getValue(Double::class.java)?.toDouble()!!
                        product.description = productSnapshot.child("description").getValue(String::class.java).toString()
                        product.sold = productSnapshot.child("sold").getValue(Boolean::class.java) == true
                        val images = productSnapshot.child("images")
                        for (imageSnapshot in images.children) {
                            val image = ImageDisplay()
                            image.imageID = imageSnapshot.key.toString()
                            image.imageUri = imageSnapshot.child("imageURL").getValue(String::class.java).toString()
                            image.isMainImage = imageSnapshot.child("isMainImage").getValue(Boolean::class.java) == true
                            if (image.isMainImage && product.images.size != 0) {
                                product.images.add(0, image)
                            } else {
                                product.images.add(image)
                            }
                        }
                        products.add(product)
                    }
                }
                callback.onGetUserProductsComplete(Activity.RESULT_OK, products)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback.onGetUserProductsComplete(Activity.RESULT_OK, products)
            }
        }
        databaseRefUserProducts.addValueEventListener(valueEventListenerUserProducts)
    }

    fun getUser(callback: FirebaseCallBacks.FireBaseUserCallback, currentUser: FirebaseUser) {
        val databaseRef = FirebaseDatabase.getInstance(dbURL).getReference("users").child(currentUser.uid)
        var userAccount = UserAccount("", "", "","", "", "", "")
        val user = FirebaseAuth.getInstance().currentUser
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val firstName = dataSnapshot.child("firstName").getValue(String::class.java)
                    val lastName = dataSnapshot.child("lastName").getValue(String::class.java)
                    val mobileNumber = dataSnapshot.child("mobileNumber").getValue(String::class.java)
                    val facebook = dataSnapshot.child("facebook").getValue(String::class.java)
                    val lineId = dataSnapshot.child("line").getValue(String::class.java)
                    userAccount = UserAccount(firstName.toString(), lastName.toString(), mobileNumber.toString(), facebook.toString(), lineId.toString(), user?.email.toString(), user?.uid!!)
                callback.onGetUserComplete(Activity.RESULT_OK, userAccount)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onGetUserComplete(Activity.RESULT_CANCELED, userAccount)
            }
        })
    }

    fun verifyEmail(callback: FirebaseCallBacks.FireBaseCommonCallback, firebaseUser: FirebaseUser) {
        firebaseUser.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val builder = AlertDialog.Builder(activity)
                    builder.setTitle("Verify your email address")
                    builder.setMessage("A verification email has been sent to your email. Please verify your email")
                    builder.setPositiveButton(
                        "OK"
                    ) { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                        callback.onComplete(Activity.RESULT_OK)
                    }
                    val dialog = builder.create()
                    dialog.show()
                } else {
                    callback.onComplete(Activity.RESULT_CANCELED, task.exception?.message.toString())
                }
            }
    }

    fun updateProduct(userId: String, productID: String, imageID: String, fieldName: String, stringValue: String, doubleValue: Double, callback: FirebaseCallBacks.FireBaseCommonCallback) {
        val databaseRef = FirebaseDatabase.getInstance(dbURL).getReference("users").child(userId).child("products").child(productID).child(fieldName)
        if (fieldName == "images") {
            val storage = FirebaseStorage.getInstance().reference
            val imageRef = storage.child("${userId}/${productID}/${imageID}.jpg")
            val uploadTask = imageRef.putFile(stringValue.toUri())
            uploadTask.addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    imageRef.downloadUrl.addOnCompleteListener { urlTask ->
                        if (urlTask.isSuccessful) {
                            databaseRef.child(imageID).child("imageURL").setValue(urlTask.result.toString())
                            callback.onComplete(Activity.RESULT_OK, "Image Updated Successfully")
                        } else {
                            callback.onComplete(Activity.RESULT_CANCELED, urlTask.exception?.message.toString())
                        }
                    }
                } else {
                    callback.onComplete(Activity.RESULT_CANCELED, task.exception?.message.toString())
                }
            }
        } else if(stringValue != "" || fieldName == "description") {
            if (fieldName == "sold") {
                databaseRef.setValue(stringValue.toBoolean())
                if (stringValue.toBoolean()) {
                    callback.onComplete(Activity.RESULT_OK, "Status set to SOLD")
                } else {
                    callback.onComplete(Activity.RESULT_OK, "Status set to NOT SOLD")
                }
            } else {
                databaseRef.setValue(stringValue)
                callback.onComplete(Activity.RESULT_OK, "Updated Successfully")
            }
        } else if (doubleValue != 0.0) {
            databaseRef.setValue(doubleValue)
            callback.onComplete(Activity.RESULT_OK, "Updated Successfully")
        } else {
            callback.onComplete(Activity.RESULT_CANCELED, "Field cannot be empty")
        }
    }

    fun addImage(userId: String, productID: String, imageUri: Uri, callback: FirebaseCallBacks.FireBaseCommonCallback) {
        val databaseRef = FirebaseDatabase.getInstance(dbURL).getReference("users").child(userId).child("products").child(productID).child("images")
        val storage = FirebaseStorage.getInstance().reference
        val imageUUID = UUID.randomUUID()
        val imageRef = storage.child("${userId}/${productID}/${imageUUID}.jpg")
        val uploadTask = imageRef.putFile(imageUri)
        uploadTask.addOnCompleteListener(activity) { task ->
            if (task.isSuccessful) {
                imageRef.downloadUrl.addOnCompleteListener { urlTask ->
                    if (urlTask.isSuccessful) {
                        databaseRef.child(imageUUID.toString()).setValue(Image(urlTask.result.toString(), false))
                        callback.onComplete(Activity.RESULT_OK, "Image Added Successfully")
                    } else {
                        callback.onComplete(Activity.RESULT_CANCELED, uploadTask.exception?.message.toString())
                    }
                }
            } else {
                callback.onComplete(Activity.RESULT_CANCELED, task.exception?.message.toString())
            }
        }
    }

    fun deleteImage(userId: String, productID: String, imageID: String, callback: FirebaseCallBacks.FireBaseCommonCallback) {
        val databaseRef = FirebaseDatabase.getInstance(dbURL).getReference("users").child(userId).child("products").child(productID).child("images").child(imageID)
        databaseRef.removeValue()

        val storage = FirebaseStorage.getInstance().reference
        val imageRef = storage.child("${userId}/${productID}/${imageID}.jpg")
        imageRef.delete().addOnSuccessListener {
            callback.onComplete(Activity.RESULT_OK, "Image Removed Successfully")
        }.addOnFailureListener {
            callback.onComplete(Activity.RESULT_CANCELED)
        }
    }

    fun passwordReset(email: String, callback: FirebaseCallBacks.FireBaseCommonCallback) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    callback.onComplete(Activity.RESULT_OK)
                } else {
                    callback.onComplete(Activity.RESULT_CANCELED, task.exception?.message.toString())
                }
            }
    }

    fun updateEmail(currentUser: FirebaseUser, newEmail: String, password: String, callback: FirebaseCallBacks.FireBaseCommonCallback) {
        val credential = EmailAuthProvider.getCredential(currentUser.email.toString(), password)
        currentUser.reauthenticate(credential).addOnCompleteListener { task: Task<Void?> ->
            if (task.isSuccessful) {
                currentUser.updateEmail(newEmail)
                    .addOnCompleteListener { task1: Task<Void?> ->
                        if (task1.isSuccessful) {
                            currentUser.reload().addOnCompleteListener { reloadTask ->
                                if (reloadTask.isSuccessful) {
                                    callback.onComplete(Activity.RESULT_OK, "Successfully changed email")
                                } else {
                                    callback.onComplete(Activity.RESULT_CANCELED, task1.exception?.message.toString())
                                }
                            }
                        } else {
                            callback.onComplete(Activity.RESULT_CANCELED, task1.exception?.message.toString())
                        }
                    }
            } else {
                callback.onComplete(Activity.RESULT_CANCELED, task.exception?.message.toString())
            }
        }
    }

    fun removeProduct(currentUser: FirebaseUser, productID: String, callback: FirebaseCallBacks.FireBaseCommonCallback) {
        val databaseRef = FirebaseDatabase.getInstance(dbURL).getReference("users").child(currentUser.uid).child("products").child(productID)
        databaseRef.removeValue().addOnSuccessListener {
            val storageRef = FirebaseStorage.getInstance().reference.child(currentUser.uid).child(productID)
            storageRef.listAll().addOnSuccessListener { listResult ->
                listResult.items.forEach { item ->
                    item.delete().addOnSuccessListener {
                        callback.onComplete(Activity.RESULT_CANCELED, "Successfully removed product")
                    }.addOnFailureListener {
                        callback.onComplete(Activity.RESULT_CANCELED, "Failed to remove product")
                    }
                }
            }.addOnFailureListener {
                callback.onComplete(Activity.RESULT_CANCELED, "Failed to remove product")
            }
        }.addOnFailureListener {
            callback.onComplete(Activity.RESULT_CANCELED, "Failed to remove product")
        }
    }

    fun removeValueListener() {
        databaseRefProducts.removeEventListener(valueEventListener)
    }

    fun removeValueListenerUserProducts() {
        databaseRefUserProducts.removeEventListener(valueEventListenerUserProducts)
    }


}

