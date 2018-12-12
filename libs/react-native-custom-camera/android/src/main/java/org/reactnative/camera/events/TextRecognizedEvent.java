package org.reactnative.camera.events;

import android.support.v4.util.Pools;
import android.util.Log;
import android.util.SparseArray;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.google.android.cameraview.CameraView;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import org.reactnative.camera.CameraViewManager;
import org.reactnative.camera.utils.ImageDimensions;
import org.reactnative.facedetector.FaceDetectorUtils;

import static org.reactnative.camera.RNCameraView.TAG;


public class TextRecognizedEvent extends Event<TextRecognizedEvent> {

    private static final Pools.SynchronizedPool<TextRecognizedEvent> EVENTS_POOL =
            new Pools.SynchronizedPool<>(3);


    private String message;

    private TextRecognizedEvent() {
    }

    public static TextRecognizedEvent obtain(int viewTag, String message) {
        TextRecognizedEvent event = EVENTS_POOL.acquire();
        if (event == null) {
            event = new TextRecognizedEvent();
        }
        event.init(viewTag, message );
        return event;
    }

    private void init(int viewTag, String message) {
        super.init(viewTag);
        this.message = message;
    }

    @Override
    public String getEventName() {
        return CameraViewManager.Events.EVENT_ON_TEXT_RECOGNIZED.toString();
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter) {
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData(message));
    }

    private WritableMap serializeEventData(String message) {
        WritableArray textBlocksList = Arguments.createArray();
        textBlocksList.pushString(message);

        WritableMap event = Arguments.createMap();
        event.putString("type", "textBlock");
        event.putArray("textBlocks", textBlocksList);
        event.putInt("target", getViewTag());
        return event;
    }


}
