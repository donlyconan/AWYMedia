package com.utc.donlyconan.media.views.fragments.maindisplay

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.exoplayer2.MediaItem
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.repo.PlaylistRepository
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.databinding.DialogAddPlaylistBinding
import com.utc.donlyconan.media.databinding.FragmentPlaylistBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.extension.widgets.showMessage
import com.utc.donlyconan.media.viewmodels.PlaylistViewModel
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.adapter.OnItemClickListener
import com.utc.donlyconan.media.views.adapter.OnItemLongClickListener
import com.utc.donlyconan.media.views.adapter.PlaylistAdapter
import com.utc.donlyconan.media.views.fragments.options.MenuMoreOptionFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


/**
 * Represent for Playlist screen where we will manage all playlist and video in each playlist
 */
class PlaylistFragment : BaseFragment(), View.OnClickListener, OnItemClickListener,
    OnItemLongClickListener {

    val binding by lazy { FragmentPlaylistBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<PlaylistViewModel>()
    lateinit var adapter: PlaylistAdapter

    @Inject lateinit var playlistRepo: PlaylistRepository
    @Inject lateinit var videoRepo: VideoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        lsBinding = LoadingDataScreenBinding.bind(binding.icdLoading.frameContainer)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        adapter = PlaylistAdapter(context!!, arrayListOf(), true)
        adapter.onItemClickListener = this
        adapter.onItemLongClickListener = this
        binding.recyclerView.adapter = adapter
        binding.fab.setOnClickListener(this)
        showLoadingScreen()
        viewModel.listPlaylist.observe(this) { playlists ->
            lifecycleScope.launch(Dispatchers.IO) {
                playlists.forEach { playlist: Playlist ->
                    playlist.itemSize = playlistRepo.countVideos(playlist.playlistId!!)
                    playlist.firstVideo = playlistRepo.getFirstVideo(playlist.playlistId!!)
                }
                if(playlists.isEmpty()) {
                    showNoDataScreen()
                } else {
                    hideLoading()
                }
                val data = playlists.sortedWith { u, v -> u.compareTo(v, settings.playlistSortBy) }
                withContext(Dispatchers.Main) {
                    adapter.submit(data)
                }
            }
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
                R.id.btn_open -> lifecycleScope.launch(Dispatchers.IO) {
                    item.firstVideo?.let { video ->
                        startVideoDisplayActivity(video.videoId, video.videoUri, item.playlistId!!)
                    }

                }
                R.id.btn_play_music -> lifecycleScope.launch(Dispatchers.IO) {
                    playlistRepo.playlistWithVideosDao .get(item.playlistId!!)
                        ?.videos?.map { MediaItem.fromUri(it.videoUri) }
                        ?.let { uris ->
                           withContext(Dispatchers.Main) {
                               startPlayMusic(uris)
                           }
                        }
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
                    AddedPlaylistDialog(true, item.title) { text, _ ->
                        val item2 = Playlist(item.playlistId, text)
                        viewModel.playlistRepo.update(item2)
                    }.show(supportFragmentManager, TAG)
                }
            }
        }.show(supportFragmentManager, TAG)
    }

    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: ")
        when(v?.id) {
            R.id.fab -> {
                AddedPlaylistDialog(false) { text, _ ->
                    val item = Playlist(null, text)
                    viewModel.playlistRepo.insert(item)
                }.show(supportFragmentManager, TAG)
            }
            R.id.btn_sort_by_name_up -> {
                settings.playlistSortBy = Settings.SORT_BY_NAME_UP
                adapter.playlists.sortWith { u, v -> u.compareTo(v, Settings.SORT_BY_NAME_UP) }
                adapter.notifyDataSetChanged()
            }
            R.id.btn_sort_by_name_down -> {
                settings.playlistSortBy = Settings.SORT_BY_NAME_DOWN
                adapter.playlists.sortWith { u, v -> u.compareTo(v, Settings.SORT_BY_NAME_DOWN) }
                adapter.notifyDataSetChanged()
            }
        }
    }


    companion object {
        val TAG: String = PlaylistFragment::class.java.simpleName

        @JvmStatic
        fun newInstance(): PlaylistFragment {
            return PlaylistFragment()
        }
    }

    class AddedPlaylistDialog(
        val isEditMode: Boolean,
        val currentName: String? = null,
        val listener: (text: String, isEditMode: Boolean) -> Unit
    ): DialogFragment(R.layout.dialog_add_playlist) {

        private lateinit var binding: DialogAddPlaylistBinding

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return super.onCreateDialog(savedInstanceState)
            setStyle(STYLE_NORMAL, R.style.MyAlertDialog)
            isCancelable = true
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            binding = DialogAddPlaylistBinding.bind(view)
            if(isEditMode){
                binding.title.setText(R.string.rename)
                binding.ipName.setText(currentName)
            }
            binding.btnOk.setOnClickListener {
                val tvName = binding.ipName
                if(tvName.text.toString().trim().isNotEmpty()){
                    listener(binding.ipName.text.toString(), isEditMode)
                    dismiss()
                } else {
                    context?.showMessage(R.string.playlist_name_is_invalid)
                }
            }
            binding.btnCancel.setOnClickListener {
                dismiss()
            }

            dialog?.window?.apply {
                val width = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
                setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
                setBackgroundDrawableResource(R.color.transparent)
            }
        }

    }
}