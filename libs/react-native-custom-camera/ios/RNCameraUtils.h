//
//  RNCameraUtils.h
//  TestCustomCamera
//
//  Created by Eric Nguyen on 12/2/18.
//  Copyright © 2018 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#import <UIKit/UIKit.h>
#import "RNCameraManager.h"

@interface RNCameraUtils : NSObject

// Camera utilities
+ (AVCaptureDevice *)deviceWithMediaType:(NSString *)mediaType preferringPosition:(AVCaptureDevicePosition)position;

// Enum conversions
+ (NSString *)captureSessionPresetForVideoResolution:(RNCameraVideoResolution)resolution;
+ (AVCaptureVideoOrientation)videoOrientationForInterfaceOrientation:(UIInterfaceOrientation)orientation;

// Text detector utilities
+ (UIImage *)convertBufferToUIImage:(CMSampleBufferRef)sampleBuffer previewSize:(CGSize)previewSize;

@end

