import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:plug_2/main.dart';

class stop extends StatefulWidget {
  const stop({super.key});

  @override
  State<stop> createState() => _stopState();
}

class _stopState extends State<stop> {
  static const platform = const MethodChannel('android_platform_channel');
  int _randomNumber1 = 2;
  int _randomNumber2 = 2;

  final List<List<bool>> _buttonStates = List.generate(
    10,
    (index) => List.generate(10, (index) => false),
  );
  bool _showTryAgain = false;

  @override
  void initState() {
    super.initState();
    _generateRandomNumber();
  }

  void _generateRandomNumber() {
    _randomNumber1 = Random().nextInt(4) + 1;
    _randomNumber2 = Random().nextInt(4) + 1;
  }

  void _executeFunction() {
    stopForegroundService();
    Navigator.pop(context,false);
  }

  Future<void> stopForegroundService() async {
    await platform.invokeMethod('stopForegroundService');
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        GridView.builder(
          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 5, // number of columns
          ),
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          // disable scrolling
          itemCount: 25,
          // 10x10 grid
          itemBuilder: (context, index) {
            int rowIndex = index ~/ 5;
            int columnIndex = index % 5;
            return ElevatedButton(
              style: _buttonStates[rowIndex][columnIndex]
                  ? ButtonStyle(
                      backgroundColor: WidgetStateProperty.all(Colors.red),
                    )
                  : ButtonStyle(
                      backgroundColor: WidgetStateProperty.all(Colors.blue),
                    ),
              onPressed: () {
                setState(() {
                  if (rowIndex != _randomNumber1 ||
                      columnIndex != _randomNumber2) {
                    _showTryAgain = true;
                    for (int i = 0; i < 5; i++) {
                      for (int j = 0; j < 5; j++) {
                        _buttonStates[i][j] = true;
                      }
                    }
                  } else {
                    _executeFunction();
                  }
                });
              },
              child: const Text('Stop',style: TextStyle(color: Colors.white70),),
            );
          },
        ),
        if (_showTryAgain)
          ElevatedButton(
            onPressed: () {
              setState(() {
                _showTryAgain = false;
                for (int i = 0; i < 5; i++) {
                  for (int j = 0; j < 5; j++) {
                    _buttonStates[i][j] = false;
                  }
                }
              });
            },
            child: const Text(
              'Try Again',
              selectionColor: Colors.white,
              style: TextStyle(fontSize: 15),
            ),
          ),
        const Text("Choose the correct Stop...",style: TextStyle(color: Colors.white,fontSize: 28,decoration: TextDecoration.none),)
      ],
    );
  }
}
