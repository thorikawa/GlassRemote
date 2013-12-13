MyGlazz
=======

The purpose of My Glazz project is re-inventing My Glass app running on PC. My Glazz uses Bluetooth connection and built-in bluetooth protocol to communicate with Google Glass. So you no longer need ADB, Android SDK, root permission or even USB cable to control your glass!

<img src="http://thorikawa.github.io/MyGlazz/img/screenshot1.png" width="300" />&nbsp;&nbsp;&nbsp;&nbsp;<img src="http://thorikawa.github.io/MyGlazz/img/screenshot2.png" width="300" />

## Features
* Screencast
* Mouse control
* Timeline posting (text support only)
* Display device info

## Requirements
* JRE 1.6 or later.
* Bluetooth connection

## Download
[v0.3.0 Executable Jar File](https://github.com/thorikawa/MyGlazz/releases/download/v0.3.0/myglazz-awt-0.3.0.jar)

## Usage

* Make sure bluetooth is on.
* Make sure your "real" My Glass app is not connected to Google Glass.
* Download jar file and just double click it.

## Release Notes

#### Dec 12, 2013
#### v0.3.0 is out!

See [Release page](https://github.com/thorikawa/MyGlazz/releases) for more detail.

## Future Plan
* Full screen support
* Improve swipe recognition by mouse
* Better look and feel
* Stabilize Bluetooth behavior
* Error handling

## License

[Apache Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

## Acknowledgements

Most of UI code is an update of [Android Screen Monitor](https://github.com/adakoda/android-screen-monitor)'s code.

It also uses following open source libraries:

* [BlueCove](http://bluecove.org/) for Bluetooth communication.
* [GlassBluetoothProtocol](https://github.com/thorikawa/GlassBluetoothProtocol) for communicating with Google Glass.
