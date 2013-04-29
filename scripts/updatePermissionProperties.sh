#!/bin/bash
# Author: Tilman Bender <tilman.bender@rub.de>
#
# This script:
# - downloads a website (e.g. android-permissions.org or developer.android.com
# - parses for permission strings
# - writes a properties file that is used by SAAF to determine "known" permissions
#
#URL="http://www.android-permissions.org/permissionmap.html"
URL="http://developer.android.com/reference/android/Manifest.permission.html"
#PERM_PATTERN="android\.permission\.[A-Z,_]+" 
PERM_PATTERN="([a-z]+\.)+permission\.([a-z]+\.)?[A-Z,_]+"
KEY_PATTERN="[A-Z,_]+"
#PROP_FILE="src/de/rub/syssec/nongui/permissions.properties"
PROP_FILE="permissions.properties"
XML_FILE="permissions.xml"
rm $PROP_FILE 
rm $XML_FILE
rm perms
wget -O perms ${URL}
ouput=$(egrep -o ${PERM_PATTERN} perms | sort | uniq)
echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" >> $XML_FILE
echo "<permissions>" >> $XML_FILE
for LINE in ${ouput}; do
	key=$(echo ${LINE} | egrep -o ${KEY_PATTERN})
	echo "${key}=${LINE}" >> $PROP_FILE 
	echo "<permission name=\"${LINE}\" type=\"platform\" description=\"\" />" >> $XML_FILE
done
echo "</permissions>" >> $XML_FILE

