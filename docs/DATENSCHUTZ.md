# Datenschutz bei LiBoard

Stand: 8. Oktober 2026 · gilt für LiBoard 0.1 und neuer · [English below](#privacy-at-liboard)

LiBoard ist eine Tastatur. Eine Tastatur bekommt alles mit, was du tippst, auch Passwörter. Deshalb ist LiBoard so gebaut, dass nichts davon dein Gerät verlassen kann.

## Kurz gesagt

- **Kein Internetzugang.** LiBoard hat keine Internet-Berechtigung. Android lässt die App deshalb keine Verbindung aufbauen, weder zu uns noch zu sonst jemandem.
- **Keine Konten, keine Werbung, keine Statistik.** Es gibt keine Anmeldung, keine Tracker und keine Absturzberichte, die irgendwohin gesendet werden.
- **Keine Cloud-Sicherung.** Android darf die Daten von LiBoard weder in eine Cloud-Sicherung noch beim Gerätewechsel auf ein anderes Gerät übertragen.
- **Alles bleibt auf dem Gerät** und lässt sich jederzeit löschen.

## Was LiBoard auf deinem Gerät speichert

| Daten | Wozu | Ab Werk |
|---|---|---|
| Gelernte Wörter | bessere Vorschläge beim Tippen („Aus meinen Eingaben lernen“) | an |
| Eigene Wörter und Textersetzungen | im Android-Benutzerwörterbuch, das du selbst pflegst | an |
| Zuletzt benutzte Emoji | schneller Zugriff in der Emoji-Auswahl | an |
| Einstellungen | dein Aussehen und Verhalten der Tastatur | an |
| Ergebnisse des Tipp-Tests | Vergleich mit deiner bisherigen Tastatur | nur wenn du ihn machst |
| Zwischenablage-Vorschläge | zuletzt Kopiertes als Vorschlag | aus |
| Namen aus deinen Kontakten | Namen als Vorschläge | aus, Android fragt beim Einschalten |
| Namen installierter Apps | App-Namen als Vorschläge | aus |

Getippter Text selbst wird nicht gespeichert, nur die Wörter, die LiBoard für Vorschläge lernt.

## Berechtigungen

| Berechtigung | Wozu |
|---|---|
| Benutzerwörterbuch lesen und schreiben | deine eigenen Wörter und Textersetzungen |
| Kontakte lesen | nur wenn du „Namen aus Kontakten“ einschaltest |
| Vibration | Rückmeldung beim Tippen |
| Nach dem Start ausführen | nach einem Neustart oder Update die Tastatur bereitstellen |

Eine Internet-Berechtigung gibt es nicht. Du kannst das selbst prüfen: Einstellungen → Apps → LiBoard → Berechtigungen, oder im Quelltext in `app/src/main/AndroidManifest.xml`.

## Löschen

- **Gelernte Wörter:** Einstellungen von LiBoard → Textkorrektur → „Tastaturwörterbuch zurücksetzen“. Wer gar nicht lernen lassen will, schaltet dort „Aus meinen Eingaben lernen“ aus.
- **Alles:** Android-Einstellungen → Apps → LiBoard → Speicher → „Daten löschen“, oder LiBoard deinstallieren.

## Sicherung auf eigenen Wunsch

In den Einstellungen gibt es „Sichern und Wiederherstellen“. Damit schreibst du deine Einstellungen und Wörterbücher in eine Datei, die du selbst auswählst. LiBoard sendet diese Datei nirgendwohin. Wo du sie ablegst, entscheidest du.

## Herkunft

LiBoard ist ein Fork von [HeliBoard](https://github.com/HeliBorg/HeliBoard). Die freiwillige Spende von Gestendaten, die es in HeliBoard gibt, ist in LiBoard entfernt.

## Wer dahintersteht

LiBoard ist ein privates, nichtkommerzielles Projekt von Olaf Winkler aus Schleswig-Holstein. Fragen und Hinweise gern über den [Issue-Tracker auf GitHub](https://github.com/veritasX1/LiBoard/issues).

---

# Privacy at LiBoard

As of 8 October 2026 · applies to LiBoard 0.1 and later

LiBoard is a keyboard. A keyboard sees everything you type, passwords included. That is why LiBoard is built so that none of it can leave your device.

## In short

- **No internet access.** LiBoard has no internet permission, so Android does not let it connect anywhere, neither to us nor to anyone else.
- **No accounts, no ads, no analytics.** There is no sign-in, no tracker and no crash report sent anywhere.
- **No cloud backup.** Android may neither back up LiBoard's data to the cloud nor transfer it to a new device.
- **Everything stays on the device** and can be deleted at any time.

## What LiBoard stores on your device

| Data | Purpose | By default |
|---|---|---|
| Learned words | better suggestions while typing (“Learn from My Typing”) | on |
| Your own words and text replacements | in Android's user dictionary, which you maintain | on |
| Recently used emoji | quick access in the emoji picker | on |
| Settings | how the keyboard looks and behaves | on |
| Typing test results | comparison with your previous keyboard | only if you take it |
| Clipboard suggestions | recently copied text as a suggestion | off |
| Names from your contacts | names as suggestions | off, Android asks when you turn it on |
| Names of installed apps | app names as suggestions | off |

Typed text itself is not stored, only the words LiBoard learns for suggestions.

## Permissions

| Permission | Purpose |
|---|---|
| Read and write the user dictionary | your own words and text replacements |
| Read contacts | only if you turn on “Names from contacts” |
| Vibrate | feedback while typing |
| Run at startup | make the keyboard ready after a restart or update |

There is no internet permission. You can check this yourself: Settings → Apps → LiBoard → Permissions, or in the source code in `app/src/main/AndroidManifest.xml`.

## Deleting

- **Learned words:** LiBoard settings → Text Correction → “Reset Keyboard Dictionary”. To stop learning altogether, turn off “Learn from My Typing” there.
- **Everything:** Android settings → Apps → LiBoard → Storage → “Clear data”, or uninstall LiBoard.

## Backup if you want it

The settings offer “Backup and restore”. It writes your settings and dictionaries to a file you choose. LiBoard does not send this file anywhere; where you keep it is up to you.

## Origin

LiBoard is a fork of [HeliBoard](https://github.com/HeliBorg/HeliBoard). HeliBoard's optional gesture data donation has been removed from LiBoard.

## Who is behind it

LiBoard is a private, non-commercial project by Olaf Winkler from Schleswig-Holstein, Germany. Questions and feedback are welcome via the [issue tracker on GitHub](https://github.com/veritasX1/LiBoard/issues).
