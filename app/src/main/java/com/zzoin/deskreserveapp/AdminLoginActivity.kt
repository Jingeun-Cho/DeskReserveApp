package com.zzoin.deskreserveapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.zzoin.deskreserveapp.databinding.ActivityAdminLoginBinding

class AdminLoginActivity : AppCompatActivity() {
    private var firstBackPressTime = 0L
    private val defaultWaitTime = 1500L
    private val auth = Firebase.auth
    private lateinit var binding : ActivityAdminLoginBinding
    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        startActivity(user) // For Test
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initLoginButton()
    }
    private fun initLoginButton(){
        binding.btnLogin.apply {
            setOnClickListener{
                val email = binding.textEmail.text.toString()
                val password = binding.textPassword.text.toString()
                if(email.isNotBlank() && password.isNotBlank()) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //Login Success
                                val user = task.result?.user
                                startActivity(user)
                            }
                            else {
                                //Login fail
//                                Log.e("firebaseAuth", task.result.toString())
                                Snackbar.make(binding.root, "로그인이 정상적으로 되지 않았습니다.", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                }
                else{
                    Snackbar.make(binding.root, "이메일 또는 패스워드를 입력해주세요.", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startActivity (user : FirebaseUser?){
        if(user != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        Log.d("onBackPressed", firstBackPressTime.toString())
        if(firstBackPressTime == 0L || System.currentTimeMillis() - firstBackPressTime > defaultWaitTime){
            firstBackPressTime = System.currentTimeMillis()
            Snackbar.make(binding.root, "한번 더 뒤로가기를 누르면 종료합니다.",Snackbar.LENGTH_SHORT).show()
        }
        else{
            if(System.currentTimeMillis() - firstBackPressTime < defaultWaitTime){
                finish()
            }
        }

    }
}