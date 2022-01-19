aVsB:
	./gradlew run -PteamA=four -x unpackClient -PteamB=three -Pmaps=eckleburg -PprDofilerEnabled=false
bVsA:
	./gradlew run -PteamA=three -x unpackClient -PteamB=four -Pmaps=eckleburg -PprDofilerEnabled=false
