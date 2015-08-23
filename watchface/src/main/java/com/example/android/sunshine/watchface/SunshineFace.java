/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.sunshine.watchface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class SunshineFace extends CanvasWatchFaceService {
    private static final Typeface TYPEFACE =
            Typeface.create("sans-serif", Typeface.NORMAL);
    private static final Typeface MEDIUM_TYPEFACE =
            Typeface.create("sans-serif-medium", Typeface.NORMAL);
    private static final Typeface LIGHT_TYPEFACE =
            Typeface.create("sans-serif-light", Typeface.NORMAL);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        final Handler mUpdateTimeHandler = new EngineHandler(this);

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };

        boolean mRegisteredTimeZoneReceiver = false;

        Paint mBackgroundPaint;
        Paint mHourTextPaint;
        Paint mMinuteTextPaint;
        Paint mDateTextPaint;
        Paint mMaxTempTextPaint;
        Paint mMinTempTextPaint;
        Paint mIcon;
        Paint mTriadBg;
        Path mTriadPath;

        boolean mAmbient;

        Time mTime;

        float mXOffset;
        float mYOffset;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(SunshineFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());
            Resources resources = SunshineFace.this.getResources();
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(resources.getColor(R.color.digital_background));

            mHourTextPaint = new Paint();
            mHourTextPaint = createTextPaint(resources.getColor(R.color.digital_text), MEDIUM_TYPEFACE);

            mMinuteTextPaint = new Paint();
            mMinuteTextPaint = createTextPaint(resources.getColor(R.color.digital_text),LIGHT_TYPEFACE);

            mDateTextPaint = new Paint();
            mDateTextPaint = createTextPaint(resources.getColor(R.color.primary_light),MEDIUM_TYPEFACE);

            mMaxTempTextPaint = new Paint();
            mMaxTempTextPaint = createTextPaint(resources.getColor(R.color.digital_text),MEDIUM_TYPEFACE);

            mMinTempTextPaint = new Paint();
            mMinTempTextPaint = createTextPaint(resources.getColor(R.color.digital_text),LIGHT_TYPEFACE);

            mIcon = new Paint();
            mIcon.setAntiAlias(true);

            mTriadBg = new Paint();
            mTriadBg.setColor(getResources().getColor(R.color.primary));
            mTriadBg.setStyle(Paint.Style.FILL);
            mTriadBg.setAntiAlias(true);
            Point a = new Point(0, 0);
            Point c = new Point(400, 0);
            Point b = new Point(400, 400);

            mTriadPath = new Path();
            mTriadPath.setFillType(Path.FillType.EVEN_ODD);
            mTriadPath.lineTo(b.x, b.y);
            mTriadPath.lineTo(c.x, c.y);
            mTriadPath.lineTo(a.x, a.y);
            mTriadPath.close();
            mTriadPath.offset(-75, -50);

            mTime = new Time();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        private Paint createTextPaint(int textColor, Typeface typeface) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(typeface);
            paint.setAntiAlias(true);
            paint.setTextAlign(Paint.Align.RIGHT);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            SunshineFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            SunshineFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = SunshineFace.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float timeTextSize,dateTextSize,tempTextSize;
            if(isRound) {
                timeTextSize = resources.getDimension(R.dimen.digital_text_size_round);
                dateTextSize = resources.getDimension(R.dimen.date_text_size_round);
                tempTextSize = resources.getDimension(R.dimen.temp_text_size_round);
            }
            else{
                timeTextSize = resources.getDimension(R.dimen.digital_text_size);
                dateTextSize = resources.getDimension(R.dimen.date_text_size);
                tempTextSize = resources.getDimension(R.dimen.temp_text_size);
            }
            mHourTextPaint.setTextSize(timeTextSize);
            mMinuteTextPaint.setTextSize(timeTextSize);
            mDateTextPaint.setTextSize(dateTextSize);
            mMaxTempTextPaint.setTextSize(tempTextSize);
            mMinTempTextPaint.setTextSize(tempTextSize);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mHourTextPaint.setAntiAlias(!inAmbientMode);
                    mMinuteTextPaint.setAntiAlias(!inAmbientMode);
                    mDateTextPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Draw the background.
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
            canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.art_storm),
                    -22,42, mIcon);
            canvas.drawPath(mTriadPath, mTriadBg);

            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            mTime.setToNow();
//            String text = mAmbient
//                    ? String.format("%d:%02d", mTime.hour, mTime.minute)
//                    : String.format("%d:%02d:%02d", mTime.hour, mTime.minute, mTime.second);

            String hour = String.format("%02d:", mTime.hour);
            String minute = String.format("%02d", mTime.minute);
            String date = new SimpleDateFormat("EEE, MMM dd yyyy").format(new Date()).toUpperCase();
            canvas.drawText(hour, mXOffset-76, mYOffset, mHourTextPaint);
            canvas.drawText(minute, mXOffset, mYOffset, mMinuteTextPaint);
            canvas.drawText(date, mXOffset-4, mYOffset+24, mDateTextPaint);
            canvas.drawText("25\u00B0", mXOffset-40, mYOffset+ 56, mMaxTempTextPaint);
            canvas.drawText("16\u00B0", mXOffset-4, mYOffset+56, mMinTempTextPaint);
        }
        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<SunshineFace.Engine> mWeakReference;

        public EngineHandler(SunshineFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            SunshineFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }
}
