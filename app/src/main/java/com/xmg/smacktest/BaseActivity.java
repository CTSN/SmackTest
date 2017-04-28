package com.xmg.smacktest;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.xmg.smacktest.utils.AppManager;
import com.xmg.smacktest.xmpp.XmppConnection;

import org.jivesoftware.smack.XMPPConnection;


public class BaseActivity extends Activity {

    public BaseActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        AppManager.getAppManager().addActivity(this);
        activity = this;
    }

    public XMPPConnection getXmppConnection() {
        return XmppConnection.getConnection();
    }

    public String getUserName() {
        return ((AppGlobal) getApplicationContext()).getName();
    }
}
