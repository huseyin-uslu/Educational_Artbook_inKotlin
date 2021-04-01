package com.firstprojects.artbook_kotl

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream

class UploadAndShowActivity2 : AppCompatActivity() {
    lateinit var bitmap: Bitmap
    lateinit var nameOfPainting: EditText
    lateinit var nameOfArtist: EditText
    lateinit var dateText: EditText
    lateinit var selectImage: ImageView
    lateinit var getIntent: Intent
    lateinit var sharedPreferences: SharedPreferences

    //database
    lateinit var sqliteDatabase: SQLiteDatabase
    lateinit var sqliteStatement: SQLiteStatement

    //showscreen
    lateinit var textView_paintname: TextView
    lateinit var textView_artistname: TextView
    lateinit var textView_date: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //getDataFromMainActivity
        getIntent = intent
        val checkInt = getIntent.getIntExtra("check", 0)
        val idQueryIndex = getIntent.getIntExtra("id", 0) + 1

        //database initilization
        sqliteDatabase = openOrCreateDatabase("Images", MODE_PRIVATE, null)
        sqliteDatabase.execSQL("CREATE TABLE IF NOT EXISTS images(id INTEGER PRIMARY KEY,artname VANCHAR,artistname VANCHAR,date VANCHAR,image BLOB)")
        sharedPreferences = getSharedPreferences("com.firstprojects.artbook_kotl", MODE_PRIVATE)

        if (checkInt == 0) {
            setContentView(R.layout.activity_upload_and_show2)
            //initilization
            nameOfPainting = findViewById(R.id.showuploadactivity_edittext_nameofpainting)
            nameOfArtist = findViewById(R.id.showuploadactivity_edittext_nameofartist)
            dateText = findViewById(R.id.showuploadactivity_edittext_date)
            selectImage = findViewById(R.id.imageView)
            selectImage.isClickable = true
            sharedPreferences.edit().putBoolean("refresh", false).apply()
            sharedPreferences.edit().putBoolean("actalive",true).apply()


        } else if (checkInt == 1) {
            setContentView(R.layout.activity_showthepicture_userchoose)
            textView_artistname = findViewById(R.id.showScreen_artistname)
            textView_paintname = findViewById(R.id.showScreen_paintingname)
            textView_date = findViewById(R.id.showScreen_date)
            selectImage = findViewById(R.id.imageView)
            selectImage.isClickable = false
            sharedPreferences.edit().putInt("check", 0).apply()
            sharedPreferences.edit().putBoolean("refresh", false).apply()
            sharedPreferences.edit().putBoolean("actalive",true).apply()


            //databaseGetData
            try {
                val arrayId: Array<out String> = arrayOf(idQueryIndex.toString())
                val queryRaw: Cursor = sqliteDatabase.rawQuery("SELECT * FROM images WHERE id = ?", arrayId)
                val artistNameIndex = queryRaw.getColumnIndex("artistname")
                val paintNameIndex = queryRaw.getColumnIndex("artname")
                val dateIndex = queryRaw.getColumnIndex("date")
                val bitmapIndex = queryRaw.getColumnIndex("image")
                while (queryRaw.moveToNext()) {
                    textView_artistname.text = queryRaw.getString(artistNameIndex)
                    textView_date.text = queryRaw.getString(dateIndex)
                    textView_paintname.text = queryRaw.getString(paintNameIndex)
                    //bitmap Decode
                    val bytes: ByteArray = queryRaw.getBlob(bitmapIndex)
                    val bitmap: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    selectImage.setImageBitmap(bitmap)
                }
                queryRaw.close()

            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR = " + e.localizedMessage)
            }

        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            val choosenData: Uri = data.data!!
            if (Build.VERSION.SDK_INT >= 28) {

                val source =
                        ImageDecoder.createSource(this@UploadAndShowActivity2.contentResolver, choosenData)
                bitmap = ImageDecoder.decodeBitmap(source)
                bitmap = makingSmallerBitmap(bitmap, 400)
                selectImage.setImageBitmap(bitmap)
                selectImage.isClickable = false


            } else {
                try {
                    bitmap =
                            MediaStore.Images.Media.getBitmap(
                                    this@UploadAndShowActivity2.contentResolver,
                                    choosenData
                            )
                    bitmap = makingSmallerBitmap(bitmap, 400)
                    selectImage.setImageBitmap(bitmap)
                    selectImage.isClickable = false
                } catch (e: Exception) {
                    e.printStackTrace()
                    println(e.localizedMessage)
                }
            }


        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (permissions[0] == READ_EXTERNAL_STORAGE) {
                    val intent: Intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, 2)
                }
            }
        }
    }


    fun imageClick(view: View) {

        sharedPreferences.edit().putBoolean("actalive",false).apply()
        if (ContextCompat.checkSelfPermission(this@UploadAndShowActivity2,
                        READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {/////////////////////////////////////////////////////////////////////////////////////////////////
            ActivityCompat.requestPermissions(this@UploadAndShowActivity2, arrayOf(READ_EXTERNAL_STORAGE), 1)
        } else {
            val intent: Intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 2)
        }
    }

    fun makingSmallerBitmap(bitmap: Bitmap, imageSize: Int): Bitmap {

        var height: Int = bitmap.height
        var width: Int = bitmap.width
        val rate: Float = (height.toFloat() / width.toFloat())
        if (rate > 1) {

            height = imageSize
            width = (imageSize / rate).toInt()

        } else {
            height = (imageSize * rate).toInt()
            width = imageSize
        }
        return bitmap.scale(width, height, true)
    }

    fun saveButton(view: View) {
        if (nameOfPainting.text.toString().isNotEmpty() && nameOfArtist.text.toString().isNotEmpty() && dateText.text.isNotEmpty() && !selectImage.isClickable) {
            sharedPreferences.edit().putInt("check", 1).apply() //changed for notify listView
            sharedPreferences.edit().putBoolean("refresh", true).apply()
            //getData

            val paintingNameString = nameOfPainting.text.toString()
            val nameOfArtistString = nameOfArtist.text.toString()
            val dateText = dateText.text.toString()
            val byteOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, byteOutputStream)
            val bytes = byteOutputStream.toByteArray()

            //putData
            try {
                val stringIntoSqlite = "INSERT INTO images(artname,artistname,date,image) VALUES(?,?,?,?)"
                sqliteStatement = sqliteDatabase.compileStatement(stringIntoSqlite)
                sqliteStatement.bindString(1, paintingNameString)
                sqliteStatement.bindString(2, nameOfArtistString)
                sqliteStatement.bindString(3, dateText)
                sqliteStatement.bindBlob(4, bytes)
                sqliteStatement.execute()
                println("SUCCESFULL")

            } catch (e: Exception) {
                e.printStackTrace()
                println(e.localizedMessage)
            }

            finish()
            val intent = Intent(this@UploadAndShowActivity2,MainActivity::class.java)
            sharedPreferences.edit().putBoolean("actalive",true).apply()
                startActivity(intent)

        } else {
            Toast.makeText(this@UploadAndShowActivity2, "PLEASE FILL ALL THE BLANKS!!", Toast.LENGTH_SHORT).show()
        }

    }

   override fun onPause() {
        super.onPause()
        if(sharedPreferences.getBoolean("actalive",false)) {
     val intent : Intent = Intent(this@UploadAndShowActivity2,MainActivity::class.java)
            startActivity(intent)
            finish()
      sharedPreferences.edit().putBoolean("actalive",false).apply()
        }else {
            Toast.makeText(this@UploadAndShowActivity2,"you're choosing a image for uploading...!",Toast.LENGTH_LONG).show()
        }
    }
}



