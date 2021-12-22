/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyra.krpytapplication.view.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.ViewPager
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.getFileType
import com.pyra.krpytapplication.domain.UploadStatus
import com.pyra.krpytapplication.viewmodel.GalleryViewModel
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File

val EXTENSION_WHITELIST = arrayOf("JPG")

/** Fragment used to present the user with a gallery of photos taken */
class GalleryFragment internal constructor() : Fragment() {

    /** AndroidX navigation arguments */
    private val args: GalleryFragmentArgs by navArgs()

    private val mediaList: MutableList<File> by lazy { mutableListOf<File>() }

    private val galleryViewModel by viewModels<GalleryViewModel>()

    /** Adapter class used to present a fragment containing one photo or video as a page */
    inner class MediaPagerAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = mediaList.size
        override fun getItem(position: Int): Fragment = PhotoFragment.create(mediaList[position])
        override fun getItemPosition(obj: Any): Int = POSITION_NONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mark this as a retain fragment, so the lifecycle does not get restarted on config change
        retainInstance = true

        // Get root directory of media from navigation arguments
        val rootDirectory = File(args.rootDirectory)
        mediaList.add(rootDirectory)

        // Walk through all files in the root directory
        // We reverse the order of the list to present the last photos first
//        mediaList = rootDirectory.listFiles { file ->
//            EXTENSION_WHITELIST.contains(file.extension.toUpperCase(Locale.ROOT))
//        }?.sortedDescending()?.toMutableList() ?: mutableListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_gallery, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Checking media files list
        if (mediaList.isEmpty()) {
            view.findViewById<ImageButton>(R.id.delete_button).isEnabled = false
            view.findViewById<ImageButton>(R.id.share_button).isEnabled = false
        }

        // Populate the ViewPager and implement a cache of two media items
        val mediaViewPager = view.findViewById<ViewPager>(R.id.photo_view_pager).apply {
            offscreenPageLimit = 2
            adapter = MediaPagerAdapter(childFragmentManager)
        }


        lifecycleScope.launch {
            galleryViewModel.awsUploadData.collect {
                when (it) {
                    UploadStatus.Loading -> {
                        imageUpldProgressBar.visibility = View.VISIBLE
                    }
                    is UploadStatus.Success -> {
                        imageUpldProgressBar.visibility = View.GONE
                        it.awsUploadCompleted.fileUrl?.let { url ->
                            Intent().apply {
                                putExtra(
                                    Constants.IntentKeys.FILE,
                                    it.awsUploadCompleted.file?.absolutePath
                                )
                                putExtra(Constants.IntentKeys.MEDIAURL, url)
                                putExtra(
                                    Constants.IntentKeys.THUMBURL,
                                    it.awsUploadCompleted.thumbUrl
                                )
                                requireActivity().setResult(RESULT_OK, this)
                                requireActivity().finish()
                            }
                        }
                    }
                    is UploadStatus.Error -> {
                        imageUpldProgressBar.visibility = View.GONE
                        Toast.makeText(requireActivity(), it.error, Toast.LENGTH_SHORT).show()
                    }
                    UploadStatus.Empty -> {
                        imageUpldProgressBar.visibility = View.GONE
                    }
                }


            }
        }

        // Make sure that the cutout "safe area" avoids the screen notch if any
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Use extension method to pad "inside" view containing UI using display cutout's bounds
            view.findViewById<ConstraintLayout>(R.id.cutout_safe_area).padWithDisplayCutout()
        }

        // Handle back button press
        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.fragment_container).navigateUp()
        }

        // Handle share button press
        view.findViewById<ImageButton>(R.id.share_button).setOnClickListener {

            mediaList.getOrNull(mediaViewPager.currentItem)?.let { mediaFile ->

                if (args.IsVault) {
                    galleryViewModel.uploadImageFullyCompressed(mediaFile, mediaFile.getFileType())
                } else {
                    Intent().apply {
                        putExtra(Constants.IntentKeys.FILE, mediaFile.absolutePath)
                        requireActivity().setResult(RESULT_OK, this)
                        requireActivity().finish()
                    }
                }
//                // Create a sharing intent
//                val intent = Intent().apply {
//                    // Infer media type from file extension
//                    val mediaType = MimeTypeMap.getSingleton()
//                            .getMimeTypeFromExtension(mediaFile.extension)
//                    // Get URI from our FileProvider implementation
//                    val uri = FileProvider.getUriForFile(
//                            view.context, "com.krypt.chat.provider", mediaFile)
//                    // Set the appropriate intent extra, type, action and flags
//                    putExtra(Intent.EXTRA_STREAM, uri)
//                    type = mediaType
//                    action = Intent.ACTION_SEND
//                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//                }
//
//                // Launch the intent letting the user choose which app to share with
//                startActivity(Intent.createChooser(intent, getString(R.string.share_hint)))
            }
        }

        // Handle delete button press
        view.findViewById<ImageButton>(R.id.delete_button).setOnClickListener {

            mediaList.getOrNull(mediaViewPager.currentItem)?.let { mediaFile ->

                AlertDialog.Builder(view.context, android.R.style.Theme_Material_Dialog)
                    .setTitle(getString(R.string.delete_title))
                    .setMessage(getString(R.string.delete_dialog))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes) { _, _ ->

                        // Delete current photo
                        mediaFile.delete()

                        // Send relevant broadcast to notify other apps of deletion
                        MediaScannerConnection.scanFile(
                            view.context, arrayOf(mediaFile.absolutePath), null, null
                        )

                        // Notify our view pager
                        mediaList.removeAt(mediaViewPager.currentItem)
                        mediaViewPager.adapter?.notifyDataSetChanged()

                        // If all photos have been deleted, return to camera
                        if (mediaList.isEmpty()) {
                            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                                .navigateUp()
                        }

                    }

                    .setNegativeButton(android.R.string.no, null)
                    .create().showImmersive()
            }
        }
    }
}
