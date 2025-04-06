package com.coolkie.noteultra.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.coolkie.noteultra.data.NoteViewModel
import com.coolkie.noteultra.utils.LlmInferenceUtils
import com.coolkie.noteultra.utils.VectorUtils

val LocalNoteViewModel = staticCompositionLocalOf<NoteViewModel> {
    error("NoteViewModel not provided")
}

val LocalLlmInstance = staticCompositionLocalOf<LlmInferenceUtils> {
    error("LlmInstance not provided")
}

val LocalVectorUtils = staticCompositionLocalOf<VectorUtils> {
    error("VectorUtils not provided")
}