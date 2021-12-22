package com.pyra.krpytapplication.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.apaar97.translate.MainTranslatorActivity
import com.example.flightmode.MainFlightActivity
import com.macdems.disturbnow.SettingsDndActivity
import com.nicoqueijo.android.currencyconverter.kotlin.view.MainActivityCurrency
import com.oriondev.moneywallet.ui.activity.LauncherActivity
import com.physphil.android.unitconverterultimate.MainActivityConverter
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.LogUtil
import com.simplemobiletools.calculator.activities.MainCalculatorActivity
import com.simplemobiletools.clock.activities.MainClockActivity
import com.simplemobiletools.flashlight.activities.MainFlashActivity
import kotlinx.android.synthetic.main.fragment_more_menu.*

class MoreMenuFragment : Fragment(R.layout.fragment_more_menu) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clockLayout.setOnClickListener {
            LogUtil.d("MoreMenuFragment", "Clock clicked!")
            startActivity(Intent(requireContext(), MainClockActivity::class.java))
        }

        torchLayout.setOnClickListener {
            LogUtil.d("MoreMenuFragment", "Torch clicked!")
            startActivity(Intent(requireContext(), MainFlashActivity::class.java))
        }

        calculatorLayout.setOnClickListener {
            LogUtil.d("MoreMenuFragment", "Calculator clicked!")
            startActivity(Intent(requireContext(), MainCalculatorActivity::class.java))
        }

        translatorLayout.setOnClickListener {
            LogUtil.d("MoreMenuFragment", "Translator clicked!")
            startActivity(Intent(requireContext(), MainTranslatorActivity::class.java))
        }

        unitConverterLayout.setOnClickListener {
            LogUtil.d("MoreMenuFragment", "Unit Converter clicked!")
            startActivity(Intent(requireContext(), MainActivityConverter::class.java))
        }

        dndLayout.setOnClickListener {
            LogUtil.d("MoreMenuFragment", "Do not disturb clicked!")
            startActivity(Intent(requireContext(), SettingsDndActivity::class.java))
        }

        airplaneLayout.setOnClickListener {
            LogUtil.d("MoreMenuFragment", "Airplane clicked!")
            startActivity(Intent(requireContext(), MainFlightActivity::class.java))
        }

        settingLayout.setOnClickListener {
            LogUtil.d("MoreMenuFragment", "Factory reset clicked!")
//            startActivity(Intent(requireContext(), MainFlightActivity::class.java))
        }

        wallet.setOnClickListener {
            LogUtil.d("MoreMenuFragment", "Wallet clicked!")
            startActivity(Intent(requireContext(), LauncherActivity::class.java))
        }

        calendarLayout.setOnClickListener {
            LogUtil.d("MoreMenuFragment", "Calendar clicked!")
//            startActivity(Intent(requireContext(), MainFlightActivity::class.java))
        }
        currencyConverter.setOnClickListener {
            LogUtil.d("MoreMenuFragment", "Currency Converter clicked!")
            startActivity(Intent(requireContext(), MainActivityCurrency::class.java))
        }


    }

}
