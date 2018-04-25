package com.ntx.converttiff;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private MagicFileChooser magicFileChooser;
    private File mFile;
    private ProgressDialog mProgressDialog;
    private Bitmap.CompressFormat mSaveType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Saving ...");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);

        magicFileChooser = new MagicFileChooser(MainActivity.this);

        findViewById(R.id.btn_file_pick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                magicFileChooser.showFileChooser("image/tiff");
            }
        });

        findViewById(R.id.btn_save_as_png).setOnClickListener(onSaveButtonClickListener);
        findViewById(R.id.btn_save_as_jpg).setOnClickListener(onSaveButtonClickListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (magicFileChooser.onActivityResult(requestCode, resultCode, data)) {
            mFile = magicFileChooser.getChosenFiles()[0];
            ((TextView) findViewById(R.id.tv_file_path)).setText("file:" + mFile.getPath());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    Button.OnClickListener onSaveButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            mProgressDialog.show();

            DecodeTiffTask decodeTiffTask = new DecodeTiffTask(decodeTiffCallback);
            decodeTiffTask.execute(mFile);

            switch (view.getId()) {
                case R.id.btn_save_as_png:
                    mSaveType = Bitmap.CompressFormat.PNG;
                    break;
                case R.id.btn_save_as_jpg:
                    mSaveType = Bitmap.CompressFormat.JPEG;
                    break;
            }
        }
    };

    DecodeTiffTask.Callback decodeTiffCallback = new DecodeTiffTask.Callback() {
        @Override
        public void onDecodeComplete(Bitmap bitmap) {
            if (bitmap == null) {
                mProgressDialog.dismiss();
                ((TextView) findViewById(R.id.tv_save_result)).setText("Decode Fail.");
                return;
            }
            SaveImageTask saveImageTask = new SaveImageTask(MainActivity.this, saveImageCallback);
            saveImageTask.execute(bitmap, mSaveType);
        }
    };

    SaveImageTask.Callback saveImageCallback = new SaveImageTask.Callback() {
        @Override
        public void onSaveComplete(File filePath) {
            mProgressDialog.dismiss();

            ((TextView) findViewById(R.id.tv_save_result)).setText(filePath.getPath());
        }
    };
}
