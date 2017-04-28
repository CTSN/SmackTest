package com.xmg.smacktest.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.xmg.smacktest.AppGlobal;
import com.xmg.smacktest.BaseActivity;
import com.xmg.smacktest.R;
import com.xmg.smacktest.entity.Msg;
import com.xmg.smacktest.utils.DateTimeUtils;
import com.xmg.smacktest.utils.KeyBoardUtils;
import com.xmg.smacktest.utils.StringUtils;
import com.xmg.smacktest.utils.ToastUtils;
import com.xmg.smacktest.xmpp.XmppConnection;

/**
 * Created by xmg on 2016/11/4
 */
public class ChatActivity extends BaseActivity {

    private static final int TUPIAN_RESULT = 1001;

    private final static String Folder = "XmppTest";

    private final static String ALBUM_PATH = Environment.getExternalStorageDirectory() + File.separator + Folder + File.separator;

    private AppGlobal mAppGlobal;

    private TextView tvLeft, tvCenter;

    private String toUserID, toUserName;

    private ListView mListView;

    private ChatAdapter mAdapter;

    private List<Msg> chatList;

    private EditText etSend;

    private Button btnMore, btnSend;

    private ChatManager chatMan;

    private MultiUserChat muc;

    private Chat newchat;

    private boolean isGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initDatas();
        initTitle();
        initViews();
    }

    private void initDatas() {
        isGroup = getIntent().getBooleanExtra("isGroup", false);
        mAppGlobal = (AppGlobal) getApplication();
        Intent intent = getIntent();
        toUserID = intent.getStringExtra("USERID");
        toUserName = intent.getStringExtra("USERNAME");

        //接受消息监听
        if (isGroup) {
            //群聊消息
            muc = XmppConnection.getMultiUserChat();
            addGroupListener();
        } else {
            // 消息监听
            chatMan = XmppConnection.getConnection().getChatManager();
            newchat = chatMan.createChat(toUserID, null);
            addChatListener();
            //接收文件监听
            addFileListerer();
        }


    }

    private void addGroupListener() {
        muc.addMessageListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                Message message = (Message) packet;
                // 接收来自聊天室的聊天信息
                String groupName = message.getFrom();
                String[] nameOrGroup = groupName.split("/");
                //判断是否是本人发出的消息 不是则显示
                if (!nameOrGroup[1].equals(getUserName())) {
                    String[] args = new String[]{nameOrGroup[1], message.getBody()};
                    // 在handler里取出来显示消息
                    android.os.Message msg = handler.obtainMessage();
                    msg.what = 1;
                    msg.obj = args;
                    msg.sendToTarget();
                }
            }
        });
    }

    private void addChatListener() {
        chatMan.addChatListener(new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean able) {
                chat.addMessageListener(new MessageListener() {
                    @Override
                    public void processMessage(Chat chat, Message message) {
                        // 获取自己好友发来的信息
                        if (message.getFrom().contains(toUserID)) {
                            if (message.getBody().length() > 0) {
                                // 获取用户、消息、时间、IN
                                String[] args = new String[]{toUserName, message.getBody()};
                                // 在handler里取出来显示消息
                                android.os.Message msg = handler.obtainMessage();
                                msg.what = 1;
                                msg.obj = args;
                                msg.sendToTarget();
                            }
                        }
                    }
                });
            }
        });
    }

    private void addFileListerer() {
        FileTransferManager manager = XmppConnection.getFileTransferManager();
        manager.addFileTransferListener(new FileTransferListener() {
            @Override
            public void fileTransferRequest(final FileTransferRequest request) {
                new Thread() {
                    @Override
                    public void run() {
                        //文件接收
                        IncomingFileTransfer transfer = request.accept();
                        //获取文件名字
                        String fileName = transfer.getFileName();
                        //本地创建文件
                        File sdCardDir = new File(ALBUM_PATH);
                        if (!sdCardDir.exists()) {//判断文件夹目录是否存在
                            sdCardDir.mkdir();//如果不存在则创建
                        }
                        String save_path = ALBUM_PATH + fileName;
                        File file = new File(save_path);
                        //接收文件
                        try {
                            transfer.recieveFile(file);
                            while (!transfer.isDone()) {
                                if (transfer.getStatus().equals(FileTransfer.Status.error)) {
                                    System.out.println("ERROR!!! " + transfer.getError());
                                } else {
                                    System.out.println(transfer.getStatus());
                                    System.out.println(transfer.getProgress());
                                }
                                try {
                                    Thread.sleep(1000L);
                                } catch (Exception e) {
                                }
                            }
                            if (transfer.isDone()) {
                                String[] args = new String[]{toUserName, fileName};
                                android.os.Message msg = handler.obtainMessage();
                                msg.what = 2;
                                msg.obj = args;
                                msg.sendToTarget();
                            }
                        } catch (XMPPException e) {
                            e.printStackTrace();
                        }
                    }

                    ;
                }.start();
            }
        });
    }

    private void initTitle() {
        tvLeft = (TextView) findViewById(R.id.tv_header_left);
        tvCenter = (TextView) findViewById(R.id.tv_header_center);
        tvCenter.setText(toUserName);
        tvLeft.setText(R.string.back);
        tvLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
    }

    private void initViews() {
        mListView = (ListView) findViewById(R.id.lv_chat_list);
        chatList = new ArrayList<Msg>();
        mAdapter = new ChatAdapter(activity, chatList);
        mListView.setAdapter(mAdapter);

        etSend = (EditText) findViewById(R.id.et_send);
        btnMore = (Button) findViewById(R.id.btn_img);
        btnSend = (Button) findViewById(R.id.btn_send);

        if (isGroup)
            btnMore.setVisibility(View.GONE);

        etSend.setOnClickListener(mOnClickListener);
        btnMore.setOnClickListener(mOnClickListener);
        btnSend.setOnClickListener(mOnClickListener);
//		initFunction();
    }

    OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_send:
                    sendMessage();
                    break;
                case R.id.btn_img:
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, TUPIAN_RESULT);
                    break;
            }
        }
    };

    private void sendMessage() {
        final String content = etSend.getText().toString();
        if (!StringUtils.isNullOrEmpty(content)) {
            String fromUserID = mAppGlobal.getName();
            String dateStr = DateTimeUtils.formatDate(new Date());
            if (!isGroup) {
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Message msg = new Message();
                            msg.setBody(content);
                            // 发送消息
                            newchat.sendMessage(msg);
                        } catch (XMPPException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            } else {
                try {
                    muc.sendMessage(content);
                } catch (XMPPException e) {
                    e.printStackTrace();
                }
            }
            // 发送消息
            chatList.add(new Msg(dateStr, fromUserID, content, "OUT"));
            // 刷新适配器
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(ListView.FOCUS_DOWN);// 刷新到底部
            etSend.setText("");
        } else {
            ToastUtils.showShort(activity, "发送消息为空");
        }
    }

    class ChatAdapter extends BaseAdapter {

        private List<Msg> dataList;

        private LayoutInflater mInflater;

        ChatAdapter(Context context, List<Msg> dataList) {
            this.mInflater = LayoutInflater.from(context);
            this.dataList = dataList;
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.item_chat_list, null);
                holder.llLeft = (RelativeLayout) convertView
                        .findViewById(R.id.ll_chat_left);
                holder.llRight = (LinearLayout) convertView
                        .findViewById(R.id.ll_chat_right);
                holder.tvDate = (TextView) convertView
                        .findViewById(R.id.tv_chat_date);
                holder.tvName = (TextView) convertView
                        .findViewById(R.id.tv_chat_name);
                holder.tvTitle = (TextView) convertView
                        .findViewById(R.id.tv_chat_title);
                holder.tvName2 = (TextView) convertView
                        .findViewById(R.id.tv_chat_name2);
                holder.tvTitle2 = (TextView) convertView
                        .findViewById(R.id.tv_chat_title2);
                holder.iv_left = (ImageView) convertView.findViewById(R.id.iv_left);
                holder.iv_right = (ImageView) convertView.findViewById(R.id.iv_right);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Msg msg = dataList.get(position);
            holder.tvDate.setText(msg.getDate());
            String myself = msg.getMyself();
            if (myself.equals("IN")) {
                holder.llLeft.setVisibility(View.VISIBLE);
                holder.llRight.setVisibility(View.GONE);
                holder.tvName.setText(msg.getName());
                if (msg.getTitle() != null && !msg.getTitle().isEmpty()) {
                    holder.tvTitle.setText(msg.getTitle());
                    holder.tvTitle.setVisibility(View.VISIBLE);
                    holder.iv_left.setVisibility(View.GONE);
                } else {
                    holder.tvTitle.setVisibility(View.GONE);
                    holder.iv_left.setVisibility(View.VISIBLE);
                    Glide.with(ChatActivity.this).load(ALBUM_PATH + msg.getImg_path()).into(holder.iv_left);
                }
            } else if (myself.equals("OUT")) {
                holder.llLeft.setVisibility(View.GONE);
                holder.llRight.setVisibility(View.VISIBLE);
                holder.tvName2.setText(msg.getName());
                holder.tvTitle2.setText(msg.getTitle());
                if (msg.getTitle() != null && !msg.getTitle().isEmpty()) {
                    holder.tvTitle2.setText(msg.getTitle());
                    holder.tvTitle2.setVisibility(View.VISIBLE);
                    holder.iv_right.setVisibility(View.GONE);
                } else {
                    holder.tvTitle2.setVisibility(View.GONE);
                    holder.iv_right.setVisibility(View.VISIBLE);
                    Glide.with(ChatActivity.this).load(msg.getImg_path()).into(holder.iv_right);
                }
            }
            return convertView;
        }

        class ViewHolder {
            RelativeLayout llLeft;
            LinearLayout llRight;
            TextView tvDate;
            TextView tvName;
            TextView tvTitle;
            TextView tvName2;
            TextView tvTitle2;
            ImageView iv_left;
            ImageView iv_right;
        }
    }

    class SendFileTask extends AsyncTask<String, Integer, Integer> {
        protected Integer doInBackground(String... params) {
            if (params.length < 2) {
                return Integer.valueOf(-1);
            }
            String img_path = params[0];
            String toId = params[1] + "/Smack";

            FileTransferManager fileTransferManager = XmppConnection.getFileTransferManager();
            File filetosend = new File(img_path);
            if (filetosend.exists() == false) {
                return -1;
            }
            OutgoingFileTransfer transfer = fileTransferManager
                    .createOutgoingFileTransfer(toId);// 创建一个输出文件传输对象
            try {
                transfer.sendFile(filetosend, "recv img");
                while (!transfer.isDone()) {
                    if (transfer.getStatus().equals(FileTransfer.Status.error)) {
                        System.out.println("ERROR!!! " + transfer.getError());
                    } else {
                        System.out.println(transfer.getStatus());
                        System.out.println(transfer.getProgress());
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (transfer.isDone()) {
                    Log.i("main", "---->");
                    String[] args = new String[]{mAppGlobal.getName(), img_path};
                    android.os.Message msg = handler.obtainMessage();
                    msg.what = 3;
                    msg.obj = args;
                    msg.sendToTarget();
                }
            } catch (XMPPException e1) {
                e1.printStackTrace();
            }
            return 0;
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            String[] args = (String[]) msg.obj;
            switch (msg.what) {
                case 1:

                    String dateStr = DateTimeUtils.formatDate(new Date());
                    Msg m = new Msg(dateStr, args[0], args[1], "IN");
                    chatList.add(m);
                    mAdapter.notifyDataSetChanged();
                    mListView.setSelection(ListView.FOCUS_DOWN);// 刷新到底部
                    break;
                case 2:
                    String dateStr2 = DateTimeUtils.formatDate(new Date());
                    Msg m2 = new Msg(dateStr2, args[0], "", "IN", args[1]);
                    chatList.add(m2);
                    mAdapter.notifyDataSetChanged();
                    mListView.setSelection(ListView.FOCUS_DOWN);// 刷新到底部
                    break;
                case 3:
                    String dateStr3 = DateTimeUtils.formatDate(new Date());
                    Msg m3 = new Msg(dateStr3, args[0], "", "OUT", args[1]);
                    chatList.add(m3);
                    mAdapter.notifyDataSetChanged();
                    mListView.setSelection(ListView.FOCUS_DOWN);// 刷新到底部

                    break;
            }

        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //图片路径
        String picPath;
        if (data != null) {
            Uri uri = data.getData();
            if (!TextUtils.isEmpty(uri.getAuthority())) {
                Cursor cursor = getContentResolver().query(uri,
                        new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                if (null == cursor) {
                    Toast.makeText(this, "图片没找到", Toast.LENGTH_SHORT).show();
                    return;
                }
                cursor.moveToFirst();
                //的到路径
                picPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                cursor.close();
            } else {
                picPath = uri.getPath();
            }
        } else {
            Toast.makeText(this, "图片没找到", Toast.LENGTH_SHORT).show();
            return;
        }

        new SendFileTask().execute(picPath, toUserID);// 开始发送图片
    }


}
