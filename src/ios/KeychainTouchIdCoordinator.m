/*
File: KeychainTouchIdCoordinator.m
Copyright © 2019 Ergon Informatik AG

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

#import "KeychainTouchIdCoordinator.h"

#import "KeychainItemWrapper.h"

@implementation KeychainTouchIdCoordinator



-(NSString*) getPasswordForUsername:(NSString*) username {
    KeychainItemWrapper *keychainItem = [self keychainItemForUsername:username];
    return [keychainItem objectForKey:(__bridge NSString *)kSecValueData];
}

-(void) savePassword:(NSString*) password forUsername:(NSString*) username {
    KeychainItemWrapper *keychainItem = [self keychainItemForUsername:username];
    [keychainItem setObject:username forKey:(__bridge NSString *)kSecAttrAccount];
    [keychainItem setObject:password forKey:(__bridge NSString *)kSecValueData];
}

-(void) deleteInformationForUsername:(NSString*) username {
    KeychainItemWrapper *keychainItem = [self keychainItemForUsername:username];
    [keychainItem resetKeychainItem];
}

#pragma mark - Backwards compatability
-(BOOL) needsMigrationForUsername:(NSString*) username {
    return [[NSUserDefaults standardUserDefaults] boolForKey:username];
}

-(BOOL) migrateUsername:(NSString*) username {
    KeychainItemWrapper *oldKeychainItem = [self keychainItemForUsername:@"com.apple.dts.KeychainUI"];
    id result = [oldKeychainItem objectForKey:@"v_Data"];
    if (result == nil || ![result isKindOfClass:NSString.class] || ((NSString*)result).length == 0) {
        NSLog(@"Migration not possible. No password found.");
        return NO;
    }
    NSString *password = (NSString*) result;
    [self savePassword:password forUsername:username];
    [oldKeychainItem resetKeychainItem];
    [[NSUserDefaults standardUserDefaults] removeObjectForKey:username];
    return YES;
}

#pragma mark - Helper
- (KeychainItemWrapper*) keychainItemForUsername:(NSString*) username {
    return [[KeychainItemWrapper alloc] initWithIdentifier:username accessGroup:nil];
}

@end
