package com.onirutla.sentiment_analysis_cnn.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.onirutla.sentiment_analysis_cnn.R
import com.onirutla.sentiment_analysis_cnn.databinding.FragmentSentenceBinding
import com.onirutla.sentiment_analysis_cnn.domain.usecase.Analyzer
import com.onirutla.sentiment_analysis_cnn.util.toFlow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SentenceFragment : Fragment() {

    @Inject
    lateinit var analyzer: Analyzer

    private var _binding: FragmentSentenceBinding? = null
    private val binding: FragmentSentenceBinding
        get() = _binding!!

    private val predictionResultAdapter = PredictionResultAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSentenceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

            btnClassify.setOnClickListener {
                val text = inputEt.text.toString()
                viewLifecycleOwner.lifecycleScope.launch {
                    val classificationResult = analyzer.classify(text)
                    predictionResultAdapter.submitList(classificationResult.predictionResults)
                    binding.inferenceTime.text = getString(
                        R.string.process_time,
                        classificationResult.inferenceTime
                    )
                }
            }

            rvClassificationResult.apply {
                adapter = predictionResultAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

            inputEt.toFlow()
                .onEach {
                    val classificationResult = analyzer.classify(it)
                    predictionResultAdapter.submitList(classificationResult.predictionResults)
                    binding.inferenceTime.text = getString(
                        R.string.process_time,
                        classificationResult.inferenceTime
                    )
                }
                .launchIn(lifecycleScope)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}