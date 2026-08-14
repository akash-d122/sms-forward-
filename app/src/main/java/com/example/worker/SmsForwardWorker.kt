package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.helper.SmsForwardHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsForwardWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "SmsForwardWorker"
        const val KEY_SENDER = "sender"
        const val KEY_BODY = "body"
        const val KEY_TIMESTAMP = "timestamp"
        const val KEY_SIM_SLOT = "simSlot"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val sender = inputData.getString(KEY_SENDER) ?: return@withContext Result.failure()
        val body = inputData.getString(KEY_BODY) ?: return@withContext Result.failure()
        val timestamp = inputData.getLong(KEY_TIMESTAMP, System.currentTimeMillis())
        val simSlot = inputData.getInt(KEY_SIM_SLOT, -1)

        Log.d(TAG, "WorkManager background invocation triggered.")

        try {
            SmsForwardHelper.processAndForward(
                context = context,
                sender = sender,
                body = body,
                timestamp = timestamp,
                simSlot = simSlot
            )
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error matching and forwarding in worker: ${e.message}", e)
            Result.failure()
        }
    }
}
