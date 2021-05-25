#!/usr/bin/bash

translatorLine() {
	echo $1
}

export -f translatorLine

for dir in Application/src/main/res/values-*
do
	LANG=$(echo $dir | sed "s/.*-//")

	# translate LANG to java Locale specifier
	case $LANG in
		"rBR")
			LANG='"pt", "br"'
			;;
		"rNO")
			LANG='"nb"'
			;;
		*)
			LANG="\"$LANG\""
			;;
	esac

	# echo $LANG
	{
		git log --format="%an" --follow $dir/strings.xml
		git log --format="%an" --follow "$(echo $dir | sed 's/main/database/')/strings.xml"
		git log --format="%an" --follow "$(echo $dir | sed 's/main/system/')/strings.xml"
	} | sort | uniq | sed "/Weblate\|Fynn Godau/d" | xargs -i echo "new Translator(\"{}\", null, new Locale($LANG)),"
done
