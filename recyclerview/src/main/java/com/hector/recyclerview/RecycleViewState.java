package com.hector.recyclerview;

/**
 * Created by Hector on 15/12/8.
 */
public interface RecycleViewState {

    public final static int HEADER_STATE_NORMAL = 0;
    public final static int HEADER_STATE_ON_MOVE = 1;
    public final static int HEADER_STATE_REFRESHING = 2;
    public final static int HEADER_STATE_COMPLETE = 3;

    public final static int FOOTER_STATE_NORMAL = 0;
    public final static int FOOTER_STATE_LOADING = 1;
    public final static int FOOTER_STATE_COMPLETE = 2;
    public final static int FOOTER_STATE_NO_MORE = 3;
}
