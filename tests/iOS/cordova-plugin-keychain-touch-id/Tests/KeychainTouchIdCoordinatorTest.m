/*
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

#import <XCTest/XCTest.h>

#import "KeychainTouchIdCoordinator.h"


@interface KeychainTouchIdCoordinatorTest : XCTestCase

@end

@implementation KeychainTouchIdCoordinatorTest

static NSString* USERNAME = @"test@user.com";

- (void)setUp {
    NSString *domainName = [[NSBundle mainBundle] bundleIdentifier];
    [[NSUserDefaults standardUserDefaults] removePersistentDomainForName:domainName];
    
    KeychainTouchIdCoordinator *coordinator = [[KeychainTouchIdCoordinator alloc] init];
    [coordinator deleteInformationForUsername:USERNAME];
    
    NSString *password = [coordinator getPasswordForUsername:USERNAME];
    XCTAssertEqual(@"", password);
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
}

#pragma mark - Saving & Reading passwords
- (void)testInitialEmptyPassword {
    KeychainTouchIdCoordinator *coordinator = [[KeychainTouchIdCoordinator alloc] init];
    
    NSString *password = [coordinator getPasswordForUsername:USERNAME];
    
    XCTAssertTrue([password isEqualToString:@""]);
}

- (void) testSavingPassword {
    KeychainTouchIdCoordinator *coordinator = [[KeychainTouchIdCoordinator alloc] init];
    
    [coordinator savePassword:@"SuperSecret" forUsername:USERNAME];
    
    NSString *password = [coordinator getPasswordForUsername:USERNAME];
    XCTAssertTrue([password isEqualToString:@"SuperSecret"]);
}

- (void) testSavingMultipleUsersSimultaneously {
    KeychainTouchIdCoordinator *coordinator = [[KeychainTouchIdCoordinator alloc] init];
    
    [coordinator savePassword:@"SuperSecret_1" forUsername:@"user@name.com"];
    [coordinator savePassword:@"NotSecret_3" forUsername:@"testuser007@gmail.com"];
    
    NSString *passwordUser2 = [coordinator getPasswordForUsername:@"testuser007@gmail.com"];
    NSString *passwordUser1 = [coordinator getPasswordForUsername:@"user@name.com"];
    XCTAssertTrue([passwordUser1 isEqualToString:@"SuperSecret_1"]);
    XCTAssertTrue([passwordUser2 isEqualToString:@"NotSecret_3"]);
}

#pragma mark - Deleting passwords
- (void) testDeletePassword {
    KeychainTouchIdCoordinator *coordinator = [[KeychainTouchIdCoordinator alloc] init];
    [coordinator savePassword:@"SuperSecret" forUsername:USERNAME];
    
    [coordinator deleteInformationForUsername:USERNAME];
    
    NSString *password = [coordinator getPasswordForUsername:USERNAME];
    XCTAssertTrue([password isEqualToString:@""]);
}

- (void) testDeletePasswordOneInMultipleUsers {
    KeychainTouchIdCoordinator *coordinator = [[KeychainTouchIdCoordinator alloc] init];
    [coordinator savePassword:@"SuperSecret_1" forUsername:@"user@name.com"];
    [coordinator savePassword:@"NotSecret_3" forUsername:@"testuser007@gmail.com"];
    
    [coordinator deleteInformationForUsername:@"user@name.com"];
    
    NSString *passwordUser2 = [coordinator getPasswordForUsername:@"testuser007@gmail.com"];
    NSString *passwordUser1 = [coordinator getPasswordForUsername:@"user@name.com"];
    XCTAssertTrue([passwordUser1 isEqualToString:@""]);
    XCTAssertTrue([passwordUser2 isEqualToString:@"NotSecret_3"]);
}


#pragma mark - Migration Detection
- (void) testMigrationDetectionWhenInexistent {
    KeychainTouchIdCoordinator *coordinator = [[KeychainTouchIdCoordinator alloc] init];
    
    BOOL needsMigration = [coordinator needsMigrationForUsername:USERNAME];
    
    XCTAssertFalse(needsMigration);
}

- (void) testMigrationDetectionWhenNotNecessary {
    [[NSUserDefaults standardUserDefaults] setBool:NO forKey:USERNAME];
    KeychainTouchIdCoordinator *coordinator = [[KeychainTouchIdCoordinator alloc] init];
    
    BOOL needsMigration = [coordinator needsMigrationForUsername:USERNAME];
    
    XCTAssertFalse(needsMigration);
}

- (void) testMigrationDetectionWhenNecessary {
    [[NSUserDefaults standardUserDefaults] setBool:YES forKey:USERNAME];
    KeychainTouchIdCoordinator *coordinator = [[KeychainTouchIdCoordinator alloc] init];
    
    BOOL needsMigration = [coordinator needsMigrationForUsername:USERNAME];
    
    XCTAssertTrue(needsMigration);
}

#pragma mark - Migration
- (void) testMigrationSavesPasswordToNewLocation {
    KeychainTouchIdCoordinator *coordinator = [[KeychainTouchIdCoordinator alloc] init];
    [coordinator savePassword:@"SuperSecret" forUsername:@"com.apple.dts.KeychainUI"];
    
    BOOL success = [coordinator migrateUsername:USERNAME];
    
    NSString *password = [coordinator getPasswordForUsername:USERNAME];
    XCTAssertTrue([password isEqualToString:@"SuperSecret"]);
    XCTAssertTrue(success);
}

- (void) testMigrationDeletsPasswordInOldLocation {
    KeychainTouchIdCoordinator *coordinator = [[KeychainTouchIdCoordinator alloc] init];
    [coordinator savePassword:@"SuperSecret" forUsername:@"com.apple.dts.KeychainUI"];
    
    BOOL success = [coordinator migrateUsername:USERNAME];
    
    NSString *password = [coordinator getPasswordForUsername:@"com.apple.dts.KeychainUI"];
    XCTAssertTrue([password isEqualToString:@""]);
    XCTAssertTrue(success);
}

- (void) testMigrationSetsUserDefaultsToFalse {
    KeychainTouchIdCoordinator *coordinator = [[KeychainTouchIdCoordinator alloc] init];
    [coordinator savePassword:@"SuperSecret" forUsername:@"com.apple.dts.KeychainUI"];
    
    BOOL success = [coordinator migrateUsername:USERNAME];
    
    BOOL migrationRequired = [coordinator needsMigrationForUsername:USERNAME];
    XCTAssertFalse(migrationRequired);
    XCTAssertTrue(success);
}

#pragma mark - Migration Mismatch
- (void) testMigrationWhenNoKeychainIsPresent {
    [[NSUserDefaults standardUserDefaults] setBool:YES forKey:USERNAME];
    KeychainTouchIdCoordinator *coordinator = [[KeychainTouchIdCoordinator alloc] init];
    
    BOOL success = [coordinator migrateUsername:USERNAME];
    
    XCTAssertFalse(success);
}

@end
