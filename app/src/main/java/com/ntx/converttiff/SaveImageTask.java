package com.ntx.converttiff;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveImageTask extends AsyncTask<Object, Void, File> {
    private static final String TAG = "_DEBUG_";

    private final String APP_NAME;

    private final Callback mCallback;

    public SaveImageTask(Context context, Callback callback) {
        this.mCallback = callback;
        this.APP_NAME = context.getString(R.string.app_name);
    }

    @Override
    protected File doInBackground(Object... objects) {
        Bitmap image = (Bitmap) objects[0];
        Bitmap.CompressFormat compressFormat = (Bitmap.CompressFormat) objects[1];
        return saveBitmapToFile(image, compressFormat);
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);
        mCallback.onSaveComplete(file);
    }

    public interface Callback {
        void onSaveComplete(File filePath);
    }

    private File saveBitmapToFile(Bitmap bitmap, Bitmap.CompressFormat compressFormat) {
        File pictureFile = getOutputMediaFile(compressFormat);
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");
            return null;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            bitmap.compress(compressFormat, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }

        return pictureFile;

    }

    private File getOutputMediaFile(Bitmap.CompressFormat compressFormat) {

        String filenameExtension = "";
        if(compressFormat == Bitmap.CompressFormat.JPEG)
            filenameExtension = ".jpg";
        else if(compressFormat == Bitmap.CompressFormat.PNG)
            filenameExtension = ".png";

        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Log.w(TAG, "getOutputMediaFile: Environment storage not writable");
            return null;
        }

        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory
                                (Environment.DIRECTORY_PICTURES), APP_NAME);


        // Create the storage directory if it does not exist

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        String mImageName = timeStamp + filenameExtension;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;

    }

}