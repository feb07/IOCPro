package com.feb.iocpro;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import iocpro.feb.com.library.InjectClick;
import iocpro.feb.com.library.InjectContentView;
import iocpro.feb.com.library.InjectView;

@InjectContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {

    @InjectView(R.id.btn)
    private Button btn;

    @InjectView(R.id.tv)
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(MainActivity.this, "today is 2019-2-13", Toast.LENGTH_LONG).show();
//            }
//        });
    }

    @InjectClick(R.id.btn)
    public void clickMethod(View view) {
        Toast.makeText(MainActivity.this, "today is 2019-2-13", Toast.LENGTH_LONG).show();
    }
}
