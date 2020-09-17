package com.samriddha.letschartapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.samriddha.letschartapp.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private var firebaseAuth:FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebaseAuth = FirebaseAuth.getInstance()


        btnCreateNewAcc.setOnClickListener {
            startActivity(Intent(this,
                RegisterActivity::class.java))
            finish()
        }

        btnLogIn.setOnClickListener {
            logInUser()
        }

        btnLogInWithMobile.setOnClickListener {
            startActivity(Intent(this,
                PhoneLoginActivity::class.java))
        }

    }

    private fun logInUser() {

        val userEmail = loginEmail.text.toString()
        val userPassword = loginPassword.text.toString()

        if (userEmail.isEmpty() ||  userPassword.isEmpty()){
            Toast.makeText(this,"Enter User Name Or Password",Toast.LENGTH_SHORT).show()
            return
        }


        //showing progress bar
        loginProgressBar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            , WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)


        //authenticating user using email and password
        firebaseAuth?.signInWithEmailAndPassword(userEmail,userPassword)?.addOnSuccessListener {

            loginProgressBar.visibility = View.VISIBLE
            Toast.makeText(this,"Login Successful!!",Toast.LENGTH_SHORT).show()
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE); //enabling user touch
            gotoMainActivity()

        }?.addOnFailureListener {

            loginProgressBar.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE); //enabling user touch
            Toast.makeText(this,"Invalid User Or Password: ${it.message}",Toast.LENGTH_SHORT).show()
        }


    }

    private fun gotoMainActivity(){

        //Clearing back stack because we don't want the user to come back to login activity after pressing back button from main activity
        startActivity(Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
    }
}