import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.localinteraction.Client
import com.utc.donlyconan.media.databinding.ItemWifiDirectBinding
import com.utc.donlyconan.media.views.adapter.BaseAdapter

class ClientAdapter(var context: Context, data: List<Client>) : BaseAdapter<Any>(Client.diffUtil, data) {

    var inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalHolder {
        return DeviceHolder(ItemWifiDirectBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: LocalHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        (holder as DeviceHolder).bind(getItem(position) as Client)
    }

    class DeviceHolder(val binding: ItemWifiDirectBinding) :
        LocalHolder(binding), View.OnClickListener, View.OnLongClickListener {

        init {
            binding.root.setOnClickListener(null)
        }

        fun bind(client: Client) {
            binding.tvDeviceName.text = client.name ?: binding.root.context.getText(R.string.unknown)
            binding.tvAddress.text = client.socket.inetAddress.hostAddress
            binding.btDisconnect.setOnClickListener {
                onItemClickListener?.onItemClick(it, absoluteAdapterPosition)
            }
        }

    }

}