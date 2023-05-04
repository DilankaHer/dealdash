package com.example.dealdash

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.dealdash.databinding.ActivityAccountTabbedBinding
import com.example.dealdash.ui.main.SectionsPagerAdapter
import com.google.android.material.tabs.TabLayout

class AccountTabbedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountTabbedBinding
    private var productID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        productID = intent.getStringExtra("ProductID")
        binding = ActivityAccountTabbedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager, productID)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

        if (productID != null) {
            val tabLayout = findViewById<TabLayout>(R.id.tabs)
            val tab = tabLayout.getTabAt(1)
            tab?.select()
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