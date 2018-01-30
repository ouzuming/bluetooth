package com.vise.bledemo.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vise.bledemo.R;
import com.vise.bledemo.myClass.ZipImage;

import org.greenrobot.greendao.annotation.apihint.Internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static android.Manifest.*;

public class photoActivity extends AppCompatActivity implements View.OnClickListener{
    Button btn_openCamera,btn_openAlbum,btn_selectPhoto;
    ImageView iv_photo;
    private Uri imageUri;
    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    public static final String TAG = "PHOTO";
    private  File outPutImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        btn_openCamera = findViewById(R.id.bt_openCamera);
        btn_openAlbum = findViewById(R.id.bt_openAlbum);
        btn_selectPhoto = findViewById(R.id.bt_selectPhoto);
        iv_photo = findViewById(R.id.iv_photo);
        btn_openCamera.setOnClickListener(this);
        btn_openAlbum.setOnClickListener(this);
        btn_selectPhoto.setOnClickListener(this);
    }
    private void takePhoto(){
        outPutImage = new File(getExternalCacheDir(),"output_image.jpg");
        try {
            if(outPutImage.exists()){
                outPutImage.delete();
            }
            outPutImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(Build.VERSION.SDK_INT >= 24){
            Log.d(TAG, "sdk >=24" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            imageUri = FileProvider.getUriForFile(photoActivity.this,"com.example.cameraalbumtest.fileprovider",outPutImage);
        }else {
            Log.d(TAG, "sdk < 24" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            imageUri = Uri.fromFile(outPutImage);
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,TAKE_PHOTO);
    }

    private void choosePhoto() {
//        if(ContextCompat.checkSelfPermission(photoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(photoActivity.this,new String[]{permission.WRITE_EXTERNAL_STORAGE},1);
//        }else{
//            openAlbum();
//        }
        openAlbum();
    }

    private void openAlbum() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_openCamera:
                Log.d(TAG, "open camera" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                takePhoto();
                break;
            case R.id.bt_openAlbum:
                Log.d(TAG, "open album" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                choosePhoto();
                break;

            case R.id.bt_selectPhoto:
                Log.d(TAG, "select get photo mode" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                showTypeDialog();
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode){
//            case 1: if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                openAlbum();
//            }else {
//                Toast.makeText(this,"you denied the permission",Toast.LENGTH_SHORT).show();
//            }
//            break;
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        switch (requestCode){
            case TAKE_PHOTO:
                if(resultCode == RESULT_OK){
                    ZipImage.zipImage(Uri.fromFile(outPutImage).getPath());
                    iv_photo.setImageURI(Uri.fromFile(outPutImage));
                }
                break;

            case CHOOSE_PHOTO:
                if(resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    File file = getFilePath(uri);
                    ZipImage.zipImage(file.getAbsolutePath());
                    iv_photo.setImageURI(Uri.fromFile(file));
                }
                break;
            default:break;
        }
    }

    private File getFilePath(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor actualimagecursor = this.getContentResolver().query(uri, proj, null, null, null);
        int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        actualimagecursor.moveToFirst();
        String img_path = actualimagecursor.getString(actual_image_column_index);
        return new File(img_path);
    }
    private void showTypeDialog(){
        Log.d(TAG, "showTypeDialog" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        View view = View.inflate(this,R.layout.camera_dialog,null);
        TextView tv_select_gallery = view.findViewById(R.id.tv_album);
        TextView tv_select_camera = view.findViewById(R.id.tv_camera);
        TextView tv_cancel_dialog =  view.findViewById(R.id.tv_cancel);
        tv_select_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "openAlbum" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                openAlbum();
                dialog.dismiss();
            }
        });
        tv_select_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "takePhoto" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                takePhoto();
                dialog.dismiss();
            }
        });
        tv_cancel_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setView(view);
        dialog.show();
    }

}
