package com.example.fuelefficiencypredictor

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.utils.widget.MotionButton
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import de.hdodenhof.circleimageview.CircleImageView
import android.webkit.MimeTypeMap

import android.content.ContentResolver
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.opengl.Visibility
import android.util.Log
import android.view.View
import android.widget.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import java.lang.Exception
import kotlin.coroutines.Continuation
import androidx.annotation.NonNull

import com.google.android.gms.tasks.OnFailureListener
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.net.URL


class Profile : AppCompatActivity() {

    lateinit var profileImageView: CircleImageView
    lateinit var profile_Name:EditText
    lateinit var profile_Email:TextView
    lateinit var profile_Country:EditText
    lateinit var profile_VehicleCount:EditText
    lateinit var profile_Save:MotionButton
    var imageUri: Uri?= null

    lateinit var pb:ProgressBar

    lateinit var auth:FirebaseAuth
    var imageUrl:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImageView = findViewById(R.id.profile_Image)
        profile_Name = findViewById(R.id.profile_Name)
        profile_Email = findViewById(R.id.profile_Email)
        profile_Country = findViewById(R.id.profile_Country)
        profile_VehicleCount = findViewById(R.id.profile_VehicleCount)
        profile_Save = findViewById(R.id.profile_Save)
        pb = findViewById(R.id.progressBar)
        auth = FirebaseAuth.getInstance()

        val actionBar = supportActionBar
        actionBar?.setTitle("Profile")
        actionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#094D1C")))

        profileImageView.setOnClickListener {

            if(imageUri!=null)
            {
                imageUri = null
            }
            CropImage.activity(imageUri).start(this)

        }

        profile_Save.setOnClickListener{

            if(profile_Name.text.isNotEmpty())
            {
                FirebaseDatabase.getInstance().getReference().child("Profile") .child(auth.currentUser!!.uid).
                child("Name").child("name").setValue(profile_Name.text.toString())

                profile_Name.setHint(profile_Name.text.toString())
            }

             if(profile_Country.text.isNotEmpty())
            {
                FirebaseDatabase.getInstance().getReference().child("Profile").child(auth.currentUser!!.uid)
                    .child("Country").child("country").setValue(profile_Country.text.toString())

                profile_Country.setHint("Country: "+profile_Country.text.toString())


            }

            if(profile_VehicleCount.text.isNotEmpty())
            {
                FirebaseDatabase.getInstance().getReference().child("Profile").child(auth.currentUser!!.uid).
                   child("Vehicle Count").child("vehicleCount").setValue(profile_VehicleCount.text.toString())

                profile_VehicleCount.setHint("Number of vehicles own: "+profile_VehicleCount.text.toString())


            }

//                 Toast.makeText(this,"Updated !",Toast.LENGTH_LONG).show()
            Snackbar.make(it,"Profile updated",Snackbar.LENGTH_LONG).show()

        }

        profile_Name.setOnClickListener {
            Toast.makeText(this,"Name cannot be changed !",Toast.LENGTH_LONG).show()

        }

        profile_Email.setOnClickListener {
            Toast.makeText(this,"Email cannot be changed !",Toast.LENGTH_LONG).show()
        }

        getUserInfo();



    }

    private fun getUserInfo() {

        val email = auth.currentUser?.email.toString()
        profile_Email.setText(email)

        var name = ""
        for(i in email.toCharArray())
        {
            if(i=='@')
            {
                break
            }
            name +=i
        }
        profile_Name.setHint(name)

        FirebaseDatabase.getInstance().getReference().child("Profile").child(auth.currentUser!!.uid)
            .child("Name").addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    var name = ""
                    for(snap in snapshot.children)
                    {
                        name = snap.value.toString()
                    }

                    if(name!="")
                    {
                        profile_Name.setHint(name.toString())
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        FirebaseDatabase.getInstance().getReference().child("Profile").child(auth.currentUser!!.uid)
            .child("Country").addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    var country = ""
                    for(snap in snapshot.children)
                    {
                        country = snap.value.toString()
                    }

                    if(country!="")
                    {
                        profile_Country.setHint(country.toString())
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        FirebaseDatabase.getInstance().getReference().child("Profile").child(auth.currentUser!!.uid)
            .child("Vehicle Count").addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    var count = ""
                    for(snap in snapshot.children)
                    {
                        count = snap.value.toString()
                    }

                    if(count!="")
                    {
                        profile_VehicleCount.setHint(count.toString())
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK)
        {
            imageUri = CropImage.getActivityResult(data).uri
            uploading()
//            profileImageView.setImageURI(imageUri)

        } else
        {
            Toast.makeText(this,"Couldn't upload please try again !",Toast.LENGTH_LONG).show()
        }

    }


    private fun uploading() {

        pb.visibility = View.VISIBLE

        if(imageUri!=null)
        {
            val storageRef = FirebaseStorage.getInstance().getReference().child("Profile Image")
                    .child(auth.currentUser!!.uid).child(System.currentTimeMillis().toString()+".jpeg")

            val uploadTask = storageRef.putFile(imageUri!!)



            uploadTask.addOnSuccessListener { task->


                val downloadUri = task.storage.downloadUrl.addOnCompleteListener { uri ->
                    imageUrl = uri.result.toString()

                    Log.d("url_uploading", imageUrl.toString())


                    FirebaseDatabase.getInstance().getReference().child("Image Uri")
                        .child(auth.currentUser!!.uid.toString())
                        .child("imageUri").setValue(imageUrl)

                    Toast.makeText(this,"Uploading please wait! ",Toast.LENGTH_LONG).show()
                    pb.visibility = View.GONE
//                    profileImageView.setImageURI(imageUri)

                }


            }.

            addOnFailureListener {

                Toast.makeText(this, "Couldn't upload " + it.message, Toast.LENGTH_LONG).show()

            }

        }




    }

    override fun onResume() {
        super.onResume()

        FirebaseDatabase.getInstance().reference.child("Image Uri").child(auth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    for(snap in snapshot.children)
                    {
                        val url = snap.value
                        Log.d("url",url.toString())

                        Picasso.get().load(Uri.parse(url.toString())).placeholder(R.drawable.person_dark).into(profileImageView)

                    }



                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("url: ",error.toString())
                }
            })


    }

    private fun funToast() {

        Toast.makeText(this,"Updated !",Toast.LENGTH_LONG).show()

    }

}

