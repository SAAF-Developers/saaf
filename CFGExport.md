# Introduction #

During the export of CFGs it can happen that the filename is too long for the filesystem. This happens because the export filename is generated from the Classname + the Methodname and the Arguments passed to the method.



# Details #

Usually a generated name will look like the following: abc\_def(II)V.png

where "abc"   is the classname,<br>
"def"  is the methodname ,<br>
II   are the parameters ,<br>
V is the return value and<br>
.png is the fileextension.<br>
<br>
When the total length of this string exceeds 255 characters it will be too long for most filesystems, so the string will be shortened.<br>
Too shorten the string an md5 sum is calculated, this is done to prevent generating multiple files with the same name (e.g. when only the last parameter is different).<br>
<br>
So when a filename is too long, the md5 over the filename will be calculated and afterwards the end of the filename will be replaced with the md5 hash (keeping the filextension intact).<br>
<br>
As an example a string of the form classname_methodname(param1...paramn)V.cfg would result in a string of the form classname_methodname(param1...md5.cfg<br>
<br>
<br>
When such a replacement occurs, the original name and the replacement name will be saved in a file called: changed method names.txt . This file is placed in the folder of the exported cfgs that is, if all you cfgs are exported into the directory ~/cfgs and the app is called Testapk the "changed method names.txt" file will be placed in the directory ~/cfgs/Testapk_hash/<br>
<br>
The file content looks like this(the ... is just removed to make this easier too read, so in reality the Real Filename line is longer than the generated filename string):<br>
<br>
Generated Filename: <br>
test/package/Testclass_testMethod...test62febe830a4a6764c634d9e263964cee.png<br>
Real Filename:<br>
test/package/Testclass_testMethod...testMethod()V.png<br>