package com.coolkie.noteultra.utils.asr

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.util.Log
import androidx.core.app.ActivityCompat
import android.content.Context
import android.media.AudioFormat
import android.media.MediaRecorder
import kotlin.concurrent.thread
import com.k2fsa.sherpa.ncnn.SherpaNcnn
import com.k2fsa.sherpa.ncnn.getFeatureExtractorConfig
import com.k2fsa.sherpa.ncnn.getModelConfig
import com.k2fsa.sherpa.ncnn.getDecoderConfig
import com.k2fsa.sherpa.ncnn.RecognizerConfig

class VoiceRecognition(context: Context) {
  private val permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

  private lateinit var model: SherpaNcnn
  private var audioRecord: AudioRecord? = null
  private var recordingThread: Thread? = null

  private val sampleRateInHz = 16000
  private val channelConfig = AudioFormat.CHANNEL_IN_MONO
  private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

  @Volatile
  private var isRecording: Boolean = false

  fun startRecording(context: Context) {
    if (!isRecording) {
      val ret = initMicrophone(context)
      if (!ret) {
        return
      }
      audioRecord!!.startRecording()
      isRecording = true
      recordingThread = thread(true) {
        model.reset(true)
        processSamples()
      }
    } else {
      isRecording = false
      audioRecord!!.stop()
      audioRecord!!.release()
      audioRecord = null
    }
  }

  private fun processSamples() {
    val interval = 0.1
    val bufferSize = (interval * sampleRateInHz).toInt() // in samples
    val buffer = ShortArray(bufferSize)
    var fullText = ""
    Log.d("SherpaNcnn", "${isRecording}")
    while (isRecording) {
      val ret = audioRecord?.read(buffer, 0, buffer.size)
      if (ret != null && ret > 0) {
        val samples = FloatArray(ret) { buffer[it] / 32768.0f }
        model.acceptSamples(samples)
        while (model.isReady()) {
          model.decode()
        }
        val isEndpoint = model.isEndpoint()
        val text = model.text

        if (text.isNotBlank()) {
          fullText = "${fullText} ${text}"
        }
        if (isEndpoint) {
          model.reset()
          Log.d("SherpaNcnn", "FullText: $fullText")
          fullText = ""
        }
      }
    }
  }

  private fun initMicrophone(context: Context): Boolean {
    if (ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      ActivityCompat.requestPermissions(context as Activity, permissions, 200)
      return false
    }

    val numBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)

    audioRecord = AudioRecord(
      MediaRecorder.AudioSource.MIC,
      sampleRateInHz,
      channelConfig,
      audioFormat,
      numBytes * 2 // a sample has two bytes as we are using 16-bit PCM
    )
    return true
  }

  fun initModel(context: Context) {
    val featConfig = getFeatureExtractorConfig(
      sampleRate = 16000.0f,
      featureDim = 80
    )
    //Please change the argument "type" if you use a different model
    val modelConfig = getModelConfig(type = 1, useGPU = true)!!
    val decoderConfig = getDecoderConfig(method = "greedy_search", numActivePaths = 4)

    val config = RecognizerConfig(
      featConfig = featConfig,
      modelConfig = modelConfig,
      decoderConfig = decoderConfig,
      enableEndpoint = true,
      rule1MinTrailingSilence = 2.0f,
      rule2MinTrailingSilence = 0.8f,
      rule3MinUtteranceLength = 20.0f,
    )

    model = SherpaNcnn(
      assetManager = context.assets,
      config = config
    )
  }

}
