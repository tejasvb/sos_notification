package com.example.traffic_accident_detection_using_accelerometer.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.traffic_accident_detection_using_accelerometer.R
import com.example.traffic_accident_detection_using_accelerometer.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityLoginBinding
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        binding.login.setOnClickListener(this)
        binding.doNotHaveAccount.setOnClickListener(this)
        val view = binding.root
        setContentView(view)
    }
    private fun logIn(email: String, password: String) {
        when {
            email.isEmpty() -> {
                Toast.makeText(this, "please Enter email", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                return
            }
            password.isEmpty() -> {
                Toast.makeText(this, "please enter password", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                return
            }
            !email.trim().matches(emailPattern.toRegex()) -> {
                Toast.makeText(this, "Email is badly formatted ", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                return
            }
        }
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "login successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }

    }
    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val inputMethodManager =
                getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.login -> {
                binding.progressBar.visibility = View.VISIBLE
                closeKeyboard()
                logIn(binding.email.text.toString(), binding.password.text.toString())
            }
            R.id.already_have_account -> {
                val intent = Intent(this, RegistrationActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}