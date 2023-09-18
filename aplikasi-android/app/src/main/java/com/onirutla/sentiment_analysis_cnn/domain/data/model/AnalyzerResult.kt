package com.onirutla.sentiment_analysis_cnn.domain.data.model

data class AnalyzerResult(
    val predictionResults: List<PredictionResult>,
    val inferenceTime: Long,
    val predictedLabel: String = predictionResults.maxBy { it.prob }.predictionLabel,
    val index: Int? = null,
    val trueLabel: String? = null,
    val content: String? = null,
)