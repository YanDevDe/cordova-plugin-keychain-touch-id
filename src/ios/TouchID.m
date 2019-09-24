/*
File: TouchID.m

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
#import "TouchID.h"

#import "KeychainTouchIdCoordinator.h"

@implementation TouchID

- (void)isAvailable:(CDVInvokedUrlCommand*)command{
    if (NSClassFromString(@"LAContext") == NULL) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    self.laContext = [[LAContext alloc] init];
    
    if ([self.laContext canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:nil]) {
        NSString *biometryType = @"";
        if (@available(iOS 11.0, *)) {
            if (self.laContext.biometryType == LABiometryTypeFaceID) {
                biometryType = @"face";
            }
            else {
                biometryType = @"touch";
            }
            [self sendPluginResultWithStatus:CDVCommandStatus_OK message:biometryType callbackId:command.callbackId];
        }
        else {
            [self sendPluginResultWithStatus:CDVCommandStatus_OK message:@"touch" callbackId:command.callbackId];
        }
    }
    else {
        [self sendPluginResultWithStatus:CDVCommandStatus_ERROR message:@"Touch ID not available" callbackId:command.callbackId];
    }
}

- (void)setLocale:(CDVInvokedUrlCommand*)command{
    [self sendPluginResultWithStatus:CDVCommandStatus_OK callbackId:command.callbackId];
}

- (void)has:(CDVInvokedUrlCommand*)command{
    NSString *username = (NSString*)[command.arguments objectAtIndex:0];
    [self ensureBackwardsCompatabilityForUser:username];
    BOOL hasLoginKey = [self hasPasswordForUsername:username];
    if(hasLoginKey){
        [self sendPluginResultWithStatus:CDVCommandStatus_OK callbackId:command.callbackId];
    }
    else{
        [self sendPluginResultWithStatus:CDVCommandStatus_ERROR message:@"No Password in chain" callbackId:command.callbackId];
    }
}

- (void)save:(CDVInvokedUrlCommand*)command{
    KeychainTouchIdCoordinator *touchIdCoordinator = [[KeychainTouchIdCoordinator alloc] init];
    NSString *username = (NSString*)[command.arguments objectAtIndex:0];
    NSString *password = (NSString*)[command.arguments objectAtIndex:1];
    [self ensureBackwardsCompatabilityForUser:username];
    @try {
        [touchIdCoordinator savePassword:password forUsername:username];
        [self sendPluginResultWithStatus:CDVCommandStatus_OK callbackId:command.callbackId];
    }
    @catch(NSException *exception){
        [self sendPluginResultWithStatus:CDVCommandStatus_ERROR message:@"Password could not be saved in chain" callbackId:command.callbackId];
    }
}

-(void)delete:(CDVInvokedUrlCommand*)command{
    KeychainTouchIdCoordinator *touchIdCoordinator = [[KeychainTouchIdCoordinator alloc] init];
    NSString *username = (NSString*)[command.arguments objectAtIndex:0];
    [self ensureBackwardsCompatabilityForUser:username];
    @try {
        [touchIdCoordinator deleteInformationForUsername:username];
        [self sendPluginResultWithStatus:CDVCommandStatus_OK callbackId:command.callbackId];
    }
    @catch(NSException *exception) {
        [self sendPluginResultWithStatus:CDVCommandStatus_ERROR message:@"Could not delete password from chain" callbackId:command.callbackId];
    }
}

-(void)verify:(CDVInvokedUrlCommand*)command{
    NSString *username = (NSString*)[command.arguments objectAtIndex:0];
    NSString *message = (NSString*)[command.arguments objectAtIndex:1];
    [self ensureBackwardsCompatabilityForUser:username];
    self.laContext = [[LAContext alloc] init];

    BOOL hasLoginKey = [self hasPasswordForUsername:username];
    if(hasLoginKey){
        NSError *error;
        BOOL touchIDAvailable = [self.laContext canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&error];

        if(touchIDAvailable){
            [self.laContext evaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics localizedReason:message reply:^(BOOL success, NSError *error) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    
                    if(success){
                        KeychainTouchIdCoordinator *touchIdCoordinator = [[KeychainTouchIdCoordinator alloc] init];
                        NSString *password = [touchIdCoordinator getPasswordForUsername:username];
                        [self sendPluginResultWithStatus:CDVCommandStatus_OK message:password callbackId:command.callbackId];
                    }
                    if(error != nil) {
                        NSDictionary *errorDictionary = @{@"OS":@"iOS",@"ErrorCode":[NSString stringWithFormat:@"%li", (long)error.code],@"ErrorMessage":error.localizedDescription};
                        [self sendPluginResultWithStatus:CDVCommandStatus_ERROR dictMessage:errorDictionary callbackId:command.callbackId];
                    }
                });
            }];

        }
        else{
            if(error)
            {
                //If an error is returned from LA Context (should always be true in this situation)
                NSDictionary *errorDictionary = @{@"OS":@"iOS",@"ErrorCode":[NSString stringWithFormat:@"%li", (long)error.code],@"ErrorMessage":error.localizedDescription};
                [self sendPluginResultWithStatus:CDVCommandStatus_ERROR dictMessage:errorDictionary callbackId:command.callbackId];
            }
            else
            {
                //Should never come to this, but we treat it anyway
                [self sendPluginResultWithStatus:CDVCommandStatus_ERROR message:@"Touch ID not available" callbackId:command.callbackId];
            }
        }
    }
    else{
        [self sendPluginResultWithStatus:CDVCommandStatus_ERROR message:@"-1" callbackId:command.callbackId];
    }
}

- (BOOL) hasPasswordForUsername:(NSString*) username {
    KeychainTouchIdCoordinator *touchIdCoordinator = [[KeychainTouchIdCoordinator alloc] init];
    NSString *password = [touchIdCoordinator getPasswordForUsername:username];
    return password != nil && password.length > 0;
}

- (void) ensureBackwardsCompatabilityForUser:(NSString*) username {
    KeychainTouchIdCoordinator *touchIdCoordinator = [[KeychainTouchIdCoordinator alloc] init];
    if ([touchIdCoordinator needsMigrationForUsername:username]) {
        [touchIdCoordinator migrateUsername:username];
    }
}

- (void) sendPluginResultWithStatus:(CDVCommandStatus) status callbackId:(NSString*) callbackId {
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:status];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

- (void) sendPluginResultWithStatus:(CDVCommandStatus) status message:(NSString*) message callbackId:(NSString*) callbackId {
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:status messageAsString:message];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

- (void) sendPluginResultWithStatus:(CDVCommandStatus) status dictMessage:(NSDictionary*) message callbackId:(NSString*) callbackId {
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:status messageAsDictionary:message];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

@end
