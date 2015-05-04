SAAF is a static analyzer for Android apk files. It stands for Static Android Analysis Framework.

SAAF has several features:
  * It supports Program Slicing on smali code.
  * It offers several quick-checks to check if some given app makes uses of certain features (eg, uses classloaders, calls a method of interest, contains likely patched code, ...).
  * Has a GUI where the APK contents can be viewed and bytecode can be searched.
  * CFGs can be created for (selected) methods.
  * Analysis results can be persisted to an MySQL DB or to XMl files.

The main feature is the ability to calculate program slices for arbitrary method invocations and their corresponding parameters. SAAF will then calculate a slice for this so called slicing criterion and search for all constants which are part of that slice. In other words, SAAF will create def-use chains with the def information being the result and the use information being the slicing criterion. For example the slicing criterion could describe the method _android/telephony/SmsManager->sendTextMessage(...)_ and the first parameter of that method (the telephone number). SAAF will then search for all invocations of that method in the smali code and will search for all constants which could be used as input for that parameter. Doing so it is able to find hardcoded telephone numbers which one can see as suspicious as the user should be able to enter the phone number where messaged are being sent to.

More on program slicing in general can be found in this paper: "Mark Weiser: _Program Slicing_. IEEE Trans. Software Eng. 10(4): 352-357 (1984)". A more thorough description of our slicing algorithm can be found in our paper which was presented at SAC'13: "Slicing Droids: Program Slicing for Smali Code" ([pdf](http://www.syssec.rub.de/research/publications/SlicingDroids/)).

Check out the [wiki](http://code.google.com/p/saaf/wiki/howto) for a description on how to set up and use SAAF.

This work has been supported by the Federal Ministry of Education and Research (grant 01BY1020 -- MobWorm). You may also want to check the project page at http://www.mobworm.de.