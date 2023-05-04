package com.example.dealdash

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class ForgotPasswordActivity : AppCompatActivity(), FirebaseCallBacks.FireBaseCommonCallback {

    private val firebaseServices = FirebaseServices(this)
    private lateinit var email: EditText
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        email = findViewById(R.id.emailForgotPassword)
        submitButton = findViewById(R.id.submitForgotPassword)

        email.addTextChangedListener(textWatcher)

        submitButton.setOnClickListener {
            firebaseServices.passwordReset(email.text.toString(), this)
        }
    }

    private var textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            submitButton.isEnabled = email.text.toString().trim().isNotEmpty()
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onComplete(resultCode: Int, message: String) {
        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Password reset email sent successfully", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to send password reset email", Toast.LENGTH_SHORT).show()
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