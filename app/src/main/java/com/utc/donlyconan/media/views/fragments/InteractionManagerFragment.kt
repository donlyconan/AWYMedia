package com.utc.donlyconan.media.views.fragments

import ClientAdapter
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.ScaleAnimation
import androidx.appcompat.view.menu.MenuBuilder
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.zxing.integration.android.IntentIntegrator
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.localinteraction.Client
import com.utc.donlyconan.media.app.localinteraction.EGPMediaClient
import com.utc.donlyconan.media.app.localinteraction.EGPMediaServer
import com.utc.donlyconan.media.app.services.FileService
import com.utc.donlyconan.media.app.utils.gone
import com.utc.donlyconan.media.app.utils.show
import com.utc.donlyconan.media.databinding.FragmentInteractionManagerBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.viewmodels.DeviceViewModel
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.MainActivity
import com.utc.donlyconan.media.views.adapter.OnItemClickListener
import kotlinx.coroutines.Dispatchers
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException


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
    private lateinit var optionMenu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(RecycleBinFragment.TAG, "onCreate: ")
        activity.apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setDisplayShowTitleEnabled(true)
            setHasOptionsMenu(true)
        }
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
        showLoadingScreen()
        viewModel.devicesMdl.observe(this) { data ->
            if(data == null || data.isEmpty()) {
                binding.tvConnectingDevices.gone()
                showNoDataScreen()
            } else {
                binding.tvConnectingDevices.show()
                hideLoading()
            }
            deviceAdapter.submit(data)
        }
        viewModel.ipAddress.observe(this) { ip ->
            if(fileService?.isReadyService() == true) {
                showToast(R.string.disconnect_before_pair_with_another_devices)
            } else {
                fileService?.openEgmService(EGPMediaClient::class, InetAddress.getByName(ip))
            }
        }
        viewModel.qrCodeMdl.observe(this) { bitmap->
            binding.imQrCode.setImageBitmap(bitmap)
        }
        viewModel.devicesMdl.value = fileService?.egmSystem?.listClients ?: listOf()
        Log.d(TAG, "onViewCreated: args=${args.ipAddress}")
        requestPermissionIfNeed(Manifest.permission.CAMERA)
        if(!haveNetworkConnection()) {
            showToast(R.string.network_is_not_available)
        } else {
            if(args.ipAddress?.trim()?.isNotEmpty() == true) {
                viewModel.ipAddress.postValue(args.ipAddress)
            }
        }
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
        optionMenu = menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun adjustMenuName(isConnected: Boolean, isGroupOwner: Boolean) {
        Log.d(TAG, "adjustMenuName() called with: isConnected = $isConnected, isGroupOwner = $isGroupOwner")
        if(optionMenu == null) {
            Log.d(TAG, "adjustMenuName: Option menu is null")
            return
        }
        val menuItem = optionMenu.findItem(R.id.it_connect)
        if (isConnected && !isGroupOwner) {
            menuItem.setTitle(R.string.disconnect)
        } else {
            menuItem.setTitle(R.string.scan_qr_code)
        }
        val groupItem = optionMenu.findItem(R.id.it_create_group)
        if (isConnected && isGroupOwner) {
            groupItem.setTitle(R.string.remove_group)
        } else {
            groupItem.setTitle(R.string.create_group)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(RecycleBinFragment.TAG, "onOptionsItemSelected() called with: item = $item")
        when (item.itemId) {
            R.id.it_create_group -> {
                fileService?.let { service ->
                    if (service.isReadyService()) {
                        service.closeEgpSystem()
                    } else {
                        openServer()
                    }
                }
            }
            R.id.it_connect -> {
                fileService?.let { service ->
                    if (service.isReadyService()) {
                        if (service.egmSystem?.isGroupOwner() == true) {
                            showToast(R.string.group_is_working)
                        } else {
                            service.closeEgpSystem()
                        }
                    } else {
                        scanQRCode()
                    }
                }
            }
            R.id.it_reconnect -> {
                if(fileService?.oldConnectedDeviceAddress == null) {
                    showToast(R.string.no_old_connected_device)
                } else {
                    viewModel.ipAddress.value = fileService?.oldConnectedDeviceAddress
                }
            }
        }
        return true
    }


    override fun onClientConnectionChanged(clients: List<Client>) {
        Log.d(TAG, "onClientConnectionChanged() called with: clients = $clients")
        viewModel.submit(clients)
    }

    override fun onDeviceNameUpdated(deviceName: String?) {
        Log.d(TAG, "onDeviceNameUpdated() called with: deviceName = $deviceName")
        viewModel.submit(fileService?.egmSystem?.listClients ?: listOf())
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
                    return address.hostAddress
                }

            }
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult() called with: requestCode = $requestCode, resultCode = $resultCode, data = $data")
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        val ipAddress = result?.contents?.substringAfterLast('/')
        if(ipAddress != null) {
            Log.d(TAG, "onActivityResult: IPAddress=$ipAddress")
            viewModel.ipAddress.value = ipAddress
        }
    }


    private fun openServer() {
        Log.d(TAG, "openServer() called")
        application.getFileService()?.openEgmService(EGPMediaServer::class, null )
    }


    private fun showQRCode() {
        Log.d(TAG, "showQRCode() called")
        runOnWorkerThread {
            val ipAddress = "egm.media/local-interaction/${getDeviceIPAddress()}"
            viewModel.generateQRCode(ipAddress)
            with(Dispatchers.Main) {
                runOnUIThread {
                    binding.qrGroup.visibility = View.VISIBLE
                    binding.imQrCode.startAnimation(
                        ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, 0.5f, 0.5f).apply {
                            duration = 1000
                        }
                    )
                }
            }
        }
    }

    override fun onError(e: Throwable?): Boolean {
        Log.d(TAG, "onError() called with: e = $e")
        if(e is SocketException) {
            if(fileService?.egmSystem?.isGroupOwner() == false) {
                showToast(R.string.pair_is_interrupted)
            }
        }
        return super.onError(e)
    }

    override fun onEgpConnectionChanged(isConnected: Boolean, isGroupOwner: Boolean) {
        Log.d(TAG, "onEgpConnectionChanged() called with: isConnected = $isConnected, isGroupOwner = $isGroupOwner")
        runOnUIThread {
            adjustMenuName(isConnected, isGroupOwner)
            if (isConnected && isGroupOwner) {
                showQRCode()
            }
            if(!isGroupOwner) {
                binding.qrGroup.visibility = View.GONE
            }
        }
    }

}