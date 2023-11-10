import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.utc.donlyconan.media.data.models.DeviceInfo
import com.utc.donlyconan.media.databinding.ItemWifiDirectBinding
import com.utc.donlyconan.media.views.adapter.BaseAdapter
import com.utc.donlyconan.media.views.fragments.InteractionManagerFragment

class DeviceAdapter(var context: Context, data: List<DeviceInfo>, ) : BaseAdapter<Any>(DeviceInfo.diffUtil, data) {

    var inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalHolder {
        return DeviceHolder(ItemWifiDirectBinding.inflate(inflater, parent, false))
    }
    override fun onBindViewHolder(holder: LocalHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        (holder as DeviceHolder).bind(getItem(position) as DeviceInfo)
    }

    class DeviceHolder(val binding: ItemWifiDirectBinding) :
        LocalHolder(binding), View.OnClickListener, View.OnLongClickListener {

        fun bind(info: DeviceInfo) {
            binding.tvDeviceName.text = info.deviceName
            binding.tvAddress.text = info.deviceAddress
            binding.cbStatus.isChecked = info.status == WifiP2pDevice.CONNECTED
            binding.tvStatus.text = getDeviceStatus(info.status)
        }

        private fun getDeviceStatus(deviceStatus: Int): String {
            return when (deviceStatus) {
                WifiP2pDevice.AVAILABLE -> "Available"
                WifiP2pDevice.INVITED -> "Invited"
                WifiP2pDevice.CONNECTED -> "Connected"
                WifiP2pDevice.FAILED -> "Failed"
                WifiP2pDevice.UNAVAILABLE -> "Unavailable"
                else -> "Unknown"
            }
        }
    }

}