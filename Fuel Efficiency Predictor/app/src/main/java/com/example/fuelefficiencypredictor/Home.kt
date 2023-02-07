package com.example.fuelefficiencypredictor

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.sql.Time
import java.sql.Timestamp
import java.time.LocalDateTime






class Home : AppCompatActivity() {

    lateinit var result_Tv:TextView
    lateinit var spinner_Unit: Spinner
    lateinit var model_Year:EditText
    lateinit var horse_Power:EditText
    lateinit var acceleration:EditText
    lateinit var cylinders:EditText
    lateinit var weights:EditText
    lateinit var displacement:EditText
    lateinit var spinner_Origin:Spinner
    lateinit var btn_Predict:Button
    lateinit var origin_Tv:TextView
    lateinit var scrollView: ScrollView


    var result:String? = null
    var databaseReference:DatabaseReference?=null
    var firebaseAuth:FirebaseAuth?=null

    val apkLink = "https://i.diawi.com/9q4bgV"

    var actionBar:ActionBar?= null

    var interpreter:Interpreter?= null

    var mean = floatArrayOf(5.477707f, 195.318471f, 104.869427f, 2990.251592f, 15.559236f, 75.898089f, 0.624204f, 0.178344f, 0.197452f)
    var std = floatArrayOf(1.699788f, 104.331589f, 38.096214f, 843.898596f, 2.789230f, 3.675642f, 0.485101f, 0.383413f, 0.398712f)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        result_Tv = findViewById(R.id.result_Tv)
        spinner_Unit = findViewById(R.id.spinner_unit)
        model_Year = findViewById(R.id.model_Year)
        horse_Power = findViewById(R.id.horse_Power)
        acceleration = findViewById(R.id.acceleration)
        cylinders = findViewById(R.id.cylinders)
        weights = findViewById(R.id.weights)
        displacement = findViewById(R.id.displacement)
        spinner_Origin = findViewById(R.id.origin_Spinner)
//        origin_Tv = findViewById(R.id.origin_Tv)
        btn_Predict = findViewById(R.id.btn_Predict)
        scrollView = findViewById(R.id.scroll)

        actionBar = supportActionBar
        actionBar?.title = "Fuel Efficiency"
//        actionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#C0C0C0")))
        actionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#094D1C")))




//        actionBar?.setIcon(R.drawable.splash)
//        actionBar!!.setDisplayUseLogoEnabled(true)
//        actionBar!!.setDisplayShowHomeEnabled(true)

        databaseReference = FirebaseDatabase.getInstance().getReference()
        firebaseAuth = FirebaseAuth.getInstance()

        interpreter = Interpreter(loadModelFile()!!)


        val arrayAdapter_Origin = ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,
           arrayOf("USA","Europe","Japan"))
        spinner_Origin.adapter = arrayAdapter_Origin

        val arrayAdapter_Unit = ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,
        arrayOf("MPG(Miles Per Gallon)","KM\\L(Kilometre Per Litre)"))
        spinner_Unit.adapter = arrayAdapter_Unit


        btn_Predict.setOnClickListener {

            if(model_Year.text.isEmpty() || horse_Power.text.isEmpty() || acceleration.text.isEmpty() || cylinders.text
                    .isEmpty() || weights.text.isEmpty() || displacement.text.isEmpty() )
            {
                Toast.makeText(this,"Please fill all the details !",Toast.LENGTH_LONG).show()
            } else
            {
                try {
                    var input = Array(1){ FloatArray(9) }

                    input[0][0] = (cylinders.text.toString().toFloat() - mean[0])/std[0]
                    input[0][1] = (displacement.text.toString().toFloat() - mean[1])/std[1]
                    input[0][2] = (horse_Power.text.toString().toFloat() - mean[2])/std[2]
                    input[0][3] = (weights.text.toString().toFloat() - mean[3])/std[3]
                    input[0][4] = (acceleration.text.toString().toFloat() - mean[4])/std[4]
                    input[0][5] = (model_Year.text.toString().toFloat() - mean[5])/std[5]

                    if(spinner_Origin.selectedItemPosition == 0)
                    {
                        input[0][6] = (1- mean[6])/std[6]
                        input[0][7] = (0- mean[7])/std[7]
                        input[0][8] = (0- mean[8])/std[8]




                    } else if(spinner_Origin.selectedItemPosition == 1 )
                    {
                        input[0][6] = (0- mean[6])/std[6]
                        input[0][7] = (1- mean[7])/std[7]
                        input[0][8] = (0- mean[8])/std[8]



                    } else if(spinner_Origin.selectedItemPosition == 2)
                    {
                        input[0][6] = (0- mean[6])/std[6]
                        input[0][7] = (0- mean[7])/std[7]
                        input[0][8] = (1- mean[8])/std[8]



                    }

                    var output = Array(1){ FloatArray(1)}

                    interpreter!!.run(input,output)

                    if(spinner_Unit.selectedItemPosition == 0)
                    {
                        result = String.format("%.2f",output[0][0])
                        result_Tv.text = result+"  MPG"
                    } else
                    {
                        result  = (String.format("%.2f",output[0][0]!! * 0.425))
                        result_Tv.text = result+"  KM/L"

                    }


                    scrollView.scrollTo(0,0)

                    Toast.makeText(this,"Predicted value for vehicle is "+result_Tv.text,Toast.LENGTH_LONG).show()

                    database();

                }

                catch (e:Exception)
                {
                   Toast.makeText(this,"Data format is not correct..please check!",Toast.LENGTH_LONG).show()
                   Log.d("exc",e.toString())
                }


            }

        }


        spinner_Unit.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {



                if(position == 1 && result!=null )
                {

                    result  = (String.format("%.2f",result?.toFloat()!! * 0.425))
                    result_Tv.text = result+"  KM/L"
                }
                 else if(position == 0 && result!=null)
                {
                    result  = (String.format("%.2f",result?.toFloat()!! * 2.352) ).toString()
                    result_Tv.text = result+"  MPG"
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // can leave this empty
            }
        })


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.action_bar,menu)

        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId)
        {
            R.id.refresh->{
                horse_Power.text.clear()
                model_Year.text.clear()
                acceleration.text.clear()
                cylinders.text.clear()
                weights.text.clear()
                displacement.text.clear()

                result_Tv.text = "Enter your vehicle details"
                Toast.makeText(this,"Refreshed",Toast.LENGTH_LONG).show()
            }
            R.id.share -> {
                if(result!=null)
                {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent?.putExtra(
                        Intent.EXTRA_TEXT,
                        "Hey! Efficiency of my vehicle has been predicted as " + result_Tv.text+
                                " in the Fuel Efficiency Predictor App "+apkLink+"\nDownload and try!"
                    )

                    val chooser = Intent.createChooser(intent,"Share with your friends !")
                    startActivity(chooser)
                } else
                {
                    Toast.makeText(this,"Nothing to share !",Toast.LENGTH_SHORT).show()
                }

            }

            R.id.profile -> {
                val intent = Intent(this,Profile::class.java)
                startActivity(intent)
            }

            R.id.exit-> {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("Exit")
                dialog.setMessage("Do you want to close the application?")

                dialog.setPositiveButton("Yes",DialogInterface.OnClickListener{dialog, which ->
                    System.exit(0)
                })

                dialog.setNegativeButton("No",DialogInterface.OnClickListener{dialog, which ->
                    dialog.cancel()
                })

                dialog.create()
                dialog.show()

             }

            R.id.sign_out -> { firebaseAuth?.signOut()
            val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()
            }



        }

        return super.onOptionsItemSelected(item)
    }




    private fun database() {

        var time = LocalDateTime.now()

        val dbPath =  databaseReference?.child("Vehicle Info: ")?.child(firebaseAuth?.currentUser?.uid.toString())

       dbPath?.child("Model Year")?.setValue(model_Year.text.toString())
        dbPath?.child("Horse Year")?.setValue(horse_Power.text.toString())
        dbPath?.child("Acceleration")?.setValue(acceleration.text.toString())
        dbPath?.child("Cylinders")?.setValue(cylinders.text.toString())
        dbPath?.child("Weight")?.setValue(weights.text.toString())
        dbPath?.child("Displacement")?.setValue(displacement.text.toString())

        if(spinner_Origin.selectedItemPosition == 0)
        {
            dbPath?.child("Origin")?.setValue("USA")
        } else if(spinner_Origin.selectedItemPosition == 1 )
        {
            dbPath?.child("Origin")?.setValue("Europe")

        } else if(spinner_Origin.selectedItemPosition == 2)
        {
            dbPath?.child("Origin")?.setValue("Japan")
        }

    }

    fun current_Unit(pos: Int)
    {
        if(pos == 1)
        {
            Toast.makeText(this,"Unit has set to Km/l(Kilometre Per Litre)",Toast.LENGTH_LONG).show()
        } else
        {
            Toast.makeText(this,"Unit has set to MPG(Miles Per Gallon)",Toast.LENGTH_LONG).show()
        }

    }



    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer? {
        val assetFileDescriptor = this.assets.openFd("automobile.tflite")
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val length = assetFileDescriptor.length
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, length)
    }



}