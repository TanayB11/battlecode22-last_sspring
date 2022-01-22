aVsB:
	./gradlew run -PteamA=five -x unpackClient -PteamB=four -Pmaps=eckleburg -PprDofilerEnabled=false
bVsA:
	./gradlew run -PteamA=four -x unpackClient -PteamB=five -Pmaps=eckleburg -PprDofilerEnabled=false
