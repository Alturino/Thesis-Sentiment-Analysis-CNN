package com.onirutla.sentiment_analysis_cnn.domain.usecase

import android.content.Context
import android.util.Log
import com.onirutla.sentiment_analysis_cnn.domain.data.model.AnalyzerResult
import com.onirutla.sentiment_analysis_cnn.domain.data.model.PredictionResult
import com.onirutla.sentiment_analysis_cnn.domain.data.model.SentimentLabel
import com.share424.sastrawi.Stemmer.Stemmer
import com.share424.sastrawi.Stemmer.StemmerFactory
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import javax.inject.Inject
import kotlin.math.abs
import kotlin.system.measureTimeMillis

class Analyzer @Inject constructor(
    @ActivityContext private val context: Context,
    private val tflite: Interpreter,
    private val sentimentLabel: SentimentLabel,
) {
    companion object {
        const val WORD2VEC_CNN_MODEL_FILENAME = "word2vec_cbow_cnn.tflite"
        const val WORD2VEC_CNN_LABEL_FILENAME = "word2vec_cbow_cnn.txt"
        const val SLANG_DICTDefaultNARY_FILENAME = "slang_dictionary.json"
        const val STOPWORDS_FILENAME = "stopwords.json"
        const val VOCAB_FILENAME = "vocab.json"
        const val MOSTCOMMON_FILENAME = "tokenizer_mostcommon.json"
        const val INPUT_LENGTH = 50
    }

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    private lateinit var wordToIndex: Map<String, Int>
    private lateinit var mostCommon: Map<String, Int>
    private lateinit var slangDictionary: Map<String, String>
    private lateinit var stopwords: Map<String, Int>
    private lateinit var baseWords: List<String>
    private lateinit var stemmer: Stemmer

    private suspend fun loadJsonFromAsset(filename: String): String {
        return withContext(Dispatchers.Default) {
            try {
                context.assets.open(filename).bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                Log.d("loadJsonFromAsset", e.localizedMessage, e)
                ""
            }
        }
    }

    private suspend fun loadTxtFile(filename: String): List<String> {
        return withContext(Dispatchers.Default) {
            val stopwords = context.resources
                .assets
                .open(filename)
                .bufferedReader()
                .use { it.readText() }
                .split("\n")
            Log.d("loadStopwords", "$stopwords")
            stopwords
        }
    }

    private suspend fun getWordToIndex(filename: String): Map<String, Int> {
        val jsonObj = JSONObject(loadJsonFromAsset(filename))
        return withContext(Dispatchers.Default) {
            try {
                val iterator = jsonObj.keys()
                val data = mutableMapOf<String, Int>()
                while (iterator.hasNext()) {
                    val key = iterator.next()
                    data[key] = jsonObj.get(key).toString().toIntOrNull() ?: 0
                }
                data
            } catch (e: Exception) {
                Log.d("getWordToIndex", e.localizedMessage, e)
                mapOf()
            }
        }
    }

    private suspend fun getSlangDictionary(): Map<String, String> {
        val jsonObj = JSONObject(loadJsonFromAsset(SLANG_DICTDefaultNARY_FILENAME))
        return withContext(Dispatchers.Default) {
            try {
                val iterator = jsonObj.keys()
                val data = mutableMapOf<String, String>()
                while (iterator.hasNext()) {
                    val key = iterator.next()
                    data[key] = jsonObj.get(key).toString()
                }
                data
            } catch (e: Exception) {
                Log.d("getSlangDictionary", e.localizedMessage, e)
                mapOf()
            }
        }
    }

    init {
        coroutineScope.launch {
            coroutineScope {
                mostCommon = getWordToIndex(MOSTCOMMON_FILENAME)
            }
            coroutineScope {
                wordToIndex = getWordToIndex(VOCAB_FILENAME)
            }
            coroutineScope {
                slangDictionary = getSlangDictionary()
            }
            coroutineScope {
                stopwords = getWordToIndex(STOPWORDS_FILENAME)
            }
            coroutineScope {
                baseWords = loadTxtFile("kata_dasar.txt")
                stemmer = StemmerFactory(context).fromList(baseWords.toList()).create()
            }
        }
    }

    private fun cleanText(text: String): String {
        val result = text.lowercase()
            .replace(regex = Regex(pattern = "https?://.*[\\r\\n]*"), replacement = "")
            .replace(
                regex = Regex(pattern = "[\\ud83c\\udf00-\\ud83d\\ude4f]|[\\ud83d\\ude80-\\ud83d\\udeff]"),
                replacement = ""
            )
            .filter { it.isWhitespace() or it.isLetter() }
            .strip()
        Log.d("cleanText", result)
        return result
    }

    private suspend fun slangHandling(text: String): String {
        return withContext(Dispatchers.Default) {
            val firstFilter = text.split(" ")
                .joinToString(" ") {
                    if (slangDictionary.containsKey(it)) slangDictionary[it]!! else it
                }
            val secondFilter = firstFilter.split(" ").joinToString(" ") {
                if (slangDictionary.containsKey(it)) slangDictionary[it]!! else it
            }
            Log.d("slangHandling", secondFilter)
            secondFilter
        }
    }

    private suspend fun removeStopword(text: String): String {
        return withContext(Dispatchers.Default) {
            Log.d("removeStopword before", text)
            val result = text.split(" ")
                .joinToString(" ") {
                    if (stopwords.containsKey(it)) "" else it
                }
            Log.d("removeStopword result", result)
            result
        }
    }

    private suspend fun stemming(text: String): String {
        return withContext(Dispatchers.Default) {
            val stemmed = stemmer.stem(text)
            Log.d("stemming", stemmed)
            stemmed
        }
    }

    private suspend fun toSequence(text: String): IntArray {
        return withContext(Dispatchers.Default) {
            val sequence = text.split(" ").map { word ->
                if (mostCommon.containsKey(word))
                    wordToIndex[word]!!
                else
                    1
            }

            Log.d("toSequence", "$sequence")
            sequence.toIntArray()
        }
    }

    private suspend fun padSequence(sequence: IntArray): IntArray {
        return withContext(Dispatchers.Default) {
            val padded = if (sequence.size > INPUT_LENGTH)
                sequence.sliceArray(0..INPUT_LENGTH)
            else if (sequence.size < INPUT_LENGTH) {
                val temp = sequence.toMutableList()
                for (i in temp.size until INPUT_LENGTH)
                    temp.add(0)
                temp.toIntArray()
            } else
                sequence
            Log.d("padSequence", "${padded.asList()}")
            padded
        }
    }

    private suspend fun preprocess(text: String): IntArray {
        val cleaned = cleanText(text)
        val slangHandled = slangHandling(cleaned)
        val stopwordRemoved = removeStopword(slangHandled)
        val stemmed = stemming(stopwordRemoved)
        val sequence = toSequence(stemmed)
        val paddedSequence = padSequence(sequence)
        Log.d("preprocess", "${paddedSequence.toList()}")
        return paddedSequence
    }

    suspend fun classify(text: String): AnalyzerResult {
        val scores = arrayOf(FloatArray(1))
        val results: List<PredictionResult>
        val inferenceTime = measureTimeMillis {
            val paddedSequence = preprocess(text)
            val input = arrayOf(paddedSequence.map { it.toFloat() }.toFloatArray())
            val forLogging = input.map {
                it.toList()
            }
            Log.d("input.classify", "$forLogging")
            tflite.run(input, scores)

            val score = scores[0][0]
            Log.d("score", score.toString())
            results = if (score >= 0.5) {
                val positive = PredictionResult(
                    predictionLabel = sentimentLabel.labels[1],
                    prob = score
                )
                val negative = PredictionResult(
                    predictionLabel = sentimentLabel.labels[0],
                    prob = 1 - score
                )
                listOf(positive, negative)
            } else {
                val positive = PredictionResult(
                    predictionLabel = sentimentLabel.labels[1],
                    prob = score
                )
                val negative = PredictionResult(
                    predictionLabel = sentimentLabel.labels[0],
                    prob = abs(1 - score)
                )
                listOf(positive, negative)
            }
        }
        Log.d("classify", "$results, $inferenceTime")
        return AnalyzerResult(predictionResults = results, inferenceTime = inferenceTime)
    }
}
