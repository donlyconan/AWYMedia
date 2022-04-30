package com.utc.donlyconan.media.views.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.FragmentHelpAndFeedbackBinding
import com.utc.donlyconan.media.extension.widgets.showMessage
import com.utc.donlyconan.media.views.BaseFragment


class HelpAndFeedbackFragment : BaseFragment() {

    val binding by lazy { FragmentHelpAndFeedbackBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        binding.btnSubmit.setOnClickListener {
            if(binding.ratingBar.rating == 0f) {
                context?.showMessage("Bạn chưa đánh giá.")
            } else if(binding.ipComment.text.isEmpty() || binding.ipComment.text.isBlank()) {
                context?.showMessage("Hãy nhập gì đó")
            } else {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto:")
                intent.putExtra(Intent.EXTRA_EMAIL,"awydeveloper@gmail.com")
                intent.putExtra(Intent.EXTRA_SUBJECT,"Help & Feedback")
                intent.putExtra(Intent.EXTRA_TEXT,binding.ipComment.text.toString())
                startActivity(Intent.createChooser(intent, "Send email"))
            }
        }
        binding.toolbar.setNavigationOnClickListener {
            val action = HelpAndFeedbackFragmentDirections.actionHelpAndFeedbackFragmentToMainDisplayFragment()
            findNavController().navigate(action)
        }
    }

    companion object {
        val TAG = HelpAndFeedbackFragment::class.simpleName
    }
}