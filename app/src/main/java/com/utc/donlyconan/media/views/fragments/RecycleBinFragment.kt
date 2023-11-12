package com.utc.donlyconan.media.views.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.WorkerThread
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.services.FileService
import com.utc.donlyconan.media.app.utils.AlertDialogManager
import com.utc.donlyconan.media.app.utils.convertToStorageData
import com.utc.donlyconan.media.app.utils.sortedByDeletedDate
import com.utc.donlyconan.media.data.models.Trash
import com.utc.donlyconan.media.data.repo.TrashRepository
import com.utc.donlyconan.media.databinding.FragmentTrashBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.extension.components.getVideoInfo
import com.utc.donlyconan.media.viewmodels.TrashViewModel
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.adapter.OnItemLongClickListener
import com.utc.donlyconan.media.views.adapter.RecycleBinAdapter
import com.utc.donlyconan.media.views.fragments.options.MenuMoreOptionFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * Show list of video that was temporarily deleted by user
 */
class RecycleBinFragment : BaseFragment(), OnItemLongClickListener {

    @Inject lateinit var trashRepo: TrashRepository
    val binding by lazy { FragmentTrashBinding.inflate(layoutInflater) }
    private lateinit var adapter: RecycleBinAdapter
    private var locking: Boolean = false

    private val viewModel by viewModels<TrashViewModel> {
        viewModelFactory {
            initializer {
                TrashViewModel(
                    appComponent.getTrashDao(),
                    appComponent.getVideoDao()
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
        executeOnFileService {
            syncRecycleBin()
        }
        application.getFileService()?.registerOnFileServiceListener(onFileServiceListener)
    }

    private val onFileServiceListener = object : FileService.OnFileServiceListener {

        override fun onError(e: Throwable?) {
            Log.d(TAG, "onError() called with: e = $e")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        application.getFileService()?.unregisterOnFileServiceListener(onFileServiceListener)
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
            } else if(!locking) {
                hideLoading()
            }
            var items = videos.sortedByDeletedDate(true)
            binding.tvTotalSize.text = items
                .filterIsInstance<Trash>()
                .sumOf { it.size }
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
            when (v.id) {
                R.id.btn_restore -> lifecycleScope.launch(Dispatchers.Main) {
                    showRestoreDialog(trash)
                }
                R.id.btn_delete -> {
                    showDeletingDialog(trash)
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
        val items = adapter.getSelectedItems()
        if(items.isEmpty()) {
            Log.d(TAG, "onOptionsItemSelected: video list is empty!")
            showToast("You need to choose at least one item.")
            return true
        }
        if(item.itemId == R.id.it_trash) {
            showDeletingDialog(*items.filterIsInstance<Trash>().toTypedArray())
        }
        if (item.itemId == R.id.it_restore) {
            runOnWorkerThread {
                showRestoreDialog(*items.filterIsInstance<Trash>().toTypedArray())
            }
        }
       return true
    }

    private fun showDeletingDialog(vararg trash: Trash) {
        Log.d(TAG, "deleteFiles() called with: trash = $trash")
        AlertDialogManager.createDeleteAlertDialog(
            context = requireContext(),
            title = getString(R.string.app_name),
            msg = "Would you like to remove ${trash.size} files",
            onAccept = {
                executeOnFileService {
                    locking = true
                    showLoadingScreen()
                    viewModel.delete(this, *trash)
                }?.invokeOnCompletion {
                    Log.d(TAG, "invokeOnCompletion() it_trash")
                    hideLoading()
                    Snackbar.make(binding.root, "The files is deleted.", Snackbar.LENGTH_SHORT)
                        .show()
                    locking = false
                }

            }).show()
    }

    @WorkerThread
    private suspend fun showRestoreDialog(vararg trash: Trash) {
        Log.d(TAG, "showRestoreDialog() called with: trash = $trash")
        val existedFiles = trash.filter { it.externalUri != null }
            .filter { context?.contentResolver?.getVideoInfo(it.externalUri!!.toUri()) != null }
        if(existedFiles.isEmpty()) {
            restoreFiles(*trash)
        } else {
            runOnUIThread {
                AlertDialogManager.createDeleteAlertDialog(
                    context = requireContext(),
                    title = getString(R.string.app_name),
                    msg = "There are ${trash.size} files that is existed on your device. Do you want to continue restoring?",
                    onAccept = {
                        restoreFiles(*trash)
                    },
                    onDeny = {
                        trash.toMutableList().apply {
                            removeAll(existedFiles)
                            restoreFiles(*toTypedArray())
                        }
                    }
                ).show()
            }
        }
    }

    @WorkerThread
    private fun restoreFiles(vararg trash: Trash) {
        Log.d(TAG, "restoreFiles() called with: trash = $trash")
        var occuredError = false
        executeOnFileService {
            locking = true
            showLoadingScreen()
            viewModel.restore(this, *trash){e -> occuredError = true }
        }?.invokeOnCompletion {
            Log.d(TAG, "invokeOnCompletion() restoreFiles")
            hideLoading()
            if (it?.cause is IOException || occuredError || trash.isEmpty()) {
                Log.e(TAG, "restoreFiles: ", it)
                showToast(R.string.have_had_some_problems_when_restoring_files)
            } else {
                Snackbar.make(binding.root, R.string.the_files_is_restored, Snackbar.LENGTH_SHORT)
                    .show()
            }
            locking = false
        }
    }

    override fun onErrorOccurred(context: CoroutineContext, e: Throwable) {
        Log.d(TAG, "onErrorOccurred() called with: context = $context, e = $e")
        e.message?.let { msg ->
            showToast(msg)
        }
    }


    companion object {
        val TAG: String = RecycleBinFragment::class.java.simpleName
    }

}

