import 'dart:convert';

import 'package:flutter/services.dart';

class Services {
  static const platform = MethodChannel('android_platform_channel');

  static Future<void> startService(List<dynamic> listItems) async {
    try {
      String jsonString = jsonEncode(listItems);
      await platform.invokeMethod('startForegroundService',jsonString);
    } catch (e) {
      print('Error starting service: $e');
    }
  }

  static Future<void> stopService() async {
    try {
      await platform.invokeMethod('stopForegroundService');
    } catch (e) {
      print('Error stopping service: $e');
    }
  }

  static Future<List<dynamic>> getList() async{
    try{
      final String jsonString = await platform.invokeMethod("getList") as String;
      List<dynamic> list = jsonDecode(jsonString);
      return list;
    }catch(e){
      print("Error while getting the list: $e");
      return [];
    }
  }

  static Future<bool> isServiceRunning() async {
    try {
      final bool isRunning = await platform.invokeMethod('isServiceRunning');
      return isRunning;
    } catch (e) {
      print('Error checking if service is running: $e');
      return false;
    }
  }
}
