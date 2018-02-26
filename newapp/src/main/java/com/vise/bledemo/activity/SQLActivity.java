package com.vise.bledemo.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.vise.bledemo.R;
import com.vise.bledemo.myClass.MyDatabaseHelper;

public class SQLActivity extends AppCompatActivity implements View.OnClickListener{
    Button btn_sqlCreate;
    MyDatabaseHelper myDatabaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sql);
        widgetInit();
        myDatabaseHelper = new MyDatabaseHelper(this,"BookStore.db",null,1);
    }

    private void widgetInit() {
        btn_sqlCreate = findViewById(R.id.bt_sqlCreate);
        btn_sqlCreate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_sqlCreate:
                myDatabaseHelper.getWritableDatabase();
                break;
        }
    }
}
