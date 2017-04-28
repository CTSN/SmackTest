package com.xmg.smacktest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.xmg.smacktest.BaseActivity;
import com.xmg.smacktest.R;
import com.xmg.smacktest.utils.AppManager;
import com.xmg.smacktest.xmpp.XmppConnection;

/**
 * Created by xmg on 2016/11/9.
 */

public class EnterActivity extends BaseActivity {

    private Intent intent;
    private TextView tvLeft;
    private TextView tv_center;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chose_fre_group);
        initView();
        initEnvent();
    }
    private void initView() {
        tvLeft = (TextView) findViewById(R.id.tv_header_left);
        tvLeft.setVisibility(View.GONE);

        ((TextView) findViewById(R.id.tv_header_center)).setText("选择");
        findViewById(R.id.tv_header_center).setVisibility(View.VISIBLE);

    }
    private void initEnvent() {
        findViewById(R.id.btn_friend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(EnterActivity.this, FriendActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.btn_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(EnterActivity.this, GroupActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            XmppConnection.closeConnection();
            AppManager.getAppManager().finishAllActivity();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
