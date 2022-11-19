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
 * limitations under the License
 */

package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.INotificationManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settingslib.Utils;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin.MenuItem;
import com.android.systemui.statusbar.stack.StackStateAnimator;

import java.util.Set;

import static android.service.notification.NotificationListenerService.Ranking.importanceToLevel;
import static android.service.notification.NotificationListenerService.Ranking.levelToImportance;

/**
 * The guts of a notification revealed when performing a long press.
 */
public class NotificationGuts extends FrameLayout {
    private static final String TAG = "NotificationGuts";
    private static final long CLOSE_GUTS_DELAY = 8000;

<<<<<<< HEAD
    protected Drawable mBackground;
    protected int mClipTopAmount;
    protected int mActualHeight;
=======
    private Drawable mBackground;
    private int mClipTopAmount;
    private int mClipBottomAmount;
    private int mActualHeight;
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040
    private boolean mExposed;

    private Handler mHandler;
    private Runnable mFalsingCheck;
    private boolean mNeedsFalsingProtection;
    private OnGutsClosedListener mClosedListener;
    private OnHeightChangedListener mHeightListener;

    private GutsContent mGutsContent;

    public interface GutsContent {

        public void setGutsParent(NotificationGuts listener);

        /**
         * @return the view to be shown in the notification guts.
         */
        public View getContentView();

        /**
         * @return the actual height of the content.
         */
        public int getActualHeight();

        /**
         * Called when the guts view have been told to close, typically after an outside
         * interaction.
         *
         * @param save whether the state should be saved.
         * @param force whether the guts view should be forced closed regardless of state.
         * @return if closing the view has been handled.
         */
        public boolean handleCloseControls(boolean save, boolean force);

        /**
         * @return whether the notification associated with these guts is set to be removed.
         */
        public boolean willBeRemoved();

        /**
         * @return whether these guts are a leavebehind (e.g. {@link NotificationSnooze}).
         */
        public default boolean isLeavebehind() {
            return false;
        }
    }

    public interface OnGutsClosedListener {
        public void onGutsClosed(NotificationGuts guts);
    }

    public interface OnHeightChangedListener {
        public void onHeightChanged(NotificationGuts guts);
    }

    interface OnSettingsClickListener {
        void onClick(View v, int appUid);
    }

    public NotificationGuts(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        mHandler = new Handler();
        mFalsingCheck = new Runnable() {
            @Override
            public void run() {
                if (mNeedsFalsingProtection && mExposed) {
                    closeControls(-1 /* x */, -1 /* y */, false /* save */, false /* force */);
                }
            }
        };
        final TypedArray ta = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.Theme, 0, 0);
        ta.recycle();
    }

    public NotificationGuts(Context context) {
        this(context, null);
    }

    public void setGutsContent(GutsContent content) {
        mGutsContent = content;
        removeAllViews();
        addView(mGutsContent.getContentView());
    }

    public GutsContent getGutsContent() {
        return mGutsContent;
    }

    public void resetFalsingCheck() {
        mHandler.removeCallbacks(mFalsingCheck);
        if (mNeedsFalsingProtection && mExposed) {
            mHandler.postDelayed(mFalsingCheck, CLOSE_GUTS_DELAY);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        draw(canvas, mBackground);
    }

    private void draw(Canvas canvas, Drawable drawable) {
        int top = mClipTopAmount;
        int bottom = mActualHeight - mClipBottomAmount;
        if (drawable != null && top < bottom) {
            drawable.setBounds(0, top, getWidth(), bottom);
            drawable.draw(canvas);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mBackground = mContext.getDrawable(R.drawable.notification_guts_bg);
        if (mBackground != null) {
            mBackground.setCallback(this);
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == mBackground;
    }

    @Override
    protected void drawableStateChanged() {
        drawableStateChanged(mBackground);
    }

    private void drawableStateChanged(Drawable d) {
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
    }

    @Override
    public void drawableHotspotChanged(float x, float y) {
        if (mBackground != null) {
            mBackground.setHotspot(x, y);
        }
    }

    public void closeControls(boolean leavebehinds, boolean controls, int x, int y, boolean force) {
        if (mGutsContent != null) {
            if (mGutsContent.isLeavebehind() && leavebehinds) {
                closeControls(x, y, true /* save */, force);
            } else if (!mGutsContent.isLeavebehind() && controls) {
                closeControls(x, y, true /* save */, force);
            }
        }
    }

<<<<<<< HEAD
    public boolean hasImportanceChanged() {
        return mStartingUserImportance != getSelectedImportance();
    }

    void saveImportance(final StatusBarNotification sbn) {
        int importance = getSelectedImportance();
        MetricsLogger.action(mContext, MetricsEvent.ACTION_SAVE_IMPORTANCE,
                importance - mStartingUserImportance);
        try {
            mINotificationManager.setImportance(sbn.getPackageName(), sbn.getUid(), importance);
        } catch (RemoteException e) {
            // :(
        }
    }

    private int getSelectedImportance() {
        if (mSeekBar != null && mSeekBar.isShown()) {
            if (mSeekBar.isEnabled()) {
                return levelToImportance(mSeekBar.getProgress());
            } else {
                return Ranking.IMPORTANCE_UNSPECIFIED;
            }
        } else {
            if (mBlock != null && mBlock.isChecked()) {
                return Ranking.IMPORTANCE_NONE;
            } else if (mSilent != null && mSilent.isChecked()) {
                return Ranking.IMPORTANCE_LOW;
            } else {
                return Ranking.IMPORTANCE_UNSPECIFIED;
            }
        }
    }

    private void bindToggles(final View importanceButtons, final int importance,
            final boolean nonBlockable) {
        ((RadioGroup) importanceButtons).setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        resetFalsingCheck();
                    }
                });
        mBlock = (RadioButton) importanceButtons.findViewById(R.id.block_importance);
        mSilent = (RadioButton) importanceButtons.findViewById(R.id.silent_importance);
        mReset = (RadioButton) importanceButtons.findViewById(R.id.reset_importance);
        if (nonBlockable) {
            mBlock.setVisibility(View.GONE);
            mReset.setText(mContext.getString(R.string.do_not_silence));
        } else {
            mReset.setText(mContext.getString(R.string.do_not_silence_block));
        }
        mBlock.setText(mContext.getString(R.string.block));
        mSilent.setText(mContext.getString(R.string.show_silently));
        if (importance == NotificationListenerService.Ranking.IMPORTANCE_LOW) {
            mSilent.setChecked(true);
        } else {
            mReset.setChecked(true);
        }
    }

    private void bindSlider(final View importanceSlider, final boolean nonBlockable) {
        mActiveSliderTint = ColorStateList.valueOf(Utils.getColorAccent(mContext));
        mInactiveSliderTint = loadColorStateList(R.color.notification_guts_disabled_slider_color);

        mImportanceSummary = ((TextView) importanceSlider.findViewById(R.id.summary));
        mImportanceTitle = ((TextView) importanceSlider.findViewById(R.id.title));
        mSeekBar = (SeekBar) importanceSlider.findViewById(R.id.seekbar);

        final int minProgress = nonBlockable ?
                NotificationListenerService.Ranking.IMPORTANCE_MIN
                : NotificationListenerService.Ranking.IMPORTANCE_NONE;
        mSeekBar.setMax(importanceToLevel(Ranking.IMPORTANCE_MAX));
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                resetFalsingCheck();
                if (progress < minProgress) {
                    seekBar.setProgress(minProgress);
                    progress = minProgress;
                }
                updateTitleAndSummary(levelToImportance(progress));
                if (fromUser) {
                    MetricsLogger.action(mContext, MetricsEvent.ACTION_MODIFY_IMPORTANCE_SLIDER);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                resetFalsingCheck();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // no-op
            }


        });
        mSeekBar.setProgress(importanceToLevel(mNotificationImportance));

        mAutoButton = (ImageView) importanceSlider.findViewById(R.id.auto_importance);
        mAutoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuto = !mAuto;
                applyAuto();
            }
        });
        mAuto = mStartingUserImportance == Ranking.IMPORTANCE_UNSPECIFIED;
        applyAuto();
    }


    private void applyAuto() {
        mSeekBar.setEnabled(!mAuto);

        final ColorStateList starTint = mAuto ?  mActiveSliderTint : mInactiveSliderTint;
        final float alpha = mAuto ? mInactiveSliderAlpha : mActiveSliderAlpha;
        Drawable icon = mAutoButton.getDrawable().mutate();
        icon.setTintList(starTint);
        mAutoButton.setImageDrawable(icon);
        mSeekBar.setAlpha(alpha);

        if (mAuto) {
            mSeekBar.setProgress(importanceToLevel(mNotificationImportance));
            mImportanceSummary.setText(mContext.getString(
                    R.string.notification_importance_user_unspecified));
            mImportanceTitle.setText(mContext.getString(
                    R.string.user_unspecified_importance));
        } else {
            updateTitleAndSummary(levelToImportance(mSeekBar.getProgress()));
        }
    }

    private void updateTitleAndSummary(int importance) {
        mImportanceTitle.setText(String.format(mContext.getString(
           R.string.importance_level_title), importanceToLevel(importance)));
        switch (importance) {
            case Ranking.IMPORTANCE_NONE:
                mImportanceSummary.setText(mContext.getString(
                        R.string.notification_importance_blocked));
                break;
            case Ranking.IMPORTANCE_MIN:
                mImportanceSummary.setText(mContext.getString(
                        R.string.notification_importance_min));
                break;
            case Ranking.IMPORTANCE_VERY_LOW:
                mImportanceSummary.setText(mContext.getString(
                        R.string.notification_importance_very_low));
                break;
            case Ranking.IMPORTANCE_LOW:
                mImportanceSummary.setText(mContext.getString(
                        R.string.notification_importance_low));
                break;
            case Ranking.IMPORTANCE_DEFAULT:
                mImportanceSummary.setText(mContext.getString(
                        R.string.notification_importance_default));
                break;
            case Ranking.IMPORTANCE_HIGH:
                mImportanceSummary.setText(mContext.getString(
                        R.string.notification_importance_high));
                break;
            case Ranking.IMPORTANCE_MAX:
                mImportanceSummary.setText(mContext.getString(
                        R.string.notification_importance_max));
                            break;
        }
    }

    private ColorStateList loadColorStateList(int colorResId) {
        return ColorStateList.valueOf(mContext.getColor(colorResId));
    }

    public void closeControls(int x, int y, boolean notify) {
=======
    public void closeControls(int x, int y, boolean save, boolean force) {
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040
        if (getWindowToken() == null) {
            if (mClosedListener != null) {
                mClosedListener.onGutsClosed(this);
            }
            return;
        }

        if (mGutsContent == null || !mGutsContent.handleCloseControls(save, force)) {
            animateClose(x, y);
            setExposed(false, mNeedsFalsingProtection);
            if (mClosedListener != null) {
                mClosedListener.onGutsClosed(this);
            }
        }
    }

    private void animateClose(int x, int y) {
        if (x == -1 || y == -1) {
            x = (getLeft() + getRight()) / 2;
            y = (getTop() + getHeight() / 2);
        }
        final double horz = Math.max(getWidth() - x, x);
        final double vert = Math.max(getHeight() - y, y);
        final float r = (float) Math.hypot(horz, vert);
        final Animator a = ViewAnimationUtils.createCircularReveal(this,
                x, y, r, 0);
        a.setDuration(StackStateAnimator.ANIMATION_DURATION_STANDARD);
        a.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setVisibility(View.GONE);
            }
        });
        a.start();
    }

    public void setActualHeight(int actualHeight) {
        mActualHeight = actualHeight;
        invalidate();
    }

    public int getActualHeight() {
        return mActualHeight;
    }

    public int getIntrinsicHeight() {
        return mGutsContent != null && mExposed ? mGutsContent.getActualHeight() : getHeight();
    }

    public void setClipTopAmount(int clipTopAmount) {
        mClipTopAmount = clipTopAmount;
        invalidate();
    }

    public void setClipBottomAmount(int clipBottomAmount) {
        mClipBottomAmount = clipBottomAmount;
        invalidate();
    }

    @Override
    public boolean hasOverlappingRendering() {
        // Prevents this view from creating a layer when alpha is animating.
        return false;
    }

    public void setClosedListener(OnGutsClosedListener listener) {
        mClosedListener = listener;
    }

    public void setHeightChangedListener(OnHeightChangedListener listener) {
        mHeightListener = listener;
    }

    protected void onHeightChanged() {
        if (mHeightListener != null) {
            mHeightListener.onHeightChanged(this);
        }
    }

    public void setExposed(boolean exposed, boolean needsFalsingProtection) {
        final boolean wasExposed = mExposed;
        mExposed = exposed;
        mNeedsFalsingProtection = needsFalsingProtection;
        if (mExposed && mNeedsFalsingProtection) {
            resetFalsingCheck();
        } else {
            mHandler.removeCallbacks(mFalsingCheck);
        }
        if (wasExposed != mExposed && mGutsContent != null) {
            final View contentView = mGutsContent.getContentView();
            contentView.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
            if (mExposed) {
                contentView.requestAccessibilityFocus();
            }
        }
    }

    public boolean willBeRemoved() {
        return mGutsContent != null ? mGutsContent.willBeRemoved() : false;
    }

    public boolean isExposed() {
        return mExposed;
    }
}
