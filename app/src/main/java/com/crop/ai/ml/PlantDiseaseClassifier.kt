package com.crop.ai.ml

import android.content.Context
import android.graphics.Bitmap
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class PlantDiseaseClassifier(context: Context) {

    private val interpreter: Interpreter
    private val labels: List<String>

    init {
        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }
        interpreter = Interpreter(loadModelFile(context), options)
        labels = loadLabels(context)
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fd = context.assets.openFd("disease.tflite")
        val inputStream = FileInputStream(fd.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fd.startOffset,
            fd.declaredLength
        )
    }

    private fun loadLabels(context: Context): List<String> {
        val json = context.assets.open("class_labels.json")
            .bufferedReader().use { it.readText() }

        val jsonObject = JSONObject(json)

        val labelMap = mutableMapOf<String, Int>()

        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            labelMap[key] = jsonObject.getInt(key)
        }

        return labelMap
            .toList()
            .sortedBy { it.second }   // sort by index
            .map { it.first }         // return label names
    }


    fun classify(bitmap: Bitmap): Pair<String, Float> {

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        val tensorImage = TensorImage.fromBitmap(bitmap)
        val processedImage = imageProcessor.process(tensorImage)

        val outputBuffer = TensorBuffer.createFixedSize(
            intArrayOf(1, labels.size),
            org.tensorflow.lite.DataType.FLOAT32
        )

        interpreter.run(processedImage.buffer, outputBuffer.buffer)

        val scores = outputBuffer.floatArray
        val maxIndex = scores.indices.maxBy { scores[it] }

        return labels[maxIndex] to scores[maxIndex]
    }
}
