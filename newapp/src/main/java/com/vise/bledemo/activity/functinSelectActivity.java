package com.vise.bledemo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.vise.bledemo.R;
public class functinSelectActivity extends AppCompatActivity implements View.OnClickListener {
    Button mbt_atuo, mbt_manual, mbt_file;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_functin_select);
        widget_init();

    }
    private void widget_init(){
        mbt_atuo = findViewById(R.id.bt_auto);
        mbt_manual = findViewById(R.id.bt_manual);
        mbt_file = findViewById(R.id.bt_file);
        mbt_atuo.setOnClickListener(this);
        mbt_manual.setOnClickListener(this);
        mbt_file.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()){
            case R.id.bt_auto:
                intent = new Intent(functinSelectActivity.this,AutoConnectActivity.class);
                break;
            case R.id.bt_manual:
                intent = new Intent(functinSelectActivity.this,mybleActivity.class);
                break;

            case R.id.bt_file:
                intent = new Intent(functinSelectActivity.this,fileActivity.class);
                break;
        }
        startActivity(intent);
    }
}
