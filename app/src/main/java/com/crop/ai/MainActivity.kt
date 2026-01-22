package com.crop.ai

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.crop.ai.ml.PlantDiseaseClassifier

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var txtResult: TextView
    private var imageUri: Uri? = null
    private lateinit var classifier: PlantDiseaseClassifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        imageView = findViewById(R.id.imageView)
        txtResult = findViewById(R.id.txtResult)

        // Initialize TFLite classifier
        classifier = PlantDiseaseClassifier(this)

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
            txtResult.text = "Image selected. Click Detect."
        }
    }

    private fun detectDisease() {
        if (imageUri == null) {
            Toast.makeText(this, "Please upload a crop image", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert image URI → Bitmap
        val bitmap: Bitmap =
            MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)

        // Run TFLite model
        val (label, confidence) = classifier.classify(bitmap)

        // Display result
        txtResult.text = """
            Result: $label
            Confidence: ${(confidence * 100).toInt()}%
        """.trimIndent()
    }
}
