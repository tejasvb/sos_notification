package com.example.impactAlert.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.impactAlert.R
import com.example.impactAlert.databinding.ActivityUsersSelectionBinding

class UsersSelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUsersSelectionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_selection)
        binding = ActivityUsersSelectionBinding.inflate(layoutInflater)
        binding.client.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }
        binding.helper.setOnClickListener {
            val intent = Intent(this, RegisterHelperActivity::class.java)
            startActivity(intent)
        }
        val view = binding.root
        setContentView(view)

    }
}