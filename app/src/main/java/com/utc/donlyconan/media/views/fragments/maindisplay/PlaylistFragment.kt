package com.utc.donlyconan.media.views.fragments.maindisplay

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.repo.PlaylistRepository
import com.utc.donlyconan.media.databinding.DialogAddPlaylistBinding
import com.utc.donlyconan.media.databinding.FragmentPlaylistBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import com.utc.donlyconan.media.extension.widgets.OnItemLongClickListener
import com.utc.donlyconan.media.extension.widgets.showMessage
import com.utc.donlyconan.media.viewmodels.PlaylistViewModel
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.adapter.PlaylistAdapter
import com.utc.donlyconan.media.views.fragments.MainDisplayFragmentDirections
import com.utc.donlyconan.media.views.fragments.options.MenuMoreOptionFragment
import javax.inject.Inject


class PlaylistFragment : BaseFragment(), View.OnClickListener, OnItemClickListener ,
    OnItemLongClickListener {

    val binding by lazy { FragmentPlaylistBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<PlaylistViewModel>()
    lateinit var adapter: PlaylistAdapter

    @Inject lateinit var playlistRepo: PlaylistRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        applicationComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        lBinding = LoadingDataScreenBinding.bind(binding.icdLoading.frameContainer)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        adapter = PlaylistAdapter(context!!, arrayListOf(), playlistRepo)
        adapter.onItemClickListener = this
        adapter.onItemLongClickListener = this
        binding.recyclerView.adapter = adapter
        binding.fab.setOnClickListener(this)
        showLoadingScreen()
        viewModel.listPlaylist.observe(this) { playlists ->
            if(playlists.isEmpty()) {
                showNoDataScreen()
            } else {
                hideLoading()
            }
            adapter.submit(playlists)
        }
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        val item = adapter.playlists[position]
        val action = MainDisplayFragmentDirections
            .actionMainDisplayFragmentToDetailedPlaylistFragment(item.playlistId!!)
        findNavController().navigate(action)
    }

    override fun onItemLongClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        val item = adapter.playlists[position]
        MenuMoreOptionFragment.newInstance(R.layout.fragment_playlist_option) {
            when(it.id) {
                R.id.btn_open -> {
                    val action = MainDisplayFragmentDirections
                        .actionMainDisplayFragmentToDetailedPlaylistFragment(item.playlistId!!)
                    findNavController().navigate(action)
                }
                R.id.btn_add -> {
                    val action = MainDisplayFragmentDirections
                            .actionMainDisplayFragmentToExpendedPlaylistFragment(item.playlistId!!)
                    findNavController().navigate(action)
                }
                R.id.btn_delete -> {
                    playlistRepo.delete(item)
                    playlistRepo.removePlaylist(item.playlistId!!)
                }
                R.id.btn_rename -> {
                    AddedPlaylistDialog(requireContext(), true) { text, _ ->
                        val item2 = Playlist(item.playlistId, text)
                        viewModel.playlistRepo.update(item2)
                    }.show()
                }
            }
        }.show(supportFragmentManager, TAG)
    }

    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: ")
        if (v?.id == R.id.fab) {
            AddedPlaylistDialog(requireContext(), false) { text, _ ->
                val item = Playlist(null, text)
                viewModel.playlistRepo.insert(item)
            }.show()
        }
    }


    companion object {
        val TAG: String = PlaylistFragment::class.java.simpleName

        @JvmStatic
        fun newInstance(): PlaylistFragment {
            return PlaylistFragment()
        }
    }


    inner class AddedPlaylistDialog(context: Context, isEditMode: Boolean,
                                    val listener: (text: String, isEditMode: Boolean) -> Unit) : Dialog(context) {

        private val binding by lazy { DialogAddPlaylistBinding.inflate(layoutInflater) }

        init {
            if(isEditMode){
                binding.title.setText(R.string.rename)
            }
            setContentView(binding.root)
            window?.apply {
                val width = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
                setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
            }
            binding.btnOk.setOnClickListener {
                val tvName = binding.ipName
                if(tvName.text.toString().trim().isNotEmpty()){
                    listener(binding.ipName.text.toString(), isEditMode)
                    dismiss()
                } else {
                    context.showMessage("Playlist name is invalid!")
                }
            }
            binding.btnCancel.setOnClickListener {
                dismiss()
            }
        }
    }
}