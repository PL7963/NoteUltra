package com.coolkie.noteultra.utils

import android.content.Context
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder.TextEmbedderOptions

class EmbeddingUtils(
    private val context: Context
) {
    private var textEmbedder: TextEmbedder? = null

    init {
        setupTextEmbedder()
    }


    fun setupTextEmbedder() {
        var embeddingModel = "models/embedding/bert_embedder.tflite"
        val baseOptions = BaseOptions
            .builder()
            .setModelAssetPath(embeddingModel)
            .setDelegate(Delegate.GPU)
            .build()
        val optionsBuilder =
            TextEmbedderOptions.builder().setBaseOptions(baseOptions)
        val options = optionsBuilder.build()
        textEmbedder = TextEmbedder.createFromOptions(context, options)
    }

    fun embedText(input: String): FloatArray? {
        textEmbedder?.let {
            val embeddedResult = it.embed(input).embeddingResult().embeddings().first()
            return embeddedResult.floatEmbedding()
        }
        return null
    }
}
