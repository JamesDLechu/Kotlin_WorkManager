package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI
import com.example.background.R

class BlurWorker(context: Context, params: WorkerParameters): Worker(context, params) {
    override fun doWork(): Result {
        val appContext= applicationContext

        val resourceUri= inputData.getString(KEY_IMAGE_URI)

        makeStatusNotification("Comenzando proceso de blurring...", appContext)
        sleep()

        return try {
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e("BlurWorker", "Invalid input uri")
                throw IllegalArgumentException("Invalid input uri")
            }

            val resolver = appContext.contentResolver

            val picture = BitmapFactory.decodeStream(
                resolver.openInputStream(Uri.parse(resourceUri)))

            val tempFile= blurBitmap(picture, appContext)
            val localUri= writeBitmapToFile(appContext, tempFile)

            makeStatusNotification(localUri.toString(), appContext)

            val outputData= workDataOf(KEY_IMAGE_URI to localUri.toString())

            Result.success(outputData)
        }catch (throwable: Throwable){
            Log.e("BlurWorker", "Error applying blur")
            Result.failure()
        }
    }
}