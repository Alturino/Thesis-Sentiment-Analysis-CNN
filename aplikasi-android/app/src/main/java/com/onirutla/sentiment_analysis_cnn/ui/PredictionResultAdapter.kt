package com.onirutla.sentiment_analysis_cnn.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onirutla.sentiment_analysis_cnn.databinding.ItemPredictionResultBinding
import com.onirutla.sentiment_analysis_cnn.domain.data.model.PredictionResult

class PredictionResultAdapter :
    ListAdapter<PredictionResult, PredictionResultViewHolder>(Differentiator) {

    companion object {
        val Differentiator = object : DiffUtil.ItemCallback<PredictionResult>() {
            override fun areItemsTheSame(
                oldItem: PredictionResult,
                newItem: PredictionResult,
            ): Boolean = oldItem.prob == newItem.prob

            override fun areContentsTheSame(
                oldItem: PredictionResult,
                newItem: PredictionResult,
            ): Boolean = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredictionResultViewHolder {
        val binding = ItemPredictionResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PredictionResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PredictionResultViewHolder, position: Int) {
        holder.apply {
            val result = getItem(position)
            val formattedProb = String.format("%.2f", result.prob)
            binding.predictionResult.text = "${result.predictionLabel}: $formattedProb"
        }
    }

}

class PredictionResultViewHolder(val binding: ItemPredictionResultBinding) :
    RecyclerView.ViewHolder(binding.root)