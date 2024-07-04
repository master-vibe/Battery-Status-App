import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:plug_2/stop.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  static const platform = const MethodChannel('android_platform_channel');
  bool enabled = false;

  Future<void> _startService() async {
    await platform.invokeMethod('startForegroundService');
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        appBar: AppBar(
          title: Text('Plug'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Visibility(
                visible: !enabled,
                child: ElevatedButton(
                  onPressed: () {
                    _startService();
                    setState(() {
                      enabled = true;
                    });
                  },
                  child: Text('Start Fun'),
                ),
              ),
              Visibility(
                visible: enabled,
                child: Builder(
                  builder: (context) {
                    return ElevatedButton(
                      onPressed: () async {
                        final returnValue = await Navigator.push(context,
                            MaterialPageRoute(builder: (context) => stop()));
                        setState(() {
                          if (returnValue != null) {
                            enabled = returnValue;
                          }
                        });
                      },
                      child: Text(
                        "Stop Right?",
                      ),
                    );
                  },
                ),
              ),
              Visibility(
                visible: enabled,
                child: Text("Plug in your charger."),
              )
            ],
          ),
        ),
      ),
    );
  }
}
