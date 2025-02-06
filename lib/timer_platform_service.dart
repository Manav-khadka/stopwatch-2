import 'dart:async';

import 'package:flutter/services.dart';

class TimerPlatformService {
  static final TimerPlatformService _instance = TimerPlatformService._internal();
  factory TimerPlatformService() => _instance;
  TimerPlatformService._internal();

  static const platform = MethodChannel('timer_channel');

  // Stream controller to broadcast timer updates to UI
  final _timerController = StreamController<String>.broadcast();
  Stream<String> get timerStream => _timerController.stream;

  Future<void> startTimer() async {
    try {
      await platform.invokeMethod('startTimer');
      // You might want to listen for updates from native side
      _setupMethodCallHandler();
    } catch (e) {
      print('Error starting timer: $e');
      throw PlatformException(
        code: 'START_TIMER_ERROR',
        message: 'Failed to start timer: $e',
      );
    }
  }

  Future<void> stopTimer() async {
    try {
      await platform.invokeMethod('stopTimer');
    } catch (e) {
      print('Error stopping timer: $e');
      throw PlatformException(
        code: 'STOP_TIMER_ERROR',
        message: 'Failed to stop timer: $e',
      );
    }
  }

  Future<bool> isTimerRunning() async {
    try {
      final bool running = await platform.invokeMethod('getTimerState');
      return running;
    } catch (e) {
      print('Error getting timer state: $e');
      return false;
    }
  }

  void _setupMethodCallHandler() {
    platform.setMethodCallHandler((MethodCall call) async {
      switch (call.method) {
        case 'timerUpdate':
          final String time = call.arguments as String;
          _timerController.add(time);
          break;
        case 'timerComplete':
        // Handle timer completion
          break;
        case 'timerError':
        // Handle errors from native side
          final String errorMessage = call.arguments as String;
          print('Timer error: $errorMessage');
          break;
      }
    });
  }

  // Method to check if we have notification permissions (for Android 13+)
  Future<bool> checkNotificationPermissions() async {
    try {
      final bool hasPermission = await platform.invokeMethod('checkNotificationPermissions');
      return hasPermission;
    } catch (e) {
      print('Error checking notification permissions: $e');
      return false;
    }
  }

  // Method to request notification permissions (for Android 13+)
  Future<bool> requestNotificationPermissions() async {
    try {
      final bool granted = await platform.invokeMethod('requestNotificationPermissions');
      return granted;
    } catch (e) {
      print('Error requesting notification permissions: $e');
      return false;
    }
  }

  // Dispose method to clean up resources
  void dispose() {
    _timerController.close();
  }
}