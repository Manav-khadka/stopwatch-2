import 'dart:async';
import 'package:flutter/material.dart';
import 'package:stopwatch/timer_platform_service.dart';

class StopwatchWidget extends StatefulWidget {
  @override
  _StopwatchWidgetState createState() => _StopwatchWidgetState();
}

class _StopwatchWidgetState extends State<StopwatchWidget> {
  final TimerPlatformService _timerService = TimerPlatformService();
  bool _isRunning = false;
  String _currentTime = "00:00:00";

  @override
  void initState() {
    super.initState();
    _checkPermissionsAndSetup();
    _listenToTimerUpdates();
  }

  Future<void> _checkPermissionsAndSetup() async {
    if (!await _timerService.checkNotificationPermissions()) {
      await _timerService.requestNotificationPermissions();
    }

    // Check if timer was already running
    _isRunning = await _timerService.isTimerRunning();
    setState(() {});
  }

  void _listenToTimerUpdates() {
    _timerService.timerStream.listen(
          (String time) {
        setState(() {
          _currentTime = time;
        });
      },
      onError: (error) {
        print('Timer stream error: $error');
      },
    );
  }

  Future<void> _toggleTimer() async {
    try {
      if (_isRunning) {
        await _timerService.stopTimer();
      } else {
        await _timerService.startTimer();
      }
      setState(() {
        _isRunning = !_isRunning;
      });
    } catch (e) {
      // Show error to user
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Text(
          _currentTime,
          style: TextStyle(fontSize: 48, fontWeight: FontWeight.bold),
        ),
        SizedBox(height: 20),
        ElevatedButton(
          onPressed: _toggleTimer,
          child: Text(_isRunning ? 'Stop' : 'Start'),
        ),
      ],
    );
  }

  @override
  void dispose() {
    _timerService.dispose();
    super.dispose();
  }
}