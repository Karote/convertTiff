package com.ntx.converttiff;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 檔案選取器，支援檔案MIME類別篩選、強制可讀取以及複選功能。
 *
 * @author Magic Len
 */
@SuppressLint("NewApi")
public class MagicFileChooser {

    // /**
    // * 檔案選取器的Activity Request Code
    // */
    public static final int ACTIVITY_FILE_CHOOSER = 9973;


    /**
     * 儲存使用這個檔案選取器的Activity。
     */
    private final Activity activity;


    /**
     * 儲存是否正在選取檔案。
     */
    private boolean choosing = false;
    /**
     * 儲存被選到的檔案是否一定要可以讀取。
     */
    private boolean mustCanRead;
    /**
     * 儲存被選到的檔案。
     */
    private File[] chosenFiles;


    /**
     * 建構子，在Activity內使用檔案選取器。
     *
     * @param activity 傳入使用這個檔案選取器的Activity。
     */
    public MagicFileChooser(final Activity activity) {
        this.activity = activity;
    }

    /**
     * 從Uri取得絕對路徑。
     *
     * @param context 傳入Context
     * @param uris    傳入Uri陣列
     * @return 傳回絕對路徑字串陣列，若絕對路徑無法取得，則對應的陣列索引位置為null
     */
    public static String[] getAbsolutePathsFromUris(final Context context, final Uri[] uris) {
        return getAbsolutePathsFromUris(context, uris, false);
    }

    /**
     * 從多個Uri取得絕對路徑。
     *
     * @param context     傳入Context
     * @param uris        傳入Uri陣列
     * @param mustCanRead 傳入Uri所指的路徑是否一定要可以讀取
     * @return 傳回絕對路徑字串陣列，若絕對路徑無法取得或是無法讀取，則對應的陣列索引位置為null
     */
    public static String[] getAbsolutePathsFromUris(final Context context, final Uri[] uris, final boolean mustCanRead) {
        if (uris == null) {
            return null;
        }
        final int urisLength = uris.length;
        final String[] paths = new String[urisLength];
        for (int i = 0; i < urisLength; ++i) {
            final Uri uri = uris[i];
            paths[i] = getAbsolutePathFromUri(context, uri, mustCanRead);
        }
        return paths;
    }

    /**
     * 從多個Uri取得File物件。
     *
     * @param context 傳入Context
     * @param uris    傳入Uri陣列
     * @return 傳回File物件陣列，若File物件無法建立，則對應的陣列索引位置為null
     */
    public static File[] getFilesFromUris(final Context context, final Uri[] uris) {
        return getFilesFromUris(context, uris, false);
    }

    /**
     * 從多個Uri取得File物件。
     *
     * @param context     傳入Context
     * @param uris        傳入Uri陣列
     * @param mustCanRead 傳入Uri所指的路徑是否一定要可以讀取
     * @return 傳回File物件陣列，若File物件無法建立或是檔案路徑無法讀取，則對應的陣列索引位置為null
     */
    public static File[] getFilesFromUris(final Context context, final Uri[] uris, final boolean mustCanRead) {
        if (uris == null) {
            return null;
        }
        final int urisLength = uris.length;
        final File[] files = new File[urisLength];
        for (int i = 0; i < urisLength; ++i) {
            final Uri uri = uris[i];
            files[i] = getFileFromUri(context, uri, mustCanRead);
        }
        return files;
    }

    /**
     * 從Uri取得絕對路徑。
     *
     * @param context 傳入Context
     * @param uri     傳入Uri物件
     * @return 傳回絕對路徑，若絕對路徑無法取得，傳回null
     */
    public static String getAbsolutePathFromUri(final Context context, final Uri uri) {
        return getAbsolutePathFromUri(context, uri, false);
    }

    /**
     * 從Uri取得絕對路徑。
     *
     * @param context     傳入Context
     * @param uri         傳入Uri物件
     * @param mustCanRead 傳入Uri所指的路徑是否一定要可以讀取
     * @return 傳回絕對路徑，若絕對路徑無法取得或是無法讀取，傳回null
     */
    public static String getAbsolutePathFromUri(final Context context, final Uri uri, final boolean mustCanRead) {
        final File file = getFileFromUri(context, uri, mustCanRead);
        if (file != null) {
            return file.getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * 從Uri取得File物件。
     *
     * @param context 傳入Context
     * @param uri     傳入Uri物件
     * @return 傳回File物件，若File物件無法建立，傳回null
     */
    public static File getFileFromUri(final Context context, final Uri uri) {
        return getFileFromUri(context, uri, false);
    }

    /**
     * 從Uri取得File物件。
     *
     * @param context     傳入Context
     * @param uri         傳入Uri物件
     * @param mustCanRead 傳入Uri所指的路徑是否一定要可以讀取
     * @return 傳回File物件，若File物件無法建立或是檔案路徑無法讀取，傳回null
     */
    @SuppressLint("NewApi")
    public static File getFileFromUri(final Context context, final Uri uri, final boolean mustCanRead) {
        if (uri == null) {
            return null;
        }

        // 判斷是否為Android 4.4之後的版本
        final boolean after44 = Build.VERSION.SDK_INT >= 19;
        if (after44 && DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是Android 4.4之後的版本，而且屬於文件URI
            if (isExternalStorageDocument(uri)) {
                // 外部儲存空間
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] divide = docId.split(":");
                String path = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/").concat(divide[1]);
                return createFileObjFromPath(path, mustCanRead);
            } else if (isDownloadsDocument(uri)) {
                // 下載目錄
                final String docId = DocumentsContract.getDocumentId(uri);
                final Uri downloadUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(docId));
                String path = queryAbsolutePath(context, downloadUri);
                return createFileObjFromPath(path, mustCanRead);
            } else if (isMediaDocument(uri)) {
                // 圖片、影音檔案
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] divide = docId.split(":");
                final String type = divide[0];
                Uri mediaUri = null;
                if ("image".equals(type)) {
                    mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    mediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } else {
                    return null;
                }
                mediaUri = ContentUris.withAppendedId(mediaUri, Long.parseLong(divide[1]));
                String path = queryAbsolutePath(context, mediaUri);
                return createFileObjFromPath(path, mustCanRead);
            }
        } else {
            // 如果是一般的URI
            final String scheme = uri.getScheme();
            String path = null;
            if ("content".equals(scheme)) {
                // 內容URI
                path = queryAbsolutePath(context, uri);
            } else if ("file".equals(scheme)) {
                // 檔案URI
                path = uri.getPath();
            }
            return createFileObjFromPath(path, mustCanRead);
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * 將路徑轉成File物件。
     *
     * @param path 傳入檔案路徑
     * @return 傳回File物件，若File物件無法建立，傳回null。
     */
    public static File createFileObjFromPath(final String path) {
        return createFileObjFromPath(path, false);
    }

    /**
     * 將路徑轉成File物件。
     *
     * @param path        傳入檔案路徑
     * @param mustCanRead 傳入檔案路徑是否一定要可以讀取
     * @return 傳回File物件，若File物件無法建立或是檔案路徑無法讀取，傳回null
     */
    public static File createFileObjFromPath(final String path, final boolean mustCanRead) {
        if (path != null) {
            try {
                File file = new File(path);
                if (mustCanRead) {
                    file.setReadable(true);
                    if (!file.canRead()) {
                        return null;
                    }
                }
                return file.getAbsoluteFile();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 查詢MediaStroe Uri對應的絕對路徑。
     *
     * @param context 傳入Context
     * @param uri     傳入MediaStore Uri
     * @return 傳回絕對路徑
     */
    public static String queryAbsolutePath(final Context context, final Uri uri) {
        final String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                return cursor.getString(index);
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }


    /**
     * 顯示檔案選取器，選取所有檔案，不設定檔案選取器的標題，僅進行單獨選取，被選到的檔案不一定要可以讀取。
     *
     * @return 傳回檔案選取器是否開啟成功
     */
    public boolean showFileChooser() {
        return showFileChooser("*/*");
    }


    /**
     * 顯示檔案選取器，不設定檔案選取器的標題，僅進行單獨選取，被選到的檔案不一定要可以讀取。
     *
     * @param mimeType 傳入篩選的MIME類型
     * @return 傳回檔案選取器是否開啟成功
     */
    public boolean showFileChooser(final String mimeType) {
        return showFileChooser(mimeType, null);
    }

    /**
     * 顯示檔案選取器，僅進行單獨選取，被選到的檔案不一定要可以讀取。
     *
     * @param mimeType     傳入篩選的MIME類型
     * @param chooserTitle 傳入檔案選取器的標題，若為null則用預設值
     * @return 傳回檔案選取器是否開啟成功
     */
    public boolean showFileChooser(final String mimeType, final String chooserTitle) {
        return showFileChooser(mimeType, chooserTitle, false);
    }


    /**
     * 顯示檔案選取器，被選到的檔案不一定要可以讀取。
     *
     * @param mimeType      傳入篩選的MIME類型
     * @param chooserTitle  傳入檔案選取器的標題，若為null則用預設值
     * @param allowMultiple 傳入檔案選取器是否使用複選
     * @return 傳回檔案選取器是否開啟成功
     */
    public boolean showFileChooser(final String mimeType, final String chooserTitle, final boolean allowMultiple) {
        return showFileChooser(mimeType, chooserTitle, allowMultiple, false);
    }

    /**
     * 顯示檔案選取器。
     *
     * @param mimeType      傳入篩選的MIME類型
     * @param chooserTitle  傳入檔案選取器的標題，若為null則用預設值
     * @param allowMultiple 傳入檔案選取器是否使用複選
     * @param mustCanRead   傳入被選到的檔案是否一定要可以讀取
     * @return 傳回檔案選取器是否開啟成功
     */
    public boolean showFileChooser(final String mimeType, final String chooserTitle, final boolean allowMultiple, final boolean mustCanRead) {
        if (mimeType == null || choosing) {
            return false;
        }
        choosing = true;
        // 檢查是否有可用的Activity
        final PackageManager packageManager = activity.getPackageManager();
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(mimeType);
        final List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() > 0) {
            this.mustCanRead = mustCanRead;
            // 如果有可用的Activity
            final Intent picker = new Intent(Intent.ACTION_GET_CONTENT);
            picker.setType(mimeType);
            picker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple);
            picker.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            // 使用Intent Chooser
            final Intent destIntent = Intent.createChooser(picker, chooserTitle);
            activity.startActivityForResult(destIntent, ACTIVITY_FILE_CHOOSER);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 當檔案選取器被關閉後，應該要呼叫這個方法，判斷檔案選取器是否有選取到檔案，接著再用getChosenFiles方法來取得選取結果。
     *
     * @param requestCode 傳入Activity的Request Code
     * @param resultCode  傳入Activity的Request Code
     * @param data        傳入Activity的data
     * @return 傳回檔案選取器是否有選取結果。
     */
    public boolean onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == ACTIVITY_FILE_CHOOSER) {
            choosing = false;
            if (resultCode == Activity.RESULT_OK) {
                final Uri uri = data.getData();
                if (uri != null) {
                    // 單選
                    chosenFiles = getFilesFromUris(activity, new Uri[]{uri}, mustCanRead);
                    return true;
                } else if (Build.VERSION.SDK_INT >= 16) {
                    // 複選
                    final ClipData clipData = data.getClipData();
                    if (clipData != null) {
                        int count = clipData.getItemCount();
                        if (count > 0) {
                            final Uri[] uris = new Uri[count];
                            for (int i = 0; i < count; ++i) {
                                uris[i] = clipData.getItemAt(i).getUri();
                            }
                            chosenFiles = getFilesFromUris(activity, uris, mustCanRead);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 取得被選取到的檔案。
     *
     * @return 傳回被選取到的檔案，過濾掉不成功的部份
     */
    public File[] getChosenFiles(final boolean filter) {
        if (chosenFiles == null) {
            return new File[0];
        } else {
            final ArrayList<File> alFileList = new ArrayList<>();
            for (final File chosenFile : chosenFiles) {
                if (filter && chosenFile == null) {
                    continue;
                }
                alFileList.add(chosenFile);
            }
            final File[] files = new File[alFileList.size()];
            alFileList.toArray(files);
            return files;
        }
    }

    /**
     * 取得被選取到的檔案。
     *
     * @return 傳回被選取到的檔案，過濾掉不成功的部份
     */
    public File[] getChosenFiles() {
        return getChosenFiles(true);
    }


}