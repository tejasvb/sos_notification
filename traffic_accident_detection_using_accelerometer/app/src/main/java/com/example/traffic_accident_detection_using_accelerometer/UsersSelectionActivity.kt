package com.example.traffic_accident_detection_using_accelerometer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.traffic_accident_detection_using_accelerometer.databinding.ActivityRegisterHelperBinding
import com.example.traffic_accident_detection_using_accelerometer.databinding.ActivityUsersSelectionBinding

class UsersSelectionActivity : AppCompatActivity() {
    private lateinit var binding:ActivityUsersSelectionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_selection)
        binding = ActivityUsersSelectionBinding.inflate(layoutInflater)
        binding.client.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.helper.setOnClickListener {
            val intent = Intent(this, RegisterHelperActivity::class.java)
            startActivity(intent)
            finish()
        }
        val view = binding.root
        setContentView(view)

    }
}