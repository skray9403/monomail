# MonoMail Dock Demo

An isolated, placeholder-data Android app for reviewing the proposed base MonoMail improvements. It has its own application ID and does not depend on the MonoMail app or feature modules.

## Build

From the repository root:

```bash
./gradlew :dock-demo:assembleDebug
```

APK output:

`dock-demo/build/outputs/apk/debug/dock-demo-debug.apk`

## What to try

- Switch Inbox, Sent and Archive using the floating three-tab pill. The tab slots and outer pill stay fixed while the selected capsule moves and its label cross-fades.
- Open a conversation on a phone. On a wide window, select a conversation in the list and watch the reading pane and dock context update together.
- Open **Launch checklist and final review**. Its newest message is from the current account, but Reply resolves to the latest incoming sender instead of replying to the account itself.
- Scroll the conversation. The subject appears in the top app bar after the expanded subject header leaves the reading area.
- Use Reply, Forward, Compose, Drafts, Attach and Send. These are local interaction placeholders; no email is sent and no account/network integration is present.

All tab controls expose selected state and semantic labels, and the main dock controls meet a 48dp minimum touch target.
