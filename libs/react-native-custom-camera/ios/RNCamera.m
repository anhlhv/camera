//
//  RNCamera.m
//  TestCustomCamera
//
//  Created by Eric Nguyen on 12/1/18.
//  Copyright © 2018 Facebook. All rights reserved.
//

#if __has_include(<React/RCTEventDispatcher.h>)
#import <React/RCTEventDispatcher.h>
#import <React/RCTLog.h>
#import <React/RCTUtils.h>
#import <React/UIView+React.h>
#else
#import "RCTEventDispatcher.h"
#import "RCTLog.h"
#import "RCTUtils.h"
#import "UIView+React.h"
#endif



#import "RNCameraUtils.h"
#import "RNCamera.h"

@interface RNCamera()

@property (nonatomic, weak) RCTBridge *bridge;
@property (nonatomic, assign, getter=isSessionPaused) BOOL paused;
@property (nonatomic, copy) RCTDirectEventBlock onCameraReady;
@property (nonatomic, copy) RCTDirectEventBlock onTextRecognized;
@property (nonatomic, assign) BOOL finishedReadingText;
@property (nonatomic, copy) NSDate *start;
@end


@implementation RNCamera

- (id)initWithBridge:(RCTBridge *)bridge {
  if (self = [super init]) {
    self.bridge = bridge;
    self.session = [AVCaptureSession new];
    self.sessionQueue = dispatch_queue_create("customcameraQueue", DISPATCH_QUEUE_SERIAL);
    self.finishedReadingText = true;
    self.start = [NSDate date];
#if !(TARGET_IPHONE_SIMULATOR)
    self.previewLayer = [AVCaptureVideoPreviewLayer layerWithSession:self.session];
    self.previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill;
    self.previewLayer.needsDisplayOnBoundsChange = YES;
#endif
    self.paused = NO;
    [self changePreviewOrientation:[UIApplication sharedApplication].statusBarOrientation];
    [self initializeCaptureSessionInput];
    [self startSession];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(orientationChanged:)
                                                 name:UIDeviceOrientationDidChangeNotification
                                               object:nil];
    //    self.presetCamera = 1;
    if (_frequencyInterval == 0 ) {
      _frequencyInterval = 15; //Default value
    }
    
    if (_apiPath.length <= 0) {
      _apiPath = @"https://api.openalpr.com/v2/recognize";
    }
    
    if (_secretKey.length <= 0) {
      _secretKey = @"sk_c78dca764445839ff97ed52e";
    }
    if (_country.length <= 0) {
      _country = @"jp";
    }
  }
  
  return self;
}

- (void)layoutSubviews
{
  [super layoutSubviews];
  self.previewLayer.frame = self.bounds;
  [self setBackgroundColor:[UIColor blackColor]];
  [self.layer insertSublayer:self.previewLayer atIndex:0];
}

- (void)insertReactSubview:(UIView *)view atIndex:(NSInteger)atIndex
{
  [self insertSubview:view atIndex:atIndex + 1];
  [super insertReactSubview:view atIndex:atIndex];
  return;
}

- (void)removeReactSubview:(UIView *)subview
{
  [subview removeFromSuperview];
  [super removeReactSubview:subview];
  return;
}

- (void)removeFromSuperview
{
  [super removeFromSuperview];
  [[NSNotificationCenter defaultCenter] removeObserver:self name:UIDeviceOrientationDidChangeNotification object:nil];
  [self stopSession];
}


- (void)initializeCaptureSessionInput
{
  if (self.videoCaptureDeviceInput.device.position == self.presetCamera) {
    return;
  }
  __block UIInterfaceOrientation interfaceOrientation;
  
  void (^statusBlock)(void) = ^() {
    interfaceOrientation = [[UIApplication sharedApplication] statusBarOrientation];
  };
  if ([NSThread isMainThread]) {
    statusBlock();
  } else {
    dispatch_sync(dispatch_get_main_queue(), statusBlock);
  }
  
  AVCaptureVideoOrientation orientation = [RNCameraUtils videoOrientationForInterfaceOrientation:interfaceOrientation];
  dispatch_async(self.sessionQueue, ^{
    [self.session beginConfiguration];
    
    NSError *error = nil;
    AVCaptureDevice *captureDevice = [RNCameraUtils deviceWithMediaType:AVMediaTypeVideo preferringPosition:self.presetCamera];
    AVCaptureDeviceInput *captureDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:captureDevice error:&error];
    
    if (error || captureDeviceInput == nil) {
      RCTLog(@"%s: %@", __func__, error);
      return;
    }
    
    [self.session removeInput:self.videoCaptureDeviceInput];
    if ([self.session canAddInput:captureDeviceInput]) {
      [self.session addInput:captureDeviceInput];
      
      self.videoCaptureDeviceInput = captureDeviceInput;
      
      [self.previewLayer.connection setVideoOrientation:orientation];
    }
    
    [self.session commitConfiguration];
  });
}

- (void)startSession
{
#if TARGET_IPHONE_SIMULATOR
  [self onReady:nil];
  return;
#endif
  dispatch_async(self.sessionQueue, ^{
    if (self.presetCamera == AVCaptureDevicePositionUnspecified) {
      return;
    }
    
    // Default video quality AVCaptureSessionPresetHigh if non is provided
    AVCaptureSessionPreset preset =  AVCaptureSessionPresetHigh;
    
    self.session.sessionPreset = preset == AVCaptureSessionPresetHigh ? AVCaptureSessionPresetPhoto: preset;
    
    [self setupVideoDataOutput];
    
    __weak RNCamera *weakSelf = self;
    [self setRuntimeErrorHandlingObserver:
     [NSNotificationCenter.defaultCenter addObserverForName:AVCaptureSessionRuntimeErrorNotification object:self.session queue:nil usingBlock:^(NSNotification *note) {
      RNCamera *strongSelf = weakSelf;
      dispatch_async(strongSelf.sessionQueue, ^{
        // Manually restarting the session since it must
        // have been stopped due to an error.
        [strongSelf.session startRunning];
        [strongSelf onReady:nil];
      });
    }]];
    
    [self.session startRunning];
    [self onReady:nil];
  });
}

- (void)stopSession
{
#if TARGET_IPHONE_SIMULATOR
  return;
#endif
  dispatch_async(self.sessionQueue, ^{
    
    [self stopVideoDataOutput];
    [self.previewLayer removeFromSuperlayer];
    [self.session commitConfiguration];
    [self.session stopRunning];
    for (AVCaptureInput *input in self.session.inputs) {
      [self.session removeInput:input];
    }
    
    for (AVCaptureOutput *output in self.session.outputs) {
      [self.session removeOutput:output];
    }
  });
}

- (void)resumePreview
{
  [[self.previewLayer connection] setEnabled:YES];
}

- (void)pausePreview
{
  [[self.previewLayer connection] setEnabled:NO];
}

-(void)updateType
{
  dispatch_async(self.sessionQueue, ^{
    [self initializeCaptureSessionInput];
    if (!self.session.isRunning) {
      [self startSession];
    }
  });
}

#pragma mark -
- (void)setupVideoDataOutput {
  if ([self canReadText]) {
    self.videoDataOutput = [[AVCaptureVideoDataOutput alloc] init];
    if (![self.session canAddOutput:self.videoDataOutput]) {
      NSLog(@"Failed to setup video data output");
      [self stopVideoDataOutput];
      return;
    }
    NSDictionary *rgbOutputSettings = [NSDictionary
                                       dictionaryWithObject:[NSNumber numberWithInt:kCMPixelFormat_32BGRA]
                                       forKey:(id)kCVPixelBufferPixelFormatTypeKey];
    [self.videoDataOutput setVideoSettings:rgbOutputSettings];
    [self.videoDataOutput setAlwaysDiscardsLateVideoFrames:YES];
    [self.videoDataOutput setSampleBufferDelegate:self queue:self.sessionQueue];
    [self.session addOutput:self.videoDataOutput];
  }else {
    [self stopVideoDataOutput];
  }
  
}

- (void)stopVideoDataOutput {
  if (self.videoDataOutput) {
    [self.session removeOutput:self.videoDataOutput];
  }
  self.videoDataOutput = nil;
}


#pragma mark -
- (void)orientationChanged:(NSNotification *)notification
{
  UIInterfaceOrientation orientation = [[UIApplication sharedApplication] statusBarOrientation];
  [self changePreviewOrientation:orientation];
}

- (void)changePreviewOrientation:(UIInterfaceOrientation)orientation
{
  __weak typeof(self) weakSelf = self;
  AVCaptureVideoOrientation videoOrientation = [RNCameraUtils videoOrientationForInterfaceOrientation:orientation];
  dispatch_async(dispatch_get_main_queue(), ^{
    __strong typeof(self) strongSelf = weakSelf;
    if (strongSelf && strongSelf.previewLayer.connection.isVideoOrientationSupported) {
      [strongSelf.previewLayer.connection setVideoOrientation:videoOrientation];
    }
  });
}

- (void)onReady:(NSDictionary *)event
{
  if (_onCameraReady) {
    _onCameraReady(nil);
  }
  
}

- (void)onText:(NSDictionary *)event
{
  if (_onTextRecognized && _session) {
    _onTextRecognized(event);
  }
}
#pragma mark -
- (void)captureOutput:(AVCaptureOutput *)output didOutputSampleBuffer:(CMSampleBufferRef)sampleBuffer fromConnection:(AVCaptureConnection *)connection {
  // Do not submit image for text recognition too often:
  // 1. we only dispatch events every 500ms anyway
  // 2. wait until previous recognition is finished
  // 3. let user disable text recognition, e.g. onTextRecognized={someCondition ? null : this.textRecognized}
  NSDate *methodFinish = [NSDate date];
  NSTimeInterval timePassed = [methodFinish timeIntervalSinceDate:self.start];
  
  //TODO: Using here a export property to configure timePassed value.
  if (timePassed > self.frequencyInterval && _finishedReadingText && [self canReadText]) {
    CGSize previewSize = CGSizeMake(_previewLayer.frame.size.width, _previewLayer.frame.size.height);
    UIImage *image = [RNCameraUtils convertBufferToUIImage:sampleBuffer previewSize:previewSize];
    // take care of the fact that preview dimensions differ from the ones of the image that we submit for text detection
    //    float scaleX = _previewLayer.frame.size.width / image.size.width;
    //    float scaleY = _previewLayer.frame.size.height / image.size.height;
    
    // find text features
    _finishedReadingText = false;
    self.start = [NSDate date];
    
    //TODO: AFNetworking to submit image here. thpen onDone set the finished= true.
    __weak RNCamera *weakSelf = self;
    [self uploadImage:image onSuccess:^(BOOL success, NSString *result) {
      RNCamera *strongSelf = weakSelf;
      strongSelf.finishedReadingText = true;
          NSDictionary *eventText = @{@"type" : @"TextBlock", @"textBlocks" : result};
          [self onText:eventText];

    }];
//        NSArray *textBlocks = [self.textDetector findTextBlocksInFrame:image scaleX:scaleX scaleY:scaleY];
    
        _finishedReadingText = true;
  }
}

- (void) uploadImage:(UIImage *)image onSuccess:(void(^)(BOOL success, NSString *result))callback {
  NSData *imageData = UIImageJPEGRepresentation(image, 1.0);
  
  NSMutableURLRequest *request = [[AFHTTPRequestSerializer serializer] multipartFormRequestWithMethod:@"POST"
                                                                                            URLString:self.apiPath
                                                                                           parameters:@{@"secret_key": self.secretKey, @"country": self.country}
                                                                            constructingBodyWithBlock:^(id<AFMultipartFormData> formData) {
                                                                              
                                                                              [formData appendPartWithFileData:imageData name:@"image" fileName:@"image.png" mimeType:@"image/png"];
                                                                              
                                                                            } error:nil];
  
  AFURLSessionManager *manager = [[AFURLSessionManager alloc] initWithSessionConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration]];
  
  NSURLSessionUploadTask *uploadTask;
  uploadTask = [manager
                uploadTaskWithStreamedRequest:request
                progress:^(NSProgress * _Nonnull uploadProgress) {
                  // This is not called back on the main queue.
                  // You are responsible for dispatching to the main queue for UI updates
                  dispatch_async(dispatch_get_main_queue(), ^{
                    //Update the progress view
                    //                    [progressView setProgress:uploadProgress.fractionCompleted];
                  });
                }
                completionHandler:^(NSURLResponse * _Nonnull response, id  _Nullable responseObject, NSError * _Nullable error) {
                  if (error) {
                    NSLog(@"Error: %@", error);
                  } else {
                    if ([responseObject isKindOfClass:[NSDictionary class]]) {
                      NSLog(@"%@", responseObject);
                      NSDictionary *responseDict = responseObject;
                      if ([responseDict objectForKey:@"results"] != [NSNull null]) {
                        NSArray *subArray = [responseObject objectForKey:@"results"];
                        if (subArray.count > 0 ) {
                          NSDictionary *finalDict = subArray[0];
                          if ([finalDict objectForKey:@"plate"] != [NSNull null] && [finalDict objectForKey:@"confidence"] != [NSNull null]) {
                            NSString *plate = [finalDict objectForKey:@"plate"];
                            double confidence = [[finalDict objectForKey:@"confidence"] doubleValue];
                            NSLog(@"%@ - %f", plate, confidence);
                            callback(YES, [NSString stringWithFormat:@"Result: %@ \nConfidence: %f", plate, confidence]);
                          }
                        }else {
                          callback(YES, @"There is no results");
                        }
                      }else {
                        callback(NO, @"Something went wrong");
                      }
                    } else {
                      callback(NO, @"Something went wrong");
                    }
                  }
                }];
  
  [uploadTask resume];
}
@end
