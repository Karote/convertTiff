package com.ntx.converttiff;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.beyka.tiffbitmapfactory.TiffBitmapFactory;

import java.io.File;

public class DecodeTiffTask extends AsyncTask<File, Void, Bitmap> {

    private Callback mCallback;

    DecodeTiffTask(Callback callback) {
        this.mCallback = callback;
    }

    interface Callback {
        void onDecodeComplete(Bitmap bitmap);
    }

    @Override
    protected Bitmap doInBackground(File... files) {
        TiffBitmapFactory.Options options = new TiffBitmapFactory.Options();
        options.inJustDecodeBounds = false;
        Bitmap bitmap = TiffBitmapFactory.decodeFile(files[0], options);
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        mCallback.onDecodeComplete(bitmap);
    }
}
