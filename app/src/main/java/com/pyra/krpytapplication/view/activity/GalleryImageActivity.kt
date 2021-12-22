package com.pyra.krpytapplication.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.MessageType
import com.pyra.krpytapplication.utils.toMessageString
import com.pyra.krpytapplication.roomDb.AppDataBase
import com.pyra.krpytapplication.roomDb.entity.ChatMessagesSchema
import com.pyra.krpytapplication.view.adapter.GalleryImageViewAdapter
import com.pyra.krpytapplication.view.fragment.clickWithDebounce
import com.pyra.krpytapplication.viewmodel.GalleryViewModel
import kotlinx.android.synthetic.main.activity_image_gallery.*
import kotlinx.coroutines.launch
import java.util.*

class GalleryImageActivity : BaseActivity() {

    var imageViewAdapter: GalleryImageViewAdapter? = null

    val viewModel by viewModels<GalleryViewModel>()


    private val database by lazy {
        AppDataBase.getInstance(this)?.chatMessagesDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_gallery)

        cameraFAB.clickWithDebounce {
            Intent(this, CameraActivity::class.java).apply {
                putExtra(Constants.IntentKeys.ISVIDEOAVAILABLE, true)
                putExtra(Constants.IntentKeys.ISUPLOADAVAILABLE, true)
                putExtra(Constants.IntentKeys.ISVAULT, true)
                startActivityForResult(this, Constants.RequestCode.CAMERA_INTENT)
            }
        }

        initAdapter()
    }

    private fun initAdapter() {
        initObservers()
        setImageAdapter()
        viewModel.getImageList()
    }

    private fun setImageAdapter() {

        val linearLayoutManager = GridLayoutManager(this, 3)
        imageViewAdapter = GalleryImageViewAdapter(this, false, viewModel) {

//            viewModel.sendImage(it, false)

            val intent = Intent()
            intent.putExtra("imageUrl", viewModel.getImageUrl(it))
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        imageRecycler.layoutManager = linearLayoutManager
        imageRecycler.adapter = imageViewAdapter

    }

    private fun initObservers() {

        viewModel.imageList.observe(this, Observer {
            imageViewAdapter?.notifyDataChanged()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.RequestCode.CAMERA_INTENT -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.let {
                        if (!it.getStringExtra(Constants.IntentKeys.FILE).isNullOrEmpty()) {
                            if (it.getBooleanExtra(Constants.IntentKeys.ISVIDEOAVAILABLE, false)) {
                                it.getStringExtra(Constants.IntentKeys.FILE)?.let { _ ->
                                    it.getStringExtra(Constants.IntentKeys.FILE)?.let { file ->
                                        it.getStringExtra(Constants.IntentKeys.MEDIAURL)
                                            ?.let { mediaUrll ->
                                                it.getStringExtra(Constants.IntentKeys.THUMBURL)
                                                    ?.let { thumbUrll ->

                                                        with(ChatMessagesSchema()) {
                                                            messageId = UUID.randomUUID().toString()
                                                            message = ""
                                                            messageType =
                                                                MessageType.VIDEO.toMessageString()
                                                            messageStatus = ""
                                                            messageTime =
                                                                System.currentTimeMillis()
                                                                    .toString()
                                                            isSender = false
                                                            roomId = ""
                                                            isDeleted = false
                                                            isEdited = false
                                                            isUploaded = false
                                                            kryptId = ""
                                                            userImage = ""
                                                            userName = ""
                                                            mediaUrl = mediaUrll
                                                            mediaThumbUrl = thumbUrll
                                                            localMediaPath = file
                                                            lifecycleScope.launch {
                                                                database?.insert(this@with)
                                                            }

                                                        }
                                                    }
                                            }


                                    }

                                }
                            } else {
                                it.getStringExtra(Constants.IntentKeys.FILE)?.let { file ->

                                    it.getStringExtra(Constants.IntentKeys.MEDIAURL)
                                        ?.let { mediaUrll ->
                                            it.getStringExtra(Constants.IntentKeys.THUMBURL)
                                                ?.let { thumbUrll ->

                                                    with(ChatMessagesSchema()) {
                                                        messageId = UUID.randomUUID().toString()
                                                        message = ""
                                                        messageType =
                                                            MessageType.IMAGE.toMessageString()
                                                        messageStatus = ""
                                                        messageTime =
                                                            System.currentTimeMillis().toString()
                                                        isSender = false
                                                        roomId = ""
                                                        isDeleted = false
                                                        isEdited = false
                                                        isUploaded = false
                                                        kryptId = ""
                                                        mediaUrl = mediaUrll
                                                        userImage = ""
                                                        userName = ""
                                                        mediaThumbUrl = thumbUrll
                                                        localMediaPath = file
                                                        lifecycleScope.launch {
                                                            database?.insert(this@with)
                                                        }

                                                    }
                                                }
                                        }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


}