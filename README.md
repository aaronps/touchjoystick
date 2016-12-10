# touchjoystick - Touch Joystick library for Android

Currently only one joystick defined: SplitJoystick.

## Demo

Basic demo @ [touchjoystick-demo](https://github.com/aaronps/touchjoystick-demo).

Demo with tank @ [mbtjoy-demo](https://github.com/aaronps/mbtjoy-demo).

## Usage

Use a git clone/submodule/download then import into your project.

## SplitJoystick

This divides the screen in two halves, left half is up/down, right half is
left/right.

There is a little margin on the borders and on the center to avoid problems with
the fingers being almost out of the screen, or too near the center of the
screen.

The SplitJoystick class adds itself as onTouchListener of the activity.

```java
SplitJoystick mTouchJoystick = new SplitJoystick(listener, activity);
```
