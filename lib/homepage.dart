import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:file_picker/file_picker.dart';

import 'Services.dart';

class Homepage extends StatefulWidget {
  final bool isServiceRunning;

  Homepage({super.key, required this.isServiceRunning});

  @override
  State<Homepage> createState() => _HomepageState();
}

class _HomepageState extends State<Homepage>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;
  static late bool showElectricIcon; // Flag to toggle icons

  List<dynamic> listItems = [
    {
      'title': 'On Plugged In',
      'enabled': true,
      'file_path': "android.resource://com.example.plug_2/raw/full"
    },
    {
      'title': 'On Plugged Out',
      'enabled': true,
      'file_path': "android.resource://com.example.plug_2/raw/full"
    },
    {
      'title': 'On Full Charge',
      'enabled': true,
      'file_path': "android.resource://com.example.plug_2/raw/full"
    },
    {
      'title': 'On Charge At',
      'value': "100",
      'enabled': true,
      'file_path': "android.resource://com.example.plug_2/raw/full"
    },
  ];

  final TextEditingController _textEditingController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _initialize();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _initialize() async {
    _controller = AnimationController(
      duration: const Duration(milliseconds: 400),
      vsync: this,
    )..repeat(reverse: true);

    _animation = Tween<double>(begin: 0.3300, end: 0.3800).animate(
      CurvedAnimation(
        parent: _controller,
        curve: Curves.easeInBack, // Smoother transition with easeInOut curve
      ),
    );

    setState(() {
      showElectricIcon = !widget.isServiceRunning;
    });

    List<dynamic> retrievedList = await Services.getList();
    if (retrievedList.isNotEmpty) {
      setState(() {
        _textEditingController.text =
            (retrievedList[3]["value"] ?? "100").toString();
        listItems = retrievedList;
      });
    }
  }

  void _toggleIcon() async {
    if (showElectricIcon) {
      await Services.startService(listItems);
    } else {
      Services.stopService();
    }
    bool isRunning = await Services.isServiceRunning();
    setState(() {
      showElectricIcon = !isRunning; // Toggle the icon
    });
  }

  Future<void> pickAudioFile(int index) async {
    FilePickerResult? result = await FilePicker.platform.pickFiles(
      type: FileType.custom, // Use custom type to specify extensions
      allowedExtensions: ['mp3', 'wav'], // Specify the audio file extensions
    );

    if (result != null) {
      // Get the path of the selected file
      String? filePath = result.files.single.path;
      listItems[index]['file_path'] = filePath;
      // Do something with the selected file
      print("Selected audio file path: $filePath");
    } else {
      // User canceled the picker
      print("No file selected");
    }
  }

  @override
  Widget build(BuildContext context) {
    final screenWidth = MediaQuery.of(context).size.width;
    return Scaffold(
      backgroundColor: Colors.transparent,
      resizeToAvoidBottomInset: false,
      body: GestureDetector(
        onTap: () {
          // Dismiss the keyboard when tapping outside
          FocusScope.of(context).unfocus();
        },
        child: Container(
          decoration: const BoxDecoration(
            gradient: LinearGradient(
              colors: [Colors.black, Color.fromRGBO(1, 41, 41, 1)],
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
            ),
          ),
          child: SizedBox.expand(
            child: Center(
              child: Container(
                padding: EdgeInsets.symmetric(horizontal: screenWidth * 0.05),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.start,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Container(
                      margin: const EdgeInsets.only(bottom: 20),
                      child: const Text(
                        'Plug',
                        style: TextStyle(
                          fontFamily: 'Merri_Weather',
                          fontSize: 55,
                          fontWeight: FontWeight.w900,
                          color: Colors.white70,
                        ),
                      ),
                    ),
                    Flexible(
                      flex: 2,
                      fit: FlexFit.loose,
                      child: ListView.builder(
                        controller: ScrollController(),
                        addRepaintBoundaries: true,
                        shrinkWrap: true,
                        itemCount: listItems.length,
                        itemBuilder: (context, index) {
                          return Container(
                            margin: const EdgeInsets.all(10),
                            decoration: BoxDecoration(
                              borderRadius: BorderRadius.circular(5),
                              color: listItems[index]['enabled']
                                  ? const Color.fromRGBO(52, 93, 143, 0.3)
                                  : Colors.grey,
                              border: const Border(
                                bottom: BorderSide(
                                  color: Colors.grey,
                                  width: 1.0,
                                ),
                              ),
                            ),
                            child: ListTile(
                              dense: false,
                              title: Text(
                                listItems[index]['title'],
                                style: TextStyle(
                                  color: !listItems[index]['enabled']
                                      ? Colors.black
                                      : Colors.white,
                                  fontWeight: FontWeight.bold,
                                  decoration: !listItems[index]['enabled']
                                      ? TextDecoration.lineThrough
                                      : TextDecoration.none,
                                ),
                              ),
                              trailing: LayoutBuilder(
                                  builder: (context, constraints) {
                                return Row(
                                  mainAxisAlignment: MainAxisAlignment.start,
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                    IconButton(
                                      onPressed: () {
                                        if (showElectricIcon) {
                                          pickAudioFile(index);
                                          setState(() {
                                            listItems;
                                          });
                                        }
                                      },
                                      icon: Icon(
                                        Icons.library_music_outlined,
                                        color: listItems[index]['enabled']
                                            ? Colors.white
                                            : Colors.black,
                                      ),
                                    ),
                                    SizedBox(
                                      width: (listItems[index]['title'] ==
                                              "On Charge At")
                                          ? constraints.minWidth + 40
                                          : 0,
                                      height: (listItems[index]['title'] ==
                                              "On Charge At")
                                          ? constraints.minHeight + 40
                                          : 0,
                                      // Adjust width as needed
                                      child: (listItems[index]['title'] ==
                                              "On Charge At")
                                          ? TextFormField(
                                              textAlign: TextAlign.center,
                                              style: TextStyle(
                                                  color: listItems[index]
                                                          ['enabled']
                                                      ? Colors.white
                                                      : Colors.black),
                                              controller:
                                                  _textEditingController,
                                              inputFormatters: <TextInputFormatter>[
                                                FilteringTextInputFormatter
                                                    .digitsOnly,
                                              ],
                                              onChanged: (String value) {
                                                int input =
                                                    int.tryParse(value) ?? 0;
                                                if (input > 100) {
                                                  input = 100;
                                                }
                                                setState(() {
                                                  listItems[index]["value"] =
                                                      input;
                                                  _textEditingController.text =
                                                      input.toString();
                                                });
                                              },
                                              keyboardType:
                                                  TextInputType.number,
                                              decoration: const InputDecoration(
                                                labelStyle: TextStyle(
                                                    color: Colors.black12),
                                                hintText: '%',
                                                border: OutlineInputBorder(),
                                                contentPadding:
                                                    EdgeInsets.symmetric(
                                                        horizontal: 8),
                                              ),
                                            )
                                          : null,
                                    ),
                                    Switch(
                                      value: listItems[index]['enabled'],
                                      trackOutlineColor:
                                          const WidgetStatePropertyAll(
                                              Colors.transparent),
                                      // thumbColor: const WidgetStatePropertyAll(Colors.white10),
                                      activeTrackColor: Colors.redAccent,
                                      activeColor: Colors.black12,
                                      inactiveThumbColor: Colors.white,
                                      inactiveTrackColor: Colors.teal,
                                      onChanged: (bool value) {
                                        setState(() {

                                          if (showElectricIcon) {
                                            listItems[index]['enabled'] = value;
                                          }
                                        });
                                      },
                                    ),
                                  ],
                                );
                              }),
                            ),
                          );
                        },
                      ),
                    ),
                    const SizedBox(
                      height: 20,
                    ),
                    Expanded(
                      flex: 1,
                      child: Padding(
                        padding: const EdgeInsets.all(10),
                        child: LayoutBuilder(
                          builder: (context, constraints) {
                            final availableHeight = constraints.maxHeight;
                            return Container(
                              decoration: BoxDecoration(
                                shape: BoxShape.circle,
                                backgroundBlendMode: BlendMode.lighten,
                                color: Colors.black,
                                // border: BoxBorder(),
                                boxShadow: [
                                  BoxShadow(
                                    color: showElectricIcon
                                        ? Colors.teal
                                        : Colors.redAccent,
                                    // Shadow color
                                    blurRadius: 50,
                                    // Shadow blur radius
                                    spreadRadius: 11,
                                    // Spread radius
                                    offset: const Offset(
                                        0, 5), // Position of the shadow
                                  ),
                                ],
                              ),
                              margin: EdgeInsets.only(
                                  left: constraints.maxWidth * 0.1,
                                  right: constraints.maxWidth * 0.1),
                              child: AspectRatio(
                                aspectRatio: 1,
                                child: ClipOval(
                                  child: ElevatedButton(
                                    onPressed: _toggleIcon,
                                    style: ElevatedButton.styleFrom(
                                      alignment: Alignment.center,
                                      backgroundColor: Colors.grey,
                                    ),
                                    child: AnimatedBuilder(
                                      animation: _animation,
                                      builder: (context, child) {
                                        return AnimatedSwitcher(
                                          duration:
                                              const Duration(milliseconds: 100),
                                          transitionBuilder:
                                              (child, animation) {
                                            return ScaleTransition(
                                                scale: animation, child: child);
                                          },
                                          child: showElectricIcon
                                              ? Icon(
                                                  Icons.electric_bolt,
                                                  key: const ValueKey(
                                                      'electric_icon'),
                                                  applyTextScaling: true,
                                                  size: availableHeight.clamp(
                                                          constraints.minWidth,
                                                          constraints
                                                              .maxWidth) *
                                                      _animation.value,
                                                  color: Colors.teal,
                                                )
                                              : Icon(
                                                  Icons.stop_circle_outlined,
                                                  key: const ValueKey(
                                                      'stop_icon'),
                                                  size: availableHeight.clamp(
                                                          constraints.minWidth,
                                                          constraints
                                                              .maxWidth) *
                                                      _animation.value,
                                                  // Responsive icon size
                                                  color: Colors.redAccent,
                                                ),
                                        );
                                      },
                                    ),
                                  ),
                                ),
                              ),
                            );
                          },
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
