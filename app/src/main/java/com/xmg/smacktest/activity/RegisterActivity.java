package com.xmg.smacktest.activity;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Registration;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xmg.smacktest.BaseActivity;
import com.xmg.smacktest.R;
import com.xmg.smacktest.utils.ToastUtils;
import com.xmg.smacktest.xmpp.XmppConnection;

/**
 * Created by xmg on 2016/11/4
 */
public class RegisterActivity extends BaseActivity {

    private TextView tvLeft, tvCenter;

    private String email, account, name, pass;

    private EditText etEmail, etAccount, etName, etPass;

    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initTitle();
        initViews();
    }

    private void initTitle() {
        tvLeft = (TextView) findViewById(R.id.tv_header_left);
        tvCenter = (TextView) findViewById(R.id.tv_header_center);
        tvCenter.setText(R.string.user_register);
        tvLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
    }

    private void initViews() {
        etEmail = (EditText) findViewById(R.id.et_user_mail);
        etAccount = (EditText) findViewById(R.id.et_user_account);
        etName = (EditText) findViewById(R.id.et_user_name);
        etPass = (EditText) findViewById(R.id.et_user_pass);

        btnRegister = (Button) findViewById(R.id.btn_user_register);
        btnRegister.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void register() {
        email = etEmail.getText().toString();
        account = etAccount.getText().toString();
        name = etName.getText().toString();
        pass = etPass.getText().toString();
        if (email.length() == 0 || account.length() == 0 || name.length() == 0
                || pass.length() == 0) {
            ToastUtils.showShort(activity, "填写信息不能为空");
            return;
        } else {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    Registration reg = new Registration();
                    //设置类型
                    reg.setType(IQ.Type.SET);
                    //发送到服务器
                    reg.setTo(XmppConnection.getConnection().getServiceName());
                    //设置用户名
                    reg.setUsername(account);
                    //设置密码
                    reg.setPassword(pass);
                    //设置其余属性 不填可能会报500异常 连接不到服务器 amack一个Bug
                    reg.addAttribute("name", name);
                    reg.addAttribute("email", email);
                    reg.addAttribute("android", "geolo_createUser_android");       //设置安卓端登录
                    //创建包过滤器
                    PacketFilter filter = new AndFilter(new PacketIDFilter(reg
                            .getPacketID()), new PacketTypeFilter(IQ.class));
                    //创建包收集器
                    PacketCollector collector = XmppConnection.getConnection()
                            .createPacketCollector(filter);
                    //发送包
                    XmppConnection.getConnection().sendPacket(reg);
                    //获取返回信息
                    IQ result = (IQ) collector.nextResult(SmackConfiguration
                            .getPacketReplyTimeout());
                    // 停止请求results（是否成功的结果）
                    collector.cancel();
                    //通过返回信息判断
                    if (result == null) {
                        Message msg = new Message();
                        msg.obj = "服务器没有返回结果";
                        handler.sendMessage(msg);
                    } else if (result.getType() == IQ.Type.ERROR) {
                        if (result.getError().toString()
                                .equalsIgnoreCase("conflict(409)")) {
                            Message msg = new Message();
                            msg.obj = "这个账号已经存在";
                            handler.sendMessage(msg);
                        } else {
                            Message msg = new Message();
                            msg.obj = "注册失败";
                            handler.sendMessage(msg);
                        }
                    } else if (result.getType() == IQ.Type.RESULT) {
                        try {
                            XmppConnection.getConnection().login(account, pass);
                            Presence presence = new Presence(
                                    Presence.Type.available);
                            XmppConnection.getConnection().sendPacket(presence);
                            Message msg = new Message();
                            msg.obj = "注册成功！";
                            handler.sendMessage(msg);
                            Intent intent = new Intent();
//                            intent.putExtra("USERID", account);
                            intent.setClass(RegisterActivity.this,
                                    EnterActivity.class);
                            startActivity(intent);
                        } catch (XMPPException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            thread.start();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            ToastUtils.showShort(activity, msg.obj.toString());
        }
    };
}
