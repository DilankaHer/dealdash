package com.example.dealdash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity(), FirebaseCallBacks.FireBaseCommonCallback {
    private val firebaseServices = FirebaseServices(this)
    private val mAuth = FirebaseAuth.getInstance()
    private val currentUser = mAuth.currentUser
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signInButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

//        callback = object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                finish()
//            }
//        }
//        onBackPressedDispatcher.addCallback(this, callback)

        emailEditText = findViewById(R.id.emailSignInEdt)
        passwordEditText = findViewById(R.id.passwordSignInEdt)
        val forgotPassTextView = findViewById<TextView>(R.id.forgotPassTextView)
        val signUpTextView = findViewById<TextView>(R.id.signUpTextView)
        val signInVerifyTextView = findViewById<TextView>(R.id.signInVerifyTextView)
        signInButton = findViewById(R.id.signInButton)

        emailEditText.addTextChangedListener(textWatcher)
        passwordEditText.addTextChangedListener(textWatcher)

        signUpTextView.setOnClickListener {
            val signUpIntent = Intent(this, SignUpActivity::class.java)
            startActivity(signUpIntent)
            finish()
        }

        signInButton.setOnClickListener {
            firebaseServices.signIn(emailEditText.text.toString(), passwordEditText.text.toString(), this)
        }

        if (mAuth.currentUser == null) {
            signUpTextView.visibility = View.VISIBLE
            signInVerifyTextView.visibility = View.GONE
        } else if (!currentUser?.isEmailVerified!!) {
            signInVerifyTextView.visibility = View.VISIBLE
        }

        signInVerifyTextView.setOnClickListener {
            firebaseServices.verifyEmail(this, currentUser!!)
        }

        forgotPassTextView.setOnClickListener {
            val forgotPassIntent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(forgotPassIntent)
        }
    }

    private var textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            signInButton.isEnabled = emailEditText.text.toString().trim().isNotEmpty() && passwordEditText.text.toString().trim().isNotEmpty()
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onComplete(resultCode: Int, message: String) {
        if(resultCode == Activity.RESULT_OK) {
            if (message != "") {
                Toast.makeText(this@SignInActivity, message, Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            if (message != "") {
                Toast.makeText(this@SignInActivity, message, Toast.LENGTH_SHORT).show()
            }
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