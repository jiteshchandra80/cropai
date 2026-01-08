package com.crop.ai

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var txtResult: TextView
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        imageView = findViewById(R.id.imageView)
        txtResult = findViewById(R.id.txtResult)

        findViewById<Button>(R.id.btnUpload).setOnClickListener {
            pickImage()
        }

        findViewById<Button>(R.id.btnDetect).setOnClickListener {
            detectDisease()
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imageView.setImageURI(imageUri)
        }
    }

    private fun detectDisease() {
        if (imageUri == null) {
            Toast.makeText(this, "Please upload a crop image", Toast.LENGTH_SHORT).show()
            return
        }

        // ML model output (placeholder)
        txtResult.text = "Disease Detected: Leaf Blight 🌿"
    }
}
