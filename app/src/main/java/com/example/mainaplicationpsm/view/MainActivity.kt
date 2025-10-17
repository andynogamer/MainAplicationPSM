package com.example.mainaplicationpsm.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.mainaplicationpsm.ForumSearch
import com.example.mainaplicationpsm.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView2)
        bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.home -> MainFragment()
                R.id.new_p -> NewForumFragment()
                R.id.search -> ForumSearch()
                R.id.profile -> ProfileFragment()

                else -> MainFragment()
            }
            loadFragment(fragment)
            true
        }

        if (savedInstanceState == null) {
            loadFragment(MainFragment())
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .commit()
    }
}