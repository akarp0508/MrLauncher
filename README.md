# MrLauncher

A simple, low-distraction Android launcher.

No icons, no widgets, no feeds — just a clock, a few apps you pick, and a fast search for everything else. The goal is a home screen that gets you where you meant to go and then gets out of the way.

MrLauncher is inspired mostly by [Olauncher](https://github.com/tanujnotes/Olauncher) and similar minimalist launchers. It isn't trying to be a copy: the aim is the same calm, text-only idea, but with a nicer feel and a lot more room to make it your own.

## Features

- **Home screen** — clock, date, optional weather, and your pinned apps as plain text
- **Search-first app drawer** — opens with the keyboard ready; ignores accents (`lodz` finds *Łódź*), matches initials (`gm` finds *Google Maps*), and can launch automatically when only one app matches
- **Your apps, your names** — pin, reorder, rename and hide apps with a long-press; work profile apps are included
- **Gestures** — swipe up/down/left/right, double tap, and tapping the clock or date can each open the drawer, notifications, quick settings, alarms, calendar, any app, or lock the screen
- **Layout** — alignment for home and drawer, where the app list sits, spacing, text size, status bar on/off
- **Colors** — text color (or *auto*, which picks light or dark text from your wallpaper), accent color, presets or any hex value, and how much to dim the wallpaper behind the drawer
- **Fonts** — any of Android's built-in fonts, or import your own `.ttf` / `.otf`
- **Weather** — current conditions for a city you choose, from [Open-Meteo](https://open-meteo.com) (free, no account, no location permission)

## Building

Open the project in Android Studio and run the `app` configuration, or from the command line:

```sh
./gradlew :app:assembleDebug        # build
./gradlew :app:testDebugUnitTest    # unit tests
```

Then press Home on the device and choose MrLauncher as the default (also available under *Settings → Set as default launcher*).

## Permissions

| Permission | Why |
| --- | --- |
| `INTERNET` | Weather, only if you turn it on |
| `EXPAND_STATUS_BAR` | Gestures that pull down notifications or quick settings |
| `REQUEST_DELETE_PACKAGES` | The *uninstall* option on an app |
| Accessibility service (optional) | Only for the *lock screen* gesture — Android offers launchers no other way to lock. It reads nothing on screen and is off until you enable it. |

## Project layout

```
data/      app list, settings storage, weather, imported fonts
search/    app search (unit tested)
system/    system actions and the lock-screen service
ui/        view model, home, drawer, settings, theme
```
