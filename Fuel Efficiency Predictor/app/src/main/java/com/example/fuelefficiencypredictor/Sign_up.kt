package com.example.fuelefficiencypredictor

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class Sign_up : AppCompatActivity() {

    var email:EditText? = null
    var name:EditText? = null
    var password:EditText? = null
    var confirmPW:EditText? = null
    var btn_SignIn:Button? = null
    var Login:TextView? = null
    var signUp_google:ImageView? = null

    var database:FirebaseDatabase?=null

    var auth: FirebaseAuth? = null
    var Name = ""
    var Email = ""

    var googleSignInClient:GoogleSignInClient? = null

    val serverClientId = "229506478641-otk6j842ist6785j1p6r5ctlt28sfd9r.apps.googleusercontent.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        email = findViewById(R.id.email)
        name = findViewById(R.id.name)
        password = findViewById(R.id.password)
        confirmPW = findViewById(R.id.confirm_PW)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance()

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverClientId)
            .requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions)


    }

    fun btn_SignIn(view: View) {

        if(email?.text!!.isEmpty() || name!!.text.isEmpty() || password!!.text.isEmpty() || confirmPW!!.text.isEmpty())
        {
            Toast.makeText(this, "Please enter all fields !",Toast.LENGTH_LONG).show()
        } else
        if(!password?.text.toString()!!.equals(confirmPW!!.text.toString()))
        {
          Toast.makeText(this,"Password doesn't match!",Toast.LENGTH_LONG).show()
        } else
        {
            Name = name!!.text.toString()
            signInwithEmail(email!!.text.toString(),password!!.text.toString())
            database();

        }

    }

    private fun database() {

       val dbRef = database?.getReference()?.child("Users Auth Info: ")?.push()

       dbRef?.child("Email")?.setValue(email?.text.toString())
        dbRef?.child("Password")?.setValue(password?.text.toString())
        dbRef?.child("Name")?.setValue(name?.text.toString())



    }

    fun Login_Page(view: View) {

        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)

    }
    fun signInwithEmail(email:String,password:String)
    {
        auth?.createUserWithEmailAndPassword(email,password)
            ?.addOnCompleteListener{ task->

                if(task.isSuccessful)
                {
                        Log.d("createEmailPW: ","Created: "+auth?.currentUser?.uid)

                        Toast.makeText(this,"Account created !",Toast.LENGTH_LONG).show()

                        Email = auth?.currentUser?.email.toString()

                        val intent = Intent(this,Home::class.java)
                        startActivity(intent)
                        finish()

                }

            }?.addOnFailureListener{ exc->
                Log.d("signup failed: ",exc.toString())

                    Toast.makeText(this,exc.message, Toast.LENGTH_LONG).show()

            }
    }

    fun google_SignIn(view: View) {

        val intent = googleSignInClient?.signInIntent
        startActivityForResult(intent,102)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==102 && resultCode == RESULT_OK)
        {
           val task = GoogleSignIn.getSignedInAccountFromIntent(data)
           val account = task.result
           Log.d("account: ",account.email.toString())

            val dbRef = FirebaseDatabase.getInstance().getReference().child("Google Sign In").push()

            dbRef.child("Email").setValue(account.email.toString())
            dbRef.child("Name").setValue(account.displayName.toString())

           FirebaseAuthGoogle(account.idToken!!,account)

        }

    }

    private fun FirebaseAuthGoogle(idToken : String,account: GoogleSignInAccount) {

        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth?.signInWithCredential(firebaseCredential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(ContentValues.TAG, "signInWithCredential:success")
                    val user = auth?.currentUser
                    Toast.makeText(this,"Successfully Login !",Toast.LENGTH_LONG).show()
                    Log.d("result_account: ",account.email.toString())

                    val intent = Intent(this,Home::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Google_failed: ", "signInWithCredential:failure", task.exception)
                }
            }

    }


}