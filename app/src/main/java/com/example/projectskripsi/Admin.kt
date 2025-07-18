package com.example.projectskripsi

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.projectskripsi.Pelanggan.Home_pelanggan
import com.example.projectskripsi.Pelanggan.Setting_pelanggan
import com.example.projectskripsi.Penjaga.Home_penjaga
import com.example.projectskripsi.Penjaga.Setting_penjaga
import com.example.projectskripsi.config.SessionLogin

class Admin : AppCompatActivity() {
    private lateinit var homeLayout: LinearLayout
    private lateinit var settingLayout: LinearLayout
    private lateinit var homeImg: ImageView
    private lateinit var settingImg: ImageView
    private lateinit var homeTxt: TextView
    private lateinit var settingTxt: TextView
    private var selectedTab: Int = 2
    private lateinit var sessionManager: SessionLogin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionLogin(applicationContext)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin)

        homeLayout = findViewById(R.id.homeLayout)
        settingLayout = findViewById(R.id.settingLayout)

        homeImg = findViewById(R.id.homeImg)
        settingImg = findViewById(R.id.settingImg)

        homeTxt = findViewById(R.id.homeTxt)
        settingTxt = findViewById(R.id.settingTxt)

        setupClickListeners()

        switchToFragment(Home_pelanggan::class.java)
        setSelectedTab(homeImg, homeTxt, homeLayout, R.drawable.home, R.drawable.home)
        selectedTab = 1
    }

    private fun switchToFragment(fragmentClass: Class<out Fragment>) {
        val fragment = fragmentClass.getDeclaredConstructor().newInstance()
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun setSelectedTab(
        imageView: ImageView,
        textView: TextView,
        layout: LinearLayout,
        selectedImg: Int,
        defaultImg: Int
    ) {
        homeTxt.visibility = View.GONE
        settingTxt.visibility = View.GONE

        homeImg.setImageResource(R.drawable.home)
        settingImg.setImageResource(R.drawable.settings)

        homeLayout.setBackgroundColor(resources.getColor(android.R.color.transparent))
        settingLayout.setBackgroundColor(resources.getColor(android.R.color.transparent))

        textView.visibility = View.VISIBLE
        imageView.setImageResource(selectedImg)
        layout.setBackgroundResource(R.drawable.rounded_bottomnav)
    }
    private fun setupClickListeners() {
        homeLayout.setOnClickListener {
            if (selectedTab != 1) {
                switchToFragment(Home_pelanggan::class.java)
                setSelectedTab(homeImg, homeTxt, homeLayout, R.drawable.home, R.drawable.home)
                selectedTab = 1
            }
        }

        settingLayout.setOnClickListener {
            if (selectedTab != 2) {
                switchToFragment(Setting_pelanggan::class.java)
                setSelectedTab(settingImg, settingTxt, settingLayout, R.drawable.settings, R.drawable.settings)
                selectedTab = 2

            }
        }


    }
}