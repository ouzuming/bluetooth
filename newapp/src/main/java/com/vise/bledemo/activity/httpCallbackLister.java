package com.vise.bledemo.activity;

import java.io.IOException;

public interface HttpCallbackLister {
    void success(String responseData);
    void failure(IOException e);
}
