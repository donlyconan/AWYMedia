package com.utc.donlyconan.media.views.fragments.privatefolder

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.FragmentPasswordBinding
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.fragments.maindisplay.ListVideosFragment

class PasswordFragment : BaseFragment() {

    private var _binding: FragmentPasswordBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val executor = ContextCompat.getMainExecutor(requireActivity())
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(ListVideosFragment.TAG, "onAuthenticationSucceeded() called with: result = $result")
                val action = PasswordFragmentDirections.actionPasswordFragmentToPrivateFolderFragment()
                findNavController().navigate(
                    action,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.passwordFragment, true)
                        .build()
                )
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(ListVideosFragment.TAG, "onAuthenticationError() called with: errorCode = $errorCode, errString = $errString")
//                findNavController().navigateUp()
//                showToast("The authenticated process is failed!")
                // TODO refactor the error case when releasing
                val action = PasswordFragmentDirections.actionPasswordFragmentToPrivateFolderFragment()
                findNavController().navigate(
                    action,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.passwordFragment, true)
                        .build()
                )
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d(ListVideosFragment.TAG, "onAuthenticationFailed() called")
                findNavController().navigateUp()
                showToast(getString(R.string.authentication_failed))
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.app_name))
            .setSubtitle(getString(R.string.require_password_content))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }


}