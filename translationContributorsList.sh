#!/usr/bin/bash

for dir in Application/src/main/res/values-*
do
	echo $(echo $dir | sed "s/.*-//"):
	{
		git log --format="	%an" --follow $dir/strings.xml
		git log --format="	%an" --follow "$(echo $dir | sed 's/main/database/')/strings.xml"
		git log --format="	%an" --follow "$(echo $dir | sed 's/main/system/')/strings.xml"
	} | sort | uniq | sed "/Weblate\|Fynn Godau/d" | sed "s/SlaVistaPL/Oliwier Jaszczyszyn/"
done
