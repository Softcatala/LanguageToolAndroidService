#!/bin/sh
# Based on https://github.com/languagetool-org/languagetool-community-website/blob/master/i18n_update.sh

# Transifex username and password
USERNAME=jordimash
PASSWORD=`cat ~/.transifex_password`

rm -I i18n-temp
mkdir i18n-temp
cd i18n-temp

# list of languages in the same order as on https://www.transifex.com/projects/p/languagetool/:
for lang in en ast be br ca zh da nl eo fr gl de el_GR it pl ru sl es tl uk ro sk cs sv is lt km pt_PT pt_BR fa
do
  SOURCE=downloaded.tmp
  curl --user $USERNAME:$PASSWORD https://www.transifex.com/api/2/project/languagetool/resource/lt-for-android/translation/$lang/?file >strings-$lang.xml
  localLang=$lang
  if [ $lang = 'pt_PT' ]; then
    # special case: if this is named pt_PT it never becomes active because we use "lang=xx" links
    # in the web app that don't contain the country code:
    localLang=pt
  fi
done