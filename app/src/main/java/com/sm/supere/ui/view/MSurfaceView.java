package com.sm.supere.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;

public class MSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas;
    private Paint paint;
    private MediaPlayer mediaPlayer;

    public MSurfaceView(Context context) {
        this(context, null, 0);
    }

    public MSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        System.out.println("=========surfaceCreated========");
        new Thread(new Runnable() {
            @Override
            public void run() {
//                draw();
                play();
            }
        }).start();
    }

    private void draw() {
        try {
            System.out.println("============draw========");
            mCanvas = mSurfaceHolder.lockCanvas();
            mCanvas.drawCircle(500, 500, 300, paint);
//            mCanvas.drawCircle(100,100,20,paint);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null)
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        System.out.println("=========surfaceChanged========");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        System.out.println("=========surfaceDestroyed========");
        if (mediaPlayer != null) {
            stop();
        }
    }

    protected void stop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    protected void play() {
        String path = "/storage/emulated/0/DCIM/Camera/VID_20210220_190003.mp4";
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            // 设置播放的视频源
//            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.setDataSource("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
            // 设置显示视频的SurfaceHolder
            mediaPlayer.setDisplay(getHolder());

            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    replay();
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    play();
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void replay() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        } else {
            play();
        }
    }

    protected void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }
}
