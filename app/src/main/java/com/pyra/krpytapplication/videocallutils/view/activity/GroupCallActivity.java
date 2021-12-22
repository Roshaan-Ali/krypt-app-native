package com.pyra.krpytapplication.videocallutils.view.activity;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pyra.krpytapplication.R;
import com.pyra.krpytapplication.utils.SharedHelper;
import com.pyra.krpytapplication.notification.NotificationUtils;
import com.pyra.krpytapplication.roomDb.entity.ChatListSchema;
import com.pyra.krpytapplication.rxbus.RxBusNotification;
import com.pyra.krpytapplication.videocallutils.customui.GridVideoViewContainer;
import com.pyra.krpytapplication.videocallutils.customui.RecyclerItemClickListener;
import com.pyra.krpytapplication.videocallutils.customui.RtlLinearLayoutManager;
import com.pyra.krpytapplication.videocallutils.customui.SmallVideoViewAdapter;
import com.pyra.krpytapplication.videocallutils.customui.SmallVideoViewDecoration;
import com.pyra.krpytapplication.videocallutils.data.Constant;
import com.pyra.krpytapplication.videocallutils.data.UserStatusData;
import com.pyra.krpytapplication.videocallutils.data.VideoInfoData;
import com.pyra.krpytapplication.videocallutils.events.AGEventHandler;
import com.pyra.krpytapplication.videocallutils.events.ConstantApp;
import com.pyra.krpytapplication.videocallutils.events.DuringCallEventHandler;
import com.pyra.krpytapplication.viewmodel.CallViewModel;

import java.util.HashMap;
import java.util.Iterator;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class GroupCallActivity extends BaseActivity implements DuringCallEventHandler {

    public static final int LAYOUT_TYPE_DEFAULT = 0;
    public static final int LAYOUT_TYPE_SMALL = 1;

    // should only be modified under UI thread
    private final HashMap<Integer, SurfaceView> mUidsList = new HashMap<>(); // uid = 0 || uid == EngineConfig.mUid
    public int mLayoutType = LAYOUT_TYPE_DEFAULT;
    private GridVideoViewContainer mGridVideoViewContainer;
    private RelativeLayout mSmallVideoViewDock;
    private volatile boolean mVideoMuted = false;
    private volatile boolean mAudioMuted = false;
    private boolean mIsLandscape = false;
    private SmallVideoViewAdapter mSmallVideoViewAdapter;

    private Disposable notificationEventListener;

    String channelName;
    String callType;
    String incomingOutGoing;
    String roomId;

    //views
    ImageView speaker;

    private Boolean isSpeakerOnAudio = false;

    CallViewModel callViewModel;


    TextView doctoreName, doctoreNameVideo;

    ConstraintLayout callLayout;
    FrameLayout videoLayout;
    Group groupOutGoing, groupInComing;
    Group incomingVideoCall, outGoingVideoCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_call);

        callViewModel = new ViewModelProvider(this).get(CallViewModel.class);
        initView();
        initListener();


    }


    private void initListener() {


        notificationEventListener =
                RxBusNotification.INSTANCE.
                        listen(String.class).subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                switch (s) {
                                    case com.pyra.krpytapplication.utils.Constants.EventBusKeys.ACCEPT_CALL: {
                                        onCallAccepted();
                                        notificationEventListener.dispose();
                                    }
                                    //made by user
                                    case com.pyra.krpytapplication.utils.Constants.EventBusKeys.REJECT_CALL: {
//                                        endCall();
                                        notificationEventListener.dispose();
                                    }
                                    //made by both
                                    case com.pyra.krpytapplication.utils.Constants.EventBusKeys.END_CALL: {
//                                        endCall();
                                        notificationEventListener.dispose();
                                    }
                                    //made by both
                                    case com.pyra.krpytapplication.utils.Constants.EventBusKeys.HANGUP_CALL: {
//                                        endCall();
                                        notificationEventListener.dispose();
                                    }
                                }
                            }
                        });
                    }
                });

    }

    private void endCall() {
        finishAndRemoveTask();
    }


    private void initView() {

        speaker = findViewById(R.id.speaker);
        doctoreName = findViewById(R.id.doctoreName);
        doctoreNameVideo = findViewById(R.id.doctoreNameVideo);

        callLayout = findViewById(R.id.callLayout);
        videoLayout = findViewById(R.id.videoLayout);
        groupOutGoing = findViewById(R.id.groupOutGoing);
        groupInComing = findViewById(R.id.groupInComing);
        incomingVideoCall = findViewById(R.id.incomingVideoCall);
        outGoingVideoCall = findViewById(R.id.outGoingVideoCall);


    }

    private void getIntentValues() {

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            channelName = bundle.getString(com.pyra.krpytapplication.utils.Constants.NotificationIntentValues.CHANNEL_ID, "");
            callType = bundle.getString(com.pyra.krpytapplication.utils.Constants.NotificationIntentValues.CALL_TYPE, "");
            incomingOutGoing = bundle.getString(com.pyra.krpytapplication.utils.Constants.NotificationIntentValues.CALL_FROM, "");
            roomId = bundle.getString(com.pyra.krpytapplication.utils.Constants.IntentKeys.ROOMID, "");

            setUi();
            getData();
        }

    }

    private void getData() {
        callViewModel.getRoomDetails(roomId).observe(this, new Observer<ChatListSchema>() {
            @Override
            public void onChanged(ChatListSchema chatListSchema) {
                if (chatListSchema != null) {
                    doctoreName.setText(chatListSchema.getRoomName());
                    doctoreNameVideo.setText(chatListSchema.getRoomName());

                }
            }
        });
    }

    private void setUi() {

        if (incomingOutGoing.equalsIgnoreCase(com.pyra.krpytapplication.utils.Constants.ChatTypes.INCOMING_CALL)) {

            if (callType.equalsIgnoreCase(com.pyra.krpytapplication.utils.Constants.ChatTypes.VOICE_CALL)) {
                callLayout.setVisibility(View.VISIBLE);
                videoLayout.setVisibility(View.GONE);
                callLayout.bringToFront();

                groupInComing.setVisibility(View.VISIBLE);
                groupOutGoing.setVisibility(View.GONE);

                rtcEngine().disableVideo();


            } else {

                callLayout.setVisibility(View.GONE);
                videoLayout.setVisibility(View.VISIBLE);
                incomingVideoCall.setVisibility(View.VISIBLE);
                outGoingVideoCall.setVisibility(View.GONE);

                rtcEngine().enableVideo();

            }

        } else {


            if (callType.equalsIgnoreCase(com.pyra.krpytapplication.utils.Constants.ChatTypes.VOICE_CALL)) {
                callLayout.setVisibility(View.VISIBLE);
                videoLayout.setVisibility(View.GONE);
                callLayout.bringToFront();

                groupOutGoing.setVisibility(View.VISIBLE);
                groupInComing.setVisibility(View.GONE);

                rtcEngine().disableVideo();

            } else {

                callLayout.setVisibility(View.GONE);
                videoLayout.setVisibility(View.VISIBLE);
                outGoingVideoCall.setVisibility(View.VISIBLE);
                incomingVideoCall.setVisibility(View.GONE);

                rtcEngine().enableVideo();
            }

            joinChannel(channelName, config().mUid);

        }


    }


    @Override
    protected void initUIandEvent() {
        addEventHandler(this);
        channelName = getIntent().getStringExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME);
        getIntentValues();

        // programmatically layout ui below of status bar/action bar
//        LinearLayout eopsContainer = findViewById(R.id.extra_ops_container);
//        RelativeLayout.MarginLayoutParams eofmp = (RelativeLayout.MarginLayoutParams) eopsContainer.getLayoutParams();
//        eofmp.topMargin = getStatusBarHeight() + getActionBarHeight() + getResources().getDimensionPixelOffset(R.dimen.activity_vertical_margin) / 2; // status bar + action bar + divider

        final String encryptionKey = getIntent().getStringExtra(ConstantApp.ACTION_KEY_ENCRYPTION_KEY);
        final String encryptionMode = getIntent().getStringExtra(ConstantApp.ACTION_KEY_ENCRYPTION_MODE);

        doConfigEngine(encryptionKey, encryptionMode);

        mGridVideoViewContainer = (GridVideoViewContainer) findViewById(R.id.grid_video_view_container);

        SurfaceView surfaceV = RtcEngine.CreateRendererView(getApplicationContext());
        preview(true, surfaceV, 0);
        surfaceV.setZOrderOnTop(false);
        surfaceV.setZOrderMediaOverlay(false);

        mUidsList.put(0, surfaceV); // get first surface view

        mGridVideoViewContainer.initViewContainer(this, 0, mUidsList, mIsLandscape); // first is now full view


//        optional();
    }

    private void onSmallVideoViewDoubleClicked(View view, int position) {

        switchToDefaultVideoView();
    }


    private void optional() {
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }

    private void optionalDestroy() {
    }

    private int getVideoEncResolutionIndex() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int videoEncResolutionIndex = pref.getInt(ConstantApp.PrefManager.PREF_PROPERTY_VIDEO_ENC_RESOLUTION, ConstantApp.DEFAULT_VIDEO_ENC_RESOLUTION_IDX);
        if (videoEncResolutionIndex > ConstantApp.VIDEO_DIMENSIONS.length - 1) {
            videoEncResolutionIndex = ConstantApp.DEFAULT_VIDEO_ENC_RESOLUTION_IDX;

            // save the new value
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(ConstantApp.PrefManager.PREF_PROPERTY_VIDEO_ENC_RESOLUTION, videoEncResolutionIndex);
            editor.apply();
        }
        return videoEncResolutionIndex;
    }

    private int getVideoEncFpsIndex() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int videoEncFpsIndex = pref.getInt(ConstantApp.PrefManager.PREF_PROPERTY_VIDEO_ENC_FPS, ConstantApp.DEFAULT_VIDEO_ENC_FPS_IDX);
        if (videoEncFpsIndex > ConstantApp.VIDEO_FPS.length - 1) {
            videoEncFpsIndex = ConstantApp.DEFAULT_VIDEO_ENC_FPS_IDX;

            // save the new value
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(ConstantApp.PrefManager.PREF_PROPERTY_VIDEO_ENC_FPS, videoEncFpsIndex);
            editor.apply();
        }
        return videoEncFpsIndex;
    }

    private void doConfigEngine(String encryptionKey, String encryptionMode) {
        VideoEncoderConfiguration.VideoDimensions videoDimension = ConstantApp.VIDEO_DIMENSIONS[getVideoEncResolutionIndex()];
        VideoEncoderConfiguration.FRAME_RATE videoFps = ConstantApp.VIDEO_FPS[getVideoEncFpsIndex()];
        configEngine(videoDimension, videoFps, encryptionKey, encryptionMode);
    }

    public void onSwitchCameraClicked(View view) {
        RtcEngine rtcEngine = rtcEngine();
        // Switches between front and rear cameras.
        rtcEngine.switchCamera();
    }


    @Override
    protected void deInitUIandEvent() {
        optionalDestroy();
        doLeaveChannel();
        removeEventHandler(this);
        mUidsList.clear();
    }

    private void doLeaveChannel() {
        leaveChannel(config().mChannel);
        preview(false, null, 0);
    }

    public void onHangupClicked(View view) {
        endCall();
    }

    public void onVoiceCallAccepted(View view) {

        onCallAccepted();

    }

    private void onCallAccepted() {

        if (callType.equalsIgnoreCase(com.pyra.krpytapplication.utils.Constants.ChatTypes.VOICE_CALL)) {

            callLayout.setVisibility(View.VISIBLE);
            videoLayout.setVisibility(View.GONE);
            callLayout.bringToFront();

            groupInComing.setVisibility(View.GONE);
            groupOutGoing.setVisibility(View.VISIBLE);

            rtcEngine().disableVideo();

        } else {

            callLayout.setVisibility(View.GONE);
            videoLayout.setVisibility(View.VISIBLE);

            incomingVideoCall.setVisibility(View.GONE);
            outGoingVideoCall.setVisibility(View.VISIBLE);

        }

        new NotificationUtils(this).removeCallNotifications();
        joinChannel(channelName, config().mUid);

    }

    public void onSpeakerChangeClicked(View view) {
        isSpeakerOnAudio = !isSpeakerOnAudio;
        setSpeakerUi();
    }

    private void setSpeakerUi() {

        rtcEngine().setEnableSpeakerphone(isSpeakerOnAudio);

        if (isSpeakerOnAudio) {
            speaker.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.speaker_on));
        } else {
            speaker.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.speaker_off));
        }

    }


    public void onVideoMuteClicked(View view) {
        if (mUidsList.size() == 0) {
            return;
        }

        SurfaceView surfaceV = getLocalView();
        ViewParent parent;
        if (surfaceV == null || (parent = surfaceV.getParent()) == null) {
            return;
        }

        RtcEngine rtcEngine = rtcEngine();
        mVideoMuted = !mVideoMuted;

        if (mVideoMuted) {
            rtcEngine.disableVideo();
        } else {
            rtcEngine.enableVideo();
        }

        ImageView iv = (ImageView) view;

        iv.setImageResource(mVideoMuted ? R.drawable.video_off : R.drawable.video_on);

        hideLocalView(mVideoMuted);
    }

    private SurfaceView getLocalView() {
        for (HashMap.Entry<Integer, SurfaceView> entry : mUidsList.entrySet()) {
            if (entry.getKey() == 0 || entry.getKey() == config().mUid) {
                return entry.getValue();
            }
        }

        return null;
    }

    private void hideLocalView(boolean hide) {
        int uid = config().mUid;
        doHideTargetView(uid, hide);
    }

    private void doHideTargetView(int targetUid, boolean hide) {
        HashMap<Integer, Integer> status = new HashMap<>();
        status.put(targetUid, hide ? UserStatusData.VIDEO_MUTED : UserStatusData.DEFAULT_STATUS);
        if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
            mGridVideoViewContainer.notifyUiChanged(mUidsList, targetUid, status, null);
        } else if (mLayoutType == LAYOUT_TYPE_SMALL) {
            UserStatusData bigBgUser = mGridVideoViewContainer.getItem(0);
            if (bigBgUser.mUid == targetUid) { // big background is target view
                mGridVideoViewContainer.notifyUiChanged(mUidsList, targetUid, status, null);
            } else { // find target view in small video view list
                mSmallVideoViewAdapter.notifyUiChanged(mUidsList, bigBgUser.mUid, status, null);
            }
        }
    }

    public void onVoiceMuteClicked(View view) {
        if (mUidsList.size() == 0) {
            return;
        }

        RtcEngine rtcEngine = rtcEngine();
        rtcEngine.muteLocalAudioStream(mAudioMuted = !mAudioMuted);

        ImageView iv = (ImageView) view;

        iv.setImageResource(mAudioMuted ? R.drawable.btn_microphone_off : R.drawable.btn_microphone);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new SharedHelper(this).setCurrentScreen("GroupCallActivity");
    }

    @Override
    protected void onPause() {
        super.onPause();
        new SharedHelper(this).setCurrentScreen("");
    }

    @Override
    public void onUserJoined(int uid) {

        if (mUidsList.size() == 2 || incomingOutGoing.equalsIgnoreCase(com.pyra.krpytapplication.utils.Constants.ChatTypes.OUTGOING_CALL)) {
            onCallAccepted();
        }

    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {

        doRenderRemoteUi(uid);
    }

    private void doRenderRemoteUi(final int uid) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                if (mUidsList.containsKey(uid)) {
                    return;
                }

                /*
                  Creates the video renderer view.
                  CreateRendererView returns the SurfaceView type. The operation and layout of the
                  view are managed by the app, and the Agora SDK renders the view provided by the
                  app. The video display view must be created using this method instead of
                  directly calling SurfaceView.
                 */
                SurfaceView surfaceV = RtcEngine.CreateRendererView(getApplicationContext());
                mUidsList.put(uid, surfaceV);

                boolean useDefaultLayout = mLayoutType == LAYOUT_TYPE_DEFAULT;

                surfaceV.setZOrderOnTop(true);
                surfaceV.setZOrderMediaOverlay(true);

                /*
                  Initializes the video view of a remote user.
                  This method initializes the video view of a remote stream on the local device. It affects only the video view that the local user sees.
                  Call this method to bind the remote video stream to a video view and to set the rendering and mirror modes of the video view.
                 */
                rtcEngine().setupRemoteVideo(new VideoCanvas(surfaceV, VideoCanvas.RENDER_MODE_HIDDEN, uid));

                if (useDefaultLayout) {
                    switchToDefaultVideoView();
                } else {
                    int bigBgUid = mSmallVideoViewAdapter == null ? uid : mSmallVideoViewAdapter.getExceptedUid();
                    switchToSmallVideoView(bigBgUid);
                }

            }
        });
    }

    @Override
    public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        doRemoveRemoteUi(uid);
    }

    @Override
    public void onExtraCallback(final int type, final Object... data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                doHandleExtraCallback(type, data);
            }
        });
    }

    private void doHandleExtraCallback(int type, Object... data) {
        int peerUid;
        boolean muted;

        switch (type) {
            case AGEventHandler.EVENT_TYPE_ON_USER_AUDIO_MUTED:
                peerUid = (Integer) data[0];
                muted = (boolean) data[1];

                if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
                    HashMap<Integer, Integer> status = new HashMap<>();
                    status.put(peerUid, muted ? UserStatusData.AUDIO_MUTED : UserStatusData.DEFAULT_STATUS);
                    mGridVideoViewContainer.notifyUiChanged(mUidsList, config().mUid, status, null);
                }

                break;

            case AGEventHandler.EVENT_TYPE_ON_USER_VIDEO_MUTED:
                peerUid = (Integer) data[0];
                muted = (boolean) data[1];

                doHideTargetView(peerUid, muted);

                break;

            case AGEventHandler.EVENT_TYPE_ON_USER_VIDEO_STATS:
                IRtcEngineEventHandler.RemoteVideoStats stats = (IRtcEngineEventHandler.RemoteVideoStats) data[0];

                if (Constant.SHOW_VIDEO_INFO) {
                    if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
                        mGridVideoViewContainer.addVideoInfo(stats.uid, new VideoInfoData(stats.width, stats.height, stats.delay, stats.rendererOutputFrameRate, stats.receivedBitrate));
                        int uid = config().mUid;
                        int profileIndex = getVideoEncResolutionIndex();
                        String resolution = getResources().getStringArray(R.array.string_array_resolutions)[profileIndex];
                        String fps = getResources().getStringArray(R.array.string_array_frame_rate)[profileIndex];

                        String[] rwh = resolution.split("x");
                        int width = Integer.valueOf(rwh[0]);
                        int height = Integer.valueOf(rwh[1]);

                        mGridVideoViewContainer.addVideoInfo(uid, new VideoInfoData(width > height ? width : height,
                                width > height ? height : width,
                                0, Integer.valueOf(fps), Integer.valueOf(0)));
                    }
                } else {
                    mGridVideoViewContainer.cleanVideoInfo();
                }

                break;

            case AGEventHandler.EVENT_TYPE_ON_SPEAKER_STATS:
                IRtcEngineEventHandler.AudioVolumeInfo[] infos = (IRtcEngineEventHandler.AudioVolumeInfo[]) data[0];

                if (infos.length == 1 && infos[0].uid == 0) { // local guy, ignore it
                    break;
                }

                if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
                    HashMap<Integer, Integer> volume = new HashMap<>();

                    for (IRtcEngineEventHandler.AudioVolumeInfo each : infos) {
                        peerUid = each.uid;
                        int peerVolume = each.volume;

                        if (peerUid == 0) {
                            continue;
                        }
                        volume.put(peerUid, peerVolume);
                    }
                    mGridVideoViewContainer.notifyUiChanged(mUidsList, config().mUid, null, volume);
                }

                break;

            case AGEventHandler.EVENT_TYPE_ON_APP_ERROR:
                int subType = (int) data[0];

                if (subType == ConstantApp.AppError.NO_CONNECTION_ERROR) {
                    String msg = getString(R.string.msg_connection_error);
                    showLongToast(msg);
                }

                break;

            case AGEventHandler.EVENT_TYPE_ON_DATA_CHANNEL_MSG:

                peerUid = (Integer) data[0];
                final byte[] content = (byte[]) data[1];

                break;

            case AGEventHandler.EVENT_TYPE_ON_AGORA_MEDIA_ERROR: {
                int error = (int) data[0];
                String description = (String) data[1];


                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_AUDIO_ROUTE_CHANGED:

                break;

        }
    }

    private void requestRemoteStreamType(final int currentHostCount) {
    }

    private void doRemoveRemoteUi(final int uid) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                Object target = mUidsList.remove(uid);
                if (target == null) {
                    return;
                }

                int bigBgUid = -1;
                if (mSmallVideoViewAdapter != null) {
                    bigBgUid = mSmallVideoViewAdapter.getExceptedUid();
                }


                if (mLayoutType == LAYOUT_TYPE_DEFAULT || uid == bigBgUid) {
                    switchToDefaultVideoView();
                } else {
                    switchToSmallVideoView(bigBgUid);
                }
            }
        });
    }

    private void switchToDefaultVideoView() {
        if (mSmallVideoViewDock != null) {
            mSmallVideoViewDock.setVisibility(View.GONE);
        }
        mGridVideoViewContainer.initViewContainer(this, config().mUid, mUidsList, mIsLandscape);

        mLayoutType = LAYOUT_TYPE_DEFAULT;
        boolean setRemoteUserPriorityFlag = false;
        int sizeLimit = mUidsList.size();
        if (sizeLimit > ConstantApp.MAX_PEER_COUNT + 1) {
            sizeLimit = ConstantApp.MAX_PEER_COUNT + 1;
        }
        for (int i = 0; i < sizeLimit; i++) {
            int uid = mGridVideoViewContainer.getItem(i).mUid;
            if (config().mUid != uid) {
                if (!setRemoteUserPriorityFlag) {
                    setRemoteUserPriorityFlag = true;
                    rtcEngine().setRemoteUserPriority(uid, Constants.USER_PRIORITY_HIGH);
                } else {
                    rtcEngine().setRemoteUserPriority(uid, Constants.USER_PRIORITY_NORANL);
                }
            }
        }
    }

    private void switchToSmallVideoView(int bigBgUid) {
        HashMap<Integer, SurfaceView> slice = new HashMap<>(1);
        slice.put(bigBgUid, mUidsList.get(bigBgUid));
        Iterator<SurfaceView> iterator = mUidsList.values().iterator();
        while (iterator.hasNext()) {
            SurfaceView s = iterator.next();
            s.setZOrderOnTop(true);
            s.setZOrderMediaOverlay(true);
        }

        mUidsList.get(bigBgUid).setZOrderOnTop(false);
        mUidsList.get(bigBgUid).setZOrderMediaOverlay(false);

        mGridVideoViewContainer.initViewContainer(this, bigBgUid, slice, mIsLandscape);

        bindToSmallVideoView(bigBgUid);

        mLayoutType = LAYOUT_TYPE_SMALL;

        requestRemoteStreamType(mUidsList.size());
    }

    private void bindToSmallVideoView(int exceptUid) {
        if (mSmallVideoViewDock == null) {
            ViewStub stub = (ViewStub) findViewById(R.id.small_video_view_dock);
            mSmallVideoViewDock = (RelativeLayout) stub.inflate();
        }

        boolean twoWayVideoCall = mUidsList.size() == 2;

        RecyclerView recycler = (RecyclerView) findViewById(R.id.small_video_view_container);

        boolean create = false;

        if (mSmallVideoViewAdapter == null) {
            create = true;
            mSmallVideoViewAdapter = new SmallVideoViewAdapter(this, config().mUid, exceptUid, mUidsList);
            mSmallVideoViewAdapter.setHasStableIds(true);
        }
        recycler.setHasFixedSize(true);


        if (twoWayVideoCall) {
            recycler.setLayoutManager(new RtlLinearLayoutManager(getApplicationContext(), RtlLinearLayoutManager.HORIZONTAL, false));
        } else {
            recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        }
        recycler.addItemDecoration(new SmallVideoViewDecoration());
        recycler.setAdapter(mSmallVideoViewAdapter);
        recycler.addOnItemTouchListener(new RecyclerItemClickListener(getBaseContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onItemDoubleClick(View view, int position) {
                onSmallVideoViewDoubleClicked(view, position);
            }
        }));

        recycler.setDrawingCacheEnabled(true);
        recycler.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);

        if (!create) {
            mSmallVideoViewAdapter.setLocalUid(config().mUid);
            mSmallVideoViewAdapter.notifyUiChanged(mUidsList, exceptUid, null, null);
        }
        for (Integer tempUid : mUidsList.keySet()) {
            if (config().mUid != tempUid) {
                if (tempUid == exceptUid) {
                    rtcEngine().setRemoteUserPriority(tempUid, Constants.USER_PRIORITY_HIGH);
                } else {
                    rtcEngine().setRemoteUserPriority(tempUid, Constants.USER_PRIORITY_NORANL);
                }
            }
        }
        recycler.setVisibility(View.VISIBLE);
        mSmallVideoViewDock.setVisibility(View.VISIBLE);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mIsLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;

        if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
            switchToDefaultVideoView();
        } else if (mSmallVideoViewAdapter != null) {
            switchToSmallVideoView(mSmallVideoViewAdapter.getExceptedUid());
        }
    }
}
