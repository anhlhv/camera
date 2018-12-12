//
//  RNCustomCamera.h
//  TestCustomCamera
//
//  Created by Eric Nguyen on 12/1/18.
//  Copyright © 2018 Facebook. All rights reserved.
//

//#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#import <React/RCTViewManager.h>
//#else
//#import "RCTBridgeModule.h"
//#import "RCTViewManager.h"
//#endif

#import <AVFoundation/AVFoundation.h>

typedef NS_ENUM(NSInteger, RNCameraType) {
  RNCameraTypeFront = AVCaptureDevicePositionFront,
  RNCameraTypeBack = AVCaptureDevicePositionBack
};

typedef NS_ENUM(NSInteger, RNCameraVideoResolution) {
  RNCameraVideo2160p = 0,
  RNCameraVideo1080p = 1,
  RNCameraVideo720p = 2,
  RNCameraVideo4x3 = 3,
  RNCameraVideo288p = 4,
};

@interface RNCameraManager : RCTViewManager <RCTBridgeModule>

@end

