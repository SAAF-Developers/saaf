# Dependencies #

All dependencies are included in the source tree. If you check everything out, you should be fine.

# Usage #

You can run the tool with '_java -jar saaf.jar_' if all libraries are present in your classpath. Otherwise, you can run the _run-saaf.sh_ script if you are using Linux etc. Without parameters SAAF will start in GUI mode. If you want to analyze a bunch of apks in one step, you can run it this way: _run-saaf.sh -hl -r /some/directory/_.

SAAF will automatically start x analysis threads, where x is your number of cores-1. You can define the number of threads in the config file. If you make use of many analysis threads, you might have to adjust the heap space available to your Java VM. You can change this value in the run script or directly as a VM parameter.

All available parameters and a description thereof can be seen with the _-h_ parameter.


# Results #

By default SAAF tries to persist all results in a MysQL DB which is configured in the config file or it will generate an XML file if the -nodb/--no-database option is used. Results are best viewed with the help of the defined views in the database.

The most interesting results can be found in the following 2 views: v\_bt and v\_h. bt is referring to backtrack patterns which describe the slicing criteria and h stands for the heuristic patterns or quick checks.

## Program Slicing ##

The backtrack\_pattern view contains the following information.

| name | the apk name |
|:-----|:-------------|
| apk\_id |  the id of the apk |
| ana\_id |  the analysis id |
| cID |  a referene to the class where the result was found |
| mID |  a referene to the method where the result was found  |
| PatternID |  the backtrack\_pattern id |
| pattern\_method |  the method of the slicing criterion |
| variabe\_type |  This describes the type of found constant. This might not always be a local or anonymous constant. SAAF also tracks registers over method boundaries and if, eg, the tracked constant is part the parameter set of an invoked method, this will also be saved if the invoked method is not part of the smali code. This would, eg, yield StringBuilder.toString() as a result (The StringBuilder object/register would also be backtracked). Such a result would be labeled EXTERNAL\_METHOD. Additionally, if the tracked register is part a method parameter and the slice reaches the beginning of the method, SAAF will search for all invocations of that method. If none is found, the corresponding method will be saved as UNCALLED\_METHOD. This can reveal dead code or the usage of the Reflection API. The value FIELD\_CONSTANT represents that the constant was found "in" a field. INTERNAL\_BYTECODE\_OP is used if something unusual happens, eg, if an exception is moved to the tracked register. ARRAY is used if the constant is part of an array. MATH\_OPCODE\_CONSTANT describes a constant values which is part of some math operation. |
| Type\_Descr |  The inferred type of the constant, eg, java/lang/Object, might also be UNKNOWN. |
| Type |  The type of the found value (constant). _Array_ if the constant is an array. _String_ if the constant is a String. All native types (_int_, _byte_, ...). _Math-Operator_ if the constant was found in a Math-opcode which makes use of a constant. _Other-Class_ describes other classes, eg, java/lang/Object. _Unkown_ if the type could not be interfered. |
| fuzzy\_level  |  This value describes how accurate the result is. 0 means absolute accurate and the higher the value, the further "away" was the constant found in relation to the slicing criterion, eg, in other methods. The value is increased by one for each method that is parth of the slice starting at the second method and if the tracked register is part of an invocation with an unknown method, eg, StringBuilder.append(). The StringBuilder object/register would be tracked with a fuzzy value of the old tracked register plus 1. If the tracking proceeds at an array or a global (not private) field, the fuzzy value is immediately increased by a defined offset (momentary 8) in order to mitigate the problem of overtracking which results in too many results. The slicing process will end for each register which has a fuzzy value greater than 10. |

The backtrack\_results table contains more information for the found constant. Next to others there are:

| in\_ad\_framework |  was this found in an ad package path? |
|:------------------|:---------------------------------------|
| search\_Id | if multiple method invocations match the slicing criterion, this integer will be the same for all results which were found for one specific method invocation. Useful for, eg, sendTextMessage(nr, null, text, null, null) to see which number correlates to which text part. The id repeats each analysis but is unique for one analysis and the corresponding invocation opcode. |
| array\_dimension | the array dimension, 0 for non array constants |
| argument |  the index of the parameter for the slicing criterion |


## Quick Check (Heuristic) ##

The view v\_h contains all the results for the quick checks. The results most of the time state that something was found in the program, eg, the usage of the java.lang.Reflection API was detected. These results can be used to get a quick overview of what an application is capable of.

All the patterns in the view contain the class, method and line number (if any) where the pattern matched. This could, eg, be a method descriptor if a superclass was searched and found. Another example would be a codeline in a method where some method of interest is invoked.



# Adding new quick checks and slicing criterions #

Just take a look into the config directory and edit the corresponding xml files for your own patterns.

# TODO #
  * The GUI needs a lot of love (currently all operations run on the main thread).
  * The Slicing algorithm cannot pinpoint all types correctly etc and will certainly miss some values.
  * The XML configuration for the quick checks (heuristic) does not fit the needs, it has to be rewritten.
  * The DB column and table names need an overhaul.
  * We are sure there are some bugs to be found.