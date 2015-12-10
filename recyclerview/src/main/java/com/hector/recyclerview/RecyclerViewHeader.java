package com.hector.recyclerview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Date;

public class RecyclerViewHeader extends LinearLayout {

    private LinearLayout mContainer;
    private TextView mViewStateTv, mUpdateTimeTv;
    private ImageView mIndicatorIv;
    private ProgressBar mHeaderProgressBar;
    private int mState;
    private int mMeasureHeight;
    private Animation arrowUpAnimation;

    public RecyclerViewHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public RecyclerViewHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RecyclerViewHeader(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mContainer = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.header_recycler_view, null);
        mViewStateTv = (TextView) mContainer.findViewById(R.id.header_tv_state);
        mUpdateTimeTv = (TextView) mContainer.findViewById(R.id.header_tv_time);
        mIndicatorIv = (ImageView) mContainer.findViewById(R.id.header_iv_indicator);
        mHeaderProgressBar = (ProgressBar) mContainer.findViewById(R.id.header_pb);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 0);
        this.setLayoutParams(lp);
        this.setPadding(0, 0, 0, 0);
        addView(mContainer, new LayoutParams(LayoutParams.MATCH_PARENT, 0));
        setGravity(Gravity.BOTTOM);
        measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mMeasureHeight = getMeasuredHeight();
        arrowUpAnimation = new RotateAnimation(0.0f, -180.f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        arrowUpAnimation.setDuration(200);
        arrowUpAnimation.setFillAfter(true);
    }

    private void setState(int state) {
        switch (state) {
            case RecycleViewState.HEADER_STATE_NORMAL:
                mIndicatorIv.setImageResource(R.drawable.arrow);
                mViewStateTv.setText(getResources().getString(R.string.state_normal));
                break;
            case RecycleViewState.HEADER_STATE_ON_MOVE:
                if (mState == RecycleViewState.HEADER_STATE_NORMAL) {
                    mIndicatorIv.startAnimation(arrowUpAnimation);
                }
                mViewStateTv.setText(getResources().getString(R.string.state_on_move));
                break;
            case RecycleViewState.HEADER_STATE_REFRESHING:
                mIndicatorIv.clearAnimation();
                mIndicatorIv.setVisibility(INVISIBLE);
                mHeaderProgressBar.setVisibility(VISIBLE);
                mViewStateTv.setText(getResources().getString(R.string.state_refreshing));
                break;
            case RecycleViewState.HEADER_STATE_COMPLETE:
                mHeaderProgressBar.setVisibility(GONE);
                mIndicatorIv.setVisibility(VISIBLE);
                mIndicatorIv.setImageResource(R.drawable.complete);
                mViewStateTv.setText(getResources().getString(R.string.state_complete));
                break;
        }
        mState = state;
    }

    public void setVisibleHeight(int height) {
        if (height < 0) {
            height = 0;
        }
        LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
        lp.height = height;
        mContainer.setLayoutParams(lp);
    }

    public int getVisibleHeight() {
        LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
        return lp.height;
    }

    public long getLastUpdateTime() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("recycler_view", Context.MODE_PRIVATE);
        return sharedPreferences.getLong("lastUpdateTime", System.currentTimeMillis());
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("recycler_view", Context.MODE_PRIVATE);
        sharedPreferences.edit().putLong("lastUpdateTime", lastUpdateTime.getTime()).commit();
    }

    public void smoothScrollTo(int destHeight) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(getVisibleHeight(), destHeight);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setVisibleHeight((Integer) animation.getAnimatedValue());
            }
        });
        valueAnimator.start();
    }

    public void autoDisplay() {
        smoothScrollTo(mMeasureHeight);
    }

    public void resetHeader() {
        smoothScrollTo(0);
        setState(RecycleViewState.HEADER_STATE_NORMAL);
    }

    public boolean onMove(float delta) {
        mUpdateTimeTv.setText(friendlyTime(getLastUpdateTime()));
        if ((getVisibleHeight() >= 0 || delta > 0)) {
            setVisibleHeight((int) delta + getVisibleHeight());
            if (mState <= RecycleViewState.HEADER_STATE_ON_MOVE) {
                if (getVisibleHeight() > mMeasureHeight) {
                    setState(RecycleViewState.HEADER_STATE_ON_MOVE);
                    return true;
                } else {
                    setState(RecycleViewState.HEADER_STATE_NORMAL);
                    return false;
                }
            }
        }
        return true;
    }

    public void release() {
        smoothScrollTo(mMeasureHeight);
        setState(RecycleViewState.HEADER_STATE_REFRESHING);
    }

    public void complete() {
        setState(RecycleViewState.HEADER_STATE_COMPLETE);
        setLastUpdateTime(new Date());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                resetHeader();
            }
        }, 1000);
    }

    public String friendlyTime(long time) {
        int ct = (int) ((System.currentTimeMillis() - time) / 1000);
        if (ct == 0) {
            return getResources().getString(R.string.time_just_now);
        }
        if (ct > 0 && ct < 60) {
            return ct + getResources().getString(R.string.time_seconds_ago);
        }
        if (ct >= 60 && ct < 3600) {
            return Math.max(ct / 60, 1) + getResources().getString(R.string.time_minutes_ago);
        }
        if (ct >= 3600 && ct < 86400)
            return ct / 3600 + getResources().getString(R.string.time_hours_ago);
        if (ct >= 86400 && ct < 2592000) {
            int day = ct / 86400;
            return day + getResources().getString(R.string.time_days_ago);
        }
        if (ct >= 2592000 && ct < 31104000) {
            return ct / 2592000 + getResources().getString(R.string.time_months_ago);
        }
        return ct / 31104000 + getResources().getString(R.string.time_years_ago);
    }
}
