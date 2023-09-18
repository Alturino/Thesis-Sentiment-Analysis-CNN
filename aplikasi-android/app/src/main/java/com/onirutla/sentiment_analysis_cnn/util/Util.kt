package com.onirutla.sentiment_analysis_cnn.util

import android.content.Context
import android.net.Uri
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import com.onirutla.sentiment_analysis_cnn.domain.data.model.Sentiment
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.onStart
import org.apache.commons.csv.CSVFormat

fun EditText.toFlow(): Flow<String> = callbackFlow {
    val listener = doOnTextChanged { text, _, _, _ -> trySend(text.toString()) }
    awaitClose { removeTextChangedListener(listener) }
}.onStart { emit(text.toString()) }
    .filterNot { text.isNullOrEmpty() or text.isNullOrBlank() }
    .debounce(500)
    .distinctUntilChanged()

fun Uri.csvToString(context: Context): String {
    val contentResolver = context.contentResolver
    return contentResolver.openInputStream(this)!!.bufferedReader()
        .use { reader ->
            reader.readText()
        }
}

fun Uri.csvToSentiments(context: Context): List<Sentiment> {
    val contentResolver = context.contentResolver
    return contentResolver.openInputStream(this)!!.bufferedReader()
        .use { reader ->
            CSVFormat.Builder.create(CSVFormat.DEFAULT)
                .setIgnoreEmptyLines(true)
                .build()
                .parse(reader)
                .drop(1)
                .map { csvRecord ->
                    Sentiment(
                        index = csvRecord[0].toInt(),
                        content = csvRecord[1],
                        label = csvRecord[2]
                    )
                }
        }
}
