package com.example.dealdash

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity(), FirebaseCallBacks.FireBaseGetProductsCallback, FirebaseCallBacks.FireBaseCommonCallback, NavigationView.OnNavigationItemSelectedListener, OnQueryTextListener {
    private val firebaseServices = FirebaseServices(this)
    private var mAuth = FirebaseAuth.getInstance()
    private var currentUser = mAuth.currentUser
    private lateinit var menu : Menu
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var searchView: SearchView
    private lateinit var emptySearchTV: TextView
    private lateinit var products : ArrayList<ProductDisplay>
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: HomeAdapter
    private val filteredProducts = ArrayList<ProductDisplay>()
    private var isSearching = false
    private var searchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val burgerMenu = findViewById<ImageView>(R.id.burgerMenuHome)
        searchView = findViewById(R.id.searchViewHome)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        emptySearchTV = findViewById(R.id.emptySearch)

        changeMenuState(currentUser)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        searchView.isSubmitButtonEnabled = true
        searchView.setQuery("", false)

        navigationView.setNavigationItemSelectedListener(this)
        searchView.setOnQueryTextListener(this)

        burgerMenu.setOnClickListener {
            drawerLayout.openDrawer(navigationView)
        }

        recyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(this@MainActivity, 2)
    }

    private fun changeMenuState(user: FirebaseUser?) {
        navigationView.menu.clear()
        navigationView.inflateMenu(R.menu.menu_main_nav)
        menu = navigationView.menu
        if (user == null) {
            menu.findItem(R.id.menuItemAccount).isVisible = false
            menu.findItem(R.id.menuItemSignin).isVisible = true
            menu.findItem(R.id.menuItemSignup).isVisible = true
            menu.findItem(R.id.menuItemSignout).isVisible = false
            menu.findItem(R.id.menuItemSell).isVisible = false
        } else if (!user.isEmailVerified) {
            menu.findItem(R.id.menuItemAccount).isVisible = false
            menu.findItem(R.id.menuItemSignin).isVisible = true
            menu.findItem(R.id.menuItemSignup).isVisible = false
            menu.findItem(R.id.menuItemSignout).isVisible = true
            menu.findItem(R.id.menuItemSell).isVisible = false
            Toast.makeText(this, "Please verify your email and sign-in again", Toast.LENGTH_LONG).show()
        } else if (user.isEmailVerified) {
            menu.findItem(R.id.menuItemAccount).isVisible = true
            menu.findItem(R.id.menuItemSignin).isVisible = false
            menu.findItem(R.id.menuItemSignup).isVisible = false
            menu.findItem(R.id.menuItemSignout).isVisible = true
            menu.findItem(R.id.menuItemSell).isVisible = true
        }
    }

    override fun onStart() {
        super.onStart()
        searchView.setQuery("", false)
        mAuth.addAuthStateListener(authStateListener)
        firebaseServices.getProducts(this)
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(authStateListener)
        firebaseServices.removeValueListener()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuItemSignup -> {
                drawerLayout.closeDrawer(GravityCompat.END)
                val signUpIntent = Intent(this, SignUpActivity::class.java)
                startActivity(signUpIntent)
                return true
            }
            R.id.menuItemSignin -> {
                drawerLayout.closeDrawer(GravityCompat.END)
                val signInIntent = Intent(this, SignInActivity::class.java)
                startActivity(signInIntent)
                return true
            }
            R.id.menuItemSell -> {
                drawerLayout.closeDrawer(GravityCompat.END)
                val createProductIntent = Intent(this, CreateProduct::class.java)
                createProductIntent.putExtra("UserID", currentUser?.uid)
                startActivity(createProductIntent)
            }
            R.id.menuItemSignout -> {
                drawerLayout.closeDrawer(GravityCompat.END)
                mAuth.signOut()
                Toast.makeText(
                    this,
                    "Successfully Signed Out",
                    Toast.LENGTH_LONG
                ).show()
                changeMenuState(null)
                recyclerViewAdapter.setCurrentUserID(null)
                recyclerViewAdapter.notifyDataSetChanged()
            }
            R.id.menuItemAccount -> {
                drawerLayout.closeDrawer(GravityCompat.END)
                val accountIntent = Intent(this, AccountTabbedActivity::class.java)
                startActivity(accountIntent)
            }
        }
        return false
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onGetProductsComplete(resultCode: Int, products: ArrayList<ProductDisplay>) {
        if(resultCode == Activity.RESULT_OK) {
            if (products.size == 0) {
                emptySearchTV.text = getString(R.string.no_products_home)
                emptySearchTV.visibility = View.VISIBLE
            } else {
                emptySearchTV.visibility = View.GONE
                this.products = products
                if (::recyclerViewAdapter.isInitialized) {
                    if (currentUser?.isEmailVerified == true) {
                        recyclerViewAdapter.setCurrentUserID(currentUser?.uid)
                    } else {
                        recyclerViewAdapter.setCurrentUserID(null)
                    }
                    if (isSearching) {
                        searchFilter()
                        recyclerViewAdapter.updateData(filteredProducts)
                    } else {
                        recyclerViewAdapter.updateData(this.products)
                    }
                } else {
                    recyclerViewAdapter = HomeAdapter(products, this)
                    recyclerView.adapter = recyclerViewAdapter
                    recyclerViewAdapter.setCurrentUserID(currentUser?.uid)
                    recyclerViewAdapter.notifyDataSetChanged()
                }
            }
        } else {
            Toast.makeText(this@MainActivity, "Could not retrieve products", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onComplete(resultCode: Int, message: String) {
        if(resultCode == Activity.RESULT_OK) {
            Toast.makeText(this@MainActivity, "Verification link sent to email", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@MainActivity, "Failed to send verification link", Toast.LENGTH_SHORT).show()
        }
    }

    private val authStateListener = FirebaseAuth.AuthStateListener {
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser
        this.changeMenuState(currentUser)
    }

    override fun onQueryTextSubmit(p0: String?): Boolean {
        searchQuery = searchView.query.toString().lowercase()
        searchFilter()
        return true
    }

    override fun onQueryTextChange(p0: String?): Boolean {
        if (searchView.query.toString() == "" && ::products.isInitialized) {
            if (products.size == 0) {
                emptySearchTV.text = getString(R.string.no_products_home)
                emptySearchTV.visibility = View.VISIBLE
            } else {
                emptySearchTV.visibility = View.GONE
                recyclerViewAdapter.updateData(products)
            }
            filteredProducts.clear()
            isSearching = false
            searchQuery = ""
        }
        return true
    }

    private fun searchFilter() {
        filteredProducts.clear()
        for (product in products) {
            if (product.name.lowercase().contains(searchQuery)) {
                filteredProducts.add(product)
            }
        }
        if (filteredProducts.size == 0) {
            emptySearchTV.visibility = View.VISIBLE
            emptySearchTV.text = getString(R.string.no_products_search)
        } else {
            emptySearchTV.visibility = View.GONE
            recyclerViewAdapter.updateData(filteredProducts)
        }
        isSearching = true
    }

    override fun onResume() {
        super.onResume()
        searchView.clearFocus()
    }
}




