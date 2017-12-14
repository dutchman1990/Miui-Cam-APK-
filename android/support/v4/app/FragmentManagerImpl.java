package android.support.v4.app;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.v4.app.BackStackRecord.TransitionState;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.os.BuildCompat;
import android.support.v4.util.DebugUtils;
import android.support.v4.util.LogWriter;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* compiled from: FragmentManager */
final class FragmentManagerImpl extends FragmentManager implements LayoutInflaterFactory {
    static final Interpolator ACCELERATE_CUBIC = new AccelerateInterpolator(1.5f);
    static final Interpolator ACCELERATE_QUINT = new AccelerateInterpolator(2.5f);
    static boolean DEBUG = false;
    static final Interpolator DECELERATE_CUBIC = new DecelerateInterpolator(1.5f);
    static final Interpolator DECELERATE_QUINT = new DecelerateInterpolator(2.5f);
    static final boolean HONEYCOMB;
    static Field sAnimationListenerField = null;
    ArrayList<Fragment> mActive;
    ArrayList<Fragment> mAdded;
    ArrayList<Integer> mAvailBackStackIndices;
    ArrayList<Integer> mAvailIndices;
    ArrayList<BackStackRecord> mBackStack;
    ArrayList<OnBackStackChangedListener> mBackStackChangeListeners;
    ArrayList<BackStackRecord> mBackStackIndices;
    FragmentContainer mContainer;
    ArrayList<Fragment> mCreatedMenus;
    int mCurState = 0;
    boolean mDestroyed;
    Runnable mExecCommit = new C00091();
    boolean mExecutingActions;
    boolean mHavePendingDeferredStart;
    FragmentHostCallback mHost;
    boolean mNeedMenuInvalidate;
    String mNoTransactionsBecause;
    Fragment mParent;
    ArrayList<Runnable> mPendingActions;
    SparseArray<Parcelable> mStateArray = null;
    Bundle mStateBundle = null;
    boolean mStateSaved;
    Runnable[] mTmpActions;

    /* compiled from: FragmentManager */
    class C00091 implements Runnable {
        C00091() {
        }

        public void run() {
            FragmentManagerImpl.this.execPendingActions();
        }
    }

    /* compiled from: FragmentManager */
    static class AnimateOnHWLayerIfNeededListener implements AnimationListener {
        private AnimationListener mOriginalListener;
        private boolean mShouldRunOnHWLayer;
        View mView;

        /* compiled from: FragmentManager */
        class C00111 implements Runnable {
            C00111() {
            }

            public void run() {
                ViewCompat.setLayerType(AnimateOnHWLayerIfNeededListener.this.mView, 0, null);
            }
        }

        public AnimateOnHWLayerIfNeededListener(View view, Animation animation) {
            if (view != null && animation != null) {
                this.mView = view;
            }
        }

        public AnimateOnHWLayerIfNeededListener(View view, Animation animation, AnimationListener animationListener) {
            if (view != null && animation != null) {
                this.mOriginalListener = animationListener;
                this.mView = view;
                this.mShouldRunOnHWLayer = true;
            }
        }

        @CallSuper
        public void onAnimationEnd(Animation animation) {
            if (this.mView != null && this.mShouldRunOnHWLayer) {
                if (ViewCompat.isAttachedToWindow(this.mView) || BuildCompat.isAtLeastN()) {
                    this.mView.post(new C00111());
                } else {
                    ViewCompat.setLayerType(this.mView, 0, null);
                }
            }
            if (this.mOriginalListener != null) {
                this.mOriginalListener.onAnimationEnd(animation);
            }
        }

        public void onAnimationRepeat(Animation animation) {
            if (this.mOriginalListener != null) {
                this.mOriginalListener.onAnimationRepeat(animation);
            }
        }

        @CallSuper
        public void onAnimationStart(Animation animation) {
            if (this.mOriginalListener != null) {
                this.mOriginalListener.onAnimationStart(animation);
            }
        }
    }

    /* compiled from: FragmentManager */
    static class FragmentTag {
        public static final int[] Fragment = new int[]{16842755, 16842960, 16842961};

        FragmentTag() {
        }
    }

    static {
        boolean z = false;
        if (VERSION.SDK_INT >= 11) {
            z = true;
        }
        HONEYCOMB = z;
    }

    FragmentManagerImpl() {
    }

    private void checkStateLoss() {
        if (this.mStateSaved) {
            throw new IllegalStateException("Can not perform this action after onSaveInstanceState");
        } else if (this.mNoTransactionsBecause != null) {
            throw new IllegalStateException("Can not perform this action inside of " + this.mNoTransactionsBecause);
        }
    }

    static Animation makeFadeAnimation(Context context, float f, float f2) {
        Animation alphaAnimation = new AlphaAnimation(f, f2);
        alphaAnimation.setInterpolator(DECELERATE_CUBIC);
        alphaAnimation.setDuration(220);
        return alphaAnimation;
    }

    static Animation makeOpenCloseAnimation(Context context, float f, float f2, float f3, float f4) {
        Animation animationSet = new AnimationSet(false);
        Animation scaleAnimation = new ScaleAnimation(f, f2, f, f2, 1, 0.5f, 1, 0.5f);
        scaleAnimation.setInterpolator(DECELERATE_QUINT);
        scaleAnimation.setDuration(220);
        animationSet.addAnimation(scaleAnimation);
        Animation alphaAnimation = new AlphaAnimation(f3, f4);
        alphaAnimation.setInterpolator(DECELERATE_CUBIC);
        alphaAnimation.setDuration(220);
        animationSet.addAnimation(alphaAnimation);
        return animationSet;
    }

    static boolean modifiesAlpha(Animation animation) {
        if (animation instanceof AlphaAnimation) {
            return true;
        }
        if (animation instanceof AnimationSet) {
            List animations = ((AnimationSet) animation).getAnimations();
            for (int i = 0; i < animations.size(); i++) {
                if (animations.get(i) instanceof AlphaAnimation) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int reverseTransit(int i) {
        switch (i) {
            case 4097:
                return 8194;
            case 4099:
                return 4099;
            case 8194:
                return 4097;
            default:
                return 0;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setHWLayerAnimListenerIfAlpha(android.view.View r8, android.view.animation.Animation r9) {
        /*
        r7 = this;
        r6 = 0;
        if (r8 == 0) goto L_0x0005;
    L_0x0003:
        if (r9 != 0) goto L_0x0006;
    L_0x0005:
        return;
    L_0x0006:
        r4 = shouldRunOnHWLayer(r8, r9);
        if (r4 == 0) goto L_0x0038;
    L_0x000c:
        r3 = 0;
        r4 = sAnimationListenerField;	 Catch:{ NoSuchFieldException -> 0x0044, IllegalAccessException -> 0x0039 }
        if (r4 != 0) goto L_0x0022;
    L_0x0011:
        r4 = android.view.animation.Animation.class;
        r5 = "mListener";
        r4 = r4.getDeclaredField(r5);	 Catch:{ NoSuchFieldException -> 0x0044, IllegalAccessException -> 0x0039 }
        sAnimationListenerField = r4;	 Catch:{ NoSuchFieldException -> 0x0044, IllegalAccessException -> 0x0039 }
        r4 = sAnimationListenerField;	 Catch:{ NoSuchFieldException -> 0x0044, IllegalAccessException -> 0x0039 }
        r5 = 1;
        r4.setAccessible(r5);	 Catch:{ NoSuchFieldException -> 0x0044, IllegalAccessException -> 0x0039 }
    L_0x0022:
        r4 = sAnimationListenerField;	 Catch:{ NoSuchFieldException -> 0x0044, IllegalAccessException -> 0x0039 }
        r4 = r4.get(r9);	 Catch:{ NoSuchFieldException -> 0x0044, IllegalAccessException -> 0x0039 }
        r0 = r4;
        r0 = (android.view.animation.Animation.AnimationListener) r0;	 Catch:{ NoSuchFieldException -> 0x0044, IllegalAccessException -> 0x0039 }
        r3 = r0;
    L_0x002c:
        r4 = 2;
        android.support.v4.view.ViewCompat.setLayerType(r8, r4, r6);
        r4 = new android.support.v4.app.FragmentManagerImpl$AnimateOnHWLayerIfNeededListener;
        r4.<init>(r8, r9, r3);
        r9.setAnimationListener(r4);
    L_0x0038:
        return;
    L_0x0039:
        r1 = move-exception;
        r4 = "FragmentManager";
        r5 = "Cannot access Animation's mListener field";
        android.util.Log.e(r4, r5, r1);
        goto L_0x002c;
    L_0x0044:
        r2 = move-exception;
        r4 = "FragmentManager";
        r5 = "No field with the name mListener is found in Animation class";
        android.util.Log.e(r4, r5, r2);
        goto L_0x002c;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.app.FragmentManagerImpl.setHWLayerAnimListenerIfAlpha(android.view.View, android.view.animation.Animation):void");
    }

    static boolean shouldRunOnHWLayer(View view, Animation animation) {
        return (VERSION.SDK_INT >= 19 && ViewCompat.getLayerType(view) == 0 && ViewCompat.hasOverlappingRendering(view)) ? modifiesAlpha(animation) : false;
    }

    private void throwException(RuntimeException runtimeException) {
        Log.e("FragmentManager", runtimeException.getMessage());
        Log.e("FragmentManager", "Activity state:");
        PrintWriter printWriter = new PrintWriter(new LogWriter("FragmentManager"));
        if (this.mHost != null) {
            try {
                this.mHost.onDump("  ", null, printWriter, new String[0]);
            } catch (Throwable e) {
                Log.e("FragmentManager", "Failed dumping state", e);
            }
        } else {
            try {
                dump("  ", null, printWriter, new String[0]);
            } catch (Throwable e2) {
                Log.e("FragmentManager", "Failed dumping state", e2);
            }
        }
        throw runtimeException;
    }

    public static int transitToStyleIndex(int i, boolean z) {
        switch (i) {
            case 4097:
                return z ? 1 : 2;
            case 4099:
                return z ? 5 : 6;
            case 8194:
                return z ? 3 : 4;
            default:
                return -1;
        }
    }

    void addBackStackState(BackStackRecord backStackRecord) {
        if (this.mBackStack == null) {
            this.mBackStack = new ArrayList();
        }
        this.mBackStack.add(backStackRecord);
        reportBackStackChanged();
    }

    public void addFragment(Fragment fragment, boolean z) {
        if (this.mAdded == null) {
            this.mAdded = new ArrayList();
        }
        if (DEBUG) {
            Log.v("FragmentManager", "add: " + fragment);
        }
        makeActive(fragment);
        if (!fragment.mDetached) {
            if (this.mAdded.contains(fragment)) {
                throw new IllegalStateException("Fragment already added: " + fragment);
            }
            this.mAdded.add(fragment);
            fragment.mAdded = true;
            fragment.mRemoving = false;
            if (fragment.mHasMenu && fragment.mMenuVisible) {
                this.mNeedMenuInvalidate = true;
            }
            if (z) {
                moveToState(fragment);
            }
        }
    }

    public void attachController(FragmentHostCallback fragmentHostCallback, FragmentContainer fragmentContainer, Fragment fragment) {
        if (this.mHost != null) {
            throw new IllegalStateException("Already attached");
        }
        this.mHost = fragmentHostCallback;
        this.mContainer = fragmentContainer;
        this.mParent = fragment;
    }

    public void attachFragment(Fragment fragment, int i, int i2) {
        if (DEBUG) {
            Log.v("FragmentManager", "attach: " + fragment);
        }
        if (fragment.mDetached) {
            fragment.mDetached = false;
            if (!fragment.mAdded) {
                if (this.mAdded == null) {
                    this.mAdded = new ArrayList();
                }
                if (this.mAdded.contains(fragment)) {
                    throw new IllegalStateException("Fragment already added: " + fragment);
                }
                if (DEBUG) {
                    Log.v("FragmentManager", "add from attach: " + fragment);
                }
                this.mAdded.add(fragment);
                fragment.mAdded = true;
                if (fragment.mHasMenu && fragment.mMenuVisible) {
                    this.mNeedMenuInvalidate = true;
                }
                moveToState(fragment, this.mCurState, i, i2, false);
            }
        }
    }

    public void detachFragment(Fragment fragment, int i, int i2) {
        if (DEBUG) {
            Log.v("FragmentManager", "detach: " + fragment);
        }
        if (!fragment.mDetached) {
            fragment.mDetached = true;
            if (fragment.mAdded) {
                if (this.mAdded != null) {
                    if (DEBUG) {
                        Log.v("FragmentManager", "remove from detach: " + fragment);
                    }
                    this.mAdded.remove(fragment);
                }
                if (fragment.mHasMenu && fragment.mMenuVisible) {
                    this.mNeedMenuInvalidate = true;
                }
                fragment.mAdded = false;
                moveToState(fragment, 1, i, i2, false);
            }
        }
    }

    public void dispatchActivityCreated() {
        this.mStateSaved = false;
        moveToState(2, false);
    }

    public void dispatchConfigurationChanged(Configuration configuration) {
        if (this.mAdded != null) {
            for (int i = 0; i < this.mAdded.size(); i++) {
                Fragment fragment = (Fragment) this.mAdded.get(i);
                if (fragment != null) {
                    fragment.performConfigurationChanged(configuration);
                }
            }
        }
    }

    public boolean dispatchContextItemSelected(MenuItem menuItem) {
        if (this.mAdded != null) {
            for (int i = 0; i < this.mAdded.size(); i++) {
                Fragment fragment = (Fragment) this.mAdded.get(i);
                if (fragment != null && fragment.performContextItemSelected(menuItem)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void dispatchCreate() {
        this.mStateSaved = false;
        moveToState(1, false);
    }

    public boolean dispatchCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        int i;
        Fragment fragment;
        boolean z = false;
        ArrayList arrayList = null;
        if (this.mAdded != null) {
            for (i = 0; i < this.mAdded.size(); i++) {
                fragment = (Fragment) this.mAdded.get(i);
                if (fragment != null && fragment.performCreateOptionsMenu(menu, menuInflater)) {
                    z = true;
                    if (arrayList == null) {
                        arrayList = new ArrayList();
                    }
                    arrayList.add(fragment);
                }
            }
        }
        if (this.mCreatedMenus != null) {
            for (i = 0; i < this.mCreatedMenus.size(); i++) {
                fragment = (Fragment) this.mCreatedMenus.get(i);
                if (arrayList == null || !arrayList.contains(fragment)) {
                    fragment.onDestroyOptionsMenu();
                }
            }
        }
        this.mCreatedMenus = arrayList;
        return z;
    }

    public void dispatchDestroy() {
        this.mDestroyed = true;
        execPendingActions();
        moveToState(0, false);
        this.mHost = null;
        this.mContainer = null;
        this.mParent = null;
    }

    public void dispatchDestroyView() {
        moveToState(1, false);
    }

    public void dispatchLowMemory() {
        if (this.mAdded != null) {
            for (int i = 0; i < this.mAdded.size(); i++) {
                Fragment fragment = (Fragment) this.mAdded.get(i);
                if (fragment != null) {
                    fragment.performLowMemory();
                }
            }
        }
    }

    public void dispatchMultiWindowModeChanged(boolean z) {
        if (this.mAdded != null) {
            for (int size = this.mAdded.size() - 1; size >= 0; size--) {
                Fragment fragment = (Fragment) this.mAdded.get(size);
                if (fragment != null) {
                    fragment.performMultiWindowModeChanged(z);
                }
            }
        }
    }

    public boolean dispatchOptionsItemSelected(MenuItem menuItem) {
        if (this.mAdded != null) {
            for (int i = 0; i < this.mAdded.size(); i++) {
                Fragment fragment = (Fragment) this.mAdded.get(i);
                if (fragment != null && fragment.performOptionsItemSelected(menuItem)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void dispatchOptionsMenuClosed(Menu menu) {
        if (this.mAdded != null) {
            for (int i = 0; i < this.mAdded.size(); i++) {
                Fragment fragment = (Fragment) this.mAdded.get(i);
                if (fragment != null) {
                    fragment.performOptionsMenuClosed(menu);
                }
            }
        }
    }

    public void dispatchPause() {
        moveToState(4, false);
    }

    public void dispatchPictureInPictureModeChanged(boolean z) {
        if (this.mAdded != null) {
            for (int size = this.mAdded.size() - 1; size >= 0; size--) {
                Fragment fragment = (Fragment) this.mAdded.get(size);
                if (fragment != null) {
                    fragment.performPictureInPictureModeChanged(z);
                }
            }
        }
    }

    public boolean dispatchPrepareOptionsMenu(Menu menu) {
        boolean z = false;
        if (this.mAdded != null) {
            for (int i = 0; i < this.mAdded.size(); i++) {
                Fragment fragment = (Fragment) this.mAdded.get(i);
                if (fragment != null && fragment.performPrepareOptionsMenu(menu)) {
                    z = true;
                }
            }
        }
        return z;
    }

    public void dispatchReallyStop() {
        moveToState(2, false);
    }

    public void dispatchResume() {
        this.mStateSaved = false;
        moveToState(5, false);
    }

    public void dispatchStart() {
        this.mStateSaved = false;
        moveToState(4, false);
    }

    public void dispatchStop() {
        this.mStateSaved = true;
        moveToState(3, false);
    }

    void doPendingDeferredStart() {
        if (this.mHavePendingDeferredStart) {
            int i = 0;
            for (int i2 = 0; i2 < this.mActive.size(); i2++) {
                Fragment fragment = (Fragment) this.mActive.get(i2);
                if (!(fragment == null || fragment.mLoaderManager == null)) {
                    i |= fragment.mLoaderManager.hasRunningLoaders();
                }
            }
            if (i == 0) {
                this.mHavePendingDeferredStart = false;
                startPendingDeferredFragments();
            }
        }
    }

    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        int size;
        int i;
        Fragment fragment;
        String str2 = str + "    ";
        if (this.mActive != null) {
            size = this.mActive.size();
            if (size > 0) {
                printWriter.print(str);
                printWriter.print("Active Fragments in ");
                printWriter.print(Integer.toHexString(System.identityHashCode(this)));
                printWriter.println(":");
                for (i = 0; i < size; i++) {
                    fragment = (Fragment) this.mActive.get(i);
                    printWriter.print(str);
                    printWriter.print("  #");
                    printWriter.print(i);
                    printWriter.print(": ");
                    printWriter.println(fragment);
                    if (fragment != null) {
                        fragment.dump(str2, fileDescriptor, printWriter, strArr);
                    }
                }
            }
        }
        if (this.mAdded != null) {
            size = this.mAdded.size();
            if (size > 0) {
                printWriter.print(str);
                printWriter.println("Added Fragments:");
                for (i = 0; i < size; i++) {
                    fragment = (Fragment) this.mAdded.get(i);
                    printWriter.print(str);
                    printWriter.print("  #");
                    printWriter.print(i);
                    printWriter.print(": ");
                    printWriter.println(fragment.toString());
                }
            }
        }
        if (this.mCreatedMenus != null) {
            size = this.mCreatedMenus.size();
            if (size > 0) {
                printWriter.print(str);
                printWriter.println("Fragments Created Menus:");
                for (i = 0; i < size; i++) {
                    fragment = (Fragment) this.mCreatedMenus.get(i);
                    printWriter.print(str);
                    printWriter.print("  #");
                    printWriter.print(i);
                    printWriter.print(": ");
                    printWriter.println(fragment.toString());
                }
            }
        }
        if (this.mBackStack != null) {
            size = this.mBackStack.size();
            if (size > 0) {
                printWriter.print(str);
                printWriter.println("Back Stack:");
                for (i = 0; i < size; i++) {
                    BackStackRecord backStackRecord = (BackStackRecord) this.mBackStack.get(i);
                    printWriter.print(str);
                    printWriter.print("  #");
                    printWriter.print(i);
                    printWriter.print(": ");
                    printWriter.println(backStackRecord.toString());
                    backStackRecord.dump(str2, fileDescriptor, printWriter, strArr);
                }
            }
        }
        synchronized (this) {
            if (this.mBackStackIndices != null) {
                size = this.mBackStackIndices.size();
                if (size > 0) {
                    printWriter.print(str);
                    printWriter.println("Back Stack Indices:");
                    for (i = 0; i < size; i++) {
                        backStackRecord = (BackStackRecord) this.mBackStackIndices.get(i);
                        printWriter.print(str);
                        printWriter.print("  #");
                        printWriter.print(i);
                        printWriter.print(": ");
                        printWriter.println(backStackRecord);
                    }
                }
            }
            if (this.mAvailBackStackIndices != null && this.mAvailBackStackIndices.size() > 0) {
                printWriter.print(str);
                printWriter.print("mAvailBackStackIndices: ");
                printWriter.println(Arrays.toString(this.mAvailBackStackIndices.toArray()));
            }
        }
        if (this.mPendingActions != null) {
            size = this.mPendingActions.size();
            if (size > 0) {
                printWriter.print(str);
                printWriter.println("Pending Actions:");
                for (i = 0; i < size; i++) {
                    Runnable runnable = (Runnable) this.mPendingActions.get(i);
                    printWriter.print(str);
                    printWriter.print("  #");
                    printWriter.print(i);
                    printWriter.print(": ");
                    printWriter.println(runnable);
                }
            }
        }
        printWriter.print(str);
        printWriter.println("FragmentManager misc state:");
        printWriter.print(str);
        printWriter.print("  mHost=");
        printWriter.println(this.mHost);
        printWriter.print(str);
        printWriter.print("  mContainer=");
        printWriter.println(this.mContainer);
        if (this.mParent != null) {
            printWriter.print(str);
            printWriter.print("  mParent=");
            printWriter.println(this.mParent);
        }
        printWriter.print(str);
        printWriter.print("  mCurState=");
        printWriter.print(this.mCurState);
        printWriter.print(" mStateSaved=");
        printWriter.print(this.mStateSaved);
        printWriter.print(" mDestroyed=");
        printWriter.println(this.mDestroyed);
        if (this.mNeedMenuInvalidate) {
            printWriter.print(str);
            printWriter.print("  mNeedMenuInvalidate=");
            printWriter.println(this.mNeedMenuInvalidate);
        }
        if (this.mNoTransactionsBecause != null) {
            printWriter.print(str);
            printWriter.print("  mNoTransactionsBecause=");
            printWriter.println(this.mNoTransactionsBecause);
        }
        if (this.mAvailIndices != null && this.mAvailIndices.size() > 0) {
            printWriter.print(str);
            printWriter.print("  mAvailIndices: ");
            printWriter.println(Arrays.toString(this.mAvailIndices.toArray()));
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean execPendingActions() {
        /*
        r7 = this;
        r6 = 0;
        r5 = 0;
        r3 = r7.mExecutingActions;
        if (r3 == 0) goto L_0x000f;
    L_0x0006:
        r3 = new java.lang.IllegalStateException;
        r4 = "FragmentManager is already executing transactions";
        r3.<init>(r4);
        throw r3;
    L_0x000f:
        r3 = android.os.Looper.myLooper();
        r4 = r7.mHost;
        r4 = r4.getHandler();
        r4 = r4.getLooper();
        if (r3 == r4) goto L_0x0028;
    L_0x001f:
        r3 = new java.lang.IllegalStateException;
        r4 = "Must be called from main thread of fragment host";
        r3.<init>(r4);
        throw r3;
    L_0x0028:
        r0 = 0;
    L_0x0029:
        monitor-enter(r7);
        r3 = r7.mPendingActions;	 Catch:{ all -> 0x007a }
        if (r3 == 0) goto L_0x0036;
    L_0x002e:
        r3 = r7.mPendingActions;	 Catch:{ all -> 0x007a }
        r3 = r3.size();	 Catch:{ all -> 0x007a }
        if (r3 != 0) goto L_0x003b;
    L_0x0036:
        monitor-exit(r7);
        r7.doPendingDeferredStart();
        return r0;
    L_0x003b:
        r3 = r7.mPendingActions;	 Catch:{ all -> 0x007a }
        r2 = r3.size();	 Catch:{ all -> 0x007a }
        r3 = r7.mTmpActions;	 Catch:{ all -> 0x007a }
        if (r3 == 0) goto L_0x004a;
    L_0x0045:
        r3 = r7.mTmpActions;	 Catch:{ all -> 0x007a }
        r3 = r3.length;	 Catch:{ all -> 0x007a }
        if (r3 >= r2) goto L_0x004e;
    L_0x004a:
        r3 = new java.lang.Runnable[r2];	 Catch:{ all -> 0x007a }
        r7.mTmpActions = r3;	 Catch:{ all -> 0x007a }
    L_0x004e:
        r3 = r7.mPendingActions;	 Catch:{ all -> 0x007a }
        r4 = r7.mTmpActions;	 Catch:{ all -> 0x007a }
        r3.toArray(r4);	 Catch:{ all -> 0x007a }
        r3 = r7.mPendingActions;	 Catch:{ all -> 0x007a }
        r3.clear();	 Catch:{ all -> 0x007a }
        r3 = r7.mHost;	 Catch:{ all -> 0x007a }
        r3 = r3.getHandler();	 Catch:{ all -> 0x007a }
        r4 = r7.mExecCommit;	 Catch:{ all -> 0x007a }
        r3.removeCallbacks(r4);	 Catch:{ all -> 0x007a }
        monitor-exit(r7);
        r3 = 1;
        r7.mExecutingActions = r3;
        r1 = 0;
    L_0x006a:
        if (r1 >= r2) goto L_0x007d;
    L_0x006c:
        r3 = r7.mTmpActions;
        r3 = r3[r1];
        r3.run();
        r3 = r7.mTmpActions;
        r3[r1] = r5;
        r1 = r1 + 1;
        goto L_0x006a;
    L_0x007a:
        r3 = move-exception;
        monitor-exit(r7);
        throw r3;
    L_0x007d:
        r7.mExecutingActions = r6;
        r0 = 1;
        goto L_0x0029;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.app.FragmentManagerImpl.execPendingActions():boolean");
    }

    public boolean executePendingTransactions() {
        return execPendingActions();
    }

    public Fragment findFragmentById(int i) {
        int size;
        Fragment fragment;
        if (this.mAdded != null) {
            for (size = this.mAdded.size() - 1; size >= 0; size--) {
                fragment = (Fragment) this.mAdded.get(size);
                if (fragment != null && fragment.mFragmentId == i) {
                    return fragment;
                }
            }
        }
        if (this.mActive != null) {
            for (size = this.mActive.size() - 1; size >= 0; size--) {
                fragment = (Fragment) this.mActive.get(size);
                if (fragment != null && fragment.mFragmentId == i) {
                    return fragment;
                }
            }
        }
        return null;
    }

    public Fragment findFragmentByTag(String str) {
        int size;
        Fragment fragment;
        if (!(this.mAdded == null || str == null)) {
            for (size = this.mAdded.size() - 1; size >= 0; size--) {
                fragment = (Fragment) this.mAdded.get(size);
                if (fragment != null && str.equals(fragment.mTag)) {
                    return fragment;
                }
            }
        }
        if (!(this.mActive == null || str == null)) {
            for (size = this.mActive.size() - 1; size >= 0; size--) {
                fragment = (Fragment) this.mActive.get(size);
                if (fragment != null && str.equals(fragment.mTag)) {
                    return fragment;
                }
            }
        }
        return null;
    }

    public Fragment findFragmentByWho(String str) {
        if (!(this.mActive == null || str == null)) {
            for (int size = this.mActive.size() - 1; size >= 0; size--) {
                Fragment fragment = (Fragment) this.mActive.get(size);
                if (fragment != null) {
                    fragment = fragment.findFragmentByWho(str);
                    if (fragment != null) {
                        return fragment;
                    }
                }
            }
        }
        return null;
    }

    public void freeBackStackIndex(int i) {
        synchronized (this) {
            this.mBackStackIndices.set(i, null);
            if (this.mAvailBackStackIndices == null) {
                this.mAvailBackStackIndices = new ArrayList();
            }
            if (DEBUG) {
                Log.v("FragmentManager", "Freeing back stack index " + i);
            }
            this.mAvailBackStackIndices.add(Integer.valueOf(i));
        }
    }

    public Fragment getFragment(Bundle bundle, String str) {
        int i = bundle.getInt(str, -1);
        if (i == -1) {
            return null;
        }
        if (i >= this.mActive.size()) {
            throwException(new IllegalStateException("Fragment no longer exists for key " + str + ": index " + i));
        }
        Fragment fragment = (Fragment) this.mActive.get(i);
        if (fragment == null) {
            throwException(new IllegalStateException("Fragment no longer exists for key " + str + ": index " + i));
        }
        return fragment;
    }

    LayoutInflaterFactory getLayoutInflaterFactory() {
        return this;
    }

    public void hideFragment(Fragment fragment, int i, int i2) {
        if (DEBUG) {
            Log.v("FragmentManager", "hide: " + fragment);
        }
        if (!fragment.mHidden) {
            fragment.mHidden = true;
            if (fragment.mView != null) {
                Animation loadAnimation = loadAnimation(fragment, i, false, i2);
                if (loadAnimation != null) {
                    setHWLayerAnimListenerIfAlpha(fragment.mView, loadAnimation);
                    fragment.mView.startAnimation(loadAnimation);
                }
                fragment.mView.setVisibility(8);
            }
            if (fragment.mAdded && fragment.mHasMenu && fragment.mMenuVisible) {
                this.mNeedMenuInvalidate = true;
            }
            fragment.onHiddenChanged(true);
        }
    }

    boolean isStateAtLeast(int i) {
        return this.mCurState >= i;
    }

    Animation loadAnimation(Fragment fragment, int i, boolean z, int i2) {
        Animation onCreateAnimation = fragment.onCreateAnimation(i, z, fragment.mNextAnim);
        if (onCreateAnimation != null) {
            return onCreateAnimation;
        }
        if (fragment.mNextAnim != 0) {
            Animation loadAnimation = AnimationUtils.loadAnimation(this.mHost.getContext(), fragment.mNextAnim);
            if (loadAnimation != null) {
                return loadAnimation;
            }
        }
        if (i == 0) {
            return null;
        }
        int transitToStyleIndex = transitToStyleIndex(i, z);
        if (transitToStyleIndex < 0) {
            return null;
        }
        switch (transitToStyleIndex) {
            case 1:
                return makeOpenCloseAnimation(this.mHost.getContext(), 1.125f, 1.0f, 0.0f, 1.0f);
            case 2:
                return makeOpenCloseAnimation(this.mHost.getContext(), 1.0f, 0.975f, 1.0f, 0.0f);
            case 3:
                return makeOpenCloseAnimation(this.mHost.getContext(), 0.975f, 1.0f, 0.0f, 1.0f);
            case 4:
                return makeOpenCloseAnimation(this.mHost.getContext(), 1.0f, 1.075f, 1.0f, 0.0f);
            case 5:
                return makeFadeAnimation(this.mHost.getContext(), 0.0f, 1.0f);
            case 6:
                return makeFadeAnimation(this.mHost.getContext(), 1.0f, 0.0f);
            default:
                if (i2 == 0 && this.mHost.onHasWindowAnimations()) {
                    i2 = this.mHost.onGetWindowAnimations();
                }
                return i2 == 0 ? null : null;
        }
    }

    void makeActive(Fragment fragment) {
        if (fragment.mIndex < 0) {
            if (this.mAvailIndices == null || this.mAvailIndices.size() <= 0) {
                if (this.mActive == null) {
                    this.mActive = new ArrayList();
                }
                fragment.setIndex(this.mActive.size(), this.mParent);
                this.mActive.add(fragment);
            } else {
                fragment.setIndex(((Integer) this.mAvailIndices.remove(this.mAvailIndices.size() - 1)).intValue(), this.mParent);
                this.mActive.set(fragment.mIndex, fragment);
            }
            if (DEBUG) {
                Log.v("FragmentManager", "Allocated fragment index " + fragment);
            }
        }
    }

    void makeInactive(Fragment fragment) {
        if (fragment.mIndex >= 0) {
            if (DEBUG) {
                Log.v("FragmentManager", "Freeing fragment index " + fragment);
            }
            this.mActive.set(fragment.mIndex, null);
            if (this.mAvailIndices == null) {
                this.mAvailIndices = new ArrayList();
            }
            this.mAvailIndices.add(Integer.valueOf(fragment.mIndex));
            this.mHost.inactivateFragment(fragment.mWho);
            fragment.initState();
        }
    }

    void moveToState(int i, int i2, int i3, boolean z) {
        if (this.mHost == null && i != 0) {
            throw new IllegalStateException("No host");
        } else if (z || this.mCurState != i) {
            this.mCurState = i;
            if (this.mActive != null) {
                int i4 = 0;
                for (int i5 = 0; i5 < this.mActive.size(); i5++) {
                    Fragment fragment = (Fragment) this.mActive.get(i5);
                    if (fragment != null) {
                        moveToState(fragment, i, i2, i3, false);
                        if (fragment.mLoaderManager != null) {
                            i4 |= fragment.mLoaderManager.hasRunningLoaders();
                        }
                    }
                }
                if (i4 == 0) {
                    startPendingDeferredFragments();
                }
                if (this.mNeedMenuInvalidate && this.mHost != null && this.mCurState == 5) {
                    this.mHost.onSupportInvalidateOptionsMenu();
                    this.mNeedMenuInvalidate = false;
                }
            }
        }
    }

    void moveToState(int i, boolean z) {
        moveToState(i, 0, 0, z);
    }

    void moveToState(Fragment fragment) {
        moveToState(fragment, this.mCurState, 0, 0, false);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void moveToState(android.support.v4.app.Fragment r18, int r19, int r20, int r21, boolean r22) {
        /*
        r17 = this;
        r0 = r18;
        r4 = r0.mAdded;
        if (r4 == 0) goto L_0x000c;
    L_0x0006:
        r0 = r18;
        r4 = r0.mDetached;
        if (r4 == 0) goto L_0x0013;
    L_0x000c:
        r4 = 1;
        r0 = r19;
        if (r0 <= r4) goto L_0x0013;
    L_0x0011:
        r19 = 1;
    L_0x0013:
        r0 = r18;
        r4 = r0.mRemoving;
        if (r4 == 0) goto L_0x0027;
    L_0x0019:
        r0 = r18;
        r4 = r0.mState;
        r0 = r19;
        if (r0 <= r4) goto L_0x0027;
    L_0x0021:
        r0 = r18;
        r0 = r0.mState;
        r19 = r0;
    L_0x0027:
        r0 = r18;
        r4 = r0.mDeferStart;
        if (r4 == 0) goto L_0x003b;
    L_0x002d:
        r0 = r18;
        r4 = r0.mState;
        r5 = 4;
        if (r4 >= r5) goto L_0x003b;
    L_0x0034:
        r4 = 3;
        r0 = r19;
        if (r0 <= r4) goto L_0x003b;
    L_0x0039:
        r19 = 3;
    L_0x003b:
        r0 = r18;
        r4 = r0.mState;
        r0 = r19;
        if (r4 >= r0) goto L_0x0438;
    L_0x0043:
        r0 = r18;
        r4 = r0.mFromLayout;
        if (r4 == 0) goto L_0x004f;
    L_0x0049:
        r0 = r18;
        r4 = r0.mInLayout;
        if (r4 == 0) goto L_0x00bd;
    L_0x004f:
        r0 = r18;
        r4 = r0.mAnimatingAway;
        if (r4 == 0) goto L_0x0068;
    L_0x0055:
        r4 = 0;
        r0 = r18;
        r0.mAnimatingAway = r4;
        r0 = r18;
        r6 = r0.mStateAfterAnimating;
        r7 = 0;
        r8 = 0;
        r9 = 1;
        r4 = r17;
        r5 = r18;
        r4.moveToState(r5, r6, r7, r8, r9);
    L_0x0068:
        r0 = r18;
        r4 = r0.mState;
        switch(r4) {
            case 0: goto L_0x00be;
            case 1: goto L_0x022d;
            case 2: goto L_0x033c;
            case 3: goto L_0x0346;
            case 4: goto L_0x036e;
            default: goto L_0x006f;
        };
    L_0x006f:
        r0 = r18;
        r4 = r0.mState;
        r0 = r19;
        if (r4 == r0) goto L_0x00bc;
    L_0x0077:
        r4 = "FragmentManager";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "moveToState: Fragment state for ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r6 = " not updated inline; ";
        r5 = r5.append(r6);
        r6 = "expected state ";
        r5 = r5.append(r6);
        r0 = r19;
        r5 = r5.append(r0);
        r6 = " found ";
        r5 = r5.append(r6);
        r0 = r18;
        r6 = r0.mState;
        r5 = r5.append(r6);
        r5 = r5.toString();
        android.util.Log.w(r4, r5);
        r0 = r19;
        r1 = r18;
        r1.mState = r0;
    L_0x00bc:
        return;
    L_0x00bd:
        return;
    L_0x00be:
        r4 = DEBUG;
        if (r4 == 0) goto L_0x00de;
    L_0x00c2:
        r4 = "FragmentManager";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "moveto CREATED: ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r5 = r5.toString();
        android.util.Log.v(r4, r5);
    L_0x00de:
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        if (r4 == 0) goto L_0x014f;
    L_0x00e4:
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r0 = r17;
        r5 = r0.mHost;
        r5 = r5.getContext();
        r5 = r5.getClassLoader();
        r4.setClassLoader(r5);
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r5 = "android:view_state";
        r4 = r4.getSparseParcelableArray(r5);
        r0 = r18;
        r0.mSavedViewState = r4;
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r5 = "android:target_state";
        r0 = r17;
        r4 = r0.getFragment(r4, r5);
        r0 = r18;
        r0.mTarget = r4;
        r0 = r18;
        r4 = r0.mTarget;
        if (r4 == 0) goto L_0x012d;
    L_0x011d:
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r5 = "android:target_req_state";
        r6 = 0;
        r4 = r4.getInt(r5, r6);
        r0 = r18;
        r0.mTargetRequestCode = r4;
    L_0x012d:
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r5 = "android:user_visible_hint";
        r6 = 1;
        r4 = r4.getBoolean(r5, r6);
        r0 = r18;
        r0.mUserVisibleHint = r4;
        r0 = r18;
        r4 = r0.mUserVisibleHint;
        if (r4 != 0) goto L_0x014f;
    L_0x0143:
        r4 = 1;
        r0 = r18;
        r0.mDeferStart = r4;
        r4 = 3;
        r0 = r19;
        if (r0 <= r4) goto L_0x014f;
    L_0x014d:
        r19 = 3;
    L_0x014f:
        r0 = r17;
        r4 = r0.mHost;
        r0 = r18;
        r0.mHost = r4;
        r0 = r17;
        r4 = r0.mParent;
        r0 = r18;
        r0.mParentFragment = r4;
        r0 = r17;
        r4 = r0.mParent;
        if (r4 == 0) goto L_0x01aa;
    L_0x0165:
        r0 = r17;
        r4 = r0.mParent;
        r4 = r4.mChildFragmentManager;
    L_0x016b:
        r0 = r18;
        r0.mFragmentManager = r4;
        r4 = 0;
        r0 = r18;
        r0.mCalled = r4;
        r0 = r17;
        r4 = r0.mHost;
        r4 = r4.getContext();
        r0 = r18;
        r0.onAttach(r4);
        r0 = r18;
        r4 = r0.mCalled;
        if (r4 != 0) goto L_0x01b3;
    L_0x0187:
        r4 = new android.support.v4.app.SuperNotCalledException;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "Fragment ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r6 = " did not call through to super.onAttach()";
        r5 = r5.append(r6);
        r5 = r5.toString();
        r4.<init>(r5);
        throw r4;
    L_0x01aa:
        r0 = r17;
        r4 = r0.mHost;
        r4 = r4.getFragmentManagerImpl();
        goto L_0x016b;
    L_0x01b3:
        r0 = r18;
        r4 = r0.mParentFragment;
        if (r4 != 0) goto L_0x03a2;
    L_0x01b9:
        r0 = r17;
        r4 = r0.mHost;
        r0 = r18;
        r4.onAttachFragment(r0);
    L_0x01c2:
        r0 = r18;
        r4 = r0.mRetaining;
        if (r4 != 0) goto L_0x03ad;
    L_0x01c8:
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r0 = r18;
        r0.performCreate(r4);
    L_0x01d1:
        r4 = 0;
        r0 = r18;
        r0.mRetaining = r4;
        r0 = r18;
        r4 = r0.mFromLayout;
        if (r4 == 0) goto L_0x022d;
    L_0x01dc:
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r0 = r18;
        r4 = r0.getLayoutInflater(r4);
        r0 = r18;
        r5 = r0.mSavedFragmentState;
        r6 = 0;
        r0 = r18;
        r4 = r0.performCreateView(r4, r6, r5);
        r0 = r18;
        r0.mView = r4;
        r0 = r18;
        r4 = r0.mView;
        if (r4 == 0) goto L_0x03cb;
    L_0x01fb:
        r0 = r18;
        r4 = r0.mView;
        r0 = r18;
        r0.mInnerView = r4;
        r4 = android.os.Build.VERSION.SDK_INT;
        r5 = 11;
        if (r4 < r5) goto L_0x03bd;
    L_0x0209:
        r0 = r18;
        r4 = r0.mView;
        r5 = 0;
        android.support.v4.view.ViewCompat.setSaveFromParentEnabled(r4, r5);
    L_0x0211:
        r0 = r18;
        r4 = r0.mHidden;
        if (r4 == 0) goto L_0x0220;
    L_0x0217:
        r0 = r18;
        r4 = r0.mView;
        r5 = 8;
        r4.setVisibility(r5);
    L_0x0220:
        r0 = r18;
        r4 = r0.mView;
        r0 = r18;
        r5 = r0.mSavedFragmentState;
        r0 = r18;
        r0.onViewCreated(r4, r5);
    L_0x022d:
        r4 = 1;
        r0 = r19;
        if (r0 <= r4) goto L_0x033c;
    L_0x0232:
        r4 = DEBUG;
        if (r4 == 0) goto L_0x0252;
    L_0x0236:
        r4 = "FragmentManager";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "moveto ACTIVITY_CREATED: ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r5 = r5.toString();
        android.util.Log.v(r4, r5);
    L_0x0252:
        r0 = r18;
        r4 = r0.mFromLayout;
        if (r4 != 0) goto L_0x031f;
    L_0x0258:
        r11 = 0;
        r0 = r18;
        r4 = r0.mContainerId;
        if (r4 == 0) goto L_0x02a3;
    L_0x025f:
        r0 = r18;
        r4 = r0.mContainerId;
        r5 = -1;
        if (r4 != r5) goto L_0x028d;
    L_0x0266:
        r4 = new java.lang.IllegalArgumentException;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "Cannot create fragment ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r6 = " for a container view with no id";
        r5 = r5.append(r6);
        r5 = r5.toString();
        r4.<init>(r5);
        r0 = r17;
        r0.throwException(r4);
    L_0x028d:
        r0 = r17;
        r4 = r0.mContainer;
        r0 = r18;
        r5 = r0.mContainerId;
        r11 = r4.onFindViewById(r5);
        r11 = (android.view.ViewGroup) r11;
        if (r11 != 0) goto L_0x02a3;
    L_0x029d:
        r0 = r18;
        r4 = r0.mRestored;
        if (r4 == 0) goto L_0x03d2;
    L_0x02a3:
        r0 = r18;
        r0.mContainer = r11;
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r0 = r18;
        r4 = r0.getLayoutInflater(r4);
        r0 = r18;
        r5 = r0.mSavedFragmentState;
        r0 = r18;
        r4 = r0.performCreateView(r4, r11, r5);
        r0 = r18;
        r0.mView = r4;
        r0 = r18;
        r4 = r0.mView;
        if (r4 == 0) goto L_0x0431;
    L_0x02c5:
        r0 = r18;
        r4 = r0.mView;
        r0 = r18;
        r0.mInnerView = r4;
        r4 = android.os.Build.VERSION.SDK_INT;
        r5 = 11;
        if (r4 < r5) goto L_0x0423;
    L_0x02d3:
        r0 = r18;
        r4 = r0.mView;
        r5 = 0;
        android.support.v4.view.ViewCompat.setSaveFromParentEnabled(r4, r5);
    L_0x02db:
        if (r11 == 0) goto L_0x0303;
    L_0x02dd:
        r4 = 1;
        r0 = r17;
        r1 = r18;
        r2 = r20;
        r3 = r21;
        r10 = r0.loadAnimation(r1, r2, r4, r3);
        if (r10 == 0) goto L_0x02fc;
    L_0x02ec:
        r0 = r18;
        r4 = r0.mView;
        r0 = r17;
        r0.setHWLayerAnimListenerIfAlpha(r4, r10);
        r0 = r18;
        r4 = r0.mView;
        r4.startAnimation(r10);
    L_0x02fc:
        r0 = r18;
        r4 = r0.mView;
        r11.addView(r4);
    L_0x0303:
        r0 = r18;
        r4 = r0.mHidden;
        if (r4 == 0) goto L_0x0312;
    L_0x0309:
        r0 = r18;
        r4 = r0.mView;
        r5 = 8;
        r4.setVisibility(r5);
    L_0x0312:
        r0 = r18;
        r4 = r0.mView;
        r0 = r18;
        r5 = r0.mSavedFragmentState;
        r0 = r18;
        r0.onViewCreated(r4, r5);
    L_0x031f:
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r0 = r18;
        r0.performActivityCreated(r4);
        r0 = r18;
        r4 = r0.mView;
        if (r4 == 0) goto L_0x0337;
    L_0x032e:
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r0 = r18;
        r0.restoreViewState(r4);
    L_0x0337:
        r4 = 0;
        r0 = r18;
        r0.mSavedFragmentState = r4;
    L_0x033c:
        r4 = 2;
        r0 = r19;
        if (r0 <= r4) goto L_0x0346;
    L_0x0341:
        r4 = 3;
        r0 = r18;
        r0.mState = r4;
    L_0x0346:
        r4 = 3;
        r0 = r19;
        if (r0 <= r4) goto L_0x036e;
    L_0x034b:
        r4 = DEBUG;
        if (r4 == 0) goto L_0x036b;
    L_0x034f:
        r4 = "FragmentManager";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "moveto STARTED: ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r5 = r5.toString();
        android.util.Log.v(r4, r5);
    L_0x036b:
        r18.performStart();
    L_0x036e:
        r4 = 4;
        r0 = r19;
        if (r0 <= r4) goto L_0x006f;
    L_0x0373:
        r4 = DEBUG;
        if (r4 == 0) goto L_0x0393;
    L_0x0377:
        r4 = "FragmentManager";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "moveto RESUMED: ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r5 = r5.toString();
        android.util.Log.v(r4, r5);
    L_0x0393:
        r18.performResume();
        r4 = 0;
        r0 = r18;
        r0.mSavedFragmentState = r4;
        r4 = 0;
        r0 = r18;
        r0.mSavedViewState = r4;
        goto L_0x006f;
    L_0x03a2:
        r0 = r18;
        r4 = r0.mParentFragment;
        r0 = r18;
        r4.onAttachFragment(r0);
        goto L_0x01c2;
    L_0x03ad:
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r0 = r18;
        r0.restoreChildFragmentState(r4);
        r4 = 1;
        r0 = r18;
        r0.mState = r4;
        goto L_0x01d1;
    L_0x03bd:
        r0 = r18;
        r4 = r0.mView;
        r4 = android.support.v4.app.NoSaveStateFrameLayout.wrap(r4);
        r0 = r18;
        r0.mView = r4;
        goto L_0x0211;
    L_0x03cb:
        r4 = 0;
        r0 = r18;
        r0.mInnerView = r4;
        goto L_0x022d;
    L_0x03d2:
        r4 = r18.getResources();	 Catch:{ NotFoundException -> 0x041e }
        r0 = r18;
        r5 = r0.mContainerId;	 Catch:{ NotFoundException -> 0x041e }
        r14 = r4.getResourceName(r5);	 Catch:{ NotFoundException -> 0x041e }
    L_0x03de:
        r4 = new java.lang.IllegalArgumentException;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "No view found for id 0x";
        r5 = r5.append(r6);
        r0 = r18;
        r6 = r0.mContainerId;
        r6 = java.lang.Integer.toHexString(r6);
        r5 = r5.append(r6);
        r6 = " (";
        r5 = r5.append(r6);
        r5 = r5.append(r14);
        r6 = ") for fragment ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r5 = r5.toString();
        r4.<init>(r5);
        r0 = r17;
        r0.throwException(r4);
        goto L_0x02a3;
    L_0x041e:
        r12 = move-exception;
        r14 = "unknown";
        goto L_0x03de;
    L_0x0423:
        r0 = r18;
        r4 = r0.mView;
        r4 = android.support.v4.app.NoSaveStateFrameLayout.wrap(r4);
        r0 = r18;
        r0.mView = r4;
        goto L_0x02db;
    L_0x0431:
        r4 = 0;
        r0 = r18;
        r0.mInnerView = r4;
        goto L_0x031f;
    L_0x0438:
        r0 = r18;
        r4 = r0.mState;
        r0 = r19;
        if (r4 <= r0) goto L_0x006f;
    L_0x0440:
        r0 = r18;
        r4 = r0.mState;
        switch(r4) {
            case 1: goto L_0x0449;
            case 2: goto L_0x04ee;
            case 3: goto L_0x04c6;
            case 4: goto L_0x049e;
            case 5: goto L_0x0476;
            default: goto L_0x0447;
        };
    L_0x0447:
        goto L_0x006f;
    L_0x0449:
        r4 = 1;
        r0 = r19;
        if (r0 >= r4) goto L_0x006f;
    L_0x044e:
        r0 = r17;
        r4 = r0.mDestroyed;
        if (r4 == 0) goto L_0x0466;
    L_0x0454:
        r0 = r18;
        r4 = r0.mAnimatingAway;
        if (r4 == 0) goto L_0x0466;
    L_0x045a:
        r0 = r18;
        r15 = r0.mAnimatingAway;
        r4 = 0;
        r0 = r18;
        r0.mAnimatingAway = r4;
        r15.clearAnimation();
    L_0x0466:
        r0 = r18;
        r4 = r0.mAnimatingAway;
        if (r4 == 0) goto L_0x05a1;
    L_0x046c:
        r0 = r19;
        r1 = r18;
        r1.mStateAfterAnimating = r0;
        r19 = 1;
        goto L_0x006f;
    L_0x0476:
        r4 = 5;
        r0 = r19;
        if (r0 >= r4) goto L_0x049e;
    L_0x047b:
        r4 = DEBUG;
        if (r4 == 0) goto L_0x049b;
    L_0x047f:
        r4 = "FragmentManager";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "movefrom RESUMED: ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r5 = r5.toString();
        android.util.Log.v(r4, r5);
    L_0x049b:
        r18.performPause();
    L_0x049e:
        r4 = 4;
        r0 = r19;
        if (r0 >= r4) goto L_0x04c6;
    L_0x04a3:
        r4 = DEBUG;
        if (r4 == 0) goto L_0x04c3;
    L_0x04a7:
        r4 = "FragmentManager";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "movefrom STARTED: ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r5 = r5.toString();
        android.util.Log.v(r4, r5);
    L_0x04c3:
        r18.performStop();
    L_0x04c6:
        r4 = 3;
        r0 = r19;
        if (r0 >= r4) goto L_0x04ee;
    L_0x04cb:
        r4 = DEBUG;
        if (r4 == 0) goto L_0x04eb;
    L_0x04cf:
        r4 = "FragmentManager";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "movefrom STOPPED: ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r5 = r5.toString();
        android.util.Log.v(r4, r5);
    L_0x04eb:
        r18.performReallyStop();
    L_0x04ee:
        r4 = 2;
        r0 = r19;
        if (r0 >= r4) goto L_0x0449;
    L_0x04f3:
        r4 = DEBUG;
        if (r4 == 0) goto L_0x0513;
    L_0x04f7:
        r4 = "FragmentManager";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "movefrom ACTIVITY_CREATED: ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r5 = r5.toString();
        android.util.Log.v(r4, r5);
    L_0x0513:
        r0 = r18;
        r4 = r0.mView;
        if (r4 == 0) goto L_0x052e;
    L_0x0519:
        r0 = r17;
        r4 = r0.mHost;
        r0 = r18;
        r4 = r4.onShouldSaveFragmentState(r0);
        if (r4 == 0) goto L_0x052e;
    L_0x0525:
        r0 = r18;
        r4 = r0.mSavedViewState;
        if (r4 != 0) goto L_0x052e;
    L_0x052b:
        r17.saveFragmentViewState(r18);
    L_0x052e:
        r18.performDestroyView();
        r0 = r18;
        r4 = r0.mView;
        if (r4 == 0) goto L_0x0582;
    L_0x0537:
        r0 = r18;
        r4 = r0.mContainer;
        if (r4 == 0) goto L_0x0582;
    L_0x053d:
        r10 = 0;
        r0 = r17;
        r4 = r0.mCurState;
        if (r4 <= 0) goto L_0x054a;
    L_0x0544:
        r0 = r17;
        r4 = r0.mDestroyed;
        if (r4 == 0) goto L_0x0593;
    L_0x054a:
        if (r10 == 0) goto L_0x0577;
    L_0x054c:
        r13 = r18;
        r0 = r18;
        r4 = r0.mView;
        r0 = r18;
        r0.mAnimatingAway = r4;
        r0 = r19;
        r1 = r18;
        r1.mStateAfterAnimating = r0;
        r0 = r18;
        r0 = r0.mView;
        r16 = r0;
        r4 = new android.support.v4.app.FragmentManagerImpl$5;
        r0 = r17;
        r1 = r16;
        r2 = r18;
        r4.<init>(r1, r10, r2);
        r10.setAnimationListener(r4);
        r0 = r18;
        r4 = r0.mView;
        r4.startAnimation(r10);
    L_0x0577:
        r0 = r18;
        r4 = r0.mContainer;
        r0 = r18;
        r5 = r0.mView;
        r4.removeView(r5);
    L_0x0582:
        r4 = 0;
        r0 = r18;
        r0.mContainer = r4;
        r4 = 0;
        r0 = r18;
        r0.mView = r4;
        r4 = 0;
        r0 = r18;
        r0.mInnerView = r4;
        goto L_0x0449;
    L_0x0593:
        r4 = 0;
        r0 = r17;
        r1 = r18;
        r2 = r20;
        r3 = r21;
        r10 = r0.loadAnimation(r1, r2, r4, r3);
        goto L_0x054a;
    L_0x05a1:
        r4 = DEBUG;
        if (r4 == 0) goto L_0x05c1;
    L_0x05a5:
        r4 = "FragmentManager";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "movefrom CREATED: ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r5 = r5.toString();
        android.util.Log.v(r4, r5);
    L_0x05c1:
        r0 = r18;
        r4 = r0.mRetaining;
        if (r4 != 0) goto L_0x05da;
    L_0x05c7:
        r18.performDestroy();
    L_0x05ca:
        r18.performDetach();
        if (r22 != 0) goto L_0x006f;
    L_0x05cf:
        r0 = r18;
        r4 = r0.mRetaining;
        if (r4 != 0) goto L_0x05e0;
    L_0x05d5:
        r17.makeInactive(r18);
        goto L_0x006f;
    L_0x05da:
        r4 = 0;
        r0 = r18;
        r0.mState = r4;
        goto L_0x05ca;
    L_0x05e0:
        r4 = 0;
        r0 = r18;
        r0.mHost = r4;
        r4 = 0;
        r0 = r18;
        r0.mParentFragment = r4;
        r4 = 0;
        r0 = r18;
        r0.mFragmentManager = r4;
        goto L_0x006f;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.app.FragmentManagerImpl.moveToState(android.support.v4.app.Fragment, int, int, int, boolean):void");
    }

    public void noteStateNotSaved() {
        this.mStateSaved = false;
    }

    public View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        Fragment fragment = null;
        if (!"fragment".equals(str)) {
            return null;
        }
        String attributeValue = attributeSet.getAttributeValue(null, "class");
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, FragmentTag.Fragment);
        if (attributeValue == null) {
            attributeValue = obtainStyledAttributes.getString(0);
        }
        int resourceId = obtainStyledAttributes.getResourceId(1, -1);
        String string = obtainStyledAttributes.getString(2);
        obtainStyledAttributes.recycle();
        if (!Fragment.isSupportFragmentClass(this.mHost.getContext(), attributeValue)) {
            return null;
        }
        int id = view != null ? view.getId() : 0;
        if (id == -1 && resourceId == -1 && string == null) {
            throw new IllegalArgumentException(attributeSet.getPositionDescription() + ": Must specify unique android:id, android:tag, or have a parent with an id for " + attributeValue);
        }
        if (resourceId != -1) {
            fragment = findFragmentById(resourceId);
        }
        if (fragment == null && string != null) {
            fragment = findFragmentByTag(string);
        }
        if (fragment == null && id != -1) {
            fragment = findFragmentById(id);
        }
        if (DEBUG) {
            Log.v("FragmentManager", "onCreateView: id=0x" + Integer.toHexString(resourceId) + " fname=" + attributeValue + " existing=" + fragment);
        }
        if (fragment == null) {
            fragment = Fragment.instantiate(context, attributeValue);
            fragment.mFromLayout = true;
            fragment.mFragmentId = resourceId != 0 ? resourceId : id;
            fragment.mContainerId = id;
            fragment.mTag = string;
            fragment.mInLayout = true;
            fragment.mFragmentManager = this;
            fragment.mHost = this.mHost;
            fragment.onInflate(this.mHost.getContext(), attributeSet, fragment.mSavedFragmentState);
            addFragment(fragment, true);
        } else if (fragment.mInLayout) {
            throw new IllegalArgumentException(attributeSet.getPositionDescription() + ": Duplicate id 0x" + Integer.toHexString(resourceId) + ", tag " + string + ", or parent id 0x" + Integer.toHexString(id) + " with another fragment for " + attributeValue);
        } else {
            fragment.mInLayout = true;
            fragment.mHost = this.mHost;
            if (!fragment.mRetaining) {
                fragment.onInflate(this.mHost.getContext(), attributeSet, fragment.mSavedFragmentState);
            }
        }
        if (this.mCurState >= 1 || !fragment.mFromLayout) {
            moveToState(fragment);
        } else {
            moveToState(fragment, 1, 0, 0, false);
        }
        if (fragment.mView == null) {
            throw new IllegalStateException("Fragment " + attributeValue + " did not create a view.");
        }
        if (resourceId != 0) {
            fragment.mView.setId(resourceId);
        }
        if (fragment.mView.getTag() == null) {
            fragment.mView.setTag(string);
        }
        return fragment.mView;
    }

    public void performPendingDeferredStart(Fragment fragment) {
        if (fragment.mDeferStart) {
            if (this.mExecutingActions) {
                this.mHavePendingDeferredStart = true;
                return;
            }
            fragment.mDeferStart = false;
            moveToState(fragment, this.mCurState, 0, 0, false);
        }
    }

    public boolean popBackStackImmediate() {
        checkStateLoss();
        executePendingTransactions();
        return popBackStackState(this.mHost.getHandler(), null, -1, 0);
    }

    boolean popBackStackState(Handler handler, String str, int i, int i2) {
        if (this.mBackStack == null) {
            return false;
        }
        BackStackRecord backStackRecord;
        SparseArray sparseArray;
        SparseArray sparseArray2;
        if (str == null && i < 0 && (i2 & 1) == 0) {
            int size = this.mBackStack.size() - 1;
            if (size < 0) {
                return false;
            }
            backStackRecord = (BackStackRecord) this.mBackStack.remove(size);
            sparseArray = new SparseArray();
            sparseArray2 = new SparseArray();
            if (this.mCurState >= 1) {
                backStackRecord.calculateBackFragments(sparseArray, sparseArray2);
            }
            backStackRecord.popFromBackStack(true, null, sparseArray, sparseArray2);
            reportBackStackChanged();
        } else {
            int i3 = -1;
            if (str != null || i >= 0) {
                i3 = this.mBackStack.size() - 1;
                while (i3 >= 0) {
                    backStackRecord = (BackStackRecord) this.mBackStack.get(i3);
                    if ((str != null && str.equals(backStackRecord.getName())) || (i >= 0 && i == backStackRecord.mIndex)) {
                        break;
                    }
                    i3--;
                }
                if (i3 < 0) {
                    return false;
                }
                if ((i2 & 1) != 0) {
                    i3--;
                    while (i3 >= 0) {
                        backStackRecord = (BackStackRecord) this.mBackStack.get(i3);
                        if ((str == null || !str.equals(backStackRecord.getName())) && (i < 0 || i != backStackRecord.mIndex)) {
                            break;
                        }
                        i3--;
                    }
                }
            }
            if (i3 == this.mBackStack.size() - 1) {
                return false;
            }
            int size2;
            ArrayList arrayList = new ArrayList();
            for (size2 = this.mBackStack.size() - 1; size2 > i3; size2--) {
                arrayList.add((BackStackRecord) this.mBackStack.remove(size2));
            }
            int size3 = arrayList.size() - 1;
            sparseArray = new SparseArray();
            sparseArray2 = new SparseArray();
            if (this.mCurState >= 1) {
                for (size2 = 0; size2 <= size3; size2++) {
                    ((BackStackRecord) arrayList.get(size2)).calculateBackFragments(sparseArray, sparseArray2);
                }
            }
            TransitionState transitionState = null;
            size2 = 0;
            while (size2 <= size3) {
                if (DEBUG) {
                    Log.v("FragmentManager", "Popping back stack state: " + arrayList.get(size2));
                }
                transitionState = ((BackStackRecord) arrayList.get(size2)).popFromBackStack(size2 == size3, transitionState, sparseArray, sparseArray2);
                size2++;
            }
            reportBackStackChanged();
        }
        return true;
    }

    public void putFragment(Bundle bundle, String str, Fragment fragment) {
        if (fragment.mIndex < 0) {
            throwException(new IllegalStateException("Fragment " + fragment + " is not currently in the FragmentManager"));
        }
        bundle.putInt(str, fragment.mIndex);
    }

    public void removeFragment(Fragment fragment, int i, int i2) {
        int i3 = 1;
        if (DEBUG) {
            Log.v("FragmentManager", "remove: " + fragment + " nesting=" + fragment.mBackStackNesting);
        }
        Object obj = fragment.isInBackStack() ? null : 1;
        if (!fragment.mDetached || obj != null) {
            if (this.mAdded != null) {
                this.mAdded.remove(fragment);
            }
            if (fragment.mHasMenu && fragment.mMenuVisible) {
                this.mNeedMenuInvalidate = true;
            }
            fragment.mAdded = false;
            fragment.mRemoving = true;
            if (obj != null) {
                i3 = 0;
            }
            moveToState(fragment, i3, i, i2, false);
        }
    }

    void reportBackStackChanged() {
        if (this.mBackStackChangeListeners != null) {
            for (int i = 0; i < this.mBackStackChangeListeners.size(); i++) {
                ((OnBackStackChangedListener) this.mBackStackChangeListeners.get(i)).onBackStackChanged();
            }
        }
    }

    void restoreAllState(Parcelable parcelable, FragmentManagerNonConfig fragmentManagerNonConfig) {
        if (parcelable != null) {
            FragmentManagerState fragmentManagerState = (FragmentManagerState) parcelable;
            if (fragmentManagerState.mActive != null) {
                List fragments;
                int size;
                int i;
                Fragment fragment;
                FragmentState fragmentState;
                List list = null;
                if (fragmentManagerNonConfig != null) {
                    fragments = fragmentManagerNonConfig.getFragments();
                    list = fragmentManagerNonConfig.getChildNonConfigs();
                    size = fragments != null ? fragments.size() : 0;
                    for (i = 0; i < size; i++) {
                        fragment = (Fragment) fragments.get(i);
                        if (DEBUG) {
                            Log.v("FragmentManager", "restoreAllState: re-attaching retained " + fragment);
                        }
                        fragmentState = fragmentManagerState.mActive[fragment.mIndex];
                        fragmentState.mInstance = fragment;
                        fragment.mSavedViewState = null;
                        fragment.mBackStackNesting = 0;
                        fragment.mInLayout = false;
                        fragment.mAdded = false;
                        fragment.mTarget = null;
                        if (fragmentState.mSavedFragmentState != null) {
                            fragmentState.mSavedFragmentState.setClassLoader(this.mHost.getContext().getClassLoader());
                            fragment.mSavedViewState = fragmentState.mSavedFragmentState.getSparseParcelableArray("android:view_state");
                            fragment.mSavedFragmentState = fragmentState.mSavedFragmentState;
                        }
                    }
                }
                this.mActive = new ArrayList(fragmentManagerState.mActive.length);
                if (this.mAvailIndices != null) {
                    this.mAvailIndices.clear();
                }
                i = 0;
                while (i < fragmentManagerState.mActive.length) {
                    fragmentState = fragmentManagerState.mActive[i];
                    if (fragmentState != null) {
                        FragmentManagerNonConfig fragmentManagerNonConfig2 = null;
                        if (list != null && i < list.size()) {
                            fragmentManagerNonConfig2 = (FragmentManagerNonConfig) list.get(i);
                        }
                        fragment = fragmentState.instantiate(this.mHost, this.mParent, fragmentManagerNonConfig2);
                        if (DEBUG) {
                            Log.v("FragmentManager", "restoreAllState: active #" + i + ": " + fragment);
                        }
                        this.mActive.add(fragment);
                        fragmentState.mInstance = null;
                    } else {
                        this.mActive.add(null);
                        if (this.mAvailIndices == null) {
                            this.mAvailIndices = new ArrayList();
                        }
                        if (DEBUG) {
                            Log.v("FragmentManager", "restoreAllState: avail #" + i);
                        }
                        this.mAvailIndices.add(Integer.valueOf(i));
                    }
                    i++;
                }
                if (fragmentManagerNonConfig != null) {
                    fragments = fragmentManagerNonConfig.getFragments();
                    size = fragments != null ? fragments.size() : 0;
                    for (i = 0; i < size; i++) {
                        fragment = (Fragment) fragments.get(i);
                        if (fragment.mTargetIndex >= 0) {
                            if (fragment.mTargetIndex < this.mActive.size()) {
                                fragment.mTarget = (Fragment) this.mActive.get(fragment.mTargetIndex);
                            } else {
                                Log.w("FragmentManager", "Re-attaching retained fragment " + fragment + " target no longer exists: " + fragment.mTargetIndex);
                                fragment.mTarget = null;
                            }
                        }
                    }
                }
                if (fragmentManagerState.mAdded != null) {
                    this.mAdded = new ArrayList(fragmentManagerState.mAdded.length);
                    for (i = 0; i < fragmentManagerState.mAdded.length; i++) {
                        fragment = (Fragment) this.mActive.get(fragmentManagerState.mAdded[i]);
                        if (fragment == null) {
                            throwException(new IllegalStateException("No instantiated fragment for index #" + fragmentManagerState.mAdded[i]));
                        }
                        fragment.mAdded = true;
                        if (DEBUG) {
                            Log.v("FragmentManager", "restoreAllState: added #" + i + ": " + fragment);
                        }
                        if (this.mAdded.contains(fragment)) {
                            throw new IllegalStateException("Already added!");
                        }
                        this.mAdded.add(fragment);
                    }
                } else {
                    this.mAdded = null;
                }
                if (fragmentManagerState.mBackStack != null) {
                    this.mBackStack = new ArrayList(fragmentManagerState.mBackStack.length);
                    for (i = 0; i < fragmentManagerState.mBackStack.length; i++) {
                        BackStackRecord instantiate = fragmentManagerState.mBackStack[i].instantiate(this);
                        if (DEBUG) {
                            Log.v("FragmentManager", "restoreAllState: back stack #" + i + " (index " + instantiate.mIndex + "): " + instantiate);
                            instantiate.dump("  ", new PrintWriter(new LogWriter("FragmentManager")), false);
                        }
                        this.mBackStack.add(instantiate);
                        if (instantiate.mIndex >= 0) {
                            setBackStackIndex(instantiate.mIndex, instantiate);
                        }
                    }
                } else {
                    this.mBackStack = null;
                }
            }
        }
    }

    FragmentManagerNonConfig retainNonConfig() {
        List list = null;
        List list2 = null;
        if (this.mActive != null) {
            for (int i = 0; i < this.mActive.size(); i++) {
                Fragment fragment = (Fragment) this.mActive.get(i);
                if (fragment != null) {
                    if (fragment.mRetainInstance) {
                        if (list == null) {
                            list = new ArrayList();
                        }
                        list.add(fragment);
                        fragment.mRetaining = true;
                        fragment.mTargetIndex = fragment.mTarget != null ? fragment.mTarget.mIndex : -1;
                        if (DEBUG) {
                            Log.v("FragmentManager", "retainNonConfig: keeping retained " + fragment);
                        }
                    }
                    Object obj = null;
                    if (fragment.mChildFragmentManager != null) {
                        FragmentManagerNonConfig retainNonConfig = fragment.mChildFragmentManager.retainNonConfig();
                        if (retainNonConfig != null) {
                            if (list2 == null) {
                                list2 = new ArrayList();
                                for (int i2 = 0; i2 < i; i2++) {
                                    list2.add(null);
                                }
                            }
                            list2.add(retainNonConfig);
                            obj = 1;
                        }
                    }
                    if (list2 != null && r0 == null) {
                        list2.add(null);
                    }
                }
            }
        }
        return (list == null && list2 == null) ? null : new FragmentManagerNonConfig(list, list2);
    }

    Parcelable saveAllState() {
        execPendingActions();
        if (HONEYCOMB) {
            this.mStateSaved = true;
        }
        if (this.mActive == null || this.mActive.size() <= 0) {
            return null;
        }
        int i;
        int size = this.mActive.size();
        FragmentState[] fragmentStateArr = new FragmentState[size];
        Object obj = null;
        for (i = 0; i < size; i++) {
            Fragment fragment = (Fragment) this.mActive.get(i);
            if (fragment != null) {
                if (fragment.mIndex < 0) {
                    throwException(new IllegalStateException("Failure saving state: active " + fragment + " has cleared index: " + fragment.mIndex));
                }
                obj = 1;
                FragmentState fragmentState = new FragmentState(fragment);
                fragmentStateArr[i] = fragmentState;
                if (fragment.mState <= 0 || fragmentState.mSavedFragmentState != null) {
                    fragmentState.mSavedFragmentState = fragment.mSavedFragmentState;
                } else {
                    fragmentState.mSavedFragmentState = saveFragmentBasicState(fragment);
                    if (fragment.mTarget != null) {
                        if (fragment.mTarget.mIndex < 0) {
                            throwException(new IllegalStateException("Failure saving state: " + fragment + " has target not in fragment manager: " + fragment.mTarget));
                        }
                        if (fragmentState.mSavedFragmentState == null) {
                            fragmentState.mSavedFragmentState = new Bundle();
                        }
                        putFragment(fragmentState.mSavedFragmentState, "android:target_state", fragment.mTarget);
                        if (fragment.mTargetRequestCode != 0) {
                            fragmentState.mSavedFragmentState.putInt("android:target_req_state", fragment.mTargetRequestCode);
                        }
                    }
                }
                if (DEBUG) {
                    Log.v("FragmentManager", "Saved state of " + fragment + ": " + fragmentState.mSavedFragmentState);
                }
            }
        }
        if (obj == null) {
            if (DEBUG) {
                Log.v("FragmentManager", "saveAllState: no fragments!");
            }
            return null;
        }
        int[] iArr = null;
        BackStackState[] backStackStateArr = null;
        if (this.mAdded != null) {
            size = this.mAdded.size();
            if (size > 0) {
                iArr = new int[size];
                for (i = 0; i < size; i++) {
                    iArr[i] = ((Fragment) this.mAdded.get(i)).mIndex;
                    if (iArr[i] < 0) {
                        throwException(new IllegalStateException("Failure saving state: active " + this.mAdded.get(i) + " has cleared index: " + iArr[i]));
                    }
                    if (DEBUG) {
                        Log.v("FragmentManager", "saveAllState: adding fragment #" + i + ": " + this.mAdded.get(i));
                    }
                }
            }
        }
        if (this.mBackStack != null) {
            size = this.mBackStack.size();
            if (size > 0) {
                backStackStateArr = new BackStackState[size];
                for (i = 0; i < size; i++) {
                    backStackStateArr[i] = new BackStackState((BackStackRecord) this.mBackStack.get(i));
                    if (DEBUG) {
                        Log.v("FragmentManager", "saveAllState: adding back stack #" + i + ": " + this.mBackStack.get(i));
                    }
                }
            }
        }
        Parcelable fragmentManagerState = new FragmentManagerState();
        fragmentManagerState.mActive = fragmentStateArr;
        fragmentManagerState.mAdded = iArr;
        fragmentManagerState.mBackStack = backStackStateArr;
        return fragmentManagerState;
    }

    Bundle saveFragmentBasicState(Fragment fragment) {
        Bundle bundle = null;
        if (this.mStateBundle == null) {
            this.mStateBundle = new Bundle();
        }
        fragment.performSaveInstanceState(this.mStateBundle);
        if (!this.mStateBundle.isEmpty()) {
            bundle = this.mStateBundle;
            this.mStateBundle = null;
        }
        if (fragment.mView != null) {
            saveFragmentViewState(fragment);
        }
        if (fragment.mSavedViewState != null) {
            if (bundle == null) {
                bundle = new Bundle();
            }
            bundle.putSparseParcelableArray("android:view_state", fragment.mSavedViewState);
        }
        if (!fragment.mUserVisibleHint) {
            if (bundle == null) {
                bundle = new Bundle();
            }
            bundle.putBoolean("android:user_visible_hint", fragment.mUserVisibleHint);
        }
        return bundle;
    }

    void saveFragmentViewState(Fragment fragment) {
        if (fragment.mInnerView != null) {
            if (this.mStateArray == null) {
                this.mStateArray = new SparseArray();
            } else {
                this.mStateArray.clear();
            }
            fragment.mInnerView.saveHierarchyState(this.mStateArray);
            if (this.mStateArray.size() > 0) {
                fragment.mSavedViewState = this.mStateArray;
                this.mStateArray = null;
            }
        }
    }

    public void setBackStackIndex(int i, BackStackRecord backStackRecord) {
        synchronized (this) {
            if (this.mBackStackIndices == null) {
                this.mBackStackIndices = new ArrayList();
            }
            int size = this.mBackStackIndices.size();
            if (i < size) {
                if (DEBUG) {
                    Log.v("FragmentManager", "Setting back stack index " + i + " to " + backStackRecord);
                }
                this.mBackStackIndices.set(i, backStackRecord);
            } else {
                while (size < i) {
                    this.mBackStackIndices.add(null);
                    if (this.mAvailBackStackIndices == null) {
                        this.mAvailBackStackIndices = new ArrayList();
                    }
                    if (DEBUG) {
                        Log.v("FragmentManager", "Adding available back stack index " + size);
                    }
                    this.mAvailBackStackIndices.add(Integer.valueOf(size));
                    size++;
                }
                if (DEBUG) {
                    Log.v("FragmentManager", "Adding back stack index " + i + " with " + backStackRecord);
                }
                this.mBackStackIndices.add(backStackRecord);
            }
        }
    }

    public void showFragment(Fragment fragment, int i, int i2) {
        if (DEBUG) {
            Log.v("FragmentManager", "show: " + fragment);
        }
        if (fragment.mHidden) {
            fragment.mHidden = false;
            if (fragment.mView != null) {
                Animation loadAnimation = loadAnimation(fragment, i, true, i2);
                if (loadAnimation != null) {
                    setHWLayerAnimListenerIfAlpha(fragment.mView, loadAnimation);
                    fragment.mView.startAnimation(loadAnimation);
                }
                fragment.mView.setVisibility(0);
            }
            if (fragment.mAdded && fragment.mHasMenu && fragment.mMenuVisible) {
                this.mNeedMenuInvalidate = true;
            }
            fragment.onHiddenChanged(false);
        }
    }

    void startPendingDeferredFragments() {
        if (this.mActive != null) {
            for (int i = 0; i < this.mActive.size(); i++) {
                Fragment fragment = (Fragment) this.mActive.get(i);
                if (fragment != null) {
                    performPendingDeferredStart(fragment);
                }
            }
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(128);
        stringBuilder.append("FragmentManager{");
        stringBuilder.append(Integer.toHexString(System.identityHashCode(this)));
        stringBuilder.append(" in ");
        if (this.mParent != null) {
            DebugUtils.buildShortClassTag(this.mParent, stringBuilder);
        } else {
            DebugUtils.buildShortClassTag(this.mHost, stringBuilder);
        }
        stringBuilder.append("}}");
        return stringBuilder.toString();
    }
}
