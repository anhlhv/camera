//
//  RNCustomCamera.m
//  TestCustomCamera
//
//  Created by Eric Nguyen on 12/1/18.
//  Copyright © 2018 Facebook. All rights reserved.
//
//#if __has_include(<React/RCTBridge.h>)
#import <React/RCTBridge.h>
#import <React/RCTUIManager.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTLog.h>
#import <React/RCTUtils.h>
#import <React/UIView+React.h>
//#else
//#import "RCTBridge.h"
//#import "RCTUIManager.h"
//#import "RCTEventDispatcher.h"
//#import "RCTLog.h"
//#import "RCTUtils.h"
//#import "UIView+React.h"
//#endif

#import "RNCameraManager.h"
#import "RNCamera.h"

@implementation RNCameraManager

RCT_EXPORT_VIEW_PROPERTY(onCameraReady, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onTextRecognized, RCTDirectEventBlock);


- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup
{
  return YES;
}

RCT_EXPORT_MODULE();

-(UIView *)view {
  return [[RNCamera alloc] initWithBridge:self.bridge];
}

- (NSDictionary *)constantsToExport
{
  return @{
           @"Type" :
             @{@"front" : @(RNCameraTypeFront), @"back" : @(RNCameraTypeBack)},
           @"VideoQuality": @{
               @"2160p": @(RNCameraVideo2160p),
               @"1080p": @(RNCameraVideo1080p),
               @"720p": @(RNCameraVideo720p),
               @"480p": @(RNCameraVideo4x3),
               @"4:3": @(RNCameraVideo4x3),
               @"288p": @(RNCameraVideo288p),
               }
           };
}

- (NSArray<NSString *> *)supportedEvents
{
  return @[@"onCameraReady", @"onTextRecognized"];
}
RCT_CUSTOM_VIEW_PROPERTY(frequencyInterval, NSInteger, RNCamera)
{
  view.frequencyInterval = [RCTConvert NSInteger:json];
//  [view setupVideoDataOutput];
}

RCT_CUSTOM_VIEW_PROPERTY(apiPath, NSString, RNCamera)
{
  view.apiPath = [RCTConvert NSString:json];
//  [view setupVideoDataOutput];
}

RCT_CUSTOM_VIEW_PROPERTY(secretKey, NSString, RNCamera)
{
  view.secretKey = [RCTConvert NSString:json];
  //  [view setupVideoDataOutput];
}

RCT_CUSTOM_VIEW_PROPERTY(country, NSString, RNCamera)
{
  view.country = [RCTConvert NSString:json];
}


RCT_CUSTOM_VIEW_PROPERTY(textRecognizerEnabled, BOOL, RNCamera)
{
  view.canReadText = [RCTConvert BOOL:json];
  [view setupVideoDataOutput];
}


RCT_CUSTOM_VIEW_PROPERTY(type, NSInteger, RNCamera)
{
  if (view.presetCamera != [RCTConvert NSInteger:json]) {
    [view setPresetCamera:[RCTConvert NSInteger:json]];
    [view updateType];
  }
}

RCT_EXPORT_METHOD(resumePreview:(nonnull NSNumber *)reactTag)
{
#if TARGET_IPHONE_SIMULATOR
  return;
#endif
  [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, RNCamera *> *viewRegistry) {
    RNCamera *view = viewRegistry[reactTag];
    if (![view isKindOfClass:[RNCamera class]]) {
      RCTLogError(@"Invalid view returned from registry, expecting RNCamera, got: %@", view);
    } else {
      [view resumePreview];
    }
  }];
}

RCT_EXPORT_METHOD(pausePreview:(nonnull NSNumber *)reactTag)
{
#if TARGET_IPHONE_SIMULATOR
  return;
#endif
  [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, RNCamera *> *viewRegistry) {
    RNCamera *view = viewRegistry[reactTag];
    if (![view isKindOfClass:[RNCamera class]]) {
      RCTLogError(@"Invalid view returned from registry, expecting RNCamera, got: %@", view);
    } else {
      [view pausePreview];
    }
  }];
}

RCT_EXPORT_METHOD(checkDeviceAuthorizationStatus:(RCTPromiseResolveBlock)resolve
                  reject:(__unused RCTPromiseRejectBlock)reject) {
  __block NSString *mediaType = AVMediaTypeVideo;
  
  [AVCaptureDevice requestAccessForMediaType:mediaType completionHandler:^(BOOL granted) {
    if (!granted) {
      resolve(@(granted));
    }
    else {
      mediaType = AVMediaTypeAudio;
      [AVCaptureDevice requestAccessForMediaType:mediaType completionHandler:^(BOOL granted) {
        resolve(@(granted));
      }];
    }
  }];
}

RCT_EXPORT_METHOD(checkVideoAuthorizationStatus:(RCTPromiseResolveBlock)resolve
                  reject:(__unused RCTPromiseRejectBlock)reject) {
  __block NSString *mediaType = AVMediaTypeVideo;
  
  [AVCaptureDevice requestAccessForMediaType:mediaType completionHandler:^(BOOL granted) {
    resolve(@(granted));
  }];
}
@end
