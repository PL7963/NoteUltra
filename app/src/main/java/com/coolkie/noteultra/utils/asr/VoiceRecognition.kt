package com.coolkie.noteultra.utils.asr

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.coolkie.noteultra.utils.EmbeddingUtils
import com.coolkie.noteultra.utils.VectorUtils
import com.k2fsa.sherpa.ncnn.RecognizerConfig
import com.k2fsa.sherpa.ncnn.SherpaNcnn
import com.k2fsa.sherpa.ncnn.getDecoderConfig
import com.k2fsa.sherpa.ncnn.getFeatureExtractorConfig
import com.k2fsa.sherpa.ncnn.getModelConfig
import kotlin.concurrent.thread

class VoiceRecognition(vectorUtils: VectorUtils, embeddingUtils: EmbeddingUtils) {
    private lateinit var model: SherpaNcnn
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null

    private val sampleRateInHz = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private val embeddingUtil = embeddingUtils
    private val vectorUtil = vectorUtils

    @Volatile
    private var isRecording: Boolean = false

    fun startRecording() {
        if (!isRecording) {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRateInHz,
                channelConfig,
                audioFormat,
                AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat) * 2
            )
            audioRecord!!.startRecording()
            isRecording = true
            recordingThread = thread(true) {
                model.reset(true)
                processSamples()
            }
        }
    }

    fun stopRecording() {
        if (isRecording) {
            isRecording = false
            audioRecord!!.stop()
            audioRecord!!.release()
            audioRecord = null
        }
    }

    private fun processSamples() {
        val buffer = ShortArray((0.3 * sampleRateInHz).toInt())

        while (isRecording) {
            val ret = audioRecord?.read(buffer, 0, buffer.size)

            if (ret != null && ret > 0) {
                val samples = FloatArray(ret) { buffer[it] / 32768.0f }

                model.acceptSamples(samples)

                while (model.isReady()) {
                    model.decode()
                }
                if (model.isEndpoint()) {
                    model.reset()
                    model.text.takeIf { it.isNotBlank() }?.let { text ->
                        embeddingUtil.embedText(text)?.let { vectorUtil.store(text, it) }
                    }
                }
            }
        }
    }

    fun initModel(context: Context) {
        val featConfig = getFeatureExtractorConfig(
            sampleRate = 16000.0f,
            featureDim = 80
        )
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

        model = SherpaNcnn(config, context.assets)
    }
}