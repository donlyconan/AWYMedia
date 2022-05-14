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

/**
 * This is Feedback screen
 */
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
                context?.showMessage(R.string.you_have_not_rated)
            } else if(binding.ipComment.text.isEmpty() || binding.ipComment.text.isBlank()) {
                context?.showMessage(R.string.please_enter_something)
            } else {
                val content = "${getString(R.string.rating)}: ${binding.ratingBar.rating}\n${binding.ipComment.text}"
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto:")
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email_developer)))
                intent.putExtra(Intent.EXTRA_SUBJECT,"Help & Feedback")
                intent.putExtra(Intent.EXTRA_TEXT, content)
                startActivity(Intent.createChooser(intent, getString(R.string.send_email)))
            }
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    companion object {
        val TAG = HelpAndFeedbackFragment::class.simpleName
    }
}