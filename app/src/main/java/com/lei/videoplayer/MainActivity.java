package com.lei.videoplayer;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    private LinearLayout controllerLayout;
    private ImageView play_controller_img, screen_img, volume_img;
    private TextView time_current_tv, time_total_tv;
    private SeekBar play_seek, volume_seek;
    public static final int UPDATE_UI = 1;
    private int screen_width,screen_height;//当前获取屏幕的宽和高
    private RelativeLayout videoLayout;
    private AudioManager mAudioManager;//音频管理器
    private boolean isFullScreen = false;
    private boolean isAdjust = false;
    private int threshold = 100;
    private float mBrightness;
    private ImageView operation_bg,operation_prcent;
    private FrameLayout progress_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);//获取系统的音频服务
        initUI();
        setPlayerEvent();

        //视频路径
        Log.i("Main","path");
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/a.mp4";
        Log.i("Main",path);
        /**
         * 本地视频播放
         */
        videoView.setVideoPath(path);
        videoView.start();
       // UIHandler.sendEmptyMessage(UPDATE_UI);

        /**
         * 网络视频播放
         */
        //videoView.setVideoURI(Uri.parse(""));

        /**
         * 使用MediaContorller控制视频播放
         */
        // MediaController controller = new MediaController(this);
        /**
         * 设置videoView与MediaController建立关联
         */
        //videoView.setMediaController(controller);
        /**
         * 设置MediaController与videoView建立关联
         */
        //controller.setMediaPlayer(videoView);
    }



    private void updateTextViewWithTimeFormat(TextView textView, int millisecound) {
        int second = millisecound / 1000;
        int hh = second / 3600;
        int mm = second % 3600 / 60;
        int ss = second % 60;
        String str = null;

        if (hh != 0) {
            str = String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            str = String.format("%02d:%02d", mm, ss);
        }
        textView.setText(str);
    }

    /**
     * 刷新界面
     */
    private Handler UIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == UPDATE_UI) {
                int currentPosition = videoView.getCurrentPosition();//获取当前播放时间
                int totalduration = videoView.getDuration();//获取视频总时间

                //格式化视频播放时间
                updateTextViewWithTimeFormat(time_current_tv, currentPosition);
                updateTextViewWithTimeFormat(time_total_tv, totalduration);

                //播放进度
                play_seek.setMax(totalduration);
                play_seek.setProgress(currentPosition);

                //自动刷新,每隔0.5秒
                UIHandler.sendEmptyMessageAtTime(UPDATE_UI, 500);
            }
        }
    };

    /**
     * 视频暂停状态下
     */
    @Override
    protected void onPause() {
        super.onPause();
        UIHandler.removeMessages(UPDATE_UI);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     *
     */
    private void setPlayerEvent() {

        /**
         * 控制视频的播放和暂停
         */
        play_controller_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView.isPlaying()) {
                    play_controller_img.setImageResource(R.drawable.play_btn_style);
                    //暂停播放
                    videoView.pause();
                    UIHandler.removeMessages(UPDATE_UI);
                } else {
                    play_controller_img.setImageResource(R.drawable.pause_btn_style);
                    //继续播放
                    videoView.start();
                    UIHandler.sendEmptyMessage(UPDATE_UI);
                }
            }
        });


        /**
         * 拖动进度
         */
        play_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTextViewWithTimeFormat(time_current_tv,progress);
            }

            //开始拖动
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            //停止拖动
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();

                //跳转进度，令视频的播放进度遵循seekBar停止拖动这一刻的进度
                videoView.seekTo(progress);
            }
        });

        /**
         * 调节声音
         */
        volume_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                /*
                设置当前设备的音量
                 *///                                 类型                  大小
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
            }

            //开始拖动
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            //停止拖动
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        /*
        横竖屏切换
         */
        screen_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFullScreen) {
                    //切换竖屏
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    //切换横屏
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        });

        /**
         * 控制VideoView的手势事件
         */
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();//手势触摸的x坐标
                float y = event.getY();//y坐标
                float lastX = 0, lastY = 0;
                switch(event.getAction()) {
                    /*
                    手指落下屏幕的一刻（调用一次）
                     */
                    case MotionEvent.ACTION_DOWN:
                        lastX = x;
                        lastY = y;
                        break;
                    /*
                    手指在屏幕上移动（调用多次）
                    */
                    case MotionEvent.ACTION_MOVE:
                        float detlaX = x - lastX;
                        float detlaY = y - lastY;// 手指滑动时x轴和y轴的偏移量
                        float absdetlaX = Math.abs(detlaX);
                        float absdetlaY = Math.abs(detlaY);//手指滑动时X轴和y轴偏移量的绝对值
                        if(absdetlaX > threshold && absdetlaY > threshold) {//斜着划
                            if(absdetlaX < absdetlaY) {
                                isAdjust = true;
                            } else {
                                isAdjust = false;
                            }
                        } else if(absdetlaX < threshold && absdetlaY > threshold){
                            isAdjust = true;
                        } else if(absdetlaX > threshold && absdetlaY < threshold) {
                            isAdjust = false;
                        }
                        Log.i("Main","手势是否合法" + isAdjust);
                        if(isAdjust) {
                            /**
                             * 在判断好当前手势事件已经合法的前提下，去区分此时手势应该调节亮度还是声音
                             */
                            if(x < screen_width / 2) {
                                /*
                                调节亮度
                                 */
                                if(detlaY > 0) {
                                    /*
                                    降低亮度
                                     */
                                    //Log.i("Main","降低亮度" + detlaY);
                                    Toast.makeText(MainActivity.this,"降低亮度" + detlaY,Toast.LENGTH_SHORT).show();
                                } else {
                                    /*
                                    升高亮度
                                     */
                                    Toast.makeText(MainActivity.this,"升高亮度" + detlaY,Toast.LENGTH_SHORT).show();
                                }
                                changeBrightness(-detlaY);
                            } else {
                                /*
                                调节声音
                                 */
                                if(detlaY > 0) {
                                    /*
                                    减小音量
                                     */
                                    Toast.makeText(MainActivity.this,"减小音量" + detlaY,Toast.LENGTH_SHORT).show();
                                } else {
                                    /*
                                    增加音量
                                     */
                                    Toast.makeText(MainActivity.this,"增加音量" + detlaY,Toast.LENGTH_SHORT).show();
                                }

                                changeVolume(-detlaY);
                            }
                        }
                        lastX = x;
                        lastY = y;
                        break;
                    /*
                    手指抬起的一刻（调用一次）
                     */
                    case MotionEvent.ACTION_UP:
                        progress_layout.setVisibility(View.GONE);
                        break;
                }
                return true;
            }
        });
    }
    private void changeVolume(float detlaY) {//改变音量
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//获取最大音量
        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//当前的声音
        int index = (int)(detlaY / screen_height * max * 3);//计算百分比
        int volume = Math.max(current + index, 0);//声音不能小于0
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        if(progress_layout.getVisibility() == View.GONE) {
            progress_layout.setVisibility(View.VISIBLE);
        }
        operation_bg.setImageResource(R.mipmap.volumn);
        ViewGroup.LayoutParams layoutParams = operation_prcent.getLayoutParams();
        layoutParams.width = (int) (PixelUtil.dp2px(94) * (float)volume / max);
        operation_prcent.setLayoutParams(layoutParams);
        volume_seek.setProgress(volume);
    }

    private void changeBrightness(float detlaY) {//调节亮度
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        mBrightness = attributes.screenBrightness;
        float index = detlaY / screen_height;
        mBrightness += index;
        if(mBrightness > 1.0f) {
            mBrightness = 1.0f;
        }
        if(mBrightness < 0.01f) {
            mBrightness = 0.01f;
        }
        attributes.screenBrightness = mBrightness;

        getWindow().setAttributes(attributes);
    }
    /**
     * 监听屏幕方向的改变
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            /*
            当屏幕就放行为横屏的时候
             */
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            volume_img.setVisibility(View.VISIBLE);
            volume_seek.setVisibility(View.VISIBLE);
            isFullScreen = true;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            //移除半屏状态
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //设置全屏
        } else {
            /*
            当屏幕方向为竖屏的时候
             */
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, PixelUtil.dp2px(240));
            volume_img.setVisibility(View.GONE);
            volume_seek.setVisibility(View.GONE);
            isFullScreen = false;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        }
    }

    /**
     * 设置videoView的宽和高
     */
    private void setVideoViewScale(int width,int height) {
        ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        videoView.setLayoutParams(layoutParams);

        ViewGroup.LayoutParams layoutParams1 = videoView.getLayoutParams();
        layoutParams1.width = width;
        layoutParams1.height = height;
        videoLayout.setLayoutParams(layoutParams1);
    }


    /**
     * 初始化UI布局
     */
    private void initUI() {
        PixelUtil.initContext(this);
        videoView = findViewById(R.id.videoView);
        controllerLayout = findViewById(R.id.controllerbar_layout);
        play_controller_img = findViewById(R.id.pause_img);
        screen_img = findViewById(R.id.screen_img);
        volume_img = findViewById(R.id.volume_img);
        time_current_tv = findViewById(R.id.time_current_tv);
        time_total_tv = findViewById(R.id.time_total_tv);
        play_seek = findViewById(R.id.play_seek);
        volume_seek = findViewById(R.id.volume_seek);
        videoLayout = findViewById(R.id.videoLayout);
        screen_width = videoView.getResources().getDisplayMetrics().widthPixels;
        screen_height = videoView.getResources().getDisplayMetrics().heightPixels;
        operation_bg = findViewById(R.id.operation_bg);
        operation_prcent = findViewById(R.id.operation_prcent);
        progress_layout = findViewById(R.id.progress_layout);
        /*
        当前设备的最大音量
         */
        int streamMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        /*
        获取设备当前的音量
         */
        int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volume_seek.setMax(streamMaxVolume);
        volume_seek.setProgress(streamVolume);

    }
}
