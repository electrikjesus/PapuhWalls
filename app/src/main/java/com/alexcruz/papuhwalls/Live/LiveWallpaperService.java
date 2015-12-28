package com.alexcruz.papuhwalls.Live;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.os.Environment;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.alexcruz.papuhwalls.MainActivity;
import com.alexcruz.papuhwalls.Preferences;
import com.alexcruz.papuhwalls.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Daniel Huber on 09.12.2015.
 */
public class LiveWallpaperService extends WallpaperService {

    private LiveWallEngine engine;
    private Preferences preferences;
    public static final String settingsChangedAction = "settingsChanged";
    public static final String updateWallAction = "updateWall";

    private AlarmManager alarmManager;
    private Intent intentAlarm;
    private PendingIntent pendingIntent;

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(settingsChangedReceiver);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = new Preferences(this);

        IntentFilter iF = new IntentFilter(settingsChangedAction);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        intentAlarm = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, Preferences.pendingIntentUnique, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);

        setupAlarmManager(false);
        registerReceiver(settingsChangedReceiver, iF);

        if(preferences.getLiveWalls().size() == 0 & MainActivity.isConnected(this))
            new MainActivity.SetupLW(this).execute();
    }

    @Override
    public Engine onCreateEngine() {
        if (engine!=null) {
            engine = null;
        }
        engine = new LiveWallEngine();

        return engine;
    }

    private void disableAlarm(){
        pendingIntent.cancel();
        alarmManager.cancel(pendingIntent);
    }

    private void setupAlarmManager(boolean restart){

        if(restart)
            disableAlarm();

        int updateInterval = preferences.LWinterval() * 1000;

        pendingIntent = PendingIntent.getBroadcast(this, Preferences.pendingIntentUnique, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                updateInterval, pendingIntent);

    }

    private BroadcastReceiver settingsChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setupAlarmManager(true);
        }
    };

    public class LiveWallEngine extends Engine {

        private int height = 0, width = 0;
        private int currentPos = 0;
        private int xLength = 0;
        int wallWidth = 0;

        private ArrayList<String> liveWallPool = new ArrayList<>();
        Bitmap currentWall;

        private BroadcastReceiver updateWallReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                initLiveWallPool();

                if(currentPos == liveWallPool.size())
                    currentPos = 0;
                setWallpaper();
                currentPos++;
            }
        };

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
            if(currentWall != null) {
                scrollWall((int) ((width - wallWidth) * xOffset));
            }
        }

        private void initLiveWallPool(){
            File saveWallLoc = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + getResources().getString(R.string.walls_save_location));

            File file[] = saveWallLoc.listFiles();
            for (File wall : file) {
                if(wall.getName().startsWith("PapuhLive"))
                    liveWallPool.add(wall.getAbsolutePath());
            }
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            initLiveWallPool();

            IntentFilter intentFilter = new IntentFilter(updateWallAction);
            registerReceiver(updateWallReceiver, intentFilter);

            if(currentPos == liveWallPool.size())
                currentPos = 0;

            setWallpaper();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);

        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format,
                                     int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            this.height = height;
            this.width = width;
            setWallpaper();
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
                super.onTouchEvent(event);
        }

        private void scrollWall(int offset){
            SurfaceHolder holder = getSurfaceHolder();

            Canvas canvas = holder.lockCanvas();
            if(canvas != null) {
                canvas.drawBitmap(currentWall, offset, 0, null);
                holder.unlockCanvasAndPost(canvas);
            }
        }

        private void setWallpaper() {

            if (liveWallPool.size() > 0) {

                SurfaceHolder holder = getSurfaceHolder();

                Canvas canvas = holder.lockCanvas();
                if (canvas != null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferQualityOverSpeed = true;

                    if (currentPos == liveWallPool.size())
                        currentPos = 0;

                    String currentWallPath = liveWallPool.get(currentPos);

                    currentWall = BitmapFactory.decodeFile(currentWallPath, options);

                    int wallHeight = currentWall.getHeight();
                    wallWidth = currentWall.getWidth();

                    int maxHeight = canvas.getMaximumBitmapHeight();

                    if (wallHeight > maxHeight) {

                        float resizeRatio = ((float) maxHeight) / wallHeight;
                        currentWall = Bitmap.createScaledBitmap(currentWall, (int) (wallWidth * resizeRatio), maxHeight, true);
                    } else if (wallHeight < height) {
                        float resizeRatio = ((float) height) / wallHeight;
                        currentWall = Bitmap.createScaledBitmap(currentWall, (int) (wallWidth * resizeRatio), height, true);
                    }

                    Paint paint = new Paint();
                    paint.setColor(Color.TRANSPARENT);

                    canvas.drawPaint(paint);
                    canvas.drawBitmap(currentWall, xLength, 0, null);
                    holder.unlockCanvasAndPost(canvas);
                }
            } else if(isPreview()){
                // let's try again in one second
                new CountDownTimer(1000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        initLiveWallPool();
                        setWallpaper();
                    }
                }.start();
            }
        }
    }
}