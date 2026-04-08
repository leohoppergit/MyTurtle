# MyTurtle

Eine kleine, lokal arbeitende Android-App zur Dokumentation des Lebens einer Schildkröte.

> Status: `v0.1.0-alpha`

[![Get it on Obtainium](https://img.shields.io/badge/Get%20it%20on-Obtainium-3ddc84?logo=android&logoColor=white)](https://github.com/leohoppergit/MyTurtle/releases/latest)

## Enthalten in der Grundversion

- Mehrere Schildkröten anlegen
- Schlupfdatum, Art, Geschlecht und Notizen verwalten
- Gewichts- und Längenmessungen mit Datum eintragen
- Lebensereignisse dokumentieren
- Jahresfotos lokal per Galerie oder Kamera verknüpfen
- Mehrere Fotos pro Messung
- Einfache Verlaufsgrafik für Gewicht oder Panzerlänge
- Papierkorb mit automatischer Löschung nach 30 Tagen
- Room-Datenbank nur auf dem Gerät, ohne Konto und ohne Cloud-Zwang
- EXIF-Daten werden beim Import aus Fotos entfernt

## Datenschutz

- Alle Daten bleiben lokal auf dem Gerät.
- Es gibt kein Konto, keine Cloud und kein Tracking.
- Fotos werden vor dem Speichern in die App ohne EXIF-Metadaten übernommen.

## Installation

### GitHub Releases

Die signierte Release-APK wird über GitHub Releases bereitgestellt.

### Obtainium

Sobald das Repository veröffentlicht ist, kann die App direkt über Obtainium aus den GitHub Releases installiert und aktualisiert werden.

### AppVerifier

Verifiziere die Signatur des Release-Zertifikats mit diesem SHA-256-Fingerprint:

`92:1B:88:ED:B4:0C:D5:95:EF:AF:BB:70:5E:1D:D2:35:D0:3D:F6:EE:FF:CF:91:5F:60:9A:66:DF:E3:35:2D:B2`

## Projektstart aus dem Quellcode

1. Ordner in Android Studio öffnen.
2. Falls `local.properties` fehlt, Android Studio den SDK-Pfad setzen lassen.
3. Gradle-Sync starten.
4. App auf Emulator oder Gerät ausführen.

## Hinweise zum Alpha-Status

- Fotos werden als lokale `content://`-URIs gespeichert.
- Die App nutzt den System-Dokumentenpicker, damit keine breit gefassten Medienspeicherrechte nötig sind.
- Backup-Regeln sind vorbereitet, damit lokale Daten bei Gerätetransfers einfacher mitgenommen werden können.
- Das Datenbankschema kann sich in Alpha-Versionen noch ändern.
- Durch die aktuell verwendete destruktive Migration können lokale Testdaten bei künftigen Schema-Änderungen verloren gehen.
