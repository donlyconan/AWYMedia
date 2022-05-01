package com.utc.donlyconan.media.views.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.view.menu.MenuBuilder
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.app.utils.AlertDialogManager
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.databinding.FragmentTrashBinding
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import com.utc.donlyconan.media.extension.widgets.OnItemLongClickListener
import com.utc.donlyconan.media.extension.widgets.showMessage
import com.utc.donlyconan.media.viewmodels.TrashViewModel
import com.utc.donlyconan.media.views.MainActivity
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.options.MenuMoreOptionFragment
import com.utc.donlyconan.media.views.fragments.options.TrashItemOptionFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Show list of video that was temporarily deleted by user
 */
class TrashFragment : Fragment(), OnItemClickListener {

    val binding by lazy { FragmentTrashBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<TrashViewModel>()
    private lateinit var adapter: VideoAdapter
    @Inject lateinit var settings: Settings
    @Inject lateinit var videoDao: VideoDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        (context?.applicationContext as AwyMediaApplication).applicationComponent()
            .inject(this)
        setHasOptionsMenu(true)
        val appCompat = activity as MainActivity
        appCompat.setSupportActionBar(binding.toolbar)
        appCompat.supportActionBar?.setDisplayShowTitleEnabled(false)
        appCompat.supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        Log.d(TAG, "onCreateView: ")
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        adapter = VideoAdapter(context!!, arrayListOf())
        adapter.onItemClickListener = this
        binding.recyclerView.adapter = adapter
        viewModel.apply {

        }
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemLongClick() called with: v = $v, position = $position")
        val video = adapter.getVideo(position)
        MenuMoreOptionFragment.newInstance(R.layout.fragment_trash_item_option) { v ->
            Log.d(TAG, "onItemLongClick() called with: v = $v")
            when(v.id) {
                R.id.btn_restore -> {
                    videoDao.update(video)
                }
                R.id.btn_delete -> {
                    AlertDialogManager.createDeleteAlertDialog(requireContext(),
                        "Deleting file", "Do you want to delete file \"${video.title}\"?") {
                        viewModel.viewModelScope.launch {
                            videoDao.delete(video.videoId)
                        }
                    }.show()
                }
            }
        }.show(requireActivity().supportFragmentManager, TAG)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.d(TAG, "onCreateOptionsMenu: ")
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        inflater.inflate(R.menu.menu_trash_bar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected() called with: item = $item")
        when (item.itemId) {
            // remove all trash items on db
            R.id.it_trash -> {
                if(adapter.itemCount == 0) {
                    Log.d(TAG, "onOptionsItemSelected: video list is empty!")
                    requireContext().showMessage("The list files is empty!")
                    return false
                }
                AlertDialogManager.createDeleteAlertDialog(
                    requireContext(), "Deleting file", "Do you wan to remove all files?") {
                }.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    companion object {
        val TAG: String = TrashFragment::class.java.simpleName
    }

}