package com.onirutla.sentiment_analysis_cnn.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onirutla.sentiment_analysis_cnn.databinding.ItemAnalyzerResultBinding
import com.onirutla.sentiment_analysis_cnn.domain.data.model.AnalyzerResult

class AnalyzerResultAdapter :
    ListAdapter<AnalyzerResult, AnalzyerResultViewHolder>(Differentiator) {

    object Differentiator : DiffUtil.ItemCallback<AnalyzerResult>() {
        override fun areItemsTheSame(
            oldItem: AnalyzerResult,
            newItem: AnalyzerResult,
        ): Boolean = oldItem.content == newItem.content

        override fun areContentsTheSame(
            oldItem: AnalyzerResult,
            newItem: AnalyzerResult,
        ): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): AnalzyerResultViewHolder {
        val binding = ItemAnalyzerResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AnalzyerResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnalzyerResultViewHolder, position: Int) {
        holder.binding.apply {
            val result = getItem(position)
            result.apply {
                tvActualLabelValue.text = trueLabel
                tvPredictionLabelValue.text = predictedLabel
                tvContentValue.text = content
                tvIndexValue.text = index.toString()
            }
        }
    }
}

class AnalzyerResultViewHolder(val binding: ItemAnalyzerResultBinding) :
    RecyclerView.ViewHolder(binding.root)