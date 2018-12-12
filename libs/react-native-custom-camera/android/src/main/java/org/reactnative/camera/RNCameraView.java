package org.reactnative.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.CamcorderProfile;
import android.media.MediaActionSound;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.os.AsyncTask;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.react.bridge.*;
import com.facebook.react.uimanager.ThemedReactContext;
import com.google.android.cameraview.CameraView;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.kaopiz.kprogresshud.KProgressHUD;


import org.reactnative.barcodedetector.RNBarcodeDetector;
import org.reactnative.camera.tasks.*;
import org.reactnative.camera.utils.ImageDimensions;
import org.reactnative.camera.utils.RNFileUtils;
import org.reactnative.facedetector.RNFaceDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import api.DefaultApi;
import api.invoker.ApiException;
import api.models.InlineResponse200;
import api.models.PlateDetails;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RNCameraView extends CameraView implements LifecycleEventListener, BarCodeScannerAsyncTaskDelegate, FaceDetectorAsyncTaskDelegate,
        BarcodeDetectorAsyncTaskDelegate, TextRecognizerAsyncTaskDelegate, PictureSavedDelegate {
    public static final String TAG = RNCameraView.class.getSimpleName();
    private ThemedReactContext mThemedReactContext;
    private Promise mVideoRecordedPromise;
    private List<String> mBarCodeTypes = null;

    private boolean mIsPaused = false;
    private boolean mIsNew = true;
    private boolean invertImageData = false;

    // Concurrency lock for scanners to avoid flooding the runtime
    public volatile boolean barCodeScannerTaskLock = false;
    public volatile boolean faceDetectorTaskLock = false;
    public volatile boolean googleBarcodeDetectorTaskLock = false;
    public volatile boolean textRecognizerTaskLock = false;

    // Scanning-related properties
    private MultiFormatReader mMultiFormatReader;
    private RNBarcodeDetector mGoogleBarcodeDetector;
    private TextRecognizer mTextRecognizer;
    private boolean mShouldDetectFaces = false;
    private boolean mShouldGoogleDetectBarcodes = false;
    private boolean mShouldScanBarCodes = false;
    private boolean mShouldRecognizeText = false;
    private int mGoogleVisionBarCodeType = Barcode.ALL_FORMATS;
    private int mGoogleVisionBarCodeMode = RNBarcodeDetector.NORMAL_MODE;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ProgressDialog progressDialog;

    public RNCameraView(ThemedReactContext themedReactContext) {
        super(themedReactContext, true);
        mThemedReactContext = themedReactContext;
        themedReactContext.addLifecycleEventListener(this);

        initProgress();

        addCallback(new Callback() {
            @Override
            public void onCameraOpened(CameraView cameraView) {
                Log.e(TAG, "onCameraOpened: ");
                RNCameraViewHelper.emitCameraReadyEvent(cameraView);
            }

            @Override
            public void onMountError(CameraView cameraView) {
                RNCameraViewHelper.emitMountErrorEvent(cameraView, "Camera view threw an error - component could not be rendered.");
            }

            @Override
            public void onPictureTaken(CameraView cameraView, final byte[] data) {
                FileOutputStream outStream;
                String path = null;
                try {
                    File filePath = createImageFile("Openalpr");
                    path = filePath.getAbsolutePath();
                    outStream = new FileOutputStream(filePath);
                    outStream.write(data);
                    outStream.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Toast.makeText(getContext(), "Picture Saved", Toast.LENGTH_SHORT).show();

                if (path != null)
                    pushImageToALPR(path);

            }

            @Override
            public void onVideoRecorded(CameraView cameraView, String path) {
                if (mVideoRecordedPromise != null) {
                    if (path != null) {
                        WritableMap result = Arguments.createMap();
                        result.putString("uri", RNFileUtils.uriFromFile(new File(path)).toString());
                        mVideoRecordedPromise.resolve(result);
                    } else {
                        mVideoRecordedPromise.reject("E_RECORDING", "Couldn't stop recording - there is none in progress");
                    }
                    mVideoRecordedPromise = null;
                }
            }

            @Override
            public void onFramePreview(CameraView cameraView, byte[] data, int width, int height, int rotation) {
                int correctRotation = RNCameraViewHelper.getCorrectCameraRotation(rotation, getFacing());
                boolean willCallBarCodeTask = mShouldScanBarCodes && !barCodeScannerTaskLock && cameraView instanceof BarCodeScannerAsyncTaskDelegate;
                boolean willCallFaceTask = mShouldDetectFaces && !faceDetectorTaskLock && cameraView instanceof FaceDetectorAsyncTaskDelegate;
                boolean willCallGoogleBarcodeTask = mShouldGoogleDetectBarcodes && !googleBarcodeDetectorTaskLock && cameraView instanceof BarcodeDetectorAsyncTaskDelegate;
                boolean willCallTextTask = mShouldRecognizeText && !textRecognizerTaskLock && cameraView instanceof TextRecognizerAsyncTaskDelegate;
                if (!willCallBarCodeTask && !willCallFaceTask && !willCallGoogleBarcodeTask && !willCallTextTask) {
                    return;
                }

                if (data.length < (1.5 * width * height)) {
                    return;
                }

                if (willCallBarCodeTask) {
                    barCodeScannerTaskLock = true;
                    BarCodeScannerAsyncTaskDelegate delegate = (BarCodeScannerAsyncTaskDelegate) cameraView;
                    new BarCodeScannerAsyncTask(delegate, mMultiFormatReader, data, width, height).execute();
                }

                if (willCallGoogleBarcodeTask) {
                    googleBarcodeDetectorTaskLock = true;
                    if (mGoogleVisionBarCodeMode == RNBarcodeDetector.NORMAL_MODE) {
                        invertImageData = false;
                    } else if (mGoogleVisionBarCodeMode == RNBarcodeDetector.ALTERNATE_MODE) {
                        invertImageData = !invertImageData;
                    } else if (mGoogleVisionBarCodeMode == RNBarcodeDetector.INVERTED_MODE) {
                        invertImageData = true;
                    }
                    if (invertImageData) {
                        for (int y = 0; y < data.length; y++) {
                            data[y] = (byte) ~data[y];
                        }
                    }
                    BarcodeDetectorAsyncTaskDelegate delegate = (BarcodeDetectorAsyncTaskDelegate) cameraView;
                    new BarcodeDetectorAsyncTask(delegate, mGoogleBarcodeDetector, data, width, height, correctRotation).execute();
                }

                if (willCallTextTask) {
                    textRecognizerTaskLock = true;
                    TextRecognizerAsyncTaskDelegate delegate = (TextRecognizerAsyncTaskDelegate) cameraView;
                    new TextRecognizerAsyncTask(delegate, mTextRecognizer, data, width, height, correctRotation).execute();
                }
            }
        });
        captureImage();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        View preview = getView();
        if (null == preview) {
            return;
        }
        float width = right - left;
        float height = bottom - top;
        float ratio = 1;
        int orientation = getResources().getConfiguration().orientation;
        int correctHeight;
        int correctWidth;
        this.setBackgroundColor(Color.BLACK);
        if (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            if (ratio * height < width) {
                correctHeight = (int) (width / ratio);
                correctWidth = (int) width;
            } else {
                correctWidth = (int) (height * ratio);
                correctHeight = (int) height;
            }
        } else {
            if (ratio * width > height) {
                correctHeight = (int) (width * ratio);
                correctWidth = (int) width;
            } else {
                correctWidth = (int) (height / ratio);
                correctHeight = (int) height;
            }
        }
        int paddingX = (int) ((width - correctWidth) / 2);
        int paddingY = (int) ((height - correctHeight) / 2);
        preview.layout(paddingX, paddingY, correctWidth + paddingX, correctHeight + paddingY);
    }

    @SuppressLint("all")
    @Override
    public void requestLayout() {
        // React handles this for us, so we don't need to call super.requestLayout();
    }

    @Override
    public void onViewAdded(View child) {
        if (this.getView() == child || this.getView() == null) return;
        // remove and read view to make sure it is in the back.
        // @TODO figure out why there was a z order issue in the first place and fix accordingly.
        this.removeView(this.getView());
        this.addView(this.getView(), 0);
    }

    public void takePicture(ReadableMap options) {
        try {
            super.takePicture(options);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void onPictureSaved(WritableMap response) {
        RNCameraViewHelper.emitPictureSavedEvent(this, response);
    }

    public void record(ReadableMap options, final Promise promise, File cacheDirectory) {
        try {
            String path = options.hasKey("path") ? options.getString("path") : RNFileUtils.getOutputFilePath(cacheDirectory, ".mp4");
            int maxDuration = options.hasKey("maxDuration") ? options.getInt("maxDuration") : -1;
            int maxFileSize = options.hasKey("maxFileSize") ? options.getInt("maxFileSize") : -1;

            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            if (options.hasKey("quality")) {
                profile = RNCameraViewHelper.getCamcorderProfile(options.getInt("quality"));
            }

            boolean recordAudio = !options.hasKey("mute");

            if (super.record(path, maxDuration * 1000, maxFileSize, recordAudio, profile)) {
                mVideoRecordedPromise = promise;
            } else {
                promise.reject("E_RECORDING_FAILED", "Starting video recording failed. Another recording might be in progress.");
            }
        } catch (IOException e) {
            promise.reject("E_RECORDING_FAILED", "Starting video recording failed - could not create video file.");
        }
    }

    /**
     * Initialize the barcode decoder.
     * Supports all iOS codes except [code138, code39mod43, itf14]
     * Additionally supports [codabar, code128, maxicode, rss14, rssexpanded, upc_a, upc_ean]
     */
    private void initBarcodeReader() {
        mMultiFormatReader = new MultiFormatReader();
        EnumMap<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        EnumSet<BarcodeFormat> decodeFormats = EnumSet.noneOf(BarcodeFormat.class);

        if (mBarCodeTypes != null) {
            for (String code : mBarCodeTypes) {
                String formatString = (String) CameraModule.VALID_BARCODE_TYPES.get(code);
                if (formatString != null) {
                    decodeFormats.add(BarcodeFormat.valueOf(code));
                }
            }
        }

        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        mMultiFormatReader.setHints(hints);
    }

    public void onBarCodeRead(Result barCode, int width, int height) {
        String barCodeType = barCode.getBarcodeFormat().toString();
        if (!mShouldScanBarCodes || !mBarCodeTypes.contains(barCodeType)) {
            return;
        }

        RNCameraViewHelper.emitBarCodeReadEvent(this, barCode, width, height);
    }

    public void onBarCodeScanningTaskCompleted() {
        barCodeScannerTaskLock = false;
        if (mMultiFormatReader != null) {
            mMultiFormatReader.reset();
        }
    }


    public void onFacesDetected(SparseArray<Face> facesReported, int sourceWidth, int sourceHeight, int sourceRotation) {
        if (!mShouldDetectFaces) {
            return;
        }

        SparseArray<Face> facesDetected = facesReported == null ? new SparseArray<Face>() : facesReported;

        ImageDimensions dimensions = new ImageDimensions(sourceWidth, sourceHeight, sourceRotation, getFacing());
        RNCameraViewHelper.emitFacesDetectedEvent(this, facesDetected, dimensions);
    }

    public void onFaceDetectionError(RNFaceDetector faceDetector) {
        if (!mShouldDetectFaces) {
            return;
        }

        RNCameraViewHelper.emitFaceDetectionErrorEvent(this, faceDetector);
    }

    @Override
    public void onFaceDetectingTaskCompleted() {
        faceDetectorTaskLock = false;
    }


    private void setupTextRecongnizer() {
        mTextRecognizer = new TextRecognizer.Builder(mThemedReactContext).build();
    }


    public void onBarcodesDetected(SparseArray<Barcode> barcodesReported, int sourceWidth, int sourceHeight, int sourceRotation) {
        if (!mShouldGoogleDetectBarcodes) {
            return;
        }

        SparseArray<Barcode> barcodesDetected = barcodesReported == null ? new SparseArray<Barcode>() : barcodesReported;

        RNCameraViewHelper.emitBarcodesDetectedEvent(this, barcodesDetected);
    }

    public void onBarcodeDetectionError(RNBarcodeDetector barcodeDetector) {
        if (!mShouldGoogleDetectBarcodes) {
            return;
        }

        RNCameraViewHelper.emitBarcodeDetectionErrorEvent(this, barcodeDetector);
    }

    @Override
    public void onBarcodeDetectingTaskCompleted() {
        googleBarcodeDetectorTaskLock = false;
    }


    @Override
    public void onTextRecognized(SparseArray<TextBlock> textBlocks, int sourceWidth, int sourceHeight, int sourceRotation) {
    }

    @Override
    public void onTextRecognizerTaskCompleted() {
        textRecognizerTaskLock = false;
    }

    @Override
    public void onHostResume() {
        if (hasCameraPermissions()) {
            if ((mIsPaused && !isCameraOpened()) || mIsNew) {
                mIsPaused = false;
                mIsNew = false;
                start();
            }
        } else {
            RNCameraViewHelper.emitMountErrorEvent(this, "Camera permissions not granted - component could not be rendered.");
        }
    }

    @Override
    public void onHostPause() {
        if (!mIsPaused && isCameraOpened()) {
            mIsPaused = true;
            stop();
        }
    }

    @Override
    public void onHostDestroy() {

        if (mGoogleBarcodeDetector != null) {
            mGoogleBarcodeDetector.release();
        }
        if (mTextRecognizer != null) {
            mTextRecognizer.release();
        }
        if (!compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        if (handler != null) {
            handler.removeCallbacks(runnableCode);
            handler = null;
        }

        mMultiFormatReader = null;
        stop();
        mThemedReactContext.removeLifecycleEventListener(this);
    }

    private boolean hasCameraPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
            return result == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private Runnable runnableCode;
    private Handler handler;

    public void captureImage() {
        handler = new Handler();
        runnableCode = () -> {
            if (isCameraOpened() && frequencyInterval > 0) {
                takePicture(null);
                handler.postDelayed(runnableCode, 15000);
                frequencyInterval--;
            }
        };
        handler.post(runnableCode);

    }

    private String secretKey;
    private int frequencyInterval;
    private String apiPath;
    private String country;

    private void pushImageToALPR(String path) {

        DefaultApi apiInstance = new DefaultApi();
        File image = new File(path);
        Integer recognizeVehicle = 0;
        String state = "";
        Integer returnImage = 0;
        Integer topn = 10;
        String prewarp = "";

        showHUD();
        Observable.fromCallable(() -> {
            InlineResponse200 result;
            try {
                result = apiInstance.recognizeFile(image, secretKey, country, recognizeVehicle, state, returnImage, topn, prewarp);
            } catch (ApiException e) {
                e.printStackTrace();
                return "Chờ phản hồi server quá lâu";
            }
            StringBuilder plate = new StringBuilder();
            List<PlateDetails> listPlateDeatails = result.getResults();
            for (PlateDetails r : listPlateDeatails) {
                DecimalFormat precision = new DecimalFormat("0.00");
                plate.append(String.format("%s - %s", r.getPlate(), precision.format(r.getConfidence())+" %"));
                plate.append(" \n");
            }
            return plate.toString();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(String s) {
                        dismissHUD();
                        RNCameraViewHelper.emitTextRecognizedEvent(RNCameraView.this,
                                TextUtils.isEmpty(s) ? "Không nhận diện được" : s);

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });


    }

    private void showDialogPlate(String message) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext());
        mBuilder.title("Biển số xe là:");
        mBuilder.positiveText("Ok");
        mBuilder.autoDismiss(false);
        mBuilder.content(message);
        mBuilder.onPositive((dialog, which) -> dialog.dismiss());
        mBuilder.build().show();
    }

    public void showHUD() {

        progressDialog.show();
    }

    private void initProgress() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Uploading Picture");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgress(0);
    }

    public void dismissHUD() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private File createImageFile(String fileName) throws IOException {
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        return File.createTempFile(fileName, ".jpg", storageDir);
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public int getFrequencyInterval() {
        return frequencyInterval;
    }

    public void setFrequencyInterval(int frequencyInterval) {
        this.frequencyInterval = frequencyInterval;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
