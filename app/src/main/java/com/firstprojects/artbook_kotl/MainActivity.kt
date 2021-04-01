package com.firstprojects.artbook_kotl

import android.app.DownloadManager
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView


class MainActivity : AppCompatActivity() {
    lateinit var listView : ListView
    lateinit var paintingsNameArray : ArrayList<String>
    lateinit var arrayAdapter : ArrayAdapter<String>
    lateinit var sqliteDatabase : SQLiteDatabase
    lateinit var sharedPreferences : SharedPreferences


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = MenuInflater(this)
        menuInflater.inflate(R.menu.menu_activity,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.uploadSaveScreen) {
            val intent : Intent = Intent(this@MainActivity,UploadAndShowActivity2::class.java)
            intent.putExtra("check",0)
            finish()
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences("com.firstprojects.artbook_kotl", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("refresh",true).apply()
        //initilization

        listView = findViewById(R.id.listView)
        paintingsNameArray = arrayListOf()
        arrayAdapter = ArrayAdapter(this@MainActivity,android.R.layout.simple_list_item_1,paintingsNameArray)
        sqliteDatabase = openOrCreateDatabase("Images",MODE_PRIVATE,null)


        //sqlite deleted process
        //sqliteDatabase.delete("images",null,null)

    }

    override fun onStart() {
        super.onStart()

       if(sharedPreferences.getBoolean("refresh",false)){
           try {
               val queryInDatabase = sqliteDatabase.rawQuery("SELECT * FROM images",null)
               val nameIndex = queryInDatabase.getColumnIndex("artname")


               while (queryInDatabase.moveToNext()) {
                   paintingsNameArray.add(queryInDatabase.getString(nameIndex))

                   if(sharedPreferences.getInt("check",0) == 1) {
                       arrayAdapter.notifyDataSetChanged()
                       sharedPreferences.edit().putInt("check", 0).apply()
                   }

               }
               queryInDatabase.close()
               sharedPreferences.edit().putBoolean("refresh",false).apply()
               listView.adapter = arrayAdapter

           }catch (e : Exception) {
               e.printStackTrace()
               println("ERROR : " + e.localizedMessage)
           }
       }


    }

    override fun onResume() {
        super.onResume()
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val intent : Intent = Intent(this@MainActivity,UploadAndShowActivity2::class.java)
            intent.putExtra("check",1)
            intent.putExtra("id",position)
            startActivity(intent)
            finish()
        }
    }

}