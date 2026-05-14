package com.aksharadeep.tutor.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aksharadeep.tutor.R;

public class LoginActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AksharaDeepaPref";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_STUDENT_NAME = "studentName";
    private static final String KEY_STUDENT_CLASS = "studentClass";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private EditText etName, etClass, etUsername, etPassword, etLoginUsername, etLoginPassword;
    private LinearLayout layoutLogin, layoutRegister;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if already logged in
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            goToSplash();
            return;
        }

        setContentView(R.layout.activity_login);
        initViews();
    }

    private void initViews() {
        layoutLogin = findViewById(R.id.layout_login);
        layoutRegister = findViewById(R.id.layout_register);

        // Login fields
        etLoginUsername = findViewById(R.id.et_login_username);
        etLoginPassword = findViewById(R.id.et_login_password);
        Button btnLogin = findViewById(R.id.btn_login);
        TextView tvGoRegister = findViewById(R.id.tv_go_register);

        // Register fields
        etName = findViewById(R.id.et_reg_name);
        etClass = findViewById(R.id.et_reg_class);
        etUsername = findViewById(R.id.et_reg_username);
        etPassword = findViewById(R.id.et_reg_password);
        Button btnRegister = findViewById(R.id.btn_register);
        TextView tvGoLogin = findViewById(R.id.tv_go_login);

        btnLogin.setOnClickListener(v -> handleLogin());
        btnRegister.setOnClickListener(v -> handleRegister());

        tvGoRegister.setOnClickListener(v -> switchToRegister());
        tvGoLogin.setOnClickListener(v -> switchToLogin());
    }

    private void handleLogin() {
        String username = etLoginUsername.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedUsername = prefs.getString(KEY_USERNAME, "");
        String savedPassword = prefs.getString(KEY_PASSWORD, "");

        if (username.equals(savedUsername) && password.equals(savedPassword)) {
            prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply();
            Toast.makeText(this, "Welcome back, " + prefs.getString(KEY_STUDENT_NAME, "Student") + "! 👋", Toast.LENGTH_SHORT).show();
            goToSplash();
        } else {
            Toast.makeText(this, "❌ Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleRegister() {
        String name = etName.getText().toString().trim();
        String studentClass = etClass.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { etName.setError("Enter your name"); return; }
        if (TextUtils.isEmpty(studentClass)) { etClass.setError("Enter your class/school"); return; }
        if (TextUtils.isEmpty(username)) { etUsername.setError("Choose a username"); return; }
        if (password.length() < 4) { etPassword.setError("Password must be at least 4 characters"); return; }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_STUDENT_NAME, name)
                .putString(KEY_STUDENT_CLASS, studentClass)
                .putString(KEY_USERNAME, username)
                .putString(KEY_PASSWORD, password)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();

        Toast.makeText(this, "Account created! Welcome, " + name + " 🎉", Toast.LENGTH_LONG).show();
        goToSplash();
    }

    private void switchToRegister() {
        isLoginMode = false;
        layoutLogin.setVisibility(View.GONE);
        layoutRegister.setVisibility(View.VISIBLE);
    }

    private void switchToLogin() {
        isLoginMode = true;
        layoutRegister.setVisibility(View.GONE);
        layoutLogin.setVisibility(View.VISIBLE);
    }

    private void goToSplash() {
        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }
}
