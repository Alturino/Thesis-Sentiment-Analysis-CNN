package com.onirutla.sentiment_analysis_cnn.module

import android.content.Context
import com.onirutla.sentiment_analysis_cnn.domain.data.model.SentimentLabel
import com.onirutla.sentiment_analysis_cnn.domain.usecase.Analyzer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil

@Module
@InstallIn(ActivityComponent::class)
object ClassifierModule {

    @Provides
    @ActivityScoped
    fun provideTfLite(
        @ApplicationContext context: Context,
    ): Interpreter = Interpreter(
        FileUtil.loadMappedFile(
            context,
            Analyzer.WORD2VEC_CNN_MODEL_FILENAME
        )
    )

    @Provides
    @ActivityScoped
    fun provideLabels(
        @ApplicationContext context: Context,
    ): SentimentLabel = SentimentLabel(
        FileUtil.loadLabels(
            context,
            Analyzer.WORD2VEC_CNN_LABEL_FILENAME
        )
    )
}