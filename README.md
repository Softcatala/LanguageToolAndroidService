
**Update: this application is NO longer published in Google Play and it's no longer actively maintained.
**

--- Introduction

This a spell checking service for Android 4.4 or higher that uses LanguageTool as backend.

See notes: http://www.softcatala.org/wiki/Usuari:Jmas/Experiements_Corrector_Softcatala_Android (Catalan)

To enable debugging, you need to call 'android.os.Debug.waitForDebugger()'		

-- Adding a new language

* Translate src/main/res/values/strings.xml
* Add the language to src/main/res/xml/spellchecker.xml
* Add language Android to LT mapping src/main/java/org/softcatala/corrector/LanguageToolRequest.java
* Localize https://play.google.com/store/apps/details?id=org.softcatala.corrector content

-- References

Android:

* https://android.googlesource.com/platform/packages/inputmethods/LatinIME/+/master
* https://github.com/android/platform_frameworks_base/tree/master/core/java/android/view/textservice
* https://github.com/android/platform_packages_apps_settings/blob/master/src/com/android/settings/inputmethod/SpellCheckersSettings.java

Implementations:

* https://github.com/voikko/droidvoikko/tree/master/src/org/puimula/droidvoikko
* https://github.com/mweimerskirch/AndroidHunspellService
* https://github.com/lubekgc/AnySoftKeyboard-AnySoftKeyboard/blob/master/src/main/java/com/anysoftkeyboard/spellcheck/AnySpellCheckerService.java

-- Contact Information
Jordi Mas - jmas@softcatala.org


