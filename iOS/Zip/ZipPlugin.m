/*
 * Copyright (C) 2012 by Emergya
 *
 * Author: Alejandro Leiva <aleiva@emergya.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

#import "ZipPlugin.h"

#import "ZipFile.h"
#import "ZipException.h"
#import "FileInZipInfo.h"
#import "ZipWriteStream.h"
#import "ZipReadStream.h"

@implementation ZipPlugin

- (void) info:(NSMutableArray*)arguments withDict:(NSMutableDictionary*)options
{
    NSString* callbackId = [arguments pop];
    VERIFY_ARGUMENTS(arguments, 1, callbackId)
    
    NSString* source = [arguments objectAtIndex:0];
    
    CDVPluginResult* result = nil;
    NSString* jsString = nil;
    
    @try {
        ZipFile *unzipFile = [[ZipFile alloc] initWithFileName:source mode:ZipFileModeUnzip];
        NSArray *infos = [unzipFile listFileInZipInfos];
        
        for (FileInZipInfo *info in infos) {
            NSLog(@"FILE - %@ %@ %d (%d)", info.name, info.date, info.size, info.level);
        }
        
        NSInteger entries = [unzipFile numFilesInZip];
        NSLog(@"Entries: %d", entries);
        
        [unzipFile close];
        
        // TODO: Use CDVFile getDirectoryEntry, it provides the same info as a NSDictionary.
        NSMutableDictionary* zipEntry = [NSMutableDictionary dictionaryWithCapacity:5];
        NSString* lastPart = [source lastPathComponent];
        
        [zipEntry setObject:[NSNumber numberWithBool: YES]  forKey:@"isFile"];
        [zipEntry setObject:[NSNumber numberWithBool: NO]  forKey:@"isDirectory"];
        [zipEntry setObject: source forKey: @"fullPath"];
        [zipEntry setObject: lastPart forKey:@"name"];
        [zipEntry setObject:[NSNumber numberWithInteger: entries] forKey:@"entries"];

        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:zipEntry];
        jsString = [result toSuccessCallbackString:callbackId];
        
    }
    @catch (ZipException *ze) {
        NSLog(@"ZipException caught: %d - %@", ze.error, [ze reason]);
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT messageAsString:[ze reason]];
        jsString = [result toErrorCallbackString:callbackId];
    }
    @catch (id e) {
        NSLog(@"Exception caught: %@ - %@", [[e class] description], [e description]);
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT messageAsString:[e description]];
        jsString = [result toErrorCallbackString:callbackId];
    }
    @finally {
        if (jsString != nil)
            [self writeJavascript: jsString];
    }
   
}

- (void) compress:(NSMutableArray *)arguments withDict:(NSMutableDictionary *)options
{
    
}

- (void) uncompress:(NSMutableArray*)arguments withDict:(NSMutableDictionary*)options
{
    NSString* callbackId = [arguments pop];
    VERIFY_ARGUMENTS(arguments, 1, callbackId)
    
    CDVPluginResult* result = nil;
    NSString* jsString = nil;
    
    NSString* source = [arguments objectAtIndex:0];
    NSString* target = [arguments objectAtIndex:1];
    NSLog(@"source: %@ target: %@", source, target);
    
    [self writeJavascript: jsString];
}

@end
