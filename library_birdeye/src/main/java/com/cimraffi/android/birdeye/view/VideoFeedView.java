package com.cimraffi.android.birdeye.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.RelativeLayout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import dji.midware.usb.P3.UsbAccessoryService;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.thirdparty.rx.Observable;
import dji.thirdparty.rx.android.schedulers.AndroidSchedulers;
import dji.thirdparty.rx.functions.Action1;

/**
 * VideoView will show the live video for the given video feed.
 */
public class VideoFeedView extends TextureView implements SurfaceTextureListener {
    private Context mContext;
    //region Properties
    private final static String TAG = "DULFpvWidget";
    private DJICodecManager codecManager = null;
    private VideoFeeder.VideoDataListener videoDataListener = null;
    private int videoWidth;
    private int videoHeight;
    private boolean isPrimaryVideoFeed;
    private RelativeLayout coverLayout;
    private final long WAIT_TIME = 500; // Half of a second
    private AtomicLong lastReceivedFrameTime = new AtomicLong(0);
    private Observable timer =
            Observable.timer(100, TimeUnit.MICROSECONDS).observeOn(AndroidSchedulers.mainThread()).repeat();

    //endregion

    //region Life-Cycle
    public VideoFeedView(Context context) {
        this(context, null, 0);
    }

    public VideoFeedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoFeedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    public void setCoverLayout(RelativeLayout view) {
        coverLayout = view;
    }

    private void init() {
        // Avoid the rending exception in the Android Studio Preview view.
        if (isInEditMode()) {
            return;
        }

        setSurfaceTextureListener(this);
        videoDataListener = new VideoFeeder.VideoDataListener() {

            @Override
            public void onReceive(byte[] videoBuffer, int size) {

                lastReceivedFrameTime.set(System.currentTimeMillis());

                if (codecManager != null) {
                    codecManager.sendDataToDecoder(videoBuffer,
                            size,
                            isPrimaryVideoFeed
                                    ? UsbAccessoryService.VideoStreamSource.Camera.getIndex()
                                    : UsbAccessoryService.VideoStreamSource.Fpv.getIndex());
                }
            }
        };

        timer.subscribe(new Action1() {
            @Override
            public void call(Object o) {
                final long now = System.currentTimeMillis();
                final long ellapsedTime = now - lastReceivedFrameTime.get();
                if (coverLayout != null) {
                    if (ellapsedTime > WAIT_TIME) {
                    } else {
                    }
                }
            }
        });
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        // SurfaceTexture????????????
        if (codecManager == null) {
            codecManager = new DJICodecManager(this.getContext(),
                    surface,
                    width,
                    height,
                    isPrimaryVideoFeed
                            ? UsbAccessoryService.VideoStreamSource.Camera
                            : UsbAccessoryService.VideoStreamSource.Fpv);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // SurfaceTexture??????????????????
//        if (videoHeight != codecManager.getVideoHeight() || videoWidth != codecManager.getVideoWidth()) {
//            videoWidth = codecManager.getVideoWidth();
//            videoHeight = codecManager.getVideoHeight();
//            adjustAspectRatio(videoWidth, videoHeight);
//        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        // SurfaceTexture???????????????
        if (codecManager != null) {
            codecManager.cleanSurface();
            codecManager.destroyCodec();
            codecManager = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // SurfaceTexture??????updateImage??????
        if (videoHeight != codecManager.getVideoHeight() || videoWidth != codecManager.getVideoWidth()) {
            videoWidth = codecManager.getVideoWidth();
            videoHeight = codecManager.getVideoHeight();
            adjustAspectRatio(videoWidth, videoHeight);
        }
    }
    //endregion

    //region Logic
    public VideoFeeder.VideoDataListener registerLiveVideo(VideoFeeder.VideoFeed videoFeed, boolean isPrimary) {
        isPrimaryVideoFeed = isPrimary;

        if (videoDataListener != null && videoFeed != null) {
            videoFeed.addVideoDataListener(videoDataListener);
            return videoDataListener;
        }
        return null;
    }

    public void changeSourceResetKeyFrame() {
        if (codecManager != null) {
            codecManager.resetKeyFrame();
        }
    }
    //endregion

    //region Helper method

    /**
     * This method should not to be called until the size of `TextureView` is fixed.
     */
    private void adjustAspectRatio(int videoWidth, int videoHeight) {

        int viewWidth = this.getWidth();
        int viewHeight = this.getHeight();
        double aspectRatio = (double) videoHeight / videoWidth;

        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;

        Matrix txform = new Matrix();
        this.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        txform.postTranslate(xoff, yoff);
        this.setTransform(txform);
    }
    //endregion
}
