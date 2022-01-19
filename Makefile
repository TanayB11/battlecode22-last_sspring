aVsB:
	./gradlew run -PteamA=three -x unpackClient -PteamB=three_copy -Pmaps=eckleburg -PprDofilerEnabled=false
bVsA:
	./gradlew run -PteamA=three_copy -x unpackClient -PteamB=three -Pmaps=eckleburg -PprDofilerEnabled=false
