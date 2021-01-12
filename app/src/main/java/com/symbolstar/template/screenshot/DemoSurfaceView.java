package com.symbolstar.template.screenshot;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DemoSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    LoopThread thread;

    public DemoSurfaceView(Context context) {
        super(context);
        init(); //初始化,设置生命周期回调方法
    }
    public DemoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init() {
        SurfaceHolder holder = getHolder();
        holder.addCallback(this); //设置Surface生命周期回调
        thread = new LoopThread(holder, getContext());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.isRunning = true;
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread.isRunning = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行绘制的绘制线程
     *
     * @author Administrator
     */
    class LoopThread extends Thread {
        SurfaceHolder surfaceHolder;
        Context context;
        boolean isRunning;
        float radius = 10f;
        Paint paint;

        public LoopThread(SurfaceHolder surfaceHolder, Context context) {
            this.surfaceHolder = surfaceHolder;
            this.context = context;
            isRunning = false;
            paint = new Paint();
            paint.setColor(Color.YELLOW);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10);
        }

        @Override
        public void run() {
            Canvas c = null;
            while (isRunning) {
                try {
                    synchronized (surfaceHolder) {
                        c = surfaceHolder.lockCanvas(null);
                        doDraw(c);
                        Thread.sleep(50);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    surfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }

        public void doDraw(Canvas c) {
            c.drawColor(Color.BLACK);
            c.translate(400, 800);
            c.drawCircle(0, 0, radius++, paint);
            if (radius > 100) {
                radius = 10f;
            }


        }
    }
}
