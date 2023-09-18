package com.onirutla.sentiment_analysis_cnn.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onirutla.sentiment_analysis_cnn.databinding.ItemSentimentBinding
import com.onirutla.sentiment_analysis_cnn.domain.data.model.Sentiment

class SentimentAdapter :
    ListAdapter<Sentiment, SentimentViewHolder>(Differentiator) {

    companion object {
        val Differentiator = object : DiffUtil.ItemCallback<Sentiment>() {
            override fun areItemsTheSame(
                oldItem: Sentiment,
                newItem: Sentiment,
            ): Boolean = oldItem.content == newItem.content

            override fun areContentsTheSame(
                oldItem: Sentiment,
                newItem: Sentiment,
            ): Boolean = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SentimentViewHolder {
        val binding = ItemSentimentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SentimentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SentimentViewHolder, position: Int) {
        holder.binding.apply {
            val result = getItem(position)
            result.apply {
                tvIndexValue.text = index.toString()
                tvContentValue.text = content
                tvLabelValue.text = label
            }
        }
    }

}

class SentimentViewHolder(val binding: ItemSentimentBinding) :
    RecyclerView.ViewHolder(binding.root)