package com.example.dealdash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class SignUpActivity : AppCompatActivity(), FirebaseCallBacks.FireBaseCommonCallback {
    private val firebaseServices = FirebaseServices(this)
    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var mobile: EditText
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        firstName = findViewById(R.id.firstNameSignupEdt)
        lastName = findViewById(R.id.lastNameSignupEdt)
        email = findViewById(R.id.emailSignupEdt)
        password = findViewById(R.id.passwordSignupEdt)
        mobile = findViewById(R.id.mobileSignupEdt)
        val facebook = findViewById<EditText>(R.id.facebookSignupEdt)
        val line = findViewById<EditText>(R.id.lineSignupEdt)
        submitButton = findViewById(R.id.signUpButton)

        firstName.addTextChangedListener(textWatcher)
        lastName.addTextChangedListener(textWatcher)
        email.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)
        mobile.addTextChangedListener(textWatcher)

        submitButton.setOnClickListener {
                val user = User(firstName.text.trim().toString(), lastName.text.trim().toString(), mobile.text.trim().toString(), facebook.text.trim().toString(), line.text.trim().toString())
                firebaseServices.signUp(user, email.text.trim().toString(), password.text.trim().toString(), this)
        }
    }

    override fun onComplete(resultCode: Int, message: String) {
        if(resultCode == Activity.RESULT_OK) {
            Toast.makeText(this@SignUpActivity, "Account created successfully", Toast.LENGTH_SHORT).show()
            val signInIntent = Intent(this, SignInActivity::class.java)
            startActivity(signInIntent)
            finish()
        } else {
            Toast.makeText(this@SignUpActivity, message, Toast.LENGTH_SHORT).show()
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

    private var textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            submitButton.isEnabled = firstName.text.toString() != "" && lastName.text.toString() != "" && email.text.toString() != "" && password.text.toString() != "" && mobile.text.toString().length == 10
        }

        override fun afterTextChanged(s: Editable) {}
    }
}
