package com.bryanherbst.fragmentstate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val STATE_SAVE_STATE = "save_state"
        private const val STATE_KEEP_FRAGS = "keep_frags"
        private const val STATE_HELPER = "helper"
    }

    private lateinit var stateHelper: FragmentStateHelper

    private val fragments = mutableMapOf<Int, Fragment>()

    private val navigationSelectionListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val newFragment = if (keep_switch.isChecked)  {
            fragments[item.itemId] ?: SampleFragment()
        } else {
            // We are pretending we aren't keeping the Fragments in memory
            SampleFragment()
        }
        fragments[item.itemId] = newFragment

        if (state_switch.isChecked && navigation.selectedItemId != 0) {
            saveCurrentState()
            stateHelper.restoreState(newFragment, item.itemId)
        }

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, newFragment)
                .commitNowAllowingStateLoss()

         true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        stateHelper = FragmentStateHelper(supportFragmentManager)

        navigation.setOnNavigationItemSelectedListener(navigationSelectionListener)

        if (savedInstanceState == null) {
            navigation.selectedItemId = R.id.navigation_home
        } else {
            state_switch.isChecked = savedInstanceState.getBoolean(STATE_SAVE_STATE)
            keep_switch.isChecked = savedInstanceState.getBoolean(STATE_KEEP_FRAGS)

            val helperState = savedInstanceState.getBundle(STATE_HELPER)
            stateHelper.restoreHelperState(helperState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Make sure we save the current tab's state too!
        saveCurrentState()

        outState.putBoolean(STATE_SAVE_STATE, state_switch.isChecked)
        outState.putBoolean(STATE_KEEP_FRAGS, keep_switch.isChecked)
        outState.putBundle(STATE_HELPER, stateHelper.saveHelperState())

        super.onSaveInstanceState(outState)
    }

    private fun saveCurrentState() {
        fragments[navigation.selectedItemId]?.let { oldFragment->
            stateHelper.saveState(oldFragment, navigation.selectedItemId)
        }
    }
}
