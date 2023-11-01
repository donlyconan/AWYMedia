package com.utc.donlyconan.media.views.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.ActivityResult
import androidx.appcompat.view.menu.MenuBuilder
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.utils.AlertDialogManager
import com.utc.donlyconan.media.app.utils.Logs
import com.utc.donlyconan.media.app.utils.convertToStorageData
import com.utc.donlyconan.media.app.utils.sortedByDeletedDate
import com.utc.donlyconan.media.data.models.Trash
import com.utc.donlyconan.media.data.repo.TrashRepository
import com.utc.donlyconan.media.databinding.FragmentTrashBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.extension.widgets.showMessage
import com.utc.donlyconan.media.viewmodels.TrashViewModel
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.adapter.OnItemLongClickListener
import com.utc.donlyconan.media.views.adapter.RecycleBinAdapter
import com.utc.donlyconan.media.views.fragments.options.MenuMoreOptionFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Show list of video that was temporarily deleted by user
 */
class RecycleBinFragment : BaseFragment(), OnItemLongClickListener {

    @Inject lateinit var trashRepo: TrashRepository
    val binding by lazy { FragmentTrashBinding.inflate(layoutInflater) }
    private lateinit var adapter: RecycleBinAdapter
    private var handlingVideoTask: VideoTask? = null

    private val viewModel by viewModels<TrashViewModel> {
        viewModelFactory {
            initializer {
                TrashViewModel(
                    appComponent.getTrashDao(),
                    appComponent.getVideoDao(),
                    appComponent.getPlaylistDao()
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        application.applicationComponent().let { com ->
            com.inject(this)
        }
        setHasOptionsMenu(true)
        val appCompat = activity
        appCompat.setSupportActionBar(binding.toolbar)
        appCompat.supportActionBar?.setDisplayShowTitleEnabled(false)
        appCompat.supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            trashRepo.sync()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        Log.d(TAG, "onCreateView: ")
        lsBinding = LoadingDataScreenBinding.bind(binding.icdLoading.frameContainer)
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        adapter = RecycleBinAdapter(requireContext(), arrayListOf())
        adapter.setOnLongClickListener(this)
        binding.recyclerView.adapter = adapter
        showLoadingScreen()
        viewModel.videosMdl.observe(viewLifecycleOwner) { videos ->
            if(videos.isEmpty()) {
                showNoDataScreen()
            } else {
                hideLoading()
            }
            var items = videos.sortedByDeletedDate(true)
            binding.tvTotalSize.text = items
                .filter { it is Trash }
                .sumOf { (it as Trash).size }
                .convertToStorageData()
            Log.d(TAG, "onViewCreated: item.size=${items.size}")
            adapter.submit(items)
        }
    }


    override fun onItemLongClick(v: View, position: Int)  {
        Log.d(TAG, "onItemLongClick() called with: v = $v, position = $position")
        val trash = adapter.getItem(position) as Trash
        MenuMoreOptionFragment.newInstance(R.layout.fragment_trash_item_option) { v ->
            Log.d(TAG, "onItemLongClick() called with: v = $v")
            when(v.id) {
                R.id.btn_restore -> {
                    if(trash.isSecured) {
                        viewModel.restore(trash)
                    } else {
                        fileManager.removeFromInternal(trash.title!!) {
                            viewModel.restore(trash)
                        }
                    }
                }
                R.id.btn_delete -> {
                    AlertDialogManager.createDeleteAlertDialog(requireContext(),
                        getString(R.string.app_name), "Would you want to delete \"${trash.title}\"?") {
                        viewModel.viewModelScope.launch(Dispatchers.IO) {
                            if(context?.deleteFile(trash.title) == true) {
                                viewModel.delete(trash)
                            }
                        }
                    }.show()
                }
            }
        }.show(requireActivity().supportFragmentManager, TAG)
    }


    override fun onDeletedResult(result: ActivityResult) {
        Logs.d("onDeletedResult() called with: result = $result")
        if(result.resultCode == Activity.RESULT_OK) {
            handlingVideoTask?.succeed
            handlingVideoTask = null
        }
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
                val items = adapter.getSelectedItems()
                if(items.isEmpty()) {
                    Log.d(TAG, "onOptionsItemSelected: video list is empty!")
                    requireContext().showMessage("You need to choose at least one item.")
                    return true
                }
                AlertDialogManager.createDeleteAlertDialog(
                    requireContext(), getString(R.string.app_name), "Would you like to remove ${items.size} files") {
                    showLoadingScreen()
                    lifecycleScope.launch(Dispatchers.IO) {
                        items.filter { it is Trash }
                            .map { it as Trash }
                            .forEach { trash ->
                                if (context?.deleteFile(trash.title) == true) {
                                    viewModel.delete(trash)
                                }
                            }
                        hideLoading()
                    }

                }.show()
            }

            R.id.it_restore -> {
                val items = adapter.getSelectedItems()
                if(items.isEmpty()) {
                    Log.d(TAG, "onOptionsItemSelected: video list is empty!")
                    requireContext().showMessage("You need to choose at least one item.")
                    return true
                }
                showLoadingScreen()
                lifecycleScope.launch(Dispatchers.IO) {
                    items.filterIsInstance<Trash>().forEach { trash ->
                        if(trash.isSecured) {
                            viewModel.restore(trash)
                        } else {
                            fileManager.removeFromInternal(trash.title!!) {
                                viewModel.restore(trash)
                            }
                        }
                    }
                    hideLoading()
                }

            }
        }
        return super.onOptionsItemSelected(item)
    }


    companion object {
        val TAG: String = RecycleBinFragment::class.java.simpleName
    }

}

