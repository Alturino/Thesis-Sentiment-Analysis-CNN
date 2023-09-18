package com.onirutla.sentiment_analysis_cnn.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.onirutla.sentiment_analysis_cnn.R
import com.onirutla.sentiment_analysis_cnn.databinding.FragmentFileBinding
import com.onirutla.sentiment_analysis_cnn.domain.data.model.CM
import com.onirutla.sentiment_analysis_cnn.domain.data.model.Sentiment
import com.onirutla.sentiment_analysis_cnn.domain.usecase.BatchAnalyzer
import com.onirutla.sentiment_analysis_cnn.util.csvToSentiments
import com.onirutla.sentiment_analysis_cnn.util.csvToString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FileFragment : Fragment() {

    private var _binding: FragmentFileBinding? = null
    private val binding: FragmentFileBinding
        get() = _binding!!

    @Inject
    lateinit var batchAnalyzer: BatchAnalyzer

    private lateinit var CM: CM

    private var csvString: String? = null
    private var sentiments: List<Sentiment>? = null

    private val classificationResultAdapter: AnalyzerResultAdapter =
        AnalyzerResultAdapter()
    private val sentimentAdapter: SentimentAdapter = SentimentAdapter()

    private val launcherIntentTxt = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        csvString = uri?.csvToString(requireContext())
        sentiments = uri?.csvToSentiments(requireContext())
        sentiments?.let { sentimentAdapter.submitList(it.take(10)) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.apply {
            rvClassificationResult.apply {
                adapter = classificationResultAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
            rvSentiment.apply {
                adapter = sentimentAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
            btnOpenFile.setOnClickListener {
                launcherIntentTxt.launch("text/*")
            }
            btnClassify.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    sentiments?.let { sentiments ->
                        CM = batchAnalyzer.createCM(sentiments)

                        val tp = CM.truePositive.toFloat()
                        val tn = CM.trueNegative.toFloat()
                        val fp = CM.falsePositive.toFloat()
                        val fn = CM.falseNegative.toFloat()

                        val precision = (tp / (tp + fp))
                        val recall = (tp / (tp + fn))
                        val accuracy = ((tp + tn) / (tp + tn + fp + fn))
                        val fMeasure = (2 * ((recall * precision) / (recall + precision)))

                        tvPrecision.text = getString(R.string.precision, precision)
                        tvRecall.text = getString(R.string.recall, recall)
                        tvAccuracy.text = getString(R.string.accuracy, accuracy)
                        tvFmeasure.text = getString(R.string.f_measure, fMeasure)
                        tvInferenceTime.text = getString(
                            R.string.process_time,
                            CM.inferenceTime
                        )

                        tvTpValue.text = tp.toInt().toString()
                        tvTnValue.text = tn.toInt().toString()
                        tvFpValue.text = fp.toInt().toString()
                        tvFnValue.text = fn.toInt().toString()

                        classificationResultAdapter.submitList(CM.analyzerResults)
                    }
                }

            }
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