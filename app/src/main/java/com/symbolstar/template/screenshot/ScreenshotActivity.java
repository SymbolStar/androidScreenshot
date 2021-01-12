package com.symbolstar.template.screenshot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import com.symbolstar.template.R;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ScreenshotActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 100;
    private String storeDirectory = null;
    private int imageProduced = 0;
    private String SCREENCAP_NAME = "Capture";
    private int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY |
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private MediaProjectionManager mMediaProjectionManager;
    private ImageReader mImageReader;
    private Display mDisplay;
    private VirtualDisplay mVirtualDisplay;
    private Handler mHandler;
    private MediaProjection mMediaProjection = null;
    private int mDensity = 0;
    private int mWidth = 0;
    private int mHeight = 0;
    private int mRotation = 0;
    private OrientationChangeCallback mOrientationChangeCallback = null;
    private MediaProjectionStopCallback mMediaProjectionStopCallback;

    DemoSurfaceView mDemoSurfaceView;
    RelativeLayout mPreviewContainer;
    ImageView mPreviewImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_screenshot);
        mDemoSurfaceView = findViewById(R.id.demo_surface);
        mPreviewContainer = findViewById(R.id.rl_preview_container);
        mPreviewImage = findViewById(R.id.im_preview);

        // call for the projection manager
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }).start();
    }


    private void startProjection() {
        startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    private void stopProjection() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mMediaProjection != null) {
                    mMediaProjection.stop();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            if (mMediaProjection != null) {
                mDensity = getResources().getDisplayMetrics().densityDpi;
                mDisplay = getWindowManager().getDefaultDisplay();
                createVirtualDisplay();
                //TODO
                mOrientationChangeCallback = new OrientationChangeCallback(this);
                if (mOrientationChangeCallback.canDetectOrientation()) {
                    mOrientationChangeCallback.enable();
                }
                mMediaProjectionStopCallback = new MediaProjectionStopCallback();
                mMediaProjection.registerCallback(mMediaProjectionStopCallback, mHandler);
            }
        }
    }

    private void createVirtualDisplay() {
        Point size = new Point();
        mDisplay.getSize(size);
        mWidth = size.x;
        mHeight = size.y;
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 10);
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(SCREENCAP_NAME, mWidth, mHeight, mDensity,
                VIRTUAL_DISPLAY_FLAGS, mImageReader.getSurface(), null, mHandler);
        mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), mHandler);
    }


    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null;

            try {
                image = reader.acquireLatestImage();
                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();

                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * mWidth;
                    //rowPadding / pixelStride
                    Bitmap bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

                    Log.e("sss---", "imageloader=" + System.currentTimeMillis());
                    if (bitmap != null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (mPreviewContainer.getVisibility() == View.VISIBLE) {
                                    return;
                                }
                                Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, mWidth, mHeight);
                                mPreviewImage.setImageBitmap(resizedBitmap);
                                mPreviewContainer.setVisibility(View.VISIBLE);
                                stopProjection();
                            }
                        });
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (image != null) {
                    image.close();
                }
            }
        }
    }


    private class OrientationChangeCallback extends OrientationEventListener {

        public OrientationChangeCallback(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            int rotation = mDisplay.getRotation();
            if (rotation != mRotation) {
                mRotation = rotation;
                try {
                    if (mVirtualDisplay != null) {
                        mVirtualDisplay.release();
                    }
                    if (mImageReader != null) {
                        mImageReader.setOnImageAvailableListener(null, null);
                    }
                    createVirtualDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            Log.e("ScreenCapture", "stopping projection");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mVirtualDisplay != null) {
                        mVirtualDisplay.release();
                    }
                    if (mImageReader != null) {
                        mImageReader.setOnImageAvailableListener(null, null);
                    }
                    if (mOrientationChangeCallback != null) {
                        mOrientationChangeCallback.disable();
                    }

                    mMediaProjection.unregisterCallback(mMediaProjectionStopCallback);
                }
            });
        }
    }

    public void screenshot(View view) {
        showScreenshotAction(view);
    }

    @Override
    public void onBackPressed() {
        if (mPreviewContainer.getVisibility() == View.VISIBLE) {
            mPreviewContainer.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }


    @Nullable
    private Bitmap getBitmapFromView(@NonNull View view) {
        if (view == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private void getBitmapFromSurfaceView(SurfaceView view, BitmapCallback callback) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);

        PixelCopy.request(view, bitmap, new PixelCopy.OnPixelCopyFinishedListener() {
            @Override
            public void onPixelCopyFinished(int copyResult) {
                if (copyResult == PixelCopy.SUCCESS) {
                    callback.onSuccess(bitmap);
                } else {
                    callback.onSuccess(null);
                }
            }
        }, new Handler(Looper.getMainLooper()));

    }

    private List<SurfaceView> getAllSurfaceViews(View view) {
        List<SurfaceView> surfaceViewList = new ArrayList<>();
        if (view != null) {
            if (view instanceof SurfaceView && view.getVisibility() == View.VISIBLE) {
                surfaceViewList.add((SurfaceView) view);
            } else if (view instanceof ViewGroup && view.getVisibility() == View.VISIBLE) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    surfaceViewList.addAll(getAllSurfaceViews(((ViewGroup) view).getChildAt(i)));
                }
            }
        }
        return surfaceViewList;
    }

    interface BitmapCallback {
        void onSuccess(Bitmap bitmap);
    }

    /**
     * Prior to Android 28
     *
     * @param view
     * @return
     */
    @Nullable
    private Bitmap takeScreenshot(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private void showScreenshotAction(View v) {
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.inflate(R.menu.menu_screenshot_action);
        popupMenu.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.bar_normal) {
                captureNormalView();
                return true;
            }
            if (item.getItemId() == R.id.bar_virtual) {
                startProjection();
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

    private void captureNormalView() {
        View rootView = this.getWindow().getDecorView().getRootView();
        if (rootView != null) {
            Bitmap screenshot = takeScreenshot(rootView);
            try {
                List<SurfaceView> mSurfaceViewList = getAllSurfaceViews(rootView);
                if (screenshot != null && !mSurfaceViewList.isEmpty()) {
                    Canvas canvas = new Canvas(screenshot);
                    for (SurfaceView surfaceView : mSurfaceViewList) {
                        getBitmapFromSurfaceView(surfaceView, bitmap -> {
                            Paint paint = new Paint();
                            Xfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);
                            paint.setXfermode(xfermode);
                            int[] locationArray = new int[2];
                            surfaceView.getLocationInWindow(locationArray);
                            canvas.drawBitmap(bitmap, locationArray[0], locationArray[1],
                                    paint);

                            if (screenshot == null) {
                                return;
                            }
                            mPreviewImage.setImageBitmap(screenshot);
                            mPreviewContainer.setVisibility(View.VISIBLE);
                        });
                    }
                }
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }


        }
    }

}