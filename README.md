# Algorytm adaptarcyjnego sterowania sygnalizacją

---

## Wymagania
+ Java 21+
+ Maven 3.9+

---

## CI/CD
[![CI](https://github.com/prbartosh/automatic_traffic_lights_av/actions/workflows/ci.yml/badge.svg)](https://github.com/prbartosh/automatic_traffic_lights_av/actions)

 Projekt konfiguruje automatyczny pipeline GitHub Actions który przy kazdym pushu:
+ Buduje projekt z Maven
+ Uruchamia wszystkie testy jednostkowe i integracyjne
+ Buduje obrz Docker i uruchmia smoke test

## Docker

Aplikacje jest dostepna jako kontener Docker:
### Budowanie
```bash
docker build -t traffic-simulation .
```

### Uruchomienie
```bash
docker run -v $(pwd)/example:/data traffic-simulation /data/input.json /data/output.json
```

Obraz używa multi-stage build (Maven -> JRE) dla minimalnego rozmiaru produkcyjnego

## Makefile

Projekt korzysta makefile i umożliwia:
```bash
make build        # budowanie
make test         # testowanie
make run          # uruchomienie z przykładem
make docker-run   # uruchomienie przez Docker
```

---

## Opis algorytmu

Skrzyżowanie działa w dwóch naprzemiennych fazach:
- **NS_GREEN** — Północ + Południe mają zielone, Wschód + Zachód mają czerwone
- **EW_GREEN** — Północ + Południe mają czerwone, Wschód + Zachód mają zielone

Każda zmiana fazy przechodzi przez stan **YELLOW** (NS_YELLOW lub EW_YELLOW), podczas którego nie są wypuszczane żadne pojazdy.

### Decyzja o zmianie fazy
Na początku każdego kroku (`step`), kontroler ocenia, czy należy zmienić aktualną fazę. 
Decyzja podejmowana jest w poniższej kolejności priorytetów:

**1. YELLOW zawsze traw jeden krok.**
Jeśli aktualną fazą jest YELLOW, następuje zmiana na kolejną fazę (GREEN)

**2. Po aktywnej stronie nie ma pojazdów i druga strona czeka.**

Jeśli na drogach z aktualnie zielonym światłem nie ma pojazdów i inni czekają -> zmieniamy od razu na czerwone z pominięciem żółtego i ustawiamy innym zielone

**3. Minimalny czas trwania fazy = 2.**

**4. Maksymalny czas trwania fazy = 8.**

**5. Próg adaptacji (50%)**

Jeśli `score_alternative > score_current * 1.5` czyli w przeciwnej fazie czeka wicej niż 50% samochodów z obecnej fazy, kontroler dokonuje wczesnej zmiany fazy. Wyniki (scores) są obliczane jako suma rozmiarów kolejek dla każdej drogi w danej fazie:


```text
score_NS = queue(NORTH) + queue(SOUTH)
score_EW = queue(EAST)  + queue(WEST)
```

### Schemat wykonania kroku
```text
1. Sprawdź shouldSwitchPhase()    →  zmień current phase
2. Zastosuj obecną fazę        →  ustaw GREEN / RED / YELLOW na każdej z dróg
3. Wypuść pojazdy              →  pierwszy pojazd z każdej drogi ze światłem GREEN opuszcza skrzyżowanie
4. Zwiększ stepsInCurrentPhase
5. Zwróć listę ID pojazdów, które opuściły skrzyżowanie
```

### Gwarancja bezpieczeństwa
Zbiór dróg, które otrzymują światło GREEN w jakimkolwiek kroku, jest definiowany wyłącznie przez metodę `Phase.greenDirections()`:
- `NS_GREEN` → {NORTH, SOUTH}
- `EW_GREEN` → {EAST, WEST}
- `NS_YELLOW` / `EW_YELLOW`  (żadna droga nie ma światła GREEN)

Ponieważ w każdym momencie aktywna jest dokładnie jedna faza, z punktu widzenia struktury niemożliwe jest, aby droga na osi Północ-Południe oraz droga na osi Wschód-Zachód miały światło GREEN w tym samym czasie.

### Założenia modelowe
- Z każdej drogi w jednym kroku wypuszczany jest jeden pojazd (jeden takt = jeden samochód przejeżdżający na świetle).
- Pojazdy są wypuszczane zgodnie z zasadą FIFO (pierwszy na wejściu, pierwszy na wyjściu) w ramach kolejki na każdej z dróg.
- Kierunek skrętu (`endRoad`) jest zapisywany, ale w podstawowej wersji symulacji nie wpływa na harmonogram — wszystkie pojazdy na drodze z zielonym światłem są traktowane jednakowo.
- Stan YELLOW jest pomijany, gdy kolejka po aktywnej stronie jest pusta, co pozwala uniknąć marnowania pustego taktu.

