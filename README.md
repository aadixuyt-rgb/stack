# Kit Auto — Fabric 1.21.4

Client-side mod for Minecraft Java 1.21.4 + Fabric.

## Funkcje
- `Delete` — włącza/wyłącza automat.
- `/` — otwiera konfigurację odbieranych nagród.
- Po uruchomieniu wysyła `/kit`.
- Następne `/kit` planowane jest 60 sekund od poprzedniego wysłania.
- W pierwszym GUI szuka dokładnie **szarego shulkera** i klika go zwykłym kliknięciem (PICKUP).
- W kolejnym GUI przenosi wybrane Ender Pearls i Enchanted Golden Apples do ekwipunku przez QUICK_MOVE (odpowiednik Shift + klik).
- Po odebraniu nagród zamyka końcowe GUI.

## Budowanie
Wymagane: Java 21 i Gradle.

```bash
gradle build
```

Gotowy plik JAR pojawi się w `build/libs/kit-auto-1.0.0.jar`.

## Ważne
GUI serwerów mogą działać różnie. Ten projekt rozpoznaje szary shulker po typie przedmiotu (`GRAY_SHULKER_BOX`), a nagrody po typach (`ENDER_PEARL`, `ENCHANTED_GOLDEN_APPLE`). Jeśli serwer używa innego przedmiotu jako szarego shulkera albo inaczej otwiera drugie GUI, trzeba dopasować rozpoznawanie do jego GUI.

Mod nie ma wbudowanego obejścia zabezpieczeń serwera. Używaj go tylko tam, gdzie automatyzacja jest dozwolona.
