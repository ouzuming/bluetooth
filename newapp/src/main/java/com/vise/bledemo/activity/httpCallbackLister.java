package com.vise.bledemo.activity;

public interface HttpCallbackLister {
    void success(String responseData);
    void failure();
}
