package com.app.nacfun;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

// mWebView.loadUrl("https://nacfun.com/");
public class MainActivity extends AppCompatActivity {
    private WebView web;
    // String webUrl = "https://nacfun.com/";

    String webUrl = "https://nacfun.com/Smart/";

    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;


    public Context context;
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int FILECHOOSER_RESULTCODE = 1;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;

    // the same for Android 5.0 methods only
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    private ProgressBar progressBar;
    private Intent contentSelectionIntent;
    String shop = "https://nacfun.com/shop/";
    String tap2 = " https://nacfun.com/tap2/";
    String audit = "https://nacfun.com/audit/";
    private String webSiteUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadWebPages();
    }

    private void loadWebPages() {
        try {
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            web = (WebView) findViewById(R.id.activity_main_webview);
            web.loadUrl(webUrl);
            contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            WebSettings mywebsettings = web.getSettings();
            mywebsettings.setJavaScriptEnabled(true);

            web.setWebViewClient(new WebViewClient());

//improve webview performance
            web.getSettings().setLoadsImagesAutomatically(true);
            web.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
            web.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            web.getSettings().setAppCacheEnabled(false);
            web.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            mywebsettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
            mywebsettings.setUseWideViewPort(true);
            mywebsettings.setSavePassword(true);
            mywebsettings.setSaveFormData(true);
            mywebsettings.setEnableSmoothTransition(true);
            web.getSettings().setDomStorageEnabled(true);
            web.getSettings().setAllowContentAccess(true);
            web.getSettings().setAllowFileAccess(true);
            String USER_AGENT = "Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19";
            web.getSettings().setUserAgentString(USER_AGENT);
            CookieManager cookieManager = CookieManager.getInstance();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(web, true);
            }


            web.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView wv, String url) {
                    Log.e("url", url);
                    webSiteUrl = url;
                    if (url.startsWith("tel:") || url.startsWith("whatsapp:") || url.startsWith("intent://") || url.startsWith("http://")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        web.goBack();
                        return true;
                    } else if (url.startsWith("mailto:")) {
                        Intent mail = new Intent(Intent.ACTION_SEND);
                        mail.setType("application/octet-stream");
                        String AdressMail = new String(url.replace("mailto:", ""));
                        mail.putExtra(Intent.EXTRA_EMAIL, new String[]{AdressMail});
                        mail.putExtra(Intent.EXTRA_SUBJECT, "");
                        mail.putExtra(Intent.EXTRA_TEXT, "");
                        startActivity(mail);
                        return true;
                    } else if (url.contains(shop) || url.contains(tap2) || url.contains(audit)) {
                        Intent intent = new Intent(MainActivity.this, WebView.class);
                        intent.putExtra("websiteUrl", url);
                        startActivity(intent);
                        return true;
                    }
                    return false;
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    progressBar.setVisibility(View.VISIBLE);
                    Log.d("url", url);
                    webSiteUrl = url;
                    invalidateOptionsMenu();
                }

                public void onReceivedError(WebView view, int errorCode,
                                            String description, String failingUrl) {
                    try {
                        invalidateOptionsMenu();
                    } catch (Exception e) {

                    }

                    if (web.canGoBack()) {
                        web.goBack();
                    }

                }


                public void onPageFinished(WebView view, String url) {
                    //     pullToRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    invalidateOptionsMenu();
                }


            });

            //   web.setWebChromeClient(new MyWebChromeClient());


//enable upload part

            web.setWebChromeClient(new WebChromeClient() {
                // for Lollipop, all in one
                public boolean onShowFileChooser(
                        WebView webView, ValueCallback<Uri[]> filePathCallback,
                        FileChooserParams fileChooserParams) {
                    if (mFilePathCallback != null) {
                        mFilePathCallback.onReceiveValue(null);
                    }
                    mFilePathCallback = filePathCallback;


                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                        // create the file where the photo should go
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                            takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                            Log.e(TAG, "Unable to create Image File", ex);
                        }

                        // continue only if the file was successfully created
                        if (photoFile != null) {
                            mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(photoFile));
                        } else {
                            takePictureIntent = null;
                        }
                    }

                    if (/*webSiteUrl.contains("https://nacfun.com/cretcontpubform") ||*/ webSiteUrl.contains("https://nacfun.com/editimgvidcont.php?") ||
                            webSiteUrl.contains("https://nacfun.com/editrejcont.php?") || webSiteUrl.contains("https://nacfun.com/profile/") ||
                            webSiteUrl.contains("https://nacfun.com/smart/createcont.php") || webSiteUrl.contains("https://nacfun.com/smart/updatecontraft.php")
                            || webSiteUrl.contains("https://nacfun.com/videodraw/") || webSiteUrl.contains("https://nacfun.com/battle.php?") || webSiteUrl.contains("https://nacfun.com/yourtestimonial")
                            || webSiteUrl.contains("https://nacfun.com/challengevid.php?vid=") || webSiteUrl.contains("https://nacfun.com/smart/updatecontvideo.php") || webSiteUrl.contains("nacfun.com/smart/create/create-yellow-pages-video-ad")
                            || webSiteUrl.contains("nacfun.com/smart/create/edit-yellow-pages-video-ad") || webSiteUrl.contains("nacfun.com/smart/create/create-video-activity") || webSiteUrl.contains("nacfun.com/smart/updatevideocontraft.php")
                            || webSiteUrl.contains("nacfun.com/smart/editsvideocont.php") || webSiteUrl.contains("nacfun.com/smart/create/create-video-study-material") || webSiteUrl.contains("nacfun.com/smart/create/editvideostudymaterial.php")
                            || webSiteUrl.contains("nacfun.com/publication/create-video-blog-quiz.php") || webSiteUrl.contains("nacfun.com/smart/create/create-questions-video-study-material") || webSiteUrl.contains("nacfun.com/create/editdraftvideostudymaterialwithquestions.php")
                            || webSiteUrl.contains("nacfun.com/publication/edit-video-blog-quiz.php") || webSiteUrl.contains("nacfun.com/cretvidpubform") || webSiteUrl.contains("nacfun.com/editvidcont.php") || webSiteUrl.contains("nacfun.com/editrejvid.php")
                            || webSiteUrl.contains("nacfun.com/smart/create/testimonial") || webSiteUrl.contains("nacfun.com/yourtestimonial") || webSiteUrl.contains("nacfun.com/freestyle.php") || webSiteUrl.contains("nacfun.com/freestyle/freestyle.php")
                            || webSiteUrl.contains("nacfun.com/videodraw/national-song-no-1-class-7") || webSiteUrl.contains("nacfun.com/video-activity") || webSiteUrl.contains("nacfun.com/nachodance") || webSiteUrl.contains("nacfun.com/libraryshare.php?vid=libchal")
                            || webSiteUrl.contains("nacfun.com/libraryshare.php?vid=libchal_16072") || webSiteUrl.contains("nacfun.com/") || webSiteUrl.contains("nacfun.com/mobile/") || webSiteUrl.contains("nacfun.com/home/") || webSiteUrl.contains("nacfun.com/Register/") || webSiteUrl.contains("nacfun.com/fun-n-learn/activities-for-kids") || webSiteUrl.contains("nacfun.com/pre-school-for-kids")
                            || webSiteUrl.contains("nacfun.com/smart/create/workout-video-step2.php") || webSiteUrl.contains("nacfun.com/smart/create/workout-video-step3.php") || webSiteUrl.contains("nacfun.com/s/struff-1-test.php?contid=109521") || webSiteUrl.contains("nacfun.com/s/struff-1-test.php?contid=109548") || webSiteUrl.contains("nacfun.com/a/rotary-club-of-mumbai-lakers-charities-trust")
                    ) {
                        contentSelectionIntent.setType("video/*");
                    } else if (webSiteUrl.contains("https://nacfun.com/smart/publishlesson.php")) {
                        contentSelectionIntent.setType("image/*,application/pdf");
                    } else {
                        contentSelectionIntent.setType("image/*");
                    }


                    //  contentSelectionIntent.setType("video/*");
                    //  contentSelectionIntent.setType("image/*");
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);

                    Intent[] intentArray;
                    if (takePictureIntent != null) {
                        intentArray = new Intent[]{takePictureIntent};
                    } else {
                        intentArray = new Intent[0];
                    }

                    Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.image_chooser));
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                    startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);

                    return true;
                }

                // creating image files (Lollipop only)
                private File createImageFile() throws IOException {

                    File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DirectoryNameHere");

                    if (!imageStorageDir.exists()) {
                        imageStorageDir.mkdirs();
                    }

                    // create an image file name
                    imageStorageDir = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                    return imageStorageDir;
                }

                // openFileChooser for Android 3.0+
                public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                    mUploadMessage = uploadMsg;

                    try {
                        File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DirectoryNameHere");

                        if (!imageStorageDir.exists()) {
                            imageStorageDir.mkdirs();
                        }

                        File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");

                        mCapturedImageURI = Uri.fromFile(file); // save to the private variable

                        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                        // captureIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                        i.addCategory(Intent.CATEGORY_OPENABLE);
                        i.setType("image/*");

                        Intent chooserIntent = Intent.createChooser(i, getString(R.string.image_chooser));
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});

                        // startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                    } catch (Exception e) {
                        Toast.makeText(getBaseContext(), "Camera Exception:" + e, Toast.LENGTH_LONG).show();
                    }

                }

                // openFileChooser for Android < 3.0
                public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                    openFileChooser(uploadMsg, "");
                }

                // openFileChooser for other Android versions
                //* may not work on KitKat due to lack of implementation of openFileChooser() or onShowFileChooser()
                //   https://code.google.com/p/android/issues/detail?id=62220
                //    however newer versions of KitKat fixed it on some devices *//*
                public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                    openFileChooser(uploadMsg, acceptType);
                }

            });

            web.setDownloadListener(new DownloadListener() {
                public void onDownloadStart(String url, String userAgent,
                                            String contentDisposition, String mimetype,
                                            long contentLength) {
                    DownloadManager.Request request = new DownloadManager.Request(
                            Uri.parse(url));
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "download");
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(request);

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // return here when file selected from camera or from SD Card
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // code for all versions except of Lollipop
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

            if (requestCode == FILECHOOSER_RESULTCODE) {
                if (null == this.mUploadMessage) {
                    return;
                }

                Uri result = null;

                try {
                    if (resultCode != RESULT_OK) {
                        result = null;
                    } else {
                        // retrieve from the private variable if the intent is null
                        result = data == null ? mCapturedImageURI : data.getData();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "activity :" + e, Toast.LENGTH_LONG).show();
                }

                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }

        } // end of code for all versions except of Lollipop

        // start of code for Lollipop only
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (requestCode != FILECHOOSER_RESULTCODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }

            Uri[] results = null;

            // check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (data == null || data.getData() == null) {
                    // if there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }

            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;

        } // end of code for Lollipop only


    }


    /*class MyWebChromeClient extends WebChromeClient {
        // For 3.0+ Devices (Start)
        // onActivityResult attached before constructor
        protected void openFileChooser(ValueCallback uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
          //  i.setType("image/*");
            i.setType("image/* video/*");
            startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
        }


        // For Lollipop 5.0+ Devices
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            if (mUploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }

            uploadMessage = filePathCallback;

            Intent intent = fileChooserParams.createIntent();
            try {
                startActivityForResult(intent, FILECHOOSER_RESULTCODE);
            } catch (ActivityNotFoundException e) {
                uploadMessage = null;
                Toast.makeText(MainActivity.this, "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }

        //For Android 4.1 only
        protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUploadMessage = uploadMsg;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
          //  intent.setType("image/*");
            intent.setType("image/* video/*");
            startActivityForResult(Intent.createChooser(intent, "File Chooser"), FILECHOOSER_RESULTCODE);
        }

        protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
          //  i.setType("image/*");
            i.setType("image/* video/*");
            startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
        }
    }*/


    private Boolean exit = false;

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) {

            web.goBack();
        } else {
            if (exit) {
                finish(); // finish activity
            } else {
                Toast.makeText(this, "Press Back again to Exit.",
                        Toast.LENGTH_SHORT).show();
                exit = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        exit = false;
                    }
                }, 3 * 1000);

            }
            super.onBackPressed();
        }
    }


    /*private boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }*/

}