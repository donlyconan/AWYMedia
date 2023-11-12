package com.utc.donlyconan.media.views.fragments

import ClientAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.view.menu.MenuBuilder
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.qrcode.QRCodeWriter
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.localinteraction.Client
import com.utc.donlyconan.media.app.localinteraction.EGPMediaClient
import com.utc.donlyconan.media.app.localinteraction.EGPMediaServer
import com.utc.donlyconan.media.app.services.FileService
import com.utc.donlyconan.media.databinding.FragmentInteractionManagerBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.viewmodels.DeviceViewModel
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.MainActivity
import com.utc.donlyconan.media.views.adapter.OnItemClickListener
import kotlinx.coroutines.Dispatchers
import java.net.InetAddress
import java.net.NetworkInterface


class InteractionManagerFragment : BaseFragment(), OnItemClickListener,
    FileService.OnFileServiceListener, MainActivity.OnActivityResponse {

    companion object {
        val TAG = InteractionManagerFragment::class.simpleName
    }

    val binding by lazy { FragmentInteractionManagerBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<DeviceViewModel>()
    private val deviceAdapter by lazy { ClientAdapter(requireContext(), fileService?.egmSystem?.listClients ?: listOf()) }
    private val fileService by lazy { application.getFileService() }
    private val args by navArgs<InteractionManagerFragmentArgs>()
    private lateinit var intentIntegrator: IntentIntegrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(RecycleBinFragment.TAG, "onCreate: ")
        activity.apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setDisplayShowTitleEnabled(true)
            setHasOptionsMenu(true)
        }.listener = this
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        Log.d(RecycleBinFragment.TAG, "onCreateView: ")
        lsBinding = LoadingDataScreenBinding.bind(binding.icdLoading.frameContainer)
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(RecycleBinFragment.TAG, "onViewCreated: ")
        deviceAdapter.setOnItemClickListener(this)
        binding.recyclerView.adapter = deviceAdapter
        binding.tvDeviceName.text = Settings.Global.getString(context!!.contentResolver, Settings.Global.DEVICE_NAME)
        viewModel.devicesMdl.observe(this) { data ->
            if(data == null || data.isEmpty()) {
                showNoDataScreen()
            } else {
                hideLoading()
            }
            deviceAdapter.submit(data)
        }
        runOnWorkerThread {
            val ipAddress = "egm.media/local-interaction/${getDeviceIPAddress()}"
            val bitmap = generateQRCode(ipAddress)
            with(Dispatchers.Main) {
                binding.imQrCode.setImageBitmap(bitmap)
                hideLoading()
            }
        }

        Log.d(TAG, "onViewCreated: args=${args.ipAddress}")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
        fileService?.registerOnFileServiceListener(this)
    }


    override fun onStop() {
        super.onStop()
        fileService?.unregisterOnFileServiceListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        activity.listener = null
    }

    fun scan() {
        Log.d(TAG, "scan() called")
        intentIntegrator = IntentIntegrator(requireActivity()).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setBarcodeImageEnabled(true)
            setOrientationLocked(false)
            setPrompt("Scan QR code")
            setBeepEnabled(false)
            initiateScan()
        }
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: position = $position")
        val client = deviceAdapter.getItem(position) as Client
        runOnWorkerThread {
            client.close()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.d(RecycleBinFragment.TAG, "onCreateOptionsMenu: ")
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        inflater.inflate(R.menu.menu_wifi_direct, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(RecycleBinFragment.TAG, "onOptionsItemSelected() called with: item = $item")
        runOnUIThread {
            when (item.itemId) {
                R.id.it_refresh -> {}
                R.id.it_create_group -> {
                    application.getFileService()?.openEgmService(EGPMediaServer::class, null ){ res ->
                        if(res) {
                            showToast("Group is created")
                            runOnUIThread {
                                binding.qrGroup.visibility = View.VISIBLE
                            }
                        }
                    }
                }
                R.id.it_connect -> {
                    scan()
                }
            }

        }
        return true
    }


    override fun onClientConnectionChanged(clients: List<Client>) {
        Log.d(TAG, "onClientConnectionChanged() called with: clients = $clients")
        viewModel.submit(clients)
    }

    private fun generateQRCode(content: String): Bitmap? {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix: BitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 256, 256)
        val width: Int = bitMatrix.width
        val height: Int = bitMatrix.height
        val qrCodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                qrCodeBitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return qrCodeBitmap
    }

    private fun getDeviceIPAddress(): String? {
        Log.d(TAG, "getDeviceIPAddress() called")
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        while (networkInterfaces.hasMoreElements()) {
            val networkInterface = networkInterfaces.nextElement()
            val addresses = networkInterface.inetAddresses
            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()
                if (!address.isLoopbackAddress && address.hostAddress.indexOf(':') == -1) {
                    return address.hostAddress.let { ip ->
                        if(ip.startsWith("10.")) "127.0.0.1" else ip
                    }
                }

            }
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult() called with: requestCode = $requestCode, resultCode = $resultCode, data = $data")
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if(result != null) {
            runOnWorkerThread { 
                val ipAddress = result.contents.substringAfterLast('/')
                Log.d(TAG, "onActivityResult: IPAddress=$ipAddress")
                application.getFileService()?.openEgmService(EGPMediaClient::class, InetAddress.getByName(ipAddress)) {
                    Log.d(TAG, "onActivityResult: $it")
                }
            }
        }
    }

}