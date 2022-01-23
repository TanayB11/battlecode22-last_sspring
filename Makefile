aVsB:
	./gradlew run -PteamA=sma -x unpackClient -PteamB=four -Pmaps=intersection -PprDofilerEnabled=false
bVsA:
	./gradlew run -PteamA=four -x unpackClient -PteamB=sma -Pmaps=intersection -PprDofilerEnabled=false
