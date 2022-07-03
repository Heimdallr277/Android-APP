package adapter;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.java.mashihe.LoginActivity;
import com.java.mashihe.R;

import popview.LoginPopView;

public class LoginFragment extends Fragment {

    private EditText mEtUsername;
    private EditText mEtPassword;
    private Button mBtnLogin;
    private Button mBtnToSignup;
    private Activity mActivity;
    private LoginPopView loginPopView;
    private LinearLayout rootView;

    public LoginFragment(Activity activity) {
        mActivity = activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment_login, container, false);
        mEtUsername = view.findViewById(R.id.et_login_username);
        mEtPassword = view.findViewById(R.id.et_login_password);
        mBtnLogin = view.findViewById(R.id.btn_login);
        mBtnToSignup = view.findViewById(R.id.btn_login_tosignup);
        rootView = view.findViewById(R.id.ll_login_root);

        // 去注册
        mBtnToSignup.setOnClickListener((LoginActivity)mActivity);

        // 登录
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // 用户名输入框
        mEtUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!isUserNameValid(mEtUsername.getText().toString())) {
                    mEtUsername.setError("用户名格式错误！");
                    mBtnLogin.setEnabled(false);
                }
                if (isUserNameValid(mEtUsername.getText().toString()) && isPasswordValid(mEtPassword.getText().toString())) {
                    mBtnLogin.setEnabled(true);
                }
            }
        });

        // 密码输入框
        mEtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!isPasswordValid(mEtPassword.getText().toString())) {
                    mEtPassword.setError("密码最短为6位！");
                    mBtnLogin.setEnabled(false);
                }
                if (isUserNameValid(mEtUsername.getText().toString()) && isPasswordValid(mEtPassword.getText().toString())) {
                    mBtnLogin.setEnabled(true);
                }
            }
        });




        return view;
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }


}
