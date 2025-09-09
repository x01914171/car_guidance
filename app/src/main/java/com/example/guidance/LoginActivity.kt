package com.example.guidance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity

class LoginActivity : ComponentActivity() {
    lateinit var usernameEdit: EditText
    lateinit var passwordEdit: EditText
    lateinit var loginButton: Button
    lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        usernameEdit = findViewById(R.id.username)
        passwordEdit = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        
        loginButton.setOnClickListener {
            val username = usernameEdit.text.toString()
            val password = passwordEdit.text.toString()
            
            if (username.isNotEmpty() && password.isNotEmpty()) {
                // 简单的登录验证逻辑
                if (username == "admin" && password == "123456") {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show()
            }
        }
        
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}