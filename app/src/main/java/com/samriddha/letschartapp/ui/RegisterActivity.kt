package com.samriddha.letschartapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.samriddha.letschartapp.others.Constants.DATABASE_PATH_NAME_ALL_USERS
import com.samriddha.letschartapp.R
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseDbRef:DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDbRef = FirebaseDatabase.getInstance().reference

        btnAlreadyHaveAccount.setOnClickListener {
            gotoLoginActivity()
        }

        btnCreateAccount.setOnClickListener {
            createNewAccount()
        }

    }

    private fun createNewAccount() {

        val userEmail = registerEmail.text.toString()
        val userPassword = registerPassword.text.toString()

        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(this, "Please Enter Email Or Password", Toast.LENGTH_LONG).show()
            return
        }

        //showing progress bar and disabling user touch when showing progress bar.
        progressBar.visibility = View.VISIBLE
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            , WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        //creating firebase user account
        firebaseAuth?.createUserWithEmailAndPassword(userEmail, userPassword)
            ?.addOnSuccessListener {

                createUserDatabase()
                Toast.makeText(this, "Successfully Created Your Account", Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE); //enabling user touch
                gotoMainActivity()

            }?.addOnFailureListener {

                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE); //enabling user touch

            }

    }

    private fun createUserDatabase() {

        val currentUserId = firebaseAuth?.currentUser?.uid //Getting current user unique id

        //inserting user in firebase database with user's unique id
        firebaseDbRef
            ?.child(DATABASE_PATH_NAME_ALL_USERS)
            ?.child(currentUserId!!)
            ?.setValue("")
    }

    private fun gotoLoginActivity(){
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun gotoMainActivity(){

        //Clearing back stack because we don't want the user to come back to login activity after pressing back button from main activity
        startActivity(Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()

    }
}