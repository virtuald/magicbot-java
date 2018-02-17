Magicbot framework (Java)
=========================

[![Build Status](https://travis-ci.org/virtuald/magicbot-java.svg?branch=master)](https://travis-ci.org/virtuald/magicbot-java)

This is an implementation of the [RobotPy MagicBot framework](http://robotpy.readthedocs.io/en/stable/frameworks/magicbot.html)
for Java. MagicBot is an opinionated framework for creating Python robot
programs for the FIRST Robotics Competition. It is envisioned to be an easier
to use and less verbose alternative to the WPILib Command framework.

The code in this repository was used in the 2017/2018 season on an FRC robot.
While the code is not as extensively tested as the python framework, the unit
tests from python for the state machine functionality have been ported
to JUnit and are tested on travis-ci.


Differences between the Java and Python implementations
-------------------------------------------------------

* Components must implement the `MagicComponent` interface
* Autonomous modes must implement the `MagicAutonomous` interface (though, you really
  should be inheriting from AutonomousStateMachine)
* Need to call `addAutonomous` and `addComponent` in Robot.java
* The `tunable` functionality is not implemented
* State machines are not tunable via NetworkTables
* No per-component logger objects

Installation
------------

Download the latest Magicbot.jar from https://github.com/virtuald/magicbot-java/releases
and put it in `~/wpilib/user/java/lib`. Magicbot will be available the next time
that you restart eclipse.

Documentation
-------------

Refer to the [MagicBot python documentation](http://robotpy.readthedocs.io/en/stable/frameworks/magicbot.html),
the philosophy is the same and the implementations are very similar.

### Autonomous Mode State Machines

Here's an example of an autonomous state machine that moves a robot forward for
2.5 seconds, rotates for 1 second, then moves forward for another second. We
assume there's a variable in the main robot class of type `DriveTrain` that is
called `driveTrain`, which is copied to this class when the robot is initializing.

``` java
public class MyAutonomous extends AutonomousStateMachine {

  @MagicInject
  DriveTrain driveTrain;

  @TimedState(first=true, duration=2.5, nextState="rotate")
  public void driveForward(){
    driveTrain.move(-0.8, 0);
  }
  
  @TimedState(duration=1.0, nextState="driveForwardAgain")
  public void rotate() {
    driveTrain.move(0, 0.5);
  }
  
  @TimedState(duration=1)
  public void driveForwardAgain() {
    driveTrain.move(-0.8, 0);
  }
}
```

Obviously, this is a trivial example, but hopefully it shows the potential of
the approach! Teams I've worked with have used the python implementation of this
very successfully, and it makes writing complex sequences of steps very easy to
do.

### Magic Injection

Magic injection is cool! Say you have your robot class, and two other objects.

```java
public class Robot extends MagicRobot {
    Component1 component1;
    Component2 component2;
    
    @Override
    public void createObjects() {
        component1 = new Component1();
        component2 = new Component2();
    }
}
```

```java
public class Component1 {
    @MagicInject
    Component2 component2;
}
```

Once the robot has initialized and `createObjects` has exited, then the
`component2` variable in the `Component1` class will be set to the instance
of `component2` that was present in the original Robot class.

This is really useful because you don't have to pass variables around everywhere,
you can just use injection and they'll be injected automatically for you.

### Other stuff

Feel free to edit this README and add better docs!!

Contributing
============

I'm not planning on doing anything except bugfixes for this code, but will
happiliy accept pull requests with new features if they conform to the spirit of
MagicBot.

License
=======

Apache 2.0

Author
======

Dustin Spicuzza (dustin@virtualroadside.com)
