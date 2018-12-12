package org.reactnative.camera;

import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.google.android.cameraview.AspectRatio;
import com.google.android.cameraview.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CameraViewManager extends ViewGroupManager<RNCameraView> {
    public enum Events {
        EVENT_CAMERA_READY("onCameraReady"),
        EVENT_ON_MOUNT_ERROR("onMountError"),
        EVENT_ON_BAR_CODE_READ("onBarCodeRead"),
        EVENT_ON_FACES_DETECTED("onFacesDetected"),
        EVENT_ON_BARCODES_DETECTED("onGoogleVisionBarcodesDetected"),
        EVENT_ON_FACE_DETECTION_ERROR("onFaceDetectionError"),
        EVENT_ON_BARCODE_DETECTION_ERROR("onGoogleVisionBarcodeDetectionError"),
        EVENT_ON_TEXT_RECOGNIZED("onTextRecognized"),
        EVENT_ON_PICTURE_TAKEN("onPictureTaken"),
        EVENT_ON_PICTURE_SAVED("onPictureSaved");

        private final String mName;

        Events(final String name) {
            mName = name;
        }

        @Override
        public String toString() {
            return mName;
        }
    }

    private static final String REACT_CLASS = "RNCamera";

    @Override
    public void onDropViewInstance(RNCameraView view) {
        view.stop();
        super.onDropViewInstance(view);
    }


    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected RNCameraView createViewInstance(ThemedReactContext themedReactContext) {
        return new RNCameraView(themedReactContext);
    }

    @Override
    @Nullable
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        MapBuilder.Builder<String, Object> builder = MapBuilder.builder();
        for (Events event : Events.values()) {
            builder.put(event.toString(), MapBuilder.of("registrationName", event.toString()));
        }
        return builder.build();
    }

    @ReactProp(name = "type")
    public void setType(RNCameraView view, int type) {
        view.setFacing(type);
    }

    @ReactProp(name = "secretKey")
    public void setSecretKey(RNCameraView view, String secretKey) {
        view.setSecretKey(secretKey);
    }

    @ReactProp(name = "frequencyInterval")
    public void setFrequencyInterval(RNCameraView view, int frequencyInterval) {
        view.setFrequencyInterval(frequencyInterval);
    }

    @ReactProp(name = "apiPath")
    public void setApiPath(RNCameraView view, String apiPath) {
        view.setApiPath(apiPath);
    }

    @ReactProp(name = "country")
    public void setCountry(RNCameraView view, String country) {
        view.setCountry(country);
    }

}