package own.forecastio;


import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;


public class MainActivity  extends CanvasWatchFaceService {

    private static final String TAG = "forecastio watch";

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {


        Paint BackPaint;
        Paint TextPaint;

        @Override
        public void onCreate(SurfaceHolder holder) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onCreate");
            }
            super.onCreate(holder);

            Resources resources = MainActivity.this.getResources();
            BackPaint = new Paint();
            BackPaint.setColor(Color.BLACK);
            TextPaint = new Paint();
            TextPaint.setColor(Color.CYAN);
            TextPaint.setTypeface(Typeface.createFromAsset(resources.getAssets(), "PixelLCD-7.ttf"));
            TextPaint.setTextSize(13);

        }


        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "onDraw");
            }

            int width = bounds.width();
            int height = bounds.height();
            canvas.drawRect(0, 0, width, height, BackPaint);
            //find center//
            float centerX = width / 2f;
            float centerY = height / 2f;
            String text = String.format("103\nunder contruction");
            canvas.drawText(text, centerX, centerY, TextPaint);
            invalidate();

        }

    }
}