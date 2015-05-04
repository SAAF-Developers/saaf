# Static Android Analysis Framework (SAAF) - A static analyzer for Android apk files.

SAAF has several features:

It supports Program Slicing on smali code.
It offers several quick-checks to check if some given app makes uses of certain features (eg, uses classloaders, calls a method of interest, contains likely patched code, ...).
Has a GUI where the APK contents can be viewed and bytecode can be searched.
CFGs can be created for (selected) methods.
Analysis results can be persisted to an MySQL DB or to XMl files.

The main feature is the ability to calculate program slices for arbitrary method invocations and their corresponding parameters. 
SAAF will then calculate a slice for this so called slicing criterion and search for all constants which are part of that slice. 
In other words, SAAF will create def-use chains with the def information being the result and the use information being the slicing criterion.
For example the slicing criterion could describe the method android/telephony/SmsManager->sendTextMessage(...) and the first parameter 
of that method (the telephone number). SAAF will then search for all invocations of that method in the smali code and will search for all constants 
which could be used as input for that parameter. 

Doing so it is able to find hardcoded telephone numbers which one can see as suspicious as the user should be able to enter the phone number where 
messaged are being sent to.

## Running ##

1. To see the commandline help just type from the SAAF folder
```
sh ./scripts/run_saaf.sh --help
```
SAAF will check for the configuration file and parse it. After that your should be presented with a list of options.


2. To run SAAF in GUI-Mode (not recommended) just dont use any arguments
```
sh ./scripts/run_saaf.sh 
```
3. To run SAAF without gui on an apk file or a folder of apk files
```
sh ./scripts/run_saaf.sh -hl <filename>
```
If <filename> is an apk SAAF will analyze the apk.
If <filename> is a folder SAAF will analyze all apks that are directly contained in that folder (it will not descend into subdirs)

For each APK SAAF will do the following:

###Preprocessing:
0. Generate MD5,SHA1 and SHA256 hashes for the file
1. create a folder for the application in ./bytecode/<nameofapk>_<hashofapk> (from now on called analysis-folder)
2. extract the content of the apk to ./bytecode/<analysis-folder>/apk_content
3. decode the exctracted content to ./bytecode/<analysis-folder>/bytecode/
4. read the AndroidManifest.xml that lies under ./bytecode/<analysis-folder>/bytecode/AndroidManifest.xml
5. parse the SMALI files living at ./bytecode/<analysis-folder>/bytecode/smali
6. generate rolling hashes for the smali files (optional)

###Analysis:
1. Categorize the requested permissions in known/unknown (see conf/permissions.xml)
2. Match heurisitc patterns (see conf/heuristic-patterns.xml)
3. Perform program slicing for functions of interest (see conf/backtracking_patterns.xml)

## Getting Results ##
In normal mode SAAF will analyze an APK and create an XML report.
While this may be useful when when you want to analyze just one sample and have quick glance at its contents,
it is not useful for processing gigabytes of malware.

So if you want to analyze several apks you should look at the INSTALL file how to setup SAAF with MySQL.

Also check the [CLI-FAQ](doc/FAQ-CLI.txt) and [GUI-FAQ](doc/FAQ-GUI.txt) for more info.



