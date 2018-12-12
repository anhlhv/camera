//
//  RNCamera.h
//  TestCustomCamera
//
//  Created by Eric Nguyen on 12/1/18.
//  Copyright © 2018 Facebook. All rights reserved.
//
//#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#import <React/RCTBridge.h>
//#else
//#import "RCTBridgeModule.h"
//#import "RCTBridge.h"
//#endif
#import <AVFoundation/AVFoundation.h>
#import <UIKit/UIKit.h>
#import <AFNetworking/AFNetworking.h>


@interface RNCamera : UIView <AVCaptureVideoDataOutputSampleBufferDelegate, AVCaptureMetadataOutputObjectsDelegate>

@property(nonatomic, strong) dispatch_queue_t sessionQueue;
@property(nonatomic, strong) AVCaptureSession *session;
@property(nonatomic, strong) id runtimeErrorHandlingObserver;
@property(nonatomic, strong) AVCaptureVideoPreviewLayer *previewLayer;
@property(nonatomic, strong) AVCaptureDeviceInput *videoCaptureDeviceInput;
@property(nonatomic, strong) AVCaptureVideoDataOutput *videoDataOutput;

@property(nonatomic, assign) NSInteger presetCamera;
@property (nonatomic, assign) BOOL canReadText;

@property (nonatomic, assign) NSInteger frequencyInterval;
@property (nonatomic, copy) NSString  *apiPath;
@property (nonatomic, copy) NSString  *secretKey;
@property (nonatomic, copy) NSString  *country;
- (id)initWithBridge:(RCTBridge *)bridge;

//Update camera preset
- (void)updateType;
- (void)setupVideoDataOutput;

- (void)resumePreview;
- (void)pausePreview;

- (void)onMountingError:(NSDictionary *)event;
- (void)onReady:(NSDictionary *)event;
- (void)onText:(NSDictionary *)event;
@end

