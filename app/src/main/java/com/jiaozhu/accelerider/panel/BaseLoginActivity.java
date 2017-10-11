package com.jiaozhu.accelerider.panel;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jiaozhu.accelerider.R;
import com.jiaozhu.accelerider.support.Preferences;

/**
 * Created by jiaozhu on 16/7/20.
 */
public abstract class BaseLoginActivity extends BaseActivity {
    protected AutoCompleteTextView mName;
    protected EditText mPassword;
    protected Button mCommit;
    protected TextView mTitle;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }


    private void initView() {
        mName = findViewById(R.id.name);
        mPassword = findViewById(R.id.password);
        mCommit = findViewById(R.id.commit);
        mTitle = findViewById(R.id.title);

        mTitle.setText(R.string.app_name);
        mName.setText(Preferences.Companion.getUserName());

        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent event) {
                if (i == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    onCommitPressed();
                    return true;
                }
                return false;
            }
        });

        mCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCommitPressed();
            }
        });
    }

    /**
     * 按下登录按钮
     * 非本地登录需重写此接口
     */
    protected void onCommitPressed() {
        String name = mName.getText().toString();
        String password = mPassword.getText().toString();
        if (login(name, password)) {
            onLoginSuccess(name, password);
        } else {
            Toast toast = Toast.makeText(this, "用户名或者密码错误", Toast.LENGTH_SHORT);
            toast.setMargin(0, 0.3f);
            toast.show();
        }
    }


    protected void showToast(String msg) {
        Toast.makeText(BaseLoginActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 登录验证
     *
     * @param name
     * @param password
     * @return
     */
    abstract boolean login(String name, String password);

    /**
     * 登录成功后动作
     */
    abstract void onLoginSuccess(String name, String password);

    /**
     * 单击设定按钮之后的动作
     */
    void onSettingPressed() {

    }

}
