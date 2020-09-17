package com.samriddha.letschartapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.samriddha.letschartapp.R
import kotlinx.android.synthetic.main.activity_phone_login.*
import java.util.concurrent.TimeUnit

class PhoneLoginActivity : AppCompatActivity() {

    private lateinit var verificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_login)

        firebaseAuth = FirebaseAuth.getInstance()

        btnPhLoginSendCode.setOnClickListener {

            startVerificationProcesses()

        }

        btnPhLoginSubmitCode.setOnClickListener {

            val verificationCode = etPhLoginVCode.text.toString()

            if(verificationCode.isEmpty())
            {
                Toast.makeText(this, "Enter Your Code First", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            phLoginProgressBar.visibility = View.VISIBLE
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                , WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            val credential = PhoneAuthProvider.getCredential(verificationId,verificationCode)
            signInWithPhoneAuthCredential(credential)

        }

    }

    private fun startVerificationProcesses() {

        val phNumber = etPhLoginNumber.text.toString()
        if (phNumber.isEmpty()) {
            Toast.makeText(this, "Enter Your Phone Number First", Toast.LENGTH_SHORT).show()
            return
        }

        phLoginProgressBar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            , WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks
        ) // OnVerificationStateChangedCallback

    }

    //Verification callback for success and failure case's.
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(p0: PhoneAuthCredential) {

            phLoginProgressBar.visibility = View.VISIBLE
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                , WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            etPhLoginVCode.setText(p0.smsCode)
            signInWithPhoneAuthCredential(p0)

        }

        override fun onVerificationFailed(p0: FirebaseException) {

            phLoginProgressBar.visibility = View.INVISIBLE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            Toast.makeText(this@PhoneLoginActivity,"Enter A Valid Phone Number With Country Code",Toast.LENGTH_LONG).show()

        }

        override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(p0, p1)

            verificationId = p0
            resendToken = p1

            Toast.makeText(this@PhoneLoginActivity,"Verification Code Sent",Toast.LENGTH_LONG).show()

            changeViewEditTextAndButtons(true)

            phLoginProgressBar.visibility = View.INVISIBLE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    phLoginProgressBar.visibility = View.INVISIBLE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    Toast.makeText(this,"User Created",Toast.LENGTH_SHORT).show()
                    gotoMainActivity()

                } else {

                    changeViewEditTextAndButtons(false)

                    phLoginProgressBar.visibility = View.INVISIBLE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                    Toast.makeText(this,"Error:  ${task.exception.toString()}",Toast.LENGTH_SHORT).show()

                }
            }
    }

    private fun gotoMainActivity(){

        //Clearing back stack because we don't want the user to come back to login activity after pressing back button from main activity
        startActivity(
            Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
    }

    private fun changeViewEditTextAndButtons(showSubmitBtn:Boolean){

        if (showSubmitBtn)
        {
            //making the phone number edit text non-touchable after verification code is send.
            etPhLoginNumber.inputType = InputType.TYPE_NULL

            //send verification code button is hidden and submit verification code button is shown.
            etPhLoginVCode.visibility = View.VISIBLE
            btnPhLoginSendCode.visibility = View.INVISIBLE
            btnPhLoginSubmitCode.visibility = View.VISIBLE

        } else {

            etPhLoginNumber.inputType = InputType.TYPE_CLASS_PHONE

            //send verification code button is hidden and submit verification code button is shown.
            etPhLoginVCode.visibility = View.INVISIBLE
            btnPhLoginSendCode.visibility = View.VISIBLE
            btnPhLoginSubmitCode.visibility = View.INVISIBLE
        }

    }

}