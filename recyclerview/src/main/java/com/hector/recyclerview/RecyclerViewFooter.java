package com.hector.recyclerview;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Hector on 15/12/9.
 */
public class RecyclerViewFooter extends LinearLayout {

    private View mContainer;
    private TextView mFooterTv;
    private ProgressBar mProgressBar;

    public RecyclerViewFooter(Context context) {
        super(context);
        init(context);
    }

    public RecyclerViewFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RecyclerViewFooter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContainer = LayoutInflater.from(context).inflate(R.layout.footer_recycler_view, null, false);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 0);
        this.setLayoutParams(lp);
        this.setPadding(0, 0, 0, 0);
        addView(mContainer, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mFooterTv = (TextView) mContainer.findViewById(R.id.footer_text);
        mProgressBar = (ProgressBar) mContainer.findViewById(R.id.footer_pb);
    }

    private void setState(int state) {
        switch (state) {
            case RecycleViewState.FOOTER_STATE_NORMAL:
                mFooterTv.setText(getResources().getString(R.string.footer_state_normal));
                break;
            case RecycleViewState.FOOTER_STATE_LOADING:
                mFooterTv.setText(getResources().getString(R.string.footer_state_loading));
                break;
            case RecycleViewState.FOOTER_STATE_COMPLETE:
                mFooterTv.setText(getResources().getString(R.string.footer_state_normal));
                break;
            case RecycleViewState.FOOTER_STATE_NO_MORE:
                mFooterTv.setText(getResources().getString(R.string.footer_state_no_more));
                break;
        }
    }

    public void complete() {
        mProgressBar.setVisibility(GONE);
        setState(RecycleViewState.FOOTER_STATE_COMPLETE);
    }

    public void loading() {
        mProgressBar.setVisibility(VISIBLE);
        setState(RecycleViewState.FOOTER_STATE_LOADING);
    }

    public void noMore() {
        mProgressBar.setVisibility(GONE);
        setState(RecycleViewState.FOOTER_STATE_NO_MORE);
    }

}
