package com.java.mashihe;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import adapter.LoginFragment;
import adapter.SignupFragment;
import background.Client;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private FrameLayout frameLayout;
    private FragmentManager fragmentManager;
    public static Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        frameLayout = findViewById(R.id.fl_login);

        if (client == null) {
            client = Client.getInstance();
            client.setTimeout(3000);
            if (!MainActivity.isNetworkConnected(LoginActivity.this)) {
                Toast.makeText(LoginActivity.this, "网络连接失败！", Toast.LENGTH_SHORT).show();
            } else {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        client.init();
                    }
                });
                thread.start();
                while (thread.isAlive()) {

                }
            }
        }

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fl_login, new SignupFragment(LoginActivity.this),"signup").commitAllowingStateLoss();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login_tosignup:
                fragmentManager.beginTransaction().replace(R.id.fl_login, new SignupFragment(LoginActivity.this), "signup").commitAllowingStateLoss();
                break;
            case R.id.btn_signup_tologin:
                fragmentManager.beginTransaction().replace(R.id.fl_login, new LoginFragment(LoginActivity.this), "login").commitAllowingStateLoss();
                break;
        }
    }

    public void login(int action, int result) {

    }
}
