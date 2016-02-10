# Introduction #

System Tray application written in the Groovy scripting language which checks for unread Google Waves.

Icons:
  * Gray - no unread waves
  * Red - auth or connection error
  * Colored - unread waves available

When unread waves are available a small number will appear in the corner of the tray icon.  If more than 9 unread waves just a "+" will appear in the corner.

Hovering over the icon will display a tool-tip indicating the last time waves were checked.

Right clicking the icon will give a pop-up menu.

Left clicking on the icon will open the default browser and automatically sign you into http://wave.google.com


# Requirements #

Java 1.6 and Groovy 1.5.8 or higher.

For Linux need sun 1.6 upgrade 10 or higher which fixed the bug where the system tray did not work with all window managers - see bug id [6438179](http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6438179).  Doesn't appear to be working in latest 1.6 version of openjdk so you will need the sun jdk

# Tested #
  * Ubuntu 9.1 - Groovy Version: 1.5.8 JVM: 1.6.0\_15
  * Windows XP - Groovy Version: 1.6.5 JVM: 1.6.0\_15
  * Mac OS X - Groovy Version: 1.6.5 JVM: ?

# Download #
Right click [Current Version](http://toolbits.googlecode.com/svn/trunk/waveCheck/src/waveCheck.groovy) and Save Link As...