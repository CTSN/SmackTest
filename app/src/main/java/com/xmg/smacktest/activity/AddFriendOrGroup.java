package com.xmg.smacktest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.print.PageRange;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xmg.smacktest.AppGlobal;
import com.xmg.smacktest.BaseActivity;
import com.xmg.smacktest.R;
import com.xmg.smacktest.utils.ToastUtils;
import com.xmg.smacktest.xmpp.XmppConnection;

import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;

import java.util.Collection;
import java.util.List;

/**
 * Created by xmg on 2016/11/4
 */
public class AddFriendOrGroup extends BaseActivity {

    private TextView tv_center;
    private TextView tv_name;
    private EditText et_name;
    private Button btn_add;
    private boolean isGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_add_friend);
        isGroup = getIntent().getBooleanExtra("isGroup", false);
        initView();
        initEvent();
    }


    void initView() {
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_center = (TextView) findViewById(R.id.tv_header_center);
        tv_center.setText("添加好友/群聊");
        tv_center.setVisibility(View.VISIBLE);
        et_name = (EditText) findViewById(R.id.et_name);
        btn_add = (Button) findViewById(R.id.btn_add_friend);
        if (isGroup) {
            btn_add.setText("创建");
            tv_name.setText("群名");
        }
        findViewById(R.id.tv_header_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void initEvent() {

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGroup) {
                    addFriend();
                } else {
                    setGroup();
                }
            }
        });
    }

    private void addFriend() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String name = et_name.getText().toString();
                //设置添加好友请求
                Presence subscription = new Presence(Presence.Type.subscribe);
                //拼接好友全称
                subscription.setTo(name + "@" + XmppConnection.SERVER_NAME);
                //发送请求
                XmppConnection.getConnection().sendPacket(subscription);
            }
        }).start();
        Intent intent = new Intent();
        intent.putExtra("user", et_name.getText().toString() + "@" + XmppConnection.SERVER_NAME);
        intent.putExtra("name", et_name.getText().toString());
        setResult(1, intent);
        finish();
    }

    private void setGroup() {
        boolean isSuccess = XmppConnection.createRoom(getUserName(), et_name.getText().toString() + "", "");
        if (isSuccess) {
            Intent intent = new Intent();
            intent.putExtra("user", et_name.getText().toString());
            intent.putExtra("name", et_name.getText().toString());
            setResult(1, intent);
            finish();
        } else {
            ToastUtils.showShort(this, "创建失败");
        }
    }
}
