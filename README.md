# MorseVibe

**한국어** · [English](#english)

모스부호를 **진동**으로 느끼며 연습하는 안드로이드 앱이에요. 특정 앱의 알림이 오면 그 앱에 지정한 모스부호 패턴으로 진동하기 때문에, 화면을 보지 않고도 "어느 앱에서 알림이 왔는지" 손끝으로 알 수 있고 자연스럽게 모스부호도 익힐 수 있어요.

## 기능

- **앱별 진동 규칙** – 앱마다 모스부호를 지정해요. `SOS`, `K` 같은 문자나 `.-.` 같은 점/선을 직접 입력할 수 있어요 (글자 사이는 공백, 단어 사이는 `/`).
- **알림 기록** – 받은 알림(앱, 제목, 시간)을 기기 안에만 기록하고, 기록에서 항목을 눌러 바로 그 앱의 규칙을 만들 수 있어요.
- **진동 설정** – 점/선의 **길이**와 **세기**를 따로 조절해요.
  | 구분 방식 | 점 | 선 | 비고 |
  |---|---|---|---|
  | 길이(빠르기)로 구분 | 짧은 진동 | 긴 진동 | 세기는 하나로 공통 |
  | 세기로 구분 | 약한 진동 | 강한 진동 | 길이는 하나로 공통. **진동 세기 조절이 되는 기기에서만** 선택 가능 |

  글자/단어/기호 사이의 쉬는 시간도 조절할 수 있어요.
- **연습하기** – 무작위 글자를 진동으로 재생하고 맞춰 보는 퀴즈, 모스부호표.
- 한국어/영어 UI.

## 설치

[Actions](../../actions) 탭의 최신 빌드에서 `morsevibe-apk` 아티팩트를 받아 설치하거나, 직접 빌드하세요.

```sh
./gradlew assembleDebug   # app/build/outputs/apk/debug/
```

JDK 17과 Android SDK(compileSdk 36)가 필요해요. 최소 지원은 Android 8.0(API 26)이에요.

## 사용법

1. 앱을 열고 **알림 접근 설정 열기** → MorseVibe를 허용해요.
   - 안드로이드 13 이상에서 직접 설치한 앱은 스위치가 회색일 수 있어요. 그땐 *앱 정보 → ⋮ → 제한된 설정 허용* 후 다시 시도하세요.
2. **앱별 진동 규칙**에서 앱을 고르고 모스부호를 지정해요.
3. **진동 설정**에서 길이/세기를 취향대로 맞추고 테스트해 보세요.

## 알아둘 점

- **과거 알림 기록은 가져올 수 없어요.** 안드로이드는 앱이 지난 알림 기록(설정의 "알림 기록")을 읽는 API를 제공하지 않아요. MorseVibe는 알림 접근을 허용한 시점부터, 그리고 현재 알림창에 떠 있는 알림부터 기록해요.
- 진동 세기 모드는 `Vibrator.hasAmplitudeControl()`이 `true`인 기기에서만 켤 수 있어요. 지원하더라도 약한 세기는 기기에 따라 거의 느껴지지 않을 수 있으니, 점 세기를 너무 낮추지 마세요.
- 시스템의 "알림 진동" 설정이나 방해 금지 모드가 켜져 있으면 알림 진동이 나오지 않을 수 있어요. (앱 안의 테스트/연습은 알람 용도로 재생돼 영향을 덜 받아요.)
- 알림 내용은 기기 밖으로 나가지 않아요. 앱은 인터넷 권한이 없고, 기록에는 앱 이름·알림 제목·시간만 저장돼요.

## 개발

순수 Java + 안드로이드 프레임워크 API만 사용해요(외부 의존성 없음). 구조는 다음과 같아요.

| 파일 | 역할 |
|---|---|
| `Morse` | 모스부호표, 문자/점선 입력 → 표준 형식 변환 |
| `MorsePlayer` | 설정으로 진동 파형 생성 및 순차 재생 |
| `MorseNotificationListener` | 알림 감지, 기록, 규칙에 맞는 진동 |
| `AppRules` / `NotifLog` / `MorseSettings` | 규칙, 기록, 설정 저장 |

## 라이선스

MIT

---

## English

Practice Morse code by **feeling it**. MorseVibe vibrates a Morse pattern you assign to each app whenever that app posts a notification, so you can tell which app pinged you without looking, and learn Morse along the way.

### Features

- **Per-app rules** – give each app a pattern: text such as `SOS` or `K`, or raw dots/dashes such as `.-.` (space between letters, `/` between words).
- **Notification log** – a local log of received notifications (app, title, time); tap an entry to create a rule for that app.
- **Vibration settings** – dot/dash **length** and **intensity** are configured separately, and you choose how dots and dashes are told apart:

  | Mode | Dot | Dash | Notes |
  |---|---|---|---|
  | By length (speed) | short | long | one shared intensity |
  | By intensity | weak | strong | one shared length. **Only on devices with vibration amplitude control** |

  Pauses between symbols, letters and words are adjustable too.
- **Practice** – a quiz that vibrates a random character for you to identify, plus a Morse chart.
- Korean and English UI.

### Install

Grab the `morsevibe-apk` artifact from the latest run in [Actions](../../actions), or build it yourself:

```sh
./gradlew assembleDebug   # app/build/outputs/apk/debug/
```

Needs JDK 17 and the Android SDK (compileSdk 36). Requires Android 8.0 (API 26) or newer.

### Usage

1. Open the app, tap **Open notification access settings** and allow MorseVibe.
   - On Android 13+ a sideloaded app may have the switch greyed out: use *App info → ⋮ → Allow restricted settings*, then retry.
2. In **App vibration rules**, pick an app and assign a pattern.
3. Tune length/intensity in **Vibration settings** and use the test button.

### Good to know

- **Past notification history can't be imported.** Android offers apps no API to read the system's notification history. MorseVibe logs from the moment access is granted, plus whatever is currently in the notification shade.
- Intensity mode is only available when `Vibrator.hasAmplitudeControl()` is `true`. Even then, very weak levels can be barely perceptible on some devices — don't set the dot intensity too low.
- The system's notification-vibration setting and Do Not Disturb can suppress notification vibration. (In-app tests and practice play with alarm usage and are less affected.)
- Nothing leaves the device: the app has no internet permission, and the log stores only app name, notification title and time.

### License

MIT
