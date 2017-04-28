package com.xmg.smacktest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.xmg.smacktest.BaseActivity;
import com.xmg.smacktest.R;
import com.xmg.smacktest.xmpp.XmppConnection;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xmg on 2016/11/9.
 */

public class GroupActivity extends BaseActivity {

    private TextView tvLeft;
    private TextView tvCenter;
    private ListView lv_group;
    private Button btn_add;
    private List<Map<String, String>> groupList;
    private SimpleAdapter mAdpter;
    private XMPPConnection connection;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            refreshView();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        connection = getXmppConnection();
        initView();
        initData();
        initEvent();
    }


    private void initView() {
        btn_add = (Button) findViewById(R.id.btn_add);
        tvLeft = (TextView) findViewById(R.id.tv_header_left);
        tvCenter = (TextView) findViewById(R.id.tv_header_center);
        tvCenter.setText("群聊");
        btn_add.setVisibility(View.VISIBLE);

        lv_group = (ListView) findViewById(R.id.lv_friend_list);
    }

    /**
     * 获取服务器上所有会议室
     */
    private void initData() {
        groupList = new ArrayList<>();
        try {
            //遍历每个人所创建的群
            for (HostedRoom host : MultiUserChat.getHostedRooms(XmppConnection.getConnection(), XmppConnection.getConnection().getServiceName())) {
                //遍历某个人所创建的群
                for (HostedRoom singleHost : MultiUserChat.getHostedRooms(XmppConnection.getConnection(), host.getJid())) {
                    RoomInfo info = MultiUserChat.getRoomInfo(XmppConnection.getConnection(),
                            singleHost.getJid());
                    if (singleHost.getJid().indexOf("@") > 0) {
                        Map<String, String> map = new HashMap<>();
                        map.put("Gname", singleHost.getName());
                        groupList.add(map);
                    }
                }
            }
            mHandler.sendEmptyMessage(0);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    private void refreshView() {
        if (groupList != null && groupList.size() > 0) {
            mAdpter = new SimpleAdapter(activity, groupList,
                    R.layout.item_friend_list, new String[]{"Gname"},
                    new int[]{R.id.tv_friend_name});
            lv_group.setAdapter(mAdpter);
            lv_group.setOnItemClickListener(new OnItemClickListeren());
        } else {
            mAdpter.notifyDataSetChanged();
        }
    }

    class OnItemClickListeren implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            XmppConnection.joinMultiUserChat(getUserName(), "", groupList.get(position).get("Gname"));
            Intent intent = new Intent(GroupActivity.this, ChatActivity.class);
            intent.putExtra("isGroup", true);
            intent.putExtra("USERNAME", groupList.get(position).get("Gname"));
            startActivity(intent);
        }
    }

    private void initEvent() {
        tvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupActivity.this, AddFriendOrGroup.class);
                intent.putExtra("isGroup", true);
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String name = data.getStringExtra("name");

            Map<String, String> map = new HashMap<>();
            map.put("Gname", name);
            groupList.add(map);
            mAdpter.notifyDataSetChanged();
        }
    }
}
