package com.pyra.krpytapplication.incognitokeyboard

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.media.AudioManager
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputConnection
import com.pyra.krpytapplication.R

class KryptIncognitoKeyboard : InputMethodService(), OnKeyboardActionListener {
    private var keyboardView: KeyboardView? = null
    private var keyboard: Keyboard? = null
    private var capsClickCount = 0
    var keyboardKeyList: KeyboardKeyList? = null
    var isAlphaKeybordVisible = true
    var isSymbol1KeyboardVisible = false
    var KeyValueList: List<String>? = null
    var alphaNumerickey = "ABC"
    var symbolNumericKey = "?#1"
    var symbol1key = "@:$"
    var symbol2key = "{|["
    override fun onCreateInputView(): View {
        keyboardKeyList = KeyboardKeyList(this)
        KeyValueList = keyboardKeyList!!.alphaKeyboardKeyList
        keyboardView = layoutInflater.inflate(R.layout.keyboard_layout, null) as KeyboardView?
        keyboard = Keyboard(this, R.xml.keyboard_ui)
        keyboardView!!.keyboard = keyboard
        keyboardView!!.setOnKeyboardActionListener(this)
        keyboardView!!.invalidateAllKeys()
        return keyboardView!!
    }

    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onKey(primaryCode: Int, ints: IntArray) {
        val inputConnection = currentInputConnection
        playClick(primaryCode)
        when (primaryCode) {
            else -> if (primaryCode >= 1000) {
                if (primaryCode == 1000) {
                    if (isAlphaKeybordVisible) {
                        updateKeyCase()
                    } else if (isSymbol1KeyboardVisible) {
                        enableSymbol2Keyboard()
                    } else {
                        enableSymbol1Keyboard()
                    }
                } else if (primaryCode == 1001) {
                    deletePreviousChar(inputConnection)
                } else if (primaryCode == 1002) {
                    if (isAlphaKeybordVisible) {
                        enableSymbol1Keyboard()
                    } else {
                        enableAlphaKeyboard()
                    }
                } else if (primaryCode == 1003) {
                    spaceKey(inputConnection)
                } else if (primaryCode == 1004) {
                    doneKey(inputConnection)
                }
            } else {
                printChar(primaryCode, inputConnection)
            }
        }
    }

    private fun spaceKey(inputConnection: InputConnection) {
        val space = 32.toChar()
        inputConnection.commitText(space.toString(), 1)
    }

    private fun changeKeyboard(labelList: List<String>, str1: String, str2: String) {
        KeyValueList = labelList
        if (keyboardView == null) return
        val keys = keyboard!!.keys
        keys[38].label = str1
        keys[29].label = str2
        for (i in labelList.indices) {
            if (i == 29 || i == 37 || i == 38 || i == 40) continue
            keys[i].label = labelList[i]
        }
        keyboardView!!.invalidateAllKeys()
    }

    private fun printChar(primaryCode: Int, inputConnection: InputConnection) {
        if (capsClickCount == 0) {
            val str = KeyValueList!![primaryCode]
            inputConnection.commitText(str, 1)
        } else {
            val str = KeyValueList!![primaryCode]
            inputConnection.commitText(str.toUpperCase(), 1)
            if (capsClickCount == 1) {
                resetKeyCase()
            }
        }
    }

    private fun doneKey(inputConnection: InputConnection) {
        inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
    }

    private fun updateKeyCase() {
        if (capsClickCount == 2) {
            resetKeyCase()
        } else {
            updateShiftKey(true)
            incrementCapsClickCount()
        }
    }

    private fun updateShiftKeyLabelActive() {
        val key = keyboard!!.keys
        key[29].label = getString(R.string.keyboard_key_caps_key_selected)
        keyboardView!!.invalidateKey(29)
    }

    private fun updateShiftKeyLabelIdle() {
        val key = keyboard!!.keys
        key[29].label = getString(R.string.keyboard_key_caps_key_idle)
        keyboardView!!.invalidateKey(29)
    }

    private fun updateShiftKey(isCaps: Boolean) {
        updateShiftKeyLabelActive()
        keyboard!!.isShifted = isCaps
        keyboardView!!.invalidateAllKeys()
    }

    private fun resetKeyCase() {
        capsClickCount = 0
        updateShiftKey(false)
        updateShiftKeyLabelIdle()
    }

    private fun incrementCapsClickCount() {
        capsClickCount++
    }

    private fun deletePreviousChar(inputConnection: InputConnection) {
        inputConnection.deleteSurroundingText(1, 0)
    }

    private fun enableSymbol2Keyboard() {
        changeKeyboard(keyboardKeyList!!.symbolKeyboardKeyList2, alphaNumerickey, symbol1key)
        isSymbol1KeyboardVisible = false
        isAlphaKeybordVisible = false
    }

    private fun enableAlphaKeyboard() {
        resetKeyCase()
        changeKeyboard(
            keyboardKeyList!!.alphaKeyboardKeyList,
            symbolNumericKey,
            getString(R.string.keyboard_key_caps_key_idle)
        )
        isSymbol1KeyboardVisible = false
        isAlphaKeybordVisible = true
    }

    private fun enableSymbol1Keyboard() {
        resetKeyCase()
        changeKeyboard(keyboardKeyList!!.symbolKeyboardKeyList1, alphaNumerickey, symbol2key)
        isSymbol1KeyboardVisible = true
        isAlphaKeybordVisible = false
    }

    private fun playClick(i: Int) {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        when (i) {
            32 -> audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR)
            Keyboard.KEYCODE_DONE, 10 -> audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN)
            Keyboard.KEYCODE_DELETE -> audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE)
            else -> audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
        }
    }

    override fun onText(text: CharSequence) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}