package com.yuanbao.tutu.chatrobot;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.util.DisplayMetrics;

import com.bairuitech.*;
import com.bairuitech.anychat.AnyChatBaseEvent;
import com.bairuitech.anychat.*;

import android.widget.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.Color;

public class VideoChatActivity extends AppCompatActivity implements AnyChatBaseEvent{
    private final int UPDATEVIDEOBITDELAYMILLIS = 200; //监听音频视频的码率的间隔刷新时间（毫秒）

    private  int robotID;
    private  int userselfID;
    boolean bOnPaused = false;
    private boolean bOtherVideoOpened = false; // 对方视频是否已打开
    private Boolean mFirstGetVideoBitrate = false; //"第一次"获得视频码率的标致

    private SurfaceView mOtherView;
    private Button mCloseVideoBtn;
    private Button mrobotstateBtn;
    private ImageButton mOpenvideoBtn; // 控制视频的按钮
    private Button mTransmitBtn;
    //direction button
    private Button updirBtn;
    private Button downdirBtn;
    private Button rightdirBtn;
    private Button leftdirBtn;

    private EditText sendMsgText;
    private TextView getMsgText;

    private String cmdUp="##u#%#";
    private String cmdDown="##d#%#";
    private String cmdright="##r#%#";
    private String cmdleft="##l#%#";



    public AnyChatCoreSDK anychatSDK;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent userIntent=getIntent();
        userselfID=Integer.parseInt(userIntent.getStringExtra("UserselfID"));
        setContentView(R.layout.activity_video_chat);
        InitSDK();
        InitLayout();
        handler.postDelayed(runnable,UPDATEVIDEOBITDELAYMILLIS);
    }

    private void InitSDK() {
        anychatSDK = AnyChatCoreSDK.getInstance(this);
        anychatSDK.SetBaseEvent(this);
        anychatSDK.mSensorHelper.InitSensor(this);
        AnyChatCoreSDK.mCameraHelper.SetContext(this);
    }

    private void InitLayout() {
        this.setTitle("robotchat");
        mOtherView = (SurfaceView) findViewById(R.id.remoteVideo);

        mCloseVideoBtn = (Button) findViewById(R.id.closeVideoBut);
        mrobotstateBtn=(Button)findViewById(R.id.robotStateBut);
        mOpenvideoBtn = (ImageButton) findViewById(R.id.openvideoBut);
        mTransmitBtn=(Button)findViewById(R.id.transmitBut);

        updirBtn =(Button)findViewById(R.id.upBut);
        downdirBtn=(Button)findViewById(R.id.downBut);
        rightdirBtn=(Button)findViewById(R.id.rightBut);
        leftdirBtn=(Button)findViewById(R.id.leftBut);

        sendMsgText=(EditText)findViewById(R.id.transMitTextEdit);
        getMsgText=(TextView)findViewById(R.id.StateText);

        int[] userID=anychatSDK.GetOnlineUser();
        System.out.println("userNUM:"+userID.length);
        if(userID.length>=1){
            robotID=userID[0];
            SetonStateBtn();
        }
        else {
            SetlossStateBtn();
        }

    }

    private void SetonStateBtn(){
        mCloseVideoBtn.setOnClickListener(onClickListener);
        mrobotstateBtn.setOnClickListener(onClickListener);
        mOpenvideoBtn.setOnClickListener(onClickListener);
        mTransmitBtn.setOnClickListener(onClickListener);
        updirBtn.setOnClickListener(onClickListener);
        rightdirBtn.setOnClickListener(onClickListener);
        downdirBtn.setOnClickListener(onClickListener);
        leftdirBtn.setOnClickListener(onClickListener);
        mOpenvideoBtn.setVisibility(View.VISIBLE);
        mCloseVideoBtn.setVisibility(View.INVISIBLE);
        mrobotstateBtn.setVisibility(View.VISIBLE);
        mTransmitBtn.setVisibility(View.VISIBLE);
        updirBtn.setVisibility(View.VISIBLE);
        rightdirBtn.setVisibility(View.VISIBLE);
        downdirBtn.setVisibility(View.VISIBLE);
        leftdirBtn.setVisibility(View.VISIBLE);
        getMsgText.setVisibility(View.VISIBLE);
        sendMsgText.setVisibility(View.VISIBLE);

        // 如果是采用Java视频显示，则需要设置Surface的CallBack
        if (AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL)
                == AnyChatDefine.VIDEOSHOW_DRIVER_JAVA)
        {
            int index = anychatSDK.mVideoHelper.bindVideo(mOtherView.getHolder());
            System.out.println("set video user callback :"+robotID);
            anychatSDK.mVideoHelper.SetVideoUser(index, robotID);
        }
    }

    private void SetlossStateBtn(){
        mCloseVideoBtn.setVisibility(View.INVISIBLE);
        mrobotstateBtn.setVisibility(View.INVISIBLE);
        mOpenvideoBtn.setVisibility(View.INVISIBLE);
        mTransmitBtn.setVisibility(View.INVISIBLE);
        updirBtn.setVisibility(View.INVISIBLE);
        rightdirBtn.setVisibility(View.INVISIBLE);
        downdirBtn.setVisibility(View.INVISIBLE);
        leftdirBtn.setVisibility(View.INVISIBLE);
        getMsgText.setVisibility(View.INVISIBLE);
        sendMsgText.setVisibility(View.INVISIBLE);
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                int videoBitrate = anychatSDK.QueryUserStateInt(robotID,
                        AnyChatDefine.BRAC_USERSTATE_VIDEOBITRATE);

                if (videoBitrate > 0)
                {
                    //handler.removeCallbacks(runnable);
                    mFirstGetVideoBitrate = true;
                    mOtherView.setBackgroundColor(Color.TRANSPARENT);
                }

                if (mFirstGetVideoBitrate)
                {
                    if (videoBitrate <= 0){
                        Toast.makeText(VideoChatActivity.this, "robot video loss...!", Toast.LENGTH_SHORT).show();
                        // 重置下，如果对方退出了，有进去了的情况
                        mFirstGetVideoBitrate = false;
                    }
                }


                handler.postDelayed(runnable, UPDATEVIDEOBITDELAYMILLIS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.robotStateBut:{

                }break;

                case R.id.transmitBut:{
                    String Msg=sendMsgText.getText().toString().trim();
                    anychatSDK.SendTextMessage(-1,0,Msg);

                }break;
                case (R.id.closeVideoBut): {
                    mOpenvideoBtn.setVisibility(View.VISIBLE);
                    mCloseVideoBtn.setVisibility(View.INVISIBLE);
                    anychatSDK.UserCameraControl(robotID,0);
                    bOtherVideoOpened=false;
                }break;
                case (R.id.openvideoBut): {
                    mOpenvideoBtn.setVisibility(View.INVISIBLE);
                    mCloseVideoBtn.setVisibility(View.VISIBLE);
                    System.out.println("open robot video:"+robotID);//debug
                    anychatSDK.UserCameraControl(robotID,1);
                    bOtherVideoOpened=true;
                }break;
                case R.id.upBut:{
                    anychatSDK.SendTextMessage(-1,0,cmdUp);
                }break;
                case R.id.downBut:{
                    anychatSDK.SendTextMessage(-1,0,cmdDown);
                }break;
                case R.id.rightBut:{
                    anychatSDK.SendTextMessage(-1,0,cmdright);
                }break;
                case R.id.leftBut:{
                    anychatSDK.SendTextMessage(-1,0,cmdleft);
                }break;

                default:
                    break;
            }
        }
    };



    protected void onRestart() {

        super.onRestart();
        // 如果是采用Java视频显示，则需要设置Surface的CallBack
        if (AnyChatCoreSDK
                .GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL) == AnyChatDefine.VIDEOSHOW_DRIVER_JAVA) {
            int index = anychatSDK.mVideoHelper.bindVideo(mOtherView
                    .getHolder());
            anychatSDK.mVideoHelper.SetVideoUser(index, robotID);
        }

        refreshAV();
        bOnPaused = false;
    }
    private void refreshAV() {
        if(bOtherVideoOpened){
            anychatSDK.UserCameraControl(robotID, 1);
            mCloseVideoBtn.setVisibility(View.VISIBLE);
            mOpenvideoBtn.setVisibility(View.INVISIBLE);
        }
    }
    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
        bOnPaused = true;
        anychatSDK.UserCameraControl(robotID, 0);
    }

    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        anychatSDK.mSensorHelper.DestroySensor();
        finish();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void OnAnyChatConnectMessage(boolean bSuccess) {

    }

    @Override
    public void OnAnyChatLoginMessage(int dwUserId, int dwErrorCode) {

    }

    @Override
    public void OnAnyChatEnterRoomMessage(int dwRoomId, int dwErrorCode) {

    }

    @Override
    public void OnAnyChatOnlineUserMessage(int dwUserNum, int dwRoomId) {

    }

    @Override
    public void OnAnyChatUserAtRoomMessage(int dwUserId, boolean bEnter) {
        if (!bEnter) {
            if (dwUserId == robotID) {
                Toast.makeText(VideoChatActivity.this, "robot leave ...！", Toast.LENGTH_SHORT).show();
                robotID= 0;
                anychatSDK.UserCameraControl(dwUserId, 0);
                SetlossStateBtn();
                bOtherVideoOpened = false;
            }

        } else {
            if (robotID != 0)
                return;

            int index = anychatSDK.mVideoHelper.bindVideo(mOtherView
                    .getHolder());
            anychatSDK.mVideoHelper.SetVideoUser(index, dwUserId);
            robotID = dwUserId;
            SetonStateBtn();
//            anychatSDK.UserCameraControl(dwUserId, 1);

        }
    }

    @Override
    public void OnAnyChatLinkCloseMessage(int dwErrorCode) {
        // 网络连接断开之后，上层需要主动关闭已经打开的音视频设备
        if (bOtherVideoOpened) {
            anychatSDK.UserCameraControl(robotID, 0);
            bOtherVideoOpened = false;
        }

        Intent mIntent = new Intent("VideoActivity");
        // 发送广播
        sendBroadcast(mIntent);
    }

    private AnyChatTextMsgEvent ReceiveMsg=new AnyChatTextMsgEvent() {
        @Override
        public void OnAnyChatTextMessage(int dwFromUserid, int dwToUserid, boolean bSecret, String message) {
            if(!bSecret){
                getMsgText.setText(dwFromUserid+"->"+message);
            }

        }
    };

}
