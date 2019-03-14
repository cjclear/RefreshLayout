package com.cj.refreshlib;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.Scroller;


public class RefreshLayout extends FrameLayout {
    private int offset, loadOffset,offsetCount,lastCount;
    private Scroller scroller;
    private LoadInterface loadInterface;
    private boolean isLoading = false,isDisabled = false ;
    private float oldx,oldy;

    public void setLoadInterface(LoadInterface loadInterface) {
        this.loadInterface = loadInterface;
    }

    public void enableLoadMore(final int offsetCount){
        RecyclerView view = null;
        if(getChildCount() > 0 && getChildAt(getChildCount() == 2?1:0) instanceof RecyclerView){
            view = (RecyclerView) getChildAt(getChildCount() == 2?1:0);
        }
        if(view!=null){
            final RecyclerView.LayoutManager manager = view.getLayoutManager();
            this.offsetCount = Math.max(0,offsetCount);
            view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastVisiblePosition = 0;
                    if(manager instanceof LinearLayoutManager){
                        lastVisiblePosition = ((LinearLayoutManager)manager).findLastVisibleItemPosition();
                    }
                    if(manager instanceof GridLayoutManager){
                        lastVisiblePosition = ((GridLayoutManager)manager).findLastVisibleItemPosition();
                    }
                    if(lastVisiblePosition >= recyclerView.getAdapter().getItemCount() -  RefreshLayout.this.offsetCount - 1 && recyclerView.getAdapter().getItemCount() > lastCount){
                        lastCount = recyclerView.getAdapter().getItemCount()+1;
                        if(loadInterface!=null){
                            loadInterface.onLoadMoreBegin();
                        }
                    }

                }
            });
        }


    }


    public RefreshLayout(Context context) {
        super(context);
        init();
    }

    public int dip2px(Context context, float dpValue) {
        if (context != null) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        } else {
            return 0;
        }
    }

    public RefreshLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RefreshLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void init() {
        loadOffset = dip2px(getContext(), 100);
    }

    public int getLoadOffset() {
        return loadOffset;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void disableRefresh(){
        isDisabled = true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() == 2) {
            View head = getChildAt(0);
            View list = getChildAt(1);
            if (offset < 0) {
                offset = 0;
            }
            if (offset == 0) {
                if (loadInterface != null) {
                    loadInterface.onLoadFinish();
                }
            }
            if (loadInterface != null) {
                loadInterface.onOffsetChanged(offset);
            }
            head.layout(l, t + offset - head.getMeasuredHeight(), r, t + offset);
            list.layout(l, t + offset, r, b);
        } else {
            super.onLayout(changed, l, t, r, b);
        }
    }

    boolean isMoving = false;

    public void setOffset(int offset) {
        this.offset = offset;
        requestLayout();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (getChildCount() == 2 && !isDisabled) {
            View view =  getChildAt(1);
            switch (event.getAction()) {
                case MotionEvent.ACTION_CANCEL:

                    break;
                case MotionEvent.ACTION_DOWN:
                    oldx = event.getX();
                    oldy = event.getY();
                    removeCallbacks(runnable);
                    stopScroll();
                    dispatchTouchEventSuper(event);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) (event.getX() - oldx);
                    int dy = (int) (event.getY() - oldy);
                    oldx = event.getX();
                    oldy = event.getY();
                    if(Math.abs(dx) > Math.abs(dy)){
                        return dispatchTouchEventSuper(event);
                    }
                    boolean isMoveDown = dy > 0;
                    if (isMoveDown && canChildScrollUp(view)) {
                        return dispatchTouchEventSuper(event);
                    }
                    if (!isMoveDown && offset == 0) {
                        return dispatchTouchEventSuper(event);
                    }
                    if (isMoveDown) {
                        float dy2 = offset / (getHeight() * 0.7f + 0.00f);
                        if (dy2 > 1) {
                            dy2 = 1;
                        }
                        offset = (int) (offset + dy * (1 - dy2));
                    } else {
                        offset = offset + dy;
                    }
                    if (offset >= 0) {
                        event.setAction(MotionEvent.ACTION_CANCEL);
                        dispatchTouchEventSuper(event);
                        requestLayout();
                        return true;
                    } else {
                        offset = 0;
                        requestLayout();
                        event.setAction(MotionEvent.ACTION_DOWN);
                        return dispatchTouchEventSuper(event);
                    }

                case MotionEvent.ACTION_UP:
                    if (offset != 0) {
                        if (offset > loadOffset) {
                            offset = loadOffset;
                            isMoving = false;
                            requestLayout();
                            if (isLoading) {
                                if (loadInterface != null) {
                                    loadInterface.onLoadCancel();
                                }
                            }

                            if (loadInterface != null) {
                                isLoading = true;
                                loadInterface.onLoadStart();
                            }
                        } else {
                            isMoving = false;
                            startScroll(offset, -offset, (int) (400 * offset / (loadOffset + 0.00f)));
                        }
                        return true;
                    }
                    return dispatchTouchEventSuper(event);
            }
        }
        return dispatchTouchEventSuper(event);
    }

    public static boolean canChildScrollUp(View view) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return view.getScrollY() > 0;
            }
        } else {
            return view.canScrollVertically(-1);
        }
    }

    public boolean dispatchTouchEventSuper(MotionEvent event) {


        return super.dispatchTouchEvent(event);
    }


    public void dataGetted() {
        isLoading = false;
        lastCount = 0;
        if (offset == loadOffset) {
            startScroll(loadOffset, -loadOffset, 400);
        }

    }

    public void setLoadOffset(int loadOffset) {
        this.loadOffset = Math.min(loadOffset,getResources().getDisplayMetrics().heightPixels/2);

    }

    public void stopScroll() {
        if (scroller != null && !scroller.isFinished()) {
            scroller.forceFinished(true);
        }
    }

    public void startScroll(int startY, int dy, int duration) {
        if (scroller == null) {
            scroller = new Scroller(getContext());
        }
        if (!scroller.isFinished()) {
            scroller.forceFinished(true);
        }
        scroller.startScroll(0, startY, 0, dy, duration);
        removeCallbacks(runnable);
        post(runnable);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            boolean finish = scroller.isFinished();
            scroller.computeScrollOffset();
            if (!finish) {
                offset = scroller.getCurrY();
                requestLayout();
                post(runnable);
            }
        }
    };


}
