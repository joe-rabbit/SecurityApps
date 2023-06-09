package com.example.securityapps

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.securityapps.databinding.ActivitySignUpAcitivityBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpAcitivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpAcitivityBinding
    private lateinit var  firebaseAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySignUpAcitivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        binding.textView.setOnClickListener {
            val intent = Intent(this,SignInActivity::class.java)
            startActivity(intent)
        }
        binding.button.setOnClickListener{
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()
            val confirmPass = binding.confirmPassEt.text.toString()
            if(email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty())
            {
                if(pass== confirmPass)
                {
                    firebaseAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener{
                        if(it.isSuccessful)
                        {
                            val intent = Intent(this,SignInActivity::class.java)
                            startActivity(intent)
                        }
                        else
                        {
                            Toast.makeText(this,it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else
                {
                    Toast.makeText(this,"Password is not matching", Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                Toast.makeText(this,"Empty fields are not allowed!!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}