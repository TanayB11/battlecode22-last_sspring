aVsB:
	./gradlew run -PteamA=sma -x unpackClient -PteamB=four -Pmaps=colosseum -PprDofilerEnabled=false
bVsA:
	./gradlew run -PteamA=four -x unpackClient -PteamB=sma -Pmaps=colosseum -PprDofilerEnabled=false
