package com.example.dealdash

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AccountFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AccountFragment : Fragment(), FirebaseCallBacks.FireBaseUserCallback, FirebaseCallBacks.FireBaseCommonCallback, FirebaseCallBacks.FireBaseUpdateEmailCallback {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var userAccount: UserAccount
    private var mAuth = FirebaseAuth.getInstance()
    private var currentUser = mAuth.currentUser
    private lateinit var activity: Activity
    private lateinit var firebaseServices : FirebaseServices

    private lateinit var welcomeText: TextView
    private lateinit var firstName: TextView
    private lateinit var lastName: TextView
    private lateinit var email: TextView
    private lateinit var mobileNumber: TextView
    private lateinit var facebook: TextView
    private lateinit var line: TextView
    private lateinit var editTextDialog: EditText
    private lateinit var editEmailDialog: EditText
    private lateinit var editPasswordDialog: EditText
    private lateinit var dialog: AlertDialog

    private var notDisable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        welcomeText = view.findViewById(R.id.welcomeTextTV)
        firstName = view.findViewById(R.id.firstNameTV)!!
        lastName = view.findViewById(R.id.lastNameTV)!!
        email = view.findViewById(R.id.emailTV)!!
        mobileNumber = view.findViewById(R.id.mobileNoTV)!!
        facebook = view.findViewById(R.id.facebookTV)!!
        line = view.findViewById(R.id.lineTV)!!

        val firstNameEdit = view.findViewById<MaterialButton>(R.id.firstNameEdit)
        val lastNameEdit = view.findViewById<MaterialButton>(R.id.lastNameEdit)
        val emailEdit = view.findViewById<MaterialButton>(R.id.emailEdit)
        val mobileNumberEdit = view.findViewById<MaterialButton>(R.id.mobileNoEdit)
        val facebookEdit = view.findViewById<MaterialButton>(R.id.facebookEdit)
        val lineEdit = view.findViewById<MaterialButton>(R.id.lineEdit)

        firstNameEdit?.setOnClickListener{
            handleEditDialog("firstName", "First Name", firstName.text.toString())
        }
        lastNameEdit?.setOnClickListener{
            handleEditDialog("lastName", "Last Name", lastName.text.toString())
        }
        emailEdit?.setOnClickListener{
            handleEditDialog("email", "Email", "")
        }
        mobileNumberEdit?.setOnClickListener{
            handleEditDialog("mobileNumber", "Mobile Number", mobileNumber.text.toString())
        }
        facebookEdit?.setOnClickListener{
            handleEditDialog("facebook", "Facebook", facebook.text.toString())
        }
        lineEdit?.setOnClickListener{
            handleEditDialog("line", "Line ID", line.text.toString())
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AccountFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun handleEditDialog(fieldName: String, title: String, previousValue: String) {
        val layout = requireActivity().layoutInflater.inflate(R.layout.edit_dialog, null)
        val emailLayout = layout.findViewById<LinearLayout>(R.id.layoutEditEmail)
        editTextDialog = layout.findViewById(R.id.editTextED)
        editEmailDialog = layout.findViewById(R.id.emailNew)
        editPasswordDialog = layout.findViewById(R.id.passwordEmailNew)
        editTextDialog.addTextChangedListener(textWatcher)
        editEmailDialog.addTextChangedListener(textWatcher)
        editPasswordDialog.addTextChangedListener(textWatcher)
        if (fieldName == "email") {
            emailLayout.visibility = View.VISIBLE
            editTextDialog.visibility = View.GONE
        } else {
            emailLayout.visibility = View.GONE
            editTextDialog.visibility = View.VISIBLE
            editTextDialog.setText(previousValue)
        }
        dialog = MaterialAlertDialogBuilder(requireActivity(), R.style.CustomMaterialDialog)
            .setTitle("Edit $title")
            .setView(layout)
            .setPositiveButton("Confirm") { _, _ ->
                if (fieldName == "email") {
                    firebaseServices.updateEmail(currentUser!!, editEmailDialog.text.trim().toString(), editPasswordDialog.text.trim().toString(), this)
                } else {
                    firebaseServices.updateUser(currentUser?.uid!!, fieldName, editTextDialog.text.trim().toString())
                }
                firebaseServices.getUser(this, currentUser!!)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { _, _ ->
                dialog.cancel()
            }
            .show()

        if (arrayOf("line", "facebook").contains(fieldName)) {
            notDisable = true
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
        } else {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
            notDisable = false
        }

    }

    private var textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if(::dialog.isInitialized && !notDisable) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                    editTextDialog.text.toString().trim().isNotEmpty() || (editEmailDialog.text.toString().trim().isNotEmpty() && editPasswordDialog.text.toString().trim().isNotEmpty())
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).invalidate()
            }
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onGetUserComplete(resultCode: Int, userAccount: UserAccount) {
        if (resultCode == Activity.RESULT_OK) {
            this.userAccount = userAccount
            firstName.text = this.userAccount.firstName
            lastName.text = this.userAccount.lastName
            email.text = this.userAccount.email
            mobileNumber.text = this.userAccount.mobileNumber
            facebook.text = this.userAccount.facebook
            line.text = this.userAccount.line
            welcomeText.text = getString(R.string.welcome_message, firstName.text)
        }
    }

    override fun onStart() {
        super.onStart()
        if (isAdded) {
            activity = requireActivity()
            firebaseServices = FirebaseServices(activity)
            firebaseServices.getUser(this, currentUser!!)
        }
    }

    override fun onUpdateEmailComplete(resultCode: Int, message: String, email: String) {
        if (resultCode == Activity.RESULT_OK) {
            this.email.text = email
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onComplete(resultCode: Int, message: String) {
        if (resultCode == Activity.RESULT_OK) {
            MaterialAlertDialogBuilder(activity, R.style.CustomMaterialDialog)
            .setTitle("Email Updated")
            .setMessage("Your email was successfully updated, please click on `resend verification email` in sign-in page and verify the new email address and sign in again")
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
                val signInIntent = Intent(activity, SignInActivity::class.java)
                activity.startActivity(signInIntent)
                activity.finish()
            }
            .show()
        } else {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }
}