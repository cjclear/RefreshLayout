package com.cj.refreshlib;

public interface LoadInterface {
    public void onLoadStart();
    public void onLoadFinish();
    public void onLoadCancel();
    public void onLoadMoreBegin();
    public void onOffsetChanged(int offset);
}
