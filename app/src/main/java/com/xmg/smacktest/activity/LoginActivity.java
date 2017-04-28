package com.xmg.smacktest.activity;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.xmg.smacktest.AppGlobal;
import com.xmg.smacktest.BaseActivity;
import com.xmg.smacktest.R;
import com.xmg.smacktest.utils.ShareUtils;
import com.xmg.smacktest.utils.StringUtils;
import com.xmg.smacktest.utils.ToastUtils;
import com.xmg.smacktest.xmpp.XmppConnection;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by xmg on 2016/11/4
 */
public class LoginActivity extends BaseActivity {

    private AppGlobal mAppGlobal;

    private TextView tvLeft, tvCenter;

    private String userStr, passStr;

    private EditText etUser, etPass;

    private CheckBox checkMemory, checkAuto;

    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAppGlobal = (AppGlobal) getApplication();
        initTitle();
        initViews();
    }

    private void initTitle() {
        tvLeft = (TextView) findViewById(R.id.tv_header_left);
        tvCenter = (TextView) findViewById(R.id.tv_header_center);
        tvLeft.setVisibility(View.GONE);
        tvCenter.setText(R.string.user_login);
    }

    private void initViews() {
        etUser = (EditText) findViewById(R.id.et_login_user);
        etPass = (EditText) findViewById(R.id.et_login_pass);

        checkMemory = (CheckBox) findViewById(R.id.cb_login_memory);
        checkAuto = (CheckBox) findViewById(R.id.cb_login_auto);

        btnRegister = (Button) findViewById(R.id.btn_register);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnRegister.setOnClickListener(mOnClickListener);
        btnLogin.setOnClickListener(mOnClickListener);

        String name = (String) ShareUtils.get(activity, "user_name", "");
        String pass = (String) ShareUtils.get(activity, "user_pass", "");
        if (!StringUtils.isNullOrEmpty(name) && !StringUtils.isNullOrEmpty(pass)) {
            checkMemory.setChecked(true);
            etUser.setText(name);
            etPass.setText(pass);
        }
        boolean isAuto = (Boolean) ShareUtils.get(activity, "user_auto", false);
        if (isAuto) {//自动登录
            checkAuto.setChecked(true);
            login();
        }
    }

    OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_login:
                    login();
                    break;
                case R.id.btn_register:
                    activity.startActivity(new Intent(activity, RegisterActivity.class));
                    break;
            }
        }
    };

    private void login() {
        userStr = etUser.getText().toString();
        passStr = etPass.getText().toString();
        if (userStr.length() == 0 || passStr.length() == 0) {
            ToastUtils.showShort(activity, "帐号或密码不能为空");
            return;
        } else {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        // 连接服务器
                        XmppConnection.getConnection().login(userStr, passStr);
                        // 连接服务器成功，更改在线状态
                        Presence presence = new Presence(Presence.Type.available);
                        XmppConnection.getConnection().sendPacket(presence);
                        handler.sendEmptyMessage(1);
                    } catch (XMPPException e) {
                        XmppConnection.closeConnection();
                        handler.sendEmptyMessage(2);
                    }
                }
            });
            thread.start();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {

            if (msg.what == 1) {
                ToastUtils.showShort(activity, "登录成功！");
                toNextPage();
            } else if (msg.what == 2) {
                ToastUtils.showShort(activity, "登录失败！");
            }
        }

        ;
    };

    private void toNextPage() {
        mAppGlobal.setName(userStr);
        if (checkMemory.isChecked()) {
            ShareUtils.put(activity, "user_name", userStr);
            ShareUtils.put(activity, "user_pass", passStr);
        } else {
            ShareUtils.remove(activity, "user_name");
            ShareUtils.remove(activity, "user_pass");
        }
        if (checkAuto.isChecked()) {
            ShareUtils.put(activity, "user_auto", true);
        } else {
            ShareUtils.remove(activity, "user_auto");
        }
        // 跳转到好友列表
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, EnterActivity.class);
        startActivity(intent);
        activity.finish();
    }
}
