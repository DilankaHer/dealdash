package com.example.dealdash

class FirebaseCallBacks {

    interface FireBaseCommonCallback {
        fun onComplete(resultCode: Int, message: String = "")
    }

    interface FireBaseUserCallback {
        fun onGetUserComplete(resultCode: Int, userAccount: UserAccount)
    }

    interface FireBaseGetProductsCallback {
        fun onGetProductsComplete(resultCode: Int, products: ArrayList<ProductDisplay>)
    }

    interface FireBaseGetUserProductsCallback {
        fun onGetUserProductsComplete(resultCode: Int, products: ArrayList<ProductAccount>)
    }

    interface FireBaseUpdateEmailCallback {
         fun onUpdateEmailComplete(resultCode: Int, message: String = "", email: String = "")
    }
}