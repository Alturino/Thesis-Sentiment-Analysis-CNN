package com.onirutla.sentiment_analysis_cnn.domain.data.model

data class CM(
    val analyzerResults: List<AnalyzerResult>,
    val truePositive: Int = analyzerResults.count {
        it.trueLabel.equals("Positif") and (it.predictedLabel == "Positif")
    },
    val trueNegative: Int = analyzerResults.count {
        it.trueLabel.equals("Negatif") and (it.predictedLabel == "Negatif")
    },
    val falsePositive: Int = analyzerResults.count {
        it.trueLabel.equals("Negatif") and (it.predictedLabel == "Positif")
    },
    val falseNegative: Int = analyzerResults.count {
        it.trueLabel.equals("Positif") and (it.predictedLabel == "Negatif")
    },
    val inferenceTime: Long = analyzerResults.sumOf { it.inferenceTime },
)
