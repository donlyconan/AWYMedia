package com.utc.donlyconan.media.views.fragments

import DeviceAdapter
import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.net.wifi.p2p.WifiP2pManager.Channel
import android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_ENABLED
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.utc.donlyconan.media.app.utils.Logs
import com.utc.donlyconan.media.data.models.DeviceInfo
import com.utc.donlyconan.media.databinding.FragmentInteractionManagerBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.viewmodels.DeviceViewModel
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.adapter.OnItemClickListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield


class InteractionManagerFragment : BaseFragment(), OnItemClickListener {

    companion object {
        val TAG = InteractionManagerFragment::class.simpleName
    }

    val binding by lazy { FragmentInteractionManagerBinding.inflate(layoutInflater) }
    private val wifiManager by lazy { context!!.getSystemService(Context.WIFI_SERVICE) as WifiManager }
    private val wifiP2pManager by lazy { context!!.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager }
    private lateinit var channel: Channel
    private val viewModel by viewModels<DeviceViewModel>()
    private val intentFilter by lazy {
        IntentFilter()
            .apply {
                addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
                addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
                addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
                addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
            }
    }
    private var wifiP2pState: Boolean = false
    private val deviceAdapter by lazy { DeviceAdapter(requireContext(), listOf()) }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(RecycleBinFragment.TAG, "onCreate: ")
        setHasOptionsMenu(true)
        val appCompat = activity
        appCompat.setSupportActionBar(binding.toolbar)
        appCompat.supportActionBar?.setDisplayShowTitleEnabled(false)
        appCompat.supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        channel = wifiP2pManager?.initialize(requireContext(), Looper.getMainLooper(), null)!!

        if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES)) {
            wifiP2pManager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {}

                override fun onFailure(p0: Int) {
                    showToast("Can't discover near devices")
                }

            })
        } else {
            requestPermissionIfNeed(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES)
        }

    }

    @SuppressLint("MissingPermission")
    override fun onPermissionResult(result: Map<String, Boolean>) {
        Log.d(TAG, "onPermissionResult() called with: result = $result")
        if(result[Manifest.permission.ACCESS_FINE_LOCATION] == true && result[Manifest.permission.NEARBY_WIFI_DEVICES] == true) {
            wifiP2pManager?.discoverPeers(channel, null)
        } else {
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
        val job = lifecycleScope.launch {
            delay(15000 /*delay 15s before show loading data*/)
            yield()
            showNoDataScreen()
        }
        viewModel.devicesMdl.observe(this) { data ->
            if(data == null || data.isEmpty()) {
                showNoDataScreen()
            } else {
                hideLoading()
            }
            job.cancel()
            deviceAdapter.submit(data)
        }
    }

    @SuppressLint("MissingPermission")
    private val receiver = object : BroadcastReceiver() {

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onReceive(context: Context?, intent: Intent?) {
            Logs.d("onReceive() called with: context = $context, intent = $intent")
            when(intent?.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    wifiP2pState = state == WIFI_P2P_STATE_ENABLED
                    if(!wifiP2pState) {
                        viewModel.devicesMdl.value = listOf()
                    }
                    Log.d(TAG, "onReceive: WIFI_P2P_STATE_CHANGED_ACTION, state=$state")
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    Log.d(TAG, "onReceive: WIFI_P2P_PEERS_CHANGED_ACTION, wifiP2pManager=$wifiP2pManager")
                    wifiP2pManager?.requestPeers(channel, peerListListener)
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    // check network info
                    Log.d(TAG, "onReceive: WIFI_P2P_CONNECTION_CHANGED_ACTION")
                    // TODO
                    wifiP2pManager?.requestConnectionInfo(channel, connectionInfoListener)
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    Log.d(TAG, "onReceive: WIFI_P2P_THIS_DEVICE_CHANGED_ACTION")
                    intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE, WifiP2pDevice::class.java)?.let { device ->
                        viewModel.update(device)
                    }
                }
                else -> {
                    Logs.d( "onReceive: ${intent?.action} is not found.")
                }
            }
        }

    }

    private val peerListListener = WifiP2pManager.PeerListListener { list ->
        Log.d(TAG, "onPeersAvailable() called with: list.size = ${list.deviceList.size}")
        val devices = list.deviceList.map { info ->
            DeviceInfo(info)
        }
        viewModel.submit(devices)
    }

    private val connectionInfoListener = WifiP2pManager.ConnectionInfoListener { wifiP2pInfo ->
        val ownerAddress = wifiP2pInfo.groupOwnerAddress
        Log.d(TAG, "connectionInfoListener() called with: ownerAddress = $ownerAddress")

        if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            Log.d(TAG, "connectionInfoListener: Host")
            binding.tvStatus.text = "Group Owner"
        }
        if(wifiP2pInfo.groupFormed) {
            Log.d(TAG, "connectionInfoListener: Client")
            binding.tvStatus.text = "Client"
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")
        context?.registerReceiver(receiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        context?.unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        wifiP2pManager?.cancelConnect(channel, null)
    }

    @SuppressLint("MissingPermission")
    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: position = $position")
        val device = deviceAdapter.getItem(position) as DeviceInfo
        val wifiP2pConfig = WifiP2pConfig()
        wifiP2pConfig.deviceAddress = device.deviceAddress
        wifiP2pConfig.wps.setup = WpsInfo.PBC
        wifiP2pManager?.connect(channel, wifiP2pConfig, object : ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "onSuccess() called device = ${device.deviceName}")
            }

            override fun onFailure(p0: Int) {
                Log.d(TAG, "onFailure() called with: p0 = $p0, device = ${device.deviceName}")
            }
        })
    }


}