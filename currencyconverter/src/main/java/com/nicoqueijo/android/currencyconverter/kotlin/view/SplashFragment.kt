package com.nicoqueijo.android.currencyconverter.kotlin.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.nicoqueijo.android.currencyconverter.R
import com.nicoqueijo.android.currencyconverter.kotlin.viewmodel.SplashViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class SplashFragment : Fragment() {

    private lateinit var viewModel: SplashViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("CurrencyApp", "Splash loaded")
        viewModel = ViewModelProvider(this).get(SplashViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch(Dispatchers.IO) {
            /**
             * Small delay so the user can actually see the splash screen
             * for a moment as feedback of an attempt to retrieve data.
             */
            delay(250)
            try {
                viewModel.initCurrencies()
                withContext(Dispatchers.Main) {
                    Log.d("CurrencyApp", "Navigating to watchlist fragment")
                    findNavController().navigate(R.id.action_splashFragment_to_watchlistFragment)
                }
            } catch (e: IOException) {
                Log.d("CurrencyApp", "Navigating to error fragment ${e.message}")
                withContext(Dispatchers.Main) {
                    findNavController().navigate(R.id.action_splashFragment_to_errorFragment)
                }
                //findNavController().navigate(R.id.action_splashFragment_to_errorFragment)
            }
        }
    }
}
