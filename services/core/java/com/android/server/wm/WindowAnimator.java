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

package com.android.server.wm;

import static android.view.Display.DEFAULT_DISPLAY;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_WINDOW_TRACE;
import static com.android.server.wm.WindowManagerDebugConfig.SHOW_TRANSACTIONS;
import static com.android.server.wm.WindowManagerDebugConfig.TAG_WITH_CLASS_NAME;
import static com.android.server.wm.WindowManagerDebugConfig.TAG_WM;
import static com.android.server.wm.WindowSurfacePlacer.SET_ORIENTATION_CHANGE_COMPLETE;
import static com.android.server.wm.WindowSurfacePlacer.SET_UPDATE_ROTATION;

import android.content.Context;
import android.os.Trace;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.view.Choreographer;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.view.WindowManagerPolicy;

import com.android.internal.view.SurfaceFlingerVsyncChoreographer;
import com.android.server.AnimationThread;

import java.io.PrintWriter;

/**
 * Singleton class that carries out the animations and Surface operations in a separate task
 * on behalf of WindowManagerService.
 */
public class WindowAnimator {
    private static final String TAG = TAG_WITH_CLASS_NAME ? "WindowAnimator" : TAG_WM;

    final WindowManagerService mService;
    final Context mContext;
    final WindowManagerPolicy mPolicy;
    private final WindowSurfacePlacer mWindowPlacerLocked;

    /** Is any window animating? */
    private boolean mAnimating;
    private boolean mLastAnimating;

    /** Is any app window animating? */
    boolean mAppWindowAnimating;

    final Choreographer.FrameCallback mAnimationFrameCallback;

    /** Time of current animation step. Reset on each iteration */
    long mCurrentTime;

    /** Skip repeated AppWindowTokens initialization. Note that AppWindowsToken's version of this
     * is a long initialized to Long.MIN_VALUE so that it doesn't match this value on startup. */
    int mAnimTransactionSequence;

    /** Window currently running an animation that has requested it be detached
     * from the wallpaper.  This means we need to ensure the wallpaper is
     * visible behind it in case it animates in a way that would allow it to be
     * seen. If multiple windows satisfy this, use the lowest window. */
    WindowState mWindowDetachedWallpaper = null;

    int mBulkUpdateParams = 0;
    Object mLastWindowFreezeSource;

    SparseArray<DisplayContentsAnimator> mDisplayContentsAnimators = new SparseArray<>(2);

    boolean mInitialized = false;

<<<<<<< HEAD
    boolean mKeyguardGoingAway;
    int mKeyguardGoingAwayFlags;

    /** Use one animation for all entering activities after keyguard is dismissed. */
    Animation mPostKeyguardExitAnimation;

    // forceHiding states.
    static final int KEYGUARD_NOT_SHOWN     = 0;
    static final int KEYGUARD_SHOWN         = 1;
    static final int KEYGUARD_ANIMATING_OUT = 2;
    int mForceHiding = KEYGUARD_NOT_SHOWN;
    int offsetLayer = 0;

=======
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040
    // When set to true the animator will go over all windows after an animation frame is posted and
    // check if some got replaced and can be removed.
    private boolean mRemoveReplacedWindows = false;

    private Choreographer mChoreographer;

    /**
     * Indicates whether we have an animation frame callback scheduled, which will happen at
     * vsync-app and then schedule the animation tick at the right time (vsync-sf).
     */
    private boolean mAnimationFrameCallbackScheduled;

    WindowAnimator(final WindowManagerService service) {
        mService = service;
        mContext = service.mContext;
        mPolicy = service.mPolicy;
        mWindowPlacerLocked = service.mWindowPlacerLocked;
        AnimationThread.getHandler().runWithScissors(
                () -> mChoreographer = Choreographer.getSfInstance(), 0 /* timeout */);

        mAnimationFrameCallback = frameTimeNs -> {
            synchronized (mService.mWindowMap) {
                mAnimationFrameCallbackScheduled = false;
            }
            animate(frameTimeNs);
        };
    }

    void addDisplayLocked(final int displayId) {
        // Create the DisplayContentsAnimator object by retrieving it if the associated
        // {@link DisplayContent} exists.
        getDisplayContentsAnimatorLocked(displayId);
        if (displayId == DEFAULT_DISPLAY) {
            mInitialized = true;
        }
    }

    void removeDisplayLocked(final int displayId) {
        final DisplayContentsAnimator displayAnimator = mDisplayContentsAnimators.get(displayId);
        if (displayAnimator != null) {
            if (displayAnimator.mScreenRotationAnimation != null) {
                displayAnimator.mScreenRotationAnimation.kill();
                displayAnimator.mScreenRotationAnimation = null;
            }
        }

        mDisplayContentsAnimators.delete(displayId);
    }

    /**
     * DO NOT HOLD THE WINDOW MANAGER LOCK WHILE CALLING THIS METHOD. Reason: the method closes
     * an animation transaction, that might be blocking until the next sf-vsync, so we want to make
     * sure other threads can make progress if this happens.
     */
<<<<<<< HEAD
    private WindowState getWinShowWhenLockedOrAnimating() {
        final WindowState winShowWhenLocked = (WindowState) mPolicy.getWinShowWhenLockedLw();
        if (winShowWhenLocked != null) {
            return winShowWhenLocked;
        }
        if (mLastShowWinWhenLocked != null && mLastShowWinWhenLocked.isOnScreen()
                && mLastShowWinWhenLocked.isAnimatingLw()
                && (mLastShowWinWhenLocked.mAttrs.flags & FLAG_SHOW_WHEN_LOCKED) != 0) {
            return mLastShowWinWhenLocked;
        }
        return null;
    }

    private boolean shouldForceHide(WindowState win) {
        final WindowState imeTarget = mService.mInputMethodTarget;
        final boolean showImeOverKeyguard = imeTarget != null && imeTarget.isVisibleNow() &&
                ((imeTarget.getAttrs().flags & FLAG_SHOW_WHEN_LOCKED) != 0
                        || !mPolicy.canBeForceHidden(imeTarget, imeTarget.mAttrs));

        final WindowState winShowWhenLocked = getWinShowWhenLockedOrAnimating();
        final AppWindowToken appShowWhenLocked = winShowWhenLocked == null ?
                null : winShowWhenLocked.mAppToken;

        boolean allowWhenLocked = false;
        // Show IME over the keyguard if the target allows it
        allowWhenLocked |= (win.mIsImWindow || imeTarget == win) && showImeOverKeyguard;
        // Show SHOW_WHEN_LOCKED windows that turn on the screen
        allowWhenLocked |= (win.mAttrs.flags & FLAG_SHOW_WHEN_LOCKED) != 0 && win.mTurnOnScreen;
        // Show windows that use TYPE_STATUS_BAR_SUB_PANEL when locked
        allowWhenLocked |= win.mAttrs.type == WindowManager.LayoutParams.TYPE_KEYGUARD_PANEL;

        if (appShowWhenLocked != null) {
            allowWhenLocked |= appShowWhenLocked == win.mAppToken
                    // Show all SHOW_WHEN_LOCKED windows if some apps are shown over lockscreen
                    || (win.mAttrs.flags & FLAG_SHOW_WHEN_LOCKED) != 0
                    // Show error dialogs over apps that are shown on lockscreen
                    || (win.mAttrs.privateFlags & PRIVATE_FLAG_SYSTEM_ERROR) != 0;
        }

        // Allow showing a window that dismisses Keyguard if the policy allows it. This is used for
        // when the policy knows that the Keyguard can be dismissed without user interaction to
        // provide a smooth transition in that case.
        allowWhenLocked |= (win.mAttrs.flags & FLAG_DISMISS_KEYGUARD) != 0
                && mPolicy.canShowDismissingWindowWhileLockedLw();

        // Only hide windows if the keyguard is active and not animating away.
        boolean keyguardOn = mPolicy.isKeyguardShowingOrOccluded()
                && mForceHiding != KEYGUARD_ANIMATING_OUT;
        boolean hideDockDivider = win.mAttrs.type == TYPE_DOCK_DIVIDER
                && win.getDisplayContent().getDockedStackLocked() == null;
        return keyguardOn && !allowWhenLocked && (win.getDisplayId() == Display.DEFAULT_DISPLAY)
                || hideDockDivider;
    }

    private void updateWindowsLocked(final int displayId) {
        ++mAnimTransactionSequence;

        final WindowList windows = mService.getWindowListLocked(displayId);

        final boolean keyguardGoingAwayToShade =
                (mKeyguardGoingAwayFlags & KEYGUARD_GOING_AWAY_FLAG_TO_SHADE) != 0;
        final boolean keyguardGoingAwayNoAnimation =
                (mKeyguardGoingAwayFlags & KEYGUARD_GOING_AWAY_FLAG_NO_WINDOW_ANIMATIONS) != 0;
        final boolean keyguardGoingAwayWithWallpaper =
                (mKeyguardGoingAwayFlags & KEYGUARD_GOING_AWAY_FLAG_WITH_WALLPAPER) != 0;

        if (mKeyguardGoingAway) {
            for (int i = windows.size() - 1; i >= 0; i--) {
                WindowState win = windows.get(i);
                if (!mPolicy.isKeyguardHostWindow(win.mAttrs)) {
                    continue;
                }
                final WindowStateAnimator winAnimator = win.mWinAnimator;
                if ((win.mAttrs.privateFlags & PRIVATE_FLAG_KEYGUARD) != 0) {
                    if (!winAnimator.mAnimating) {
                        if (DEBUG_KEYGUARD) Slog.d(TAG,
                                "updateWindowsLocked: creating delay animation");

                        // Create a new animation to delay until keyguard is gone on its own.
                        winAnimator.mAnimation = new AlphaAnimation(1.0f, 1.0f);
                        winAnimator.mAnimation.setDuration(KEYGUARD_ANIM_TIMEOUT_MS);
                        winAnimator.mAnimationIsEntrance = false;
                        winAnimator.mAnimationStartTime = -1;
                        winAnimator.mKeyguardGoingAwayAnimation = true;
                        winAnimator.mKeyguardGoingAwayWithWallpaper
                                = keyguardGoingAwayWithWallpaper;
                    }
                } else {
                    if (DEBUG_KEYGUARD) Slog.d(TAG,
                            "updateWindowsLocked: StatusBar is no longer keyguard");
                    mKeyguardGoingAway = false;
                    winAnimator.clearAnimation();
                }
                break;
            }
        }

        mForceHiding = KEYGUARD_NOT_SHOWN;

        boolean wallpaperInUnForceHiding = false;
        boolean startingInUnForceHiding = false;
        ArrayList<WindowStateAnimator> unForceHiding = null;
        WindowState wallpaper = null;
        final WallpaperController wallpaperController = mService.mWallpaperControllerLocked;
        for (int i = windows.size() - 1; i >= 0; i--) {
            WindowState win = windows.get(i);
            WindowStateAnimator winAnimator = win.mWinAnimator;
            final int flags = win.mAttrs.flags;
            boolean canBeForceHidden = mPolicy.canBeForceHidden(win, win.mAttrs);
            boolean shouldBeForceHidden = shouldForceHide(win);
            if (winAnimator.hasSurface()) {
                final boolean wasAnimating = winAnimator.mWasAnimating;
                final boolean nowAnimating = winAnimator.stepAnimationLocked(mCurrentTime);
                winAnimator.mWasAnimating = nowAnimating;
                orAnimating(nowAnimating);

                if (DEBUG_WALLPAPER) {
                    Slog.v(TAG, win + ": wasAnimating=" + wasAnimating +
                            ", nowAnimating=" + nowAnimating);
                }

                if (wasAnimating && !winAnimator.mAnimating
                        && wallpaperController.isWallpaperTarget(win)) {
                    mBulkUpdateParams |= SET_WALLPAPER_MAY_CHANGE;
                    setPendingLayoutChanges(Display.DEFAULT_DISPLAY,
                            WindowManagerPolicy.FINISH_LAYOUT_REDO_WALLPAPER);
                    if (DEBUG_LAYOUT_REPEATS) {
                        mWindowPlacerLocked.debugLayoutRepeats(
                                "updateWindowsAndWallpaperLocked 2",
                                getPendingLayoutChanges(Display.DEFAULT_DISPLAY));
                    }
                }

                if (mPolicy.isForceHiding(win.mAttrs)) {
                    if (!wasAnimating && nowAnimating) {
                        if (DEBUG_KEYGUARD || DEBUG_ANIM ||
                                DEBUG_VISIBILITY) Slog.v(TAG,
                                "Animation started that could impact force hide: " + win);
                        mBulkUpdateParams |= SET_FORCE_HIDING_CHANGED;
                        setPendingLayoutChanges(displayId,
                                WindowManagerPolicy.FINISH_LAYOUT_REDO_WALLPAPER);
                        if (DEBUG_LAYOUT_REPEATS) {
                            mWindowPlacerLocked.debugLayoutRepeats(
                                    "updateWindowsAndWallpaperLocked 3",
                                    getPendingLayoutChanges(displayId));
                        }
                        mService.mFocusMayChange = true;
                    } else if (mKeyguardGoingAway && !nowAnimating) {
                        // Timeout!!
                        Slog.e(TAG, "Timeout waiting for animation to startup");
                        mPolicy.startKeyguardExitAnimation(0, 0);
                        mKeyguardGoingAway = false;
                    }
                    if (win.isReadyForDisplay()) {
                        if (nowAnimating && win.mWinAnimator.mKeyguardGoingAwayAnimation) {
                            mForceHiding = KEYGUARD_ANIMATING_OUT;
                        } else {
                            mForceHiding = win.isDrawnLw() ? KEYGUARD_SHOWN : KEYGUARD_NOT_SHOWN;
                        }
                    }
                    if (DEBUG_KEYGUARD || DEBUG_VISIBILITY) Slog.v(TAG,
                            "Force hide " + forceHidingToString()
                            + " hasSurface=" + win.mHasSurface
                            + " policyVis=" + win.mPolicyVisibility
                            + " destroying=" + win.mDestroying
                            + " attHidden=" + win.mAttachedHidden
                            + " vis=" + win.mViewVisibility
                            + " hidden=" + win.mRootToken.hidden
                            + " anim=" + win.mWinAnimator.mAnimation);
                } else if (canBeForceHidden) {
                    if (shouldBeForceHidden) {
                        if (!win.hideLw(false, false)) {
                            // Was already hidden
                            continue;
                        }
                        if (DEBUG_KEYGUARD || DEBUG_VISIBILITY) Slog.v(TAG,
                                "Now policy hidden: " + win);
                    } else {
                        boolean applyExistingExitAnimation = mPostKeyguardExitAnimation != null
                                && !mPostKeyguardExitAnimation.hasEnded()
                                && !winAnimator.mKeyguardGoingAwayAnimation
                                && win.hasDrawnLw()
                                && win.mAttachedWindow == null
                                && !win.mIsImWindow
                                && displayId == Display.DEFAULT_DISPLAY;

                        // If the window is already showing and we don't need to apply an existing
                        // Keyguard exit animation, skip.
                        if (!win.showLw(false, false) && !applyExistingExitAnimation) {
                            continue;
                        }
                        final boolean visibleNow = win.isVisibleNow();
                        if (!visibleNow) {
                            // Couldn't really show, must showLw() again when win becomes visible.
                            win.hideLw(false, false);
                            continue;
                        }
                        if (DEBUG_KEYGUARD || DEBUG_VISIBILITY) Slog.v(TAG,
                                "Now policy shown: " + win);
                        if ((mBulkUpdateParams & SET_FORCE_HIDING_CHANGED) != 0
                                && win.mAttachedWindow == null) {
                            if (unForceHiding == null) {
                                unForceHiding = new ArrayList<>();
                            }
                            unForceHiding.add(winAnimator);
                            if ((flags & FLAG_SHOW_WALLPAPER) != 0) {
                                wallpaperInUnForceHiding = true;
                            }
                            if (win.mAttrs.type == TYPE_APPLICATION_STARTING) {
                                startingInUnForceHiding = true;
                            }
                        } else if (applyExistingExitAnimation) {
                            // We're already in the middle of an animation. Use the existing
                            // animation to bring in this window.
                            if (DEBUG_KEYGUARD) Slog.v(TAG,
                                    "Applying existing Keyguard exit animation to new window: win="
                                            + win);

                            Animation a = mPolicy.createForceHideEnterAnimation(false,
                                    keyguardGoingAwayToShade);
                            winAnimator.setAnimation(a, mPostKeyguardExitAnimation.getStartTime(),
                                    STACK_CLIP_BEFORE_ANIM);
                            winAnimator.mKeyguardGoingAwayAnimation = true;
                            winAnimator.mKeyguardGoingAwayWithWallpaper
                                    = keyguardGoingAwayWithWallpaper;
                        }
                        final WindowState currentFocus = mService.mCurrentFocus;
                        if (currentFocus == null || currentFocus.mLayer < win.mLayer) {
                            // We are showing on top of the current
                            // focus, so re-evaluate focus to make
                            // sure it is correct.
                            if (DEBUG_FOCUS_LIGHT) Slog.v(TAG,
                                    "updateWindowsLocked: setting mFocusMayChange true");
                            mService.mFocusMayChange = true;
                        }
                    }
                    if ((flags & FLAG_SHOW_WALLPAPER) != 0) {
                        mBulkUpdateParams |= SET_WALLPAPER_MAY_CHANGE;
                        setPendingLayoutChanges(Display.DEFAULT_DISPLAY,
                                WindowManagerPolicy.FINISH_LAYOUT_REDO_WALLPAPER);
                        if (DEBUG_LAYOUT_REPEATS) {
                            mWindowPlacerLocked.debugLayoutRepeats(
                                    "updateWindowsAndWallpaperLocked 4",
                                    getPendingLayoutChanges(Display.DEFAULT_DISPLAY));
                        }
                    }
                }
            }

            // If the window doesn't have a surface, the only thing we care about is the correct
            // policy visibility.
            else if (canBeForceHidden) {
                if (shouldBeForceHidden) {
                    win.hideLw(false, false);
                } else {
                    win.showLw(false, false);
                }
            }

            final AppWindowToken atoken = win.mAppToken;
            if (winAnimator.mDrawState == READY_TO_SHOW) {
                if (atoken == null || atoken.allDrawn) {
                    if (winAnimator.performShowLocked()) {
                        setPendingLayoutChanges(displayId,
                                WindowManagerPolicy.FINISH_LAYOUT_REDO_ANIM);
                        if (DEBUG_LAYOUT_REPEATS) {
                            mWindowPlacerLocked.debugLayoutRepeats(
                                    "updateWindowsAndWallpaperLocked 5",
                                    getPendingLayoutChanges(displayId));
                        }
                    }
                }
            }
            final AppWindowAnimator appAnimator = winAnimator.mAppAnimator;
            if (appAnimator != null && appAnimator.thumbnail != null) {
                if (appAnimator.thumbnailTransactionSeq != mAnimTransactionSequence) {
                    appAnimator.thumbnailTransactionSeq = mAnimTransactionSequence;
                    appAnimator.thumbnailLayer = 0;
                }
                if (appAnimator.thumbnailLayer < winAnimator.mAnimLayer) {
                    appAnimator.thumbnailLayer = winAnimator.mAnimLayer;
                }
            }
            if (win.mIsWallpaper) {
                wallpaper = win;
            }
        } // end forall windows

        // If we have windows that are being show due to them no longer
        // being force-hidden, apply the appropriate animation to them if animations are not
        // disabled.
        if (unForceHiding != null) {
            if (!keyguardGoingAwayNoAnimation) {
                boolean first = true;
                for (int i=unForceHiding.size()-1; i>=0; i--) {
                    final WindowStateAnimator winAnimator = unForceHiding.get(i);
                    Animation a = mPolicy.createForceHideEnterAnimation(
                            wallpaperInUnForceHiding && !startingInUnForceHiding,
                            keyguardGoingAwayToShade);
                    if (a != null) {
                        if (DEBUG_KEYGUARD) Slog.v(TAG,
                                "Starting keyguard exit animation on window " + winAnimator.mWin);
                        winAnimator.setAnimation(a, STACK_CLIP_BEFORE_ANIM);
                        winAnimator.mKeyguardGoingAwayAnimation = true;
                        winAnimator.mKeyguardGoingAwayWithWallpaper
                                = keyguardGoingAwayWithWallpaper;
                        if (first) {
                            mPostKeyguardExitAnimation = a;
                            mPostKeyguardExitAnimation.setStartTime(mCurrentTime);
                            first = false;
                        }
                    }
                }
            } else if (mKeyguardGoingAway) {
                mPolicy.startKeyguardExitAnimation(mCurrentTime, 0 /* duration */);
                mKeyguardGoingAway = false;
            }


            // Wallpaper is going away in un-force-hide motion, animate it as well.
            if (!wallpaperInUnForceHiding && wallpaper != null && !keyguardGoingAwayNoAnimation) {
                if (DEBUG_KEYGUARD) Slog.d(TAG, "updateWindowsLocked: wallpaper animating away");
                Animation a = mPolicy.createForceHideWallpaperExitAnimation(
                        keyguardGoingAwayToShade);
                if (a != null) {
                    wallpaper.mWinAnimator.setAnimation(a);
                }
            }
        }

        if (mPostKeyguardExitAnimation != null) {
            // We're in the midst of a keyguard exit animation.
            if (mKeyguardGoingAway) {
                mPolicy.startKeyguardExitAnimation(mCurrentTime +
                        mPostKeyguardExitAnimation.getStartOffset(),
                        mPostKeyguardExitAnimation.getDuration());
                mKeyguardGoingAway = false;
            }
            // mPostKeyguardExitAnimation might either be ended normally, cancelled, or "orphaned",
            // meaning that the window it was running on was removed. We check for hasEnded() for
            // ended normally and cancelled case, and check the time for the "orphaned" case.
            else if (mPostKeyguardExitAnimation.hasEnded()
                    || mCurrentTime - mPostKeyguardExitAnimation.getStartTime()
                            > mPostKeyguardExitAnimation.getDuration()) {
                // Done with the animation, reset.
                if (DEBUG_KEYGUARD) Slog.v(TAG, "Done with Keyguard exit animations.");
                mPostKeyguardExitAnimation = null;
            }
        }

        final WindowState winShowWhenLocked = (WindowState) mPolicy.getWinShowWhenLockedLw();
        if (winShowWhenLocked != null) {
            mLastShowWinWhenLocked = winShowWhenLocked;
        }
    }

    private void updateWallpaperLocked(int displayId) {
        mService.getDisplayContentLocked(displayId).resetAnimationBackgroundAnimator();

        final WindowList windows = mService.getWindowListLocked(displayId);
        WindowState detachedWallpaper = null;

        for (int i = windows.size() - 1; i >= 0; i--) {
            final WindowState win = windows.get(i);
            WindowStateAnimator winAnimator = win.mWinAnimator;
            if (winAnimator.mSurfaceController == null || !winAnimator.hasSurface()) {
                continue;
            }

            final int flags = win.mAttrs.flags;

            // If this window is animating, make a note that we have
            // an animating window and take care of a request to run
            // a detached wallpaper animation.
            if (winAnimator.mAnimating) {
                if (winAnimator.mAnimation != null) {
                    if ((flags & FLAG_SHOW_WALLPAPER) != 0
                            && winAnimator.mAnimation.getDetachWallpaper()) {
                        detachedWallpaper = win;
                    }
                    final int color = winAnimator.mAnimation.getBackgroundColor();
                    if (color != 0) {
                        final TaskStack stack = win.getStack();
                        if (stack != null) {
                            stack.setAnimationBackground(winAnimator, color);
                        }
                    }
                }
                setAnimating(true);
            }

            // If this window's app token is running a detached wallpaper
            // animation, make a note so we can ensure the wallpaper is
            // displayed behind it.
            final AppWindowAnimator appAnimator = winAnimator.mAppAnimator;
            if (appAnimator != null && appAnimator.animation != null
                    && appAnimator.animating) {
                if ((flags & FLAG_SHOW_WALLPAPER) != 0
                        && appAnimator.animation.getDetachWallpaper()) {
                    detachedWallpaper = win;
                }

                final int color = appAnimator.animation.getBackgroundColor();
                if (color != 0) {
                    final TaskStack stack = win.getStack();
                    if (stack != null) {
                        stack.setAnimationBackground(winAnimator, color);
                    }
                }
            }
        } // end forall windows

        if (mWindowDetachedWallpaper != detachedWallpaper) {
            if (DEBUG_WALLPAPER) Slog.v(TAG,
                    "Detached wallpaper changed from " + mWindowDetachedWallpaper
                    + " to " + detachedWallpaper);
            mWindowDetachedWallpaper = detachedWallpaper;
            mBulkUpdateParams |= SET_WALLPAPER_MAY_CHANGE;
        }
    }

    /** See if any windows have been drawn, so they (and others associated with them) can now be
     *  shown. */
    private void testTokenMayBeDrawnLocked(int displayId) {
        // See if any windows have been drawn, so they (and others
        // associated with them) can now be shown.
        final ArrayList<Task> tasks = mService.getDisplayContentLocked(displayId).getTasks();
        final int numTasks = tasks.size();
        for (int taskNdx = 0; taskNdx < numTasks; ++taskNdx) {
            final AppTokenList tokens = tasks.get(taskNdx).mAppTokens;
            final int numTokens = tokens.size();
            for (int tokenNdx = 0; tokenNdx < numTokens; ++tokenNdx) {
                final AppWindowToken wtoken = tokens.get(tokenNdx);
                AppWindowAnimator appAnimator = wtoken.mAppAnimator;
                final boolean allDrawn = wtoken.allDrawn;
                if (allDrawn != appAnimator.allDrawn) {
                    appAnimator.allDrawn = allDrawn;
                    if (allDrawn) {
                        // The token has now changed state to having all
                        // windows shown...  what to do, what to do?
                        if (appAnimator.freezingScreen) {
                            appAnimator.showAllWindowsLocked();
                            mService.unsetAppFreezingScreenLocked(wtoken, false, true);
                            if (DEBUG_ORIENTATION) Slog.i(TAG,
                                    "Setting mOrientationChangeComplete=true because wtoken "
                                    + wtoken + " numInteresting=" + wtoken.numInterestingWindows
                                    + " numDrawn=" + wtoken.numDrawnWindows);
                            // This will set mOrientationChangeComplete and cause a pass through
                            // layout.
                            setAppLayoutChanges(appAnimator,
                                    WindowManagerPolicy.FINISH_LAYOUT_REDO_WALLPAPER,
                                    "testTokenMayBeDrawnLocked: freezingScreen", displayId);
                        } else {
                            setAppLayoutChanges(appAnimator,
                                    WindowManagerPolicy.FINISH_LAYOUT_REDO_ANIM,
                                    "testTokenMayBeDrawnLocked", displayId);

                            // We can now show all of the drawn windows!
                            if (!mService.mOpeningApps.contains(wtoken)) {
                                orAnimating(appAnimator.showAllWindowsLocked());
                            }
                        }
                    }
                }
            }
        }
    }


    /** Locked on mService.mWindowMap. */
    private void animateLocked(long frameTimeNs) {
        if (!mInitialized) {
            return;
        }

        mCurrentTime = frameTimeNs / TimeUtils.NANOS_PER_MS;
        mBulkUpdateParams = SET_ORIENTATION_CHANGE_COMPLETE;
        boolean wasAnimating = mAnimating;
        setAnimating(false);
        boolean isSingleHandAnimating = false;
        mAppWindowAnimating = false;
        if (DEBUG_WINDOW_TRACE) {
            Slog.i(TAG, "!!! animate: entry time=" + mCurrentTime);
        }

        if (SHOW_TRANSACTIONS) Slog.i(
                TAG, ">>> OPEN TRANSACTION animateLocked");
        SurfaceControl.openTransaction();
        SurfaceControl.setAnimationTransaction();
=======
    private void animate(long frameTimeNs) {
        boolean transactionOpen = false;
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040
        try {
            synchronized (mService.mWindowMap) {
                if (!mInitialized) {
                    return;
                }

                mCurrentTime = frameTimeNs / TimeUtils.NANOS_PER_MS;
                mBulkUpdateParams = SET_ORIENTATION_CHANGE_COMPLETE;
                mAnimating = false;
                mAppWindowAnimating = false;
                if (DEBUG_WINDOW_TRACE) {
                    Slog.i(TAG, "!!! animate: entry time=" + mCurrentTime);
                }

                if (SHOW_TRANSACTIONS) Slog.i(TAG, ">>> OPEN TRANSACTION animate");
                mService.openSurfaceTransaction();
                transactionOpen = true;
                SurfaceControl.setAnimationTransaction();

                final AccessibilityController accessibilityController =
                        mService.mAccessibilityController;
                final int numDisplays = mDisplayContentsAnimators.size();
                for (int i = 0; i < numDisplays; i++) {
                    final int displayId = mDisplayContentsAnimators.keyAt(i);
                    final DisplayContent dc = mService.mRoot.getDisplayContentOrCreate(displayId);
                    dc.stepAppWindowsAnimation(mCurrentTime);
                    DisplayContentsAnimator displayAnimator = mDisplayContentsAnimators.valueAt(i);

                    final ScreenRotationAnimation screenRotationAnimation =
                            displayAnimator.mScreenRotationAnimation;
                    if (screenRotationAnimation != null && screenRotationAnimation.isAnimating()) {
                        if (screenRotationAnimation.stepAnimationLocked(mCurrentTime)) {
                            setAnimating(true);
                        } else {
                            mBulkUpdateParams |= SET_UPDATE_ROTATION;
                            screenRotationAnimation.kill();
                            displayAnimator.mScreenRotationAnimation = null;

                            //TODO (multidisplay): Accessibility supported only for the default
                            // display.
                            if (accessibilityController != null && dc.isDefaultDisplay) {
                                // We just finished rotation animation which means we did not
                                // announce the rotation and waited for it to end, announce now.
                                accessibilityController.onRotationChangedLocked(
                                        mService.getDefaultDisplayContentLocked());
                            }
                        }
                    }

                    // Update animations of all applications, including those
                    // associated with exiting/removed apps
                    ++mAnimTransactionSequence;
                    dc.updateWindowsForAnimator(this);
                    dc.updateWallpaperForAnimator(this);
                    dc.prepareWindowSurfaces();
                }

                for (int i = 0; i < numDisplays; i++) {
                    final int displayId = mDisplayContentsAnimators.keyAt(i);
                    final DisplayContent dc = mService.mRoot.getDisplayContentOrCreate(displayId);

                    dc.checkAppWindowsReadyToShow();

                    final ScreenRotationAnimation screenRotationAnimation =
                            mDisplayContentsAnimators.valueAt(i).mScreenRotationAnimation;
                    if (screenRotationAnimation != null) {
                        screenRotationAnimation.updateSurfacesInTransaction();
                    }

                    orAnimating(dc.animateDimLayers());
                    orAnimating(dc.getDockedDividerController().animate(mCurrentTime));
                    //TODO (multidisplay): Magnification is supported only for the default display.
                    if (accessibilityController != null && dc.isDefaultDisplay) {
                        accessibilityController.drawMagnifiedRegionBorderIfNeededLocked();
                    }
                }

<<<<<<< HEAD
                // Update animations of all applications, including those
                // associated with exiting/removed apps
                updateWindowsLocked(displayId);
                updateWallpaperLocked(displayId);

                final WindowList windows = mService.getWindowListLocked(displayId);
                final int N = windows.size();
                for (int j = 0; j < N; j++) {
                    windows.get(j).mWinAnimator.prepareSurfaceLocked(true);
                    if (windows.get(j).mWinAnimator.mIsSingleHandExiting || windows.get(j).mWinAnimator.mIsSingleHandEntering)
                        isSingleHandAnimating = true;
                }
            }

            for (int i = 0; i < numDisplays; i++) {
                final int displayId = mDisplayContentsAnimators.keyAt(i);

                testTokenMayBeDrawnLocked(displayId);

                final ScreenRotationAnimation screenRotationAnimation =
                        mDisplayContentsAnimators.valueAt(i).mScreenRotationAnimation;
                if (screenRotationAnimation != null) {
                    screenRotationAnimation.updateSurfacesInTransaction();
=======
                if (mService.mDragState != null) {
                    mAnimating |= mService.mDragState.stepAnimationLocked(mCurrentTime);
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040
                }

                if (mAnimating) {
                    mService.scheduleAnimationLocked();
                }

<<<<<<< HEAD
            if (mService.mDragState != null) {
                mAnimating |= mService.mDragState.stepAnimationLocked(mCurrentTime);
            }

            if (mAnimating || isSingleHandAnimating) {
                mService.scheduleAnimationLocked();
            }

            if (mService.mWatermark != null) {
                mService.mWatermark.drawIfNeeded();
=======
                if (mService.mWatermark != null) {
                    mService.mWatermark.drawIfNeeded();
                }
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040
            }
        } catch (RuntimeException e) {
            Slog.wtf(TAG, "Unhandled exception in Window Manager", e);
        } finally {
            if (transactionOpen) {

                // Do not hold window manager lock while closing the transaction, as this might be
                // blocking until the next frame, which can lead to total lock starvation.
                mService.closeSurfaceTransaction(false /* withLockHeld */);
                if (SHOW_TRANSACTIONS) Slog.i(TAG, "<<< CLOSE TRANSACTION animate");
            }
        }

        synchronized (mService.mWindowMap) {
            boolean hasPendingLayoutChanges = mService.mRoot.hasPendingLayoutChanges(this);
            boolean doRequest = false;
            if (mBulkUpdateParams != 0) {
                doRequest = mService.mRoot.copyAnimToLayoutParams();
            }

            if (hasPendingLayoutChanges || doRequest) {
                mWindowPlacerLocked.requestTraversal();
            }

            if (mAnimating && !mLastAnimating) {

                // Usually app transitions but quite a load onto the system already (with all the
                // things happening in app), so pause task snapshot persisting to not increase the
                // load.
                mService.mTaskSnapshotController.setPersisterPaused(true);
                Trace.asyncTraceBegin(Trace.TRACE_TAG_WINDOW_MANAGER, "animating", 0);
            }
            if (!mAnimating && mLastAnimating) {
                mWindowPlacerLocked.requestTraversal();
                mService.mTaskSnapshotController.setPersisterPaused(false);
                Trace.asyncTraceEnd(Trace.TRACE_TAG_WINDOW_MANAGER, "animating", 0);
            }

            mLastAnimating = mAnimating;

            if (mRemoveReplacedWindows) {
                mService.mRoot.removeReplacedWindows();
                mRemoveReplacedWindows = false;
            }

            mService.stopUsingSavedSurfaceLocked();
            mService.destroyPreservedSurfaceLocked();
            mService.mWindowPlacerLocked.destroyPendingSurfaces();

            if (DEBUG_WINDOW_TRACE) {
                Slog.i(TAG, "!!! animate: exit mAnimating=" + mAnimating
                        + " mBulkUpdateParams=" + Integer.toHexString(mBulkUpdateParams)
                        + " mPendingLayoutChanges(DEFAULT_DISPLAY)="
                        + Integer.toHexString(getPendingLayoutChanges(DEFAULT_DISPLAY)));
            }
        }
    }

    private static String bulkUpdateParamsToString(int bulkUpdateParams) {
        StringBuilder builder = new StringBuilder(128);
        if ((bulkUpdateParams & WindowSurfacePlacer.SET_UPDATE_ROTATION) != 0) {
            builder.append(" UPDATE_ROTATION");
        }
        if ((bulkUpdateParams & WindowSurfacePlacer.SET_WALLPAPER_MAY_CHANGE) != 0) {
            builder.append(" WALLPAPER_MAY_CHANGE");
        }
        if ((bulkUpdateParams & WindowSurfacePlacer.SET_FORCE_HIDING_CHANGED) != 0) {
            builder.append(" FORCE_HIDING_CHANGED");
        }
        if ((bulkUpdateParams & WindowSurfacePlacer.SET_ORIENTATION_CHANGE_COMPLETE) != 0) {
            builder.append(" ORIENTATION_CHANGE_COMPLETE");
        }
        if ((bulkUpdateParams & WindowSurfacePlacer.SET_TURN_ON_SCREEN) != 0) {
            builder.append(" TURN_ON_SCREEN");
        }
        return builder.toString();
    }

    public void dumpLocked(PrintWriter pw, String prefix, boolean dumpAll) {
        final String subPrefix = "  " + prefix;
        final String subSubPrefix = "  " + subPrefix;

        for (int i = 0; i < mDisplayContentsAnimators.size(); i++) {
            pw.print(prefix); pw.print("DisplayContentsAnimator #");
                    pw.print(mDisplayContentsAnimators.keyAt(i));
                    pw.println(":");
            final DisplayContentsAnimator displayAnimator = mDisplayContentsAnimators.valueAt(i);
            final DisplayContent dc =
                    mService.mRoot.getDisplayContentOrCreate(mDisplayContentsAnimators.keyAt(i));
            dc.dumpWindowAnimators(pw, subPrefix);
            if (displayAnimator.mScreenRotationAnimation != null) {
                pw.print(subPrefix); pw.println("mScreenRotationAnimation:");
                displayAnimator.mScreenRotationAnimation.printTo(subSubPrefix, pw);
            } else if (dumpAll) {
                pw.print(subPrefix); pw.println("no ScreenRotationAnimation ");
            }
            pw.println();
        }

        pw.println();

        if (dumpAll) {
            pw.print(prefix); pw.print("mAnimTransactionSequence=");
                    pw.print(mAnimTransactionSequence);
            pw.print(prefix); pw.print("mCurrentTime=");
                    pw.println(TimeUtils.formatUptime(mCurrentTime));
        }
        if (mBulkUpdateParams != 0) {
            pw.print(prefix); pw.print("mBulkUpdateParams=0x");
                    pw.print(Integer.toHexString(mBulkUpdateParams));
                    pw.println(bulkUpdateParamsToString(mBulkUpdateParams));
        }
        if (mWindowDetachedWallpaper != null) {
            pw.print(prefix); pw.print("mWindowDetachedWallpaper=");
                pw.println(mWindowDetachedWallpaper);
        }
    }

    int getPendingLayoutChanges(final int displayId) {
        if (displayId < 0) {
            return 0;
        }
        final DisplayContent displayContent = mService.mRoot.getDisplayContentOrCreate(displayId);
        return (displayContent != null) ? displayContent.pendingLayoutChanges : 0;
    }

    void setPendingLayoutChanges(final int displayId, final int changes) {
        if (displayId < 0) {
            return;
        }
        final DisplayContent displayContent = mService.mRoot.getDisplayContentOrCreate(displayId);
        if (displayContent != null) {
            displayContent.pendingLayoutChanges |= changes;
        }
    }

    private DisplayContentsAnimator getDisplayContentsAnimatorLocked(int displayId) {
        if (displayId < 0) {
            return null;
        }

        DisplayContentsAnimator displayAnimator = mDisplayContentsAnimators.get(displayId);

        // It is possible that this underlying {@link DisplayContent} has been removed. In this
        // case, we do not want to create an animator associated with it as {link #animate} will
        // fail.
        if (displayAnimator == null && mService.mRoot.getDisplayContent(displayId) != null) {
            displayAnimator = new DisplayContentsAnimator();
            mDisplayContentsAnimators.put(displayId, displayAnimator);
        }
        return displayAnimator;
    }

    void setScreenRotationAnimationLocked(int displayId, ScreenRotationAnimation animation) {
        final DisplayContentsAnimator animator = getDisplayContentsAnimatorLocked(displayId);

        if (animator != null) {
            animator.mScreenRotationAnimation = animation;
        }
    }

    ScreenRotationAnimation getScreenRotationAnimationLocked(int displayId) {
        if (displayId < 0) {
            return null;
        }

        DisplayContentsAnimator animator = getDisplayContentsAnimatorLocked(displayId);
        return animator != null? animator.mScreenRotationAnimation : null;
    }

    void requestRemovalOfReplacedWindows(WindowState win) {
        mRemoveReplacedWindows = true;
    }

    void scheduleAnimation() {
        if (!mAnimationFrameCallbackScheduled) {
            mAnimationFrameCallbackScheduled = true;
            mChoreographer.postFrameCallback(mAnimationFrameCallback);
        }
    }

    private class DisplayContentsAnimator {
        ScreenRotationAnimation mScreenRotationAnimation = null;
    }

    boolean isAnimating() {
        return mAnimating;
    }

    boolean isAnimationScheduled() {
        return mAnimationFrameCallbackScheduled;
    }

    Choreographer getChoreographer() {
        return mChoreographer;
    }

    void setAnimating(boolean animating) {
        mAnimating = animating;
    }

    void orAnimating(boolean animating) {
        mAnimating |= animating;
    }
}
