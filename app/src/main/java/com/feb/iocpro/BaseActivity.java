package com.feb.iocpro;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import iocpro.feb.com.library.InjectManager;


/**
 * Created by lilichun on 2019/2/13.
 */
public class BaseActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InjectManager.inject(this);
    }
}
