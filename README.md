# MyTurtle

Eine kleine, lokal arbeitende Android-App zur Dokumentation des Lebens einer Schildkröte.

> Status: `v0.1.0-beta`

![MyTurtle Feature Graphic](docs/assets/myturtle-feature-graphic.png)

[![Get it on Obtainium](https://img.shields.io/badge/Get%20it%20on-Obtainium-3ddc84?logo=android&logoColor=white)](https://apps.obtainium.imranr.dev/redirect.html?r=obtainium://app/%257B%2522id%2522%253A%2522de.leohopper.myturtle%2522%252C%2522url%2522%253A%2522https%253A%252F%252Fgithub.com%252Fleohoppergit%252FMyTurtle%2522%252C%2522author%2522%253A%2522leohoppergit%2522%252C%2522name%2522%253A%2522MyTurtle%2522%252C%2522preferredApkIndex%2522%253A0%252C%2522additionalSettings%2522%253A%2522%257B%255C%2522includePrereleases%255C%2522%253Atrue%252C%255C%2522fallbackToOlderReleases%255C%2522%253Afalse%252C%255C%2522filterReleaseTitlesByRegEx%255C%2522%253A%255C%2522%255C%2522%252C%255C%2522filterReleaseNotesByRegEx%255C%2522%253A%255C%2522%255C%2522%252C%255C%2522verifyLatestTag%255C%2522%253Afalse%252C%255C%2522dontSortReleasesList%255C%2522%253Afalse%252C%255C%2522trackOnly%255C%2522%253Afalse%252C%255C%2522versionDetection%255C%2522%253A%255C%2522standardVersionDetection%255C%2522%252C%255C%2522apkFilterRegEx%255C%2522%253A%255C%2522%255C%2522%252C%255C%2522autoApkFilterByArch%255C%2522%253Atrue%252C%255C%2522appName%255C%2522%253A%255C%2522%255C%2522%252C%255C%2522exemptFromBackgroundUpdates%255C%2522%253Afalse%252C%255C%2522skipUpdateNotifications%255C%2522%253Afalse%252C%255C%2522about%255C%2522%253A%255C%2522%255C%2522%257D%2522%257D)
[![GitHub Release](https://img.shields.io/github/v/release/leohoppergit/MyTurtle?display_name=tag&include_prereleases&label=GitHub%20Release)](https://github.com/leohoppergit/MyTurtle/releases/tag/v0.1.0-beta)
[![AppVerifier SHA-256](https://img.shields.io/badge/AppVerifier-SHA--256-556B2F)](https://github.com/leohoppergit/MyTurtle#appverifier)

## Enthalten in der Grundversion

- Mehrere Schildkröten anlegen
- Schlupfdatum, Art, Geschlecht und Notizen verwalten
- Gewichts- und Längenmessungen mit Datum eintragen
- Lebensereignisse dokumentieren
- Jahresfotos lokal per Galerie oder Kamera verknüpfen
- Mehrere Fotos pro Messung
- Einfache Verlaufsgrafik für Gewicht oder Panzerlänge
- Papierkorb mit automatischer Löschung nach 30 Tagen
- Vollständiges Backup und Wiederherstellung als ZIP-Datei
- Room-Datenbank nur auf dem Gerät, ohne Konto und ohne Cloud-Zwang
- EXIF-Daten werden beim Import aus Fotos entfernt

## Datenschutz

- Alle Daten bleiben lokal auf dem Gerät.
- Es gibt kein Konto, keine Cloud und kein Tracking.
- Android-System-Cloud-Backups sind bewusst deaktiviert; Sicherungen laufen nur über die Exportfunktion in der App.
- Fotos werden vor dem Speichern in die App ohne EXIF-Metadaten übernommen.

## Installation

### GitHub Releases

Die signierte Release-APK wird über GitHub Releases bereitgestellt.

### Obtainium

Der Obtainium-Button oben ist jetzt ein direkter Importlink für Obtainium.

Er ist bereits so vorkonfiguriert, dass auch `v0.1.0-beta` als Pre-Release gefunden wird.

Falls dein Browser oder Android-Gerät keine App-Weiterleitung zulässt, kannst du alternativ direkt dieses Repository als GitHub-Quelle in Obtainium hinzufügen:

`https://github.com/leohoppergit/MyTurtle`

### AppVerifier

Verifiziere die Signatur des Release-Zertifikats mit diesem SHA-256-Fingerprint:

`92:1B:88:ED:B4:0C:D5:95:EF:AF:BB:70:5E:1D:D2:35:D0:3D:F6:EE:FF:CF:91:5F:60:9A:66:DF:E3:35:2D:B2`

## Projektstart aus dem Quellcode

1. Ordner in Android Studio öffnen.
2. Falls `local.properties` fehlt, Android Studio den SDK-Pfad setzen lassen.
3. Gradle-Sync starten.
4. App auf Emulator oder Gerät ausführen.

## Backup & Wiederherstellung

- In den Einstellungen kannst du vollständige ZIP-Backups exportieren und später wiederherstellen.
- Gesichert werden Schildkröten, Messungen, Lebensereignisse, Fotos und die gewählte Startscreen-Kartenansicht.
- Automatische Android-System- oder Cloud-Backups sind bewusst deaktiviert, damit die Daten nicht ungefragt außerhalb des Geräts landen.

## Hinweise zum Beta-Status

- Fotos werden als lokale `file://`-URIs im App-Speicher gehalten.
- Die App nutzt den System-Dokumentenpicker, damit keine breit gefassten Medienspeicherrechte nötig sind.
- Das Room-Schema wird exportiert und es gibt keine destruktive automatische Migration mehr.
- Release-Builds sind minifiziert und ressourcengeschrumpft.
- Gradle prüft Build-Artefakte per SHA-256; reine IDE-Quell-/Javadoc-Artefakte werden bewusst ausgenommen.
- Vor einer öffentlichen `1.0` sollte der Restore-Pfad zusätzlich noch einmal praktisch auf mehreren Geräten gegengeprüft werden.

## Brand-Assets

Die öffentlichen Brand-Assets für README, Release-Seite und spätere Store-/Repo-Grafiken liegen unter [docs/assets](docs/assets).
