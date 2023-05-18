package com.example.traffic_accident_detection_using_accelerometer.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.traffic_accident_detection_using_accelerometer.databinding.ActivityRegistrationBinding
import com.example.traffic_accident_detection_using_accelerometer.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    private val emailPattern: String
        get() = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)

        binding.alreadyHaveAccount.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.signup.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            performRegister(
                binding.name.text.toString(),
                binding.email.text.toString(),
                binding.phoneNumber.text.toString(),
                binding.password.text.toString()
            )
            closeKeyboard()
        }
        val view = binding.root
        setContentView(view)
    }

    private fun performRegister(
        name: String,
        email: String,
        phoneNumber: String,
        password: String

    ) {

        when {
            name.isEmpty() -> {
                Toast.makeText(this, "please Enter username", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                return
            }
            email.isEmpty() -> {
                Toast.makeText(this, "please Enter email", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                return
            }
            phoneNumber.isEmpty() -> {
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

            else -> {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        Toast.makeText(this, "sign in successfully", Toast.LENGTH_SHORT).show()

                        saveUserToFirebaseDatabase(email, name, phoneNumber)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                    }
            }
        }
    }

    private fun saveUserToFirebaseDatabase(email: String, name: String, phoneNumber: String) {
        if(FirebaseAuth.getInstance().uid==null){
            Toast.makeText(this, "Some error", Toast.LENGTH_SHORT).show()
            return
        }
        val uid = FirebaseAuth.getInstance().uid ?: ""

        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, email, name, phoneNumber,"client")

        ref.setValue(user)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)


            }
            .addOnFailureListener {
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
}
