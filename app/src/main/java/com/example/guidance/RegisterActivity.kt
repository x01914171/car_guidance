package com.example.guidance

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity

class RegisterActivity : ComponentActivity() {
    lateinit var usernameEdit: EditText
    lateinit var passwordEdit: EditText
    lateinit var confirmPasswordEdit: EditText
    lateinit var registerButton: Button
    lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        usernameEdit = findViewById(R.id.username)
        passwordEdit = findViewById(R.id.password)
        confirmPasswordEdit = findViewById(R.id.confirmPassword)
        registerButton = findViewById(R.id.registerButton)
        backButton = findViewById(R.id.backButton)
        
        registerButton.setOnClickListener {
            val username = usernameEdit.text.toString()
            val password = passwordEdit.text.toString()
            val confirmPassword = confirmPasswordEdit.text.toString()
            
            when {
                username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show()
                }
                password != confirmPassword -> {
                    Toast.makeText(this, "两次密码输入不一致", Toast.LENGTH_SHORT).show()
                }
                password.length < 6 -> {
                    Toast.makeText(this, "密码至少6位", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // 注册成功逻辑
                    Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        
        backButton.setOnClickListener {
            finish()
        }
    }
}