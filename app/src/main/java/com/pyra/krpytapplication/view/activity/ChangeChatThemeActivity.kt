package com.pyra.krpytapplication.view.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.view.adapter.ChatThemeListAdapter
import kotlinx.android.synthetic.main.activity_change_chat_theme.*
import kotlinx.android.synthetic.main.activity_change_chat_theme.backButton
import setColorToBackground
import java.util.ArrayList

class ChangeChatThemeActivity : BaseActivity() {

    var selectdbubleColor: String = ""
    var selectdbubleColorReciver: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_chat_theme)

        listener()
        setChatTheme()
        setChatThemeReceiver()
    }

    private fun listener() {
        backButton.setOnClickListener {
            onBackPressed()
        }

        if (!SharedHelper(this).chatBubbleColor.equals("", ignoreCase = true)) {

            val value = SharedHelper(this).chatBubbleColor

            setColorToBackground(senderText.background, value)
            setColorToBackground(senderCorner.background, value)
        }

        if (!SharedHelper(this).chatBubbleColorReciver.equals("", ignoreCase = true)) {
            val value = SharedHelper(this).chatBubbleColorReciver

            setColorToBackground(receiverText.background, value)
            setColorToBackground(receiverCorner.background, value)
        }
    }

    private fun setChatTheme() {

        val colorList: ArrayList<String> = arrayListOf(
            "#0B68DF",
            "#D63EA1",
            "#903ED6",
            "#3E78D6",
            "#5E53EB",
            "#07905B",
            "#62960E",
            "#008888",
            "#B17F21"
        )

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)
        linearLayoutManager.stackFromEnd = false
        linearLayoutManager.reverseLayout = false
        linearLayoutManager.isSmoothScrollbarEnabled = true
        val chatThemeListAdapter =
            ChatThemeListAdapter(this, colorList, SharedHelper(this).chatBubbleColor) {
                setThemeColor(it)
            }
        chatThemeList.layoutManager = linearLayoutManager
        chatThemeList?.adapter = chatThemeListAdapter
    }

    private fun setChatThemeReceiver() {

        val colorList: ArrayList<String> = arrayListOf(
            "#0B68DF",
            "#D63EA1",
            "#903ED6",
            "#3E78D6",
            "#5E53EB",
            "#07905B",
            "#62960E",
            "#008888",
            "#B17F21"
        )

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)
        linearLayoutManager.stackFromEnd = false
        linearLayoutManager.reverseLayout = false
        linearLayoutManager.isSmoothScrollbarEnabled = true

        val chatThemeListAdapter =
            ChatThemeListAdapter(this, colorList, SharedHelper(this).chatBubbleColorReciver) {
                setThemeColorReceiver(it)
            }
        chatThemeListReciver.layoutManager = linearLayoutManager
        chatThemeListReciver?.adapter = chatThemeListAdapter
    }

    fun onConfirmClicked(view: View) {
        if (!selectdbubleColor.equals("", ignoreCase = true)) {
            SharedHelper(this).chatBubbleColor = selectdbubleColor
        }

        if (!selectdbubleColorReciver.equals("", ignoreCase = true)) {
            SharedHelper(this).chatBubbleColorReciver = selectdbubleColorReciver
        }

        onBackPressed()
    }

    fun setThemeColor(value: String) {
        selectdbubleColor = value

        setColorToBackground(senderText.background, value)
        setColorToBackground(senderCorner.background, value)
    }

    fun setThemeColorReceiver(value: String) {
        selectdbubleColorReciver = value

        setColorToBackground(receiverText.background, value)
        setColorToBackground(receiverCorner.background, value)
    }

}