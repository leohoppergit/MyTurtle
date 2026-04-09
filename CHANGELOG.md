# Changelog

Alle nennenswerten Änderungen an diesem Projekt werden in dieser Datei dokumentiert.

## [0.1.0-beta] - 2026-04-09

### Hinzugefügt

- Eigenständige Einstellungsseite mit professionellerer Gliederung
- Drei umschaltbare Startscreen-Kartenmodi: kompakt, standard und groß
- Vollständiges ZIP-Backup und Wiederherstellung für Daten, Fotos und Einstellungen
- Ausklappbarer About-Bereich und getrennter Versions-/Aktualisierungsblock
- Unit-Tests für Datumseingaben, Converter und Backup-Format
- Exportiertes Room-Schema zur besseren Langzeitpflege

### Geändert

- Destruktive automatische Room-Migration entfernt
- Startscreen-Karten und Einstellungen robuster für kleine Displays gemacht
- Android-System- und Cloud-Backups bewusst deaktiviert; Sicherungen laufen ausschließlich über den expliziten ZIP-Export
- Build auf AGP 9 / Built-in Kotlin / KSP modernisiert
- Release-Build gehärtet mit Minifizierung, Resource-Shrinking und Gradle-Dependency-Verifikation

## [0.1.0-alpha] - 2026-04-09

### Hinzugefügt

- Grundlegende Android-App in Kotlin mit Jetpack Compose
- Lokale Room-Datenbank ohne Cloud- oder Konto-Zwang
- Verwaltung mehrerer Schildkröten mit Art, Schlupfdatum, Geschlecht und Notizen
- Gewichts- und Längenmessungen mit Verlaufsgrafik
- Lebensereignisse zur Langzeitdokumentation
- Jahresfotos sowie mehrere Fotos pro Messung
- Kamera- und Galerieimport mit Entfernung von EXIF-Daten
- Papierkorb für gelöschte Schildkröten mit automatischer Löschung nach 30 Tagen
- Strukturierte Artenliste und robustere Datumsvalidierung

### Bekannt

- Das Datenbankschema kann sich im Alpha-Stadium noch ändern.
- Wegen destruktiver Migration können lokale Testdaten bei späteren Alpha-Updates verloren gehen.
