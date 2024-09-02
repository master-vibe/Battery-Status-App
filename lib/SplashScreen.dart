import 'package:flutter/material.dart';
import 'package:plug_2/homepage.dart';
import 'package:plug_2/Services.dart';

class SplashScreen extends StatefulWidget {
  @override
  _SplashScreenState createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  late Future<bool> _isServiceRunningFuture;

  @override
  void initState() {
    super.initState();
    _isServiceRunningFuture = Services.isServiceRunning();
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<bool>(
      future: _isServiceRunningFuture,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          // Show loading indicator while waiting for the future to complete
          return const Scaffold(
            body: Center(child: CircularProgressIndicator(color: Colors.white,)),
          );
        } else if (snapshot.hasError) {
          // Show error message if future fails
          return Scaffold(
            body: Center(child: Text('Error: ${snapshot.error}')),
          );
        } else if (snapshot.hasData) {
          // When the future completes, use the data
          bool isServiceRunning = snapshot.data ?? false;
          return Homepage(isServiceRunning: isServiceRunning);
        } else {
          // Fallback UI if there's no data
          return Scaffold(
            body: Center(child: Text('No data available')),
          );
        }
      },
    );
  }
}
