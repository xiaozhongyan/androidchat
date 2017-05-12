package com.yuanbao.tutu.chatrobot;

import com.bairuitech.anychat.AnyChatBaseEvent;
import com.yuanbao.tutu.config.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;

public class MainActivity extends AppCompatActivity implements AnyChatBaseEvent{
    //UI value
    private EditText mEditIP;
    private EditText mEditPort;
    private EditText mEditName;
    private EditText mEditRoomID;
    private EditText mEditPwd;
    private TextView mBottomConnMsg;
    private TextView mBottomBuildMsg;
    private Button mBtnLogin;
    private Button mBtnLogout;
    private Button mBtnConfig;

    private String mStrIP = "cloud.anychat.cn";
    private String mStrName = "name";
    private String mStrPwd;
    private String mStrappuid="e8583d7a-fa16-4b57-bab5-5468458237ca";
    private int mSPort = 8906;
    private int mSRoomID = 1;
    //    private final int SHOWLOGINSTATEFLAG = 1; // 显示的按钮是登陆状态的标识
//    private final int SHOWWAITINGSTATEFLAG = 2; // 显示的按钮是等待状态的标识
//    private final int SHOWLOGOUTSTATEFLAG = 3; // 显示的按钮是登出状态的标识
    private final int LOCALVIDEOAUTOROTATION = 1; // 本地视频自动旋转控制

    private int UserselfID;
    public AnyChatCoreSDK anyChatSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitSDK();
        InitLayout();
        readLoginDate();
        initLoginConfig();

        ApplyVideoConfig();
        registerBoradcastReceiver();

    }

    private void InitSDK() {
        if (anyChatSDK == null) {
            anyChatSDK = AnyChatCoreSDK.getInstance(this);
            anyChatSDK.SetBaseEvent(this);
            anyChatSDK.InitSDK(android.os.Build.VERSION.SDK_INT, 0);
            AnyChatCoreSDK.SetSDKOptionInt(
                    AnyChatDefine.BRAC_SO_LOCALVIDEO_AUTOROTATION,
                    LOCALVIDEOAUTOROTATION);
        }
    }
    private void InitLayout() {
        System.out.println("app start");
        mEditIP=(EditText)this.findViewById(R.id.serverIPEdit);
        mEditPort = (EditText) this.findViewById(R.id.serverPortEdit);
        mEditName = (EditText) this.findViewById(R.id.UsernameEdit);
        mEditRoomID = (EditText) this.findViewById(R.id.RoomIDedit);
        mEditPwd =(EditText)this.findViewById(R.id.passWordEdit);
        mBottomConnMsg = (TextView) this.findViewById(R.id.conMsg);
        mBottomBuildMsg = (TextView) this.findViewById(R.id.buildMsg);

        mBtnLogin = (Button) this.findViewById(R.id.loginbut);
        mBtnLogout = (Button) this.findViewById(R.id.logoutbut);
        mBtnConfig=(Button)this.findViewById(R.id.video_cfg);

        mBottomConnMsg.setText("No content to the server");
        // 初始化bottom_tips信息
        mBottomBuildMsg.setText("V" + anyChatSDK.GetSDKMainVersion() + "."
                + anyChatSDK.GetSDKSubVersion() + "  Build time: "
                + anyChatSDK.GetSDKBuildTime());
        mBottomBuildMsg.setGravity(Gravity.CENTER_HORIZONTAL);

        mBtnLogin.setVisibility(View.VISIBLE);
        mBtnLogout.setVisibility(View.INVISIBLE);

        mBtnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //普通登录
                if (checkInputData()) {
                    AnyChatCoreSDK.SetSDKOptionString(AnyChatDefine.BRAC_SO_CLOUD_APPGUID,mStrappuid);

                    mBtnLogin.setVisibility(View.INVISIBLE);
                    mBtnLogout.setVisibility(View.VISIBLE);
                    mSRoomID = Integer.parseInt(mEditRoomID.getText().toString().trim());
                    mStrName = mEditName.getText().toString().trim();
                    mStrPwd = mEditPwd.getText().toString().trim();
                    mStrIP = mEditIP.getText().toString().trim();
                    mSPort = Integer.parseInt(mEditPort.getText().toString().trim());

                    /**
                     *AnyChat可以连接自主部署的服务器、也可以连接AnyChat视频云平台；
                     *连接自主部署服务器的地址为自设的服务器IP地址或域名、端口；
                     *连接AnyChat视频云平台的服务器地址为：cloud.anychat.cn；端口为：8906
                     */
                    anyChatSDK.Connect(mStrIP, mSPort);

                    /***
                     * AnyChat支持多种用户身份验证方式，包括更安全的签名登录，
                     * 详情请参考：http://bbs.anychat.cn/forum.php?mod=viewthread&tid=2211&highlight=%C7%A9%C3%FB
                     */
                    anyChatSDK.Login(mStrName, mStrPwd);
                }
            }
        });

        mBtnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mBtnLogin.setVisibility(View.VISIBLE);
                mBtnLogout.setVisibility(View.INVISIBLE);
                anyChatSDK.LeaveRoom(-1);
                anyChatSDK.Logout();
                mBottomConnMsg.setText("No connnect to the server");
            }
        });
        mBtnConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("start----config====");
                Intent intentConfig=new Intent();
                intentConfig.setClass(MainActivity.this,VideoConfigActivity.class);
                startActivity(intentConfig);
            }
        });
    }
    // 读取登陆数据
    private void readLoginDate() {
        SharedPreferences preferences = getSharedPreferences("LoginInfo", 0);
        mStrIP = preferences.getString("UserIP", "demo.anychat.cn");
        mStrName = preferences.getString("UserName", "Android01");
        mSPort = preferences.getInt("UserPort", 8906);
        mSRoomID = preferences.getInt("UserRoomID", 1);
        mStrPwd=preferences.getString("UserPasswd","yan2017");
    }
    // 保存登陆相关数据
    private void saveLoginData() {
        SharedPreferences preferences = getSharedPreferences("LoginInfo", 0);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putString("UserIP", mStrIP);
        preferencesEditor.putString("UserName", mStrName);
        preferencesEditor.putInt("UserPort", mSPort);
        preferencesEditor.putInt("UserRoomID", mSRoomID);
        preferencesEditor.putString("UserPasswd",mStrPwd);
        preferencesEditor.commit();
    }

    private void initLoginConfig() {
        mEditIP.setText(mStrIP);
        mEditName.setText(mStrName);
        mEditPort.setText(String.valueOf(mSPort));
        mEditRoomID.setText(String.valueOf(mSRoomID));
    }
    // check input data
    private boolean checkInputData() {
        String ip = mEditIP.getText().toString().trim();
        String port = mEditPort.getText().toString().trim();
        String name = mEditName.getText().toString().trim();
        String roomID = mEditRoomID.getText().toString().trim();
        if (ValueUtils.isStrEmpty(ip)) {
            mBottomConnMsg.setText("pleasw input server IP");
            return false;
        } else if (ValueUtils.isStrEmpty(port)) {
            mBottomConnMsg.setText("please input server port");
            return false;
        } else if (ValueUtils.isStrEmpty(name)) {
            mBottomConnMsg.setText("please input name");
            return false;
        } else if (ValueUtils.isStrEmpty(roomID)) {
            mBottomConnMsg.setText("please input room id");
            return false;
        } else {
            return true;
        }
    }
    // 根据配置文件配置视频参数
    private void ApplyVideoConfig()  {
        ConfigEntity configEntity = ConfigService.LoadConfig(this);
        if (configEntity.mConfigMode == configEntity.VIDEO_MODE_CUSTOMCONFIG) // 自定义视频参数配置
        {
            // 设置本地视频编码的码率（如果码率为0，则表示使用质量优先模式）
            AnyChatCoreSDK.SetSDKOptionInt(
                    AnyChatDefine.BRAC_SO_LOCALVIDEO_BITRATECTRL,
                    configEntity.mVideoBitrate);
//			if (configEntity.mVideoBitrate == 0) {
            // 设置本地视频编码的质量
            AnyChatCoreSDK.SetSDKOptionInt(
                    AnyChatDefine.BRAC_SO_LOCALVIDEO_QUALITYCTRL,
                    configEntity.mVideoQuality);
//			}
            // 设置本地视频编码的帧率
            AnyChatCoreSDK.SetSDKOptionInt(
                    AnyChatDefine.BRAC_SO_LOCALVIDEO_FPSCTRL,
                    configEntity.mVideoFps);
            // 设置本地视频编码的关键帧间隔
            AnyChatCoreSDK.SetSDKOptionInt(
                    AnyChatDefine.BRAC_SO_LOCALVIDEO_GOPCTRL,
                    configEntity.mVideoFps * 4);
            // 设置本地视频采集分辨率
            AnyChatCoreSDK.SetSDKOptionInt(
                    AnyChatDefine.BRAC_SO_LOCALVIDEO_WIDTHCTRL,
                    configEntity.mResolutionWidth);
            AnyChatCoreSDK.SetSDKOptionInt(
                    AnyChatDefine.BRAC_SO_LOCALVIDEO_HEIGHTCTRL,
                    configEntity.mResolutionHeight);
            // 设置视频编码预设参数（值越大，编码质量越高，占用CPU资源也会越高）
            AnyChatCoreSDK.SetSDKOptionInt(
                    AnyChatDefine.BRAC_SO_LOCALVIDEO_PRESETCTRL,
                    configEntity.mVideoPreset);
        }
        // 让视频参数生效
        AnyChatCoreSDK.SetSDKOptionInt(
                AnyChatDefine.BRAC_SO_LOCALVIDEO_APPLYPARAM,
                configEntity.mConfigMode);
        // P2P设置
        AnyChatCoreSDK.SetSDKOptionInt(
                AnyChatDefine.BRAC_SO_NETWORK_P2PPOLITIC,
                configEntity.mEnableP2P);
        // 本地视频Overlay模式设置
        AnyChatCoreSDK.SetSDKOptionInt(
                AnyChatDefine.BRAC_SO_LOCALVIDEO_OVERLAY,
                configEntity.mVideoOverlay);
        // 回音消除设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_AUDIO_ECHOCTRL,
                configEntity.mEnableAEC);
        // 平台硬件编码设置
        AnyChatCoreSDK.SetSDKOptionInt(
                AnyChatDefine.BRAC_SO_CORESDK_USEHWCODEC,
                configEntity.mUseHWCodec);
        // 视频旋转模式设置
        AnyChatCoreSDK.SetSDKOptionInt(
                AnyChatDefine.BRAC_SO_LOCALVIDEO_ROTATECTRL,
                configEntity.mVideoRotateMode);
        // 本地视频采集偏色修正设置
        AnyChatCoreSDK.SetSDKOptionInt(
                AnyChatDefine.BRAC_SO_LOCALVIDEO_FIXCOLORDEVIA,
                configEntity.mFixColorDeviation);
        // 视频GPU渲染设置
        AnyChatCoreSDK.SetSDKOptionInt(
                AnyChatDefine.BRAC_SO_VIDEOSHOW_GPUDIRECTRENDER,
                configEntity.mVideoShowGPURender);
        // 本地视频自动旋转设置
        AnyChatCoreSDK.SetSDKOptionInt(
                AnyChatDefine.BRAC_SO_LOCALVIDEO_AUTOROTATION,
                configEntity.mVideoAutoRotation);
    }

    // 广播
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("VideoActivity")) {
                Toast.makeText(MainActivity.this, "网络已断开！", Toast.LENGTH_SHORT)
                        .show();
                mBtnLogin.setVisibility(View.VISIBLE);
                mBtnLogout.setVisibility(View.INVISIBLE);
                mBottomConnMsg.setText("loss content to the server");
                anyChatSDK.LeaveRoom(-1);
                anyChatSDK.Logout();
            }
        }
    };

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction("VideoActivity");
        // 注册广播
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }


    //extends---AppCompatActivity
    @Override
    protected void onDestroy() {
        if(mBtnLogin.getVisibility()!=View.VISIBLE){
            anyChatSDK.Logout();
        }
        anyChatSDK.Release();
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
        anyChatSDK.SetBaseEvent(this);
    }

    //implement---baseEvent
    @Override
    public void OnAnyChatConnectMessage(boolean bSuccess) {
        //
        if (!bSuccess) {
            mBtnLogin.setVisibility(View.VISIBLE);
            mBtnLogout.setVisibility(View.INVISIBLE);
            mBottomConnMsg.setText("failed connect servers...reconnect...please wait..");
        }
    }

    @Override
    public void OnAnyChatLoginMessage(int dwUserId, int dwErrorCode) {
        if (dwErrorCode == 0) {
            saveLoginData();
            mBtnLogin.setVisibility(View.INVISIBLE);
            mBtnLogout.setVisibility(View.VISIBLE);

            mBottomConnMsg.setText("Connect to the server success...");
            int sHourseID = Integer.valueOf(mEditRoomID.getEditableText().toString());
//            System.out.println("roomid-----="+sHourseID);//debug

            anyChatSDK.EnterRoom(sHourseID, "");
            // finish();
        } else {
            mBtnLogout.setVisibility(View.VISIBLE);
            mBtnLogin.setVisibility(View.INVISIBLE);
            mBottomConnMsg.setText("login error，errorCode：" + dwErrorCode);
        }
    }

    @Override
    public void OnAnyChatEnterRoomMessage(int dwRoomId, int dwErrorCode) {
//        System.out.println("OnAnyChatEnterRoomMessage------=" + dwRoomId + "err:" + dwErrorCode);
        Intent intentVideo=new Intent();
        intentVideo.putExtra("UserselfID",String.valueOf(UserselfID));
        intentVideo.setClass(MainActivity.this,VideoChatActivity.class);
        startActivity(intentVideo);
    }

    @Override
    public void OnAnyChatOnlineUserMessage(int dwUserNum, int dwRoomId) {
        mBottomConnMsg.setText("seccessed enter the room！user:"+dwUserNum);
    }

    @Override
    public void OnAnyChatUserAtRoomMessage(int dwUserId, boolean bEnter) {
        System.out.println("at room "+dwUserId);
    }

    @Override
    public void OnAnyChatLinkCloseMessage(int dwErrorCode) {
        mBtnLogin.setVisibility(View.INVISIBLE);
        mBtnLogout.setVisibility(View.VISIBLE);
        anyChatSDK.LeaveRoom(-1);
        anyChatSDK.Logout();
        mBottomConnMsg.setText("server close connect ，errorCode：" + dwErrorCode);
    }


}
