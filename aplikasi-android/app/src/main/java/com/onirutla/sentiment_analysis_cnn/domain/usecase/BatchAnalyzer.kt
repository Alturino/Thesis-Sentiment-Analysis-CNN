package com.onirutla.sentiment_analysis_cnn.domain.usecase

import android.util.Log
import com.onirutla.sentiment_analysis_cnn.domain.data.model.AnalyzerResult
import com.onirutla.sentiment_analysis_cnn.domain.data.model.CM
import com.onirutla.sentiment_analysis_cnn.domain.data.model.Sentiment
import javax.inject.Inject

class BatchAnalyzer @Inject constructor(
    private val analyzer: Analyzer,
) {
    private suspend fun classify(sentiments: List<Sentiment>): List<AnalyzerResult> {
        val classificationResults = sentiments.map { sentiment ->
            val classificationResult = analyzer.classify(sentiment.content)

            classificationResult.copy(
                index = sentiment.index,
                content = sentiment.content,
                trueLabel = sentiment.label,
            )
        }
        return classificationResults
    }

    suspend fun createCM(sentiments: List<Sentiment>): CM {
        val analyzerResults = classify(sentiments)

        val CM = CM(analyzerResults)
        Log.d("btnOpenFile", "$CM")
        return CM
    }
}