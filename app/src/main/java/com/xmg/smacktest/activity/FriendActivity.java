package com.xmg.smacktest.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.xmg.smacktest.AppGlobal;
import com.xmg.smacktest.BaseActivity;
import com.xmg.smacktest.R;
import com.xmg.smacktest.xmpp.XmppConnection;

/**
 * Created by xmg on 2016/11/4
 */
public class FriendActivity extends BaseActivity implements View.OnClickListener {
    private static final int ADD_FRIEND = 0x001;
    private static final int FRESH_VIEW = 0x002;

    private TextView tvLeft, tvCenter;

    private ListView mListView;

    private TextView tvEmpty;
    private Button add;

    private List<Map<String, String>> friendList;
    private SimpleAdapter mAdpter;


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case FRESH_VIEW:
                    refreshViews();
                    break;
                case ADD_FRIEND:
                    showAddFriend((Packet) msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        initTitle();
        initViews();
        initEvent();
        loadDatas();
        addSubscriptionListener();
    }


    private void initTitle() {
        tvLeft = (TextView) findViewById(R.id.tv_header_left);
        tvCenter = (TextView) findViewById(R.id.tv_header_center);
        add = (Button) findViewById(R.id.btn_add);
        add.setVisibility(View.VISIBLE);
        tvLeft.setVisibility(View.VISIBLE);
        tvCenter.setText(R.string.friend_mine);
    }

    private void initViews() {
        mListView = (ListView) findViewById(R.id.lv_friend_list);
        tvEmpty = (TextView) findViewById(R.id.tv_friend_empty);
    }

    private void initEvent() {
        tvLeft.setOnClickListener(this);
        add.setOnClickListener(this);
    }

    /**
     * 刷新好友列表
     */
    private void refreshViews() {
        if (friendList != null && friendList.size() > 0) {
            mAdpter = new SimpleAdapter(activity, friendList,
                    R.layout.item_friend_list, new String[]{"Name"},
                    new int[]{R.id.tv_friend_name});
            mListView.setAdapter(mAdpter);
            mListView.setOnItemClickListener(mOnItemClickListener);
            mListView.setOnItemLongClickListener(onItemLongClickListener);
            mListView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        } else {
            mListView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }


    OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            @SuppressWarnings("unchecked")
            Map<String, String> map = (Map<String, String>) parent.getItemAtPosition(position);
            String userID = map.get("User");
            String userName = map.get("Name");
            Intent intent = new Intent(activity, ChatActivity.class);
            intent.putExtra("USERID", userID);
            intent.putExtra("USERNAME", userName);
            activity.startActivity(intent);
        }
    };

    /**
     * 长按删除监听
     */
    AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            new AlertDialog.Builder(FriendActivity.this)
                    .setTitle("删除好友")
                    .setMessage("确定删除" + friendList.get(position).get("User"))
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            XMPPConnection conn = XmppConnection.getConnection();
                            Roster roster = conn.getRoster();
                            RosterEntry entry = roster.getEntry(friendList.get(position).get("User"));
                            try {
                                roster.removeEntry(entry);
                            } catch (XMPPException e) {
                                e.printStackTrace();
                            }
                            friendList.remove(position);
                            mAdpter.notifyDataSetChanged();
                            if (friendList.size() == 0) {
                                mListView.setVisibility(View.GONE);
                                tvEmpty.setVisibility(View.VISIBLE);
                            }
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
            return true;
        }
    };

    /**
     * 加载好友数据
     */
    private void loadDatas() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    friendList = new ArrayList<Map<String, String>>();
                    Roster roster = XmppConnection.getConnection().getRoster();
                    Collection<RosterEntry> entries = roster.getEntries();
                    HashMap<String, String> map = null;
                    for (RosterEntry entry : entries) {
                        map = new HashMap<String, String>();
                        map.put("User", entry.getUser());
                        map.put("Name", entry.getUser().substring(0, entry.getUser().indexOf("@")));
                        friendList.add(map);
                    }
                    Message msg = new Message();
                    msg.what = FRESH_VIEW;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }


    /**
     * 添加一个监听，监听好友添加请求。
     */
    private void addSubscriptionListener() {
        //创建包过滤器
        PacketFilter filter = new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
                if (packet instanceof Presence) {
                    Presence presence = (Presence) packet;
                    //是好友邀请状态就返回true 向下执行
                    if (presence.getType().equals(Presence.Type.subscribe)) {
                        return true;
                    }
                }
                return false;
            }
        };
        //开启监听
        XmppConnection.getConnection().addPacketListener(subscriptionPacketListener, filter);
    }

    /**
     * 好友监听
     */
    private PacketListener subscriptionPacketListener = new PacketListener() {

        @Override
        public void processPacket(final Packet packet) {
            //过滤自己加自己的状态
            if (packet.getFrom().contains(((AppGlobal) getApplication()).getName()
                    + "@" + XmppConnection.SERVER_NAME))
                return;
            Message msg = new Message();
            msg.obj = packet;
            msg.what = ADD_FRIEND;
            handler.sendMessage(msg);
        }
    };

    //弹出好友添加框
    public void showAddFriend(final Packet packet) {
        //跳过已添加过好友
        final String name = packet.getFrom().substring(0, packet.getFrom().indexOf("@"));
        for (int i = 0; i < friendList.size(); i++) {
            if (name.equals(friendList.get(i).get("Name"))) {
                return;
            }
        }
        new AlertDialog.Builder(FriendActivity.this)
                .setTitle("好友添加邀请")
                .setMessage("确定添加" + packet.getFrom() + "为好友")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Map<String, String> map = new HashMap<String, String>();
                        map.put("User", packet.getFrom());
                        map.put("Name", name);
                        friendList.add(map);
                        if (mAdpter == null) {
                            refreshViews();
                        }
                        mAdpter.notifyDataSetChanged();

                        Roster roster = XmppConnection.getConnection().getRoster();
                        try {
                            roster.createEntry(packet.getFrom(), name, null);
                        } catch (XMPPException e) {
                            e.printStackTrace();
                        }

                        mListView.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Presence presenceRes = new Presence(Presence.Type.unsubscribe);
                        presenceRes.setTo(packet.getFrom());
                        XmppConnection.getConnection().sendPacket(presenceRes);
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    //显示加过的好友
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String user = data.getStringExtra("user");
            String name = data.getStringExtra("name");

            Map<String, String> map = new HashMap<>();
            map.put("User", user);
            map.put("Name", name);
            friendList.add(map);
            if (mAdpter == null) {
                refreshViews();
            }
            mListView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            mAdpter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_header_left:
                finish();
                break;
            case R.id.btn_add:
                Intent intent = new Intent(FriendActivity.this, AddFriendOrGroup.class);
                startActivityForResult(intent, 0);
                break;
        }
    }
}
