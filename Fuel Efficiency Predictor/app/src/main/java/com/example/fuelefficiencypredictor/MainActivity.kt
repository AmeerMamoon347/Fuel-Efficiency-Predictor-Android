package com.example.fuelefficiencypredictor

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.*
import at.markushi.ui.CircleButton
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    private var enterPW:EditText? = null
    private var enterEmail:EditText? = null
    private var loginBtn:Button? = null
    private var Dont_SignUp:TextView? = null
    private var signUp_Google:ImageView? = null

     var auth:FirebaseAuth?= null
     var googleSignInClient:GoogleSignInClient?= null

     val serverClientId = "229506478641-otk6j842ist6785j1p6r5ctlt28sfd9r.apps.googleusercontent.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




//        val intent = Intent(this,Home::class.java)
//        startActivity(intent)

        enterPW = findViewById(R.id.enter_password)
        enterEmail = findViewById(R.id.enter_email)
        loginBtn = findViewById(R.id.btn_Login)
        Dont_SignUp = findViewById(R.id.Dont_Sign_up)
        signUp_Google = findViewById(R.id.sign_Up_Google)

        auth = FirebaseAuth.getInstance()

//        var signInRequest = BeginSignInRequest.builder()
//            .setGoogleIdTokenRequestOptions(
//                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                    .setSupported(true)
//                    // Your server's client ID, not your Android client ID.
//                    .setServerClientId(serverClientId)
//                    // Only show accounts previously used to sign in.
//                    .setFilterByAuthorizedAccounts(true)
//                    .build())
//            .build()

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverClientId)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions)


        loginBtn!!.setOnClickListener {

            if(enterEmail!!.text.isEmpty() || enterPW!!.text.isEmpty())
            {
                Toast.makeText(this,"Enter both 'Email' and 'Password'",Toast.LENGTH_LONG).show()
            } else
            {
               auth!!.signInWithEmailAndPassword(enterEmail?.text.toString(),enterPW?.text.toString())
                   .addOnCompleteListener{ task->
                       if(task.isSuccessful)
                       {
                           Log.d("Successfully login: ",auth?.currentUser?.email.toString())
                           Toast.makeText(this,"Successfully Login !",Toast.LENGTH_LONG).show()

                           val intent = Intent(this,Home::class.java)
                          startActivity(intent)
                          finish()
                       }
                   }.addOnFailureListener{
                       Log.d("Login Failure: ","Something is wrong")
                       Toast.makeText(this,"Something is wrong or may be account doesn't exist!",Toast.LENGTH_LONG)
                           .show()
                   }
            }

        }

        Dont_SignUp!!.setOnClickListener{
            val intent = Intent(this,Sign_up::class.java)
            startActivity(intent)

        }



        signUp_Google?.setOnClickListener {
           googleSignIn()

        }

    }

     fun googleSignIn() {

        var intent = googleSignInClient?.signInIntent
        startActivityForResult(intent,101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==101 && resultCode == RESULT_OK)
        {
          var task = GoogleSignIn.getSignedInAccountFromIntent(data)
          var account = task.result

          Log.d("result_account: ",account.email.toString())

          FirebaseAuthGoogle(account.idToken!!,account)

        }

    }

     private fun FirebaseAuthGoogle(idToken : String,account: GoogleSignInAccount) {

        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth?.signInWithCredential(firebaseCredential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth?.currentUser
                    Toast.makeText(this,"Successfully Login !",Toast.LENGTH_LONG).show()
                    Log.d("result_account: ",account.email.toString()+" "+account.displayName+" "+account.givenName)

                    val dbRef = FirebaseDatabase.getInstance().getReference().child("Google Sign In").push()

                    dbRef.child("Email").setValue(account.email.toString())
                    dbRef.child("Name").setValue(account.displayName.toString())

                    val intent = Intent(this,Home::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }

        }

    override fun onStart() {
        super.onStart()

        if(auth?.currentUser!=null)
        {
            val intent = Intent(this,Home::class.java)
            startActivity(intent)
            finish()
        }

    }



}