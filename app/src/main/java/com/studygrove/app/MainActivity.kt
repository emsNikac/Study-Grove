package com.studygrove.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.studygrove.app.ui.StudyGroveApp
import com.studygrove.app.ui.StudyGroveViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: StudyGroveViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyGroveApp(viewModel)
        }
    }
}
