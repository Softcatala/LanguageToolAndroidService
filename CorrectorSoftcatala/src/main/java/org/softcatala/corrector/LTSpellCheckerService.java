/*
 * Copyright (C) 2014-2016 Jordi Mas i Hernàndez <jmas@softcatala.org>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package org.softcatala.corrector;

import android.service.textservice.SpellCheckerService;
import android.util.Log;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;


public class LTSpellCheckerService extends SpellCheckerService {
    private static final String TAG = LTSpellCheckerService.class
            .getSimpleName();
    private static final boolean DBG = true;

    @Override
    public Session createSession() {
        if (DBG) {
            Log.d(TAG, "createSession");
        }
        return new AndroidSpellCheckerSession();
    }

    private static class AndroidSpellCheckerSession extends Session {

        private String mLocale;
        private static final String TAG = AndroidSpellCheckerSession.class
                .getSimpleName();
        private HashSet<String> mReportedErrors;
        final int MAX_REPORTED_ERRORS_STORED = 100;

        public static int[] convertIntegers(ArrayList<Integer> integers) {
            int[] ret = new int[integers.size()];
            Iterator<Integer> iterator = integers.iterator();
            for (int i = 0; i < ret.length; i++) {
                ret[i] = iterator.next();
            }
            return ret;
        }

        private boolean isSentenceSpellCheckApiSupported() {
            // Note that the sentence level spell check APIs work on Jelly Bean or later.
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
        }

        @Override
        public void onCreate() {
            if (DBG) {
                Log.d(TAG, "onCreate");
            }
            mLocale = getLocale();
            mReportedErrors = new HashSet<>();
        }

        @Override
        public void onCancel() {
            if (DBG) {
                Log.d(TAG, "onCancel");
            }
        }

        @Override
        public void onClose() {
            if (DBG) {
                Log.d(TAG, "onClose");
            }
        }

        /**
         * This is the word level spell checking previous for Android 4.4.4. Should not be called
         */
        @Override
        public SuggestionsInfo onGetSuggestions(TextInfo textInfo,
                                                int suggestionsLimit) {
            if (DBG) {
                Log.d(TAG, "onGetSuggestions call not supported");
            }
            return null;
        }

        /**
         * Please consider providing your own implementation of sentence level
         * spell checking. Please note that this sample implementation is just a
         * mock to demonstrate how a sentence level spell checker returns the
         * result. If you don't override this method, the framework converts
         * queries of
         * {@link SpellCheckerService.Session#onGetSentenceSuggestionsMultiple(TextInfo[], int)}
         * to queries of
         * {@link SpellCheckerService.Session#onGetSuggestionsMultiple(TextInfo[], int, boolean)}
         * by the default implementation.
         */
        @Override
        public SentenceSuggestionsInfo[] onGetSentenceSuggestionsMultiple(
                TextInfo[] textInfos, int suggestionsLimit) {

            try {
                if (!isSentenceSpellCheckApiSupported()) {
                    Log.e(TAG, "Sentence spell check is not supported on this platform");
                    return null;
                }

                final ArrayList<SentenceSuggestionsInfo> retval = new ArrayList<>();
                for (final TextInfo ti : textInfos) {
                    if (DBG) {
                        Log.d(TAG, "onGetSentenceSuggestionsMultiple: " + ti.getText());
                    }

                    ArrayList<SuggestionsInfo> sis = new ArrayList<>();
                    ArrayList<Integer> offsets = new ArrayList<>();
                    ArrayList<Integer> lengths = new ArrayList<>();

                    removePreviouslyMarkedErrors(ti, sis, offsets, lengths);
                    getSuggestionsFromLT(ti, sis, offsets, lengths);

                    SentenceSuggestionsInfo ssi = getSentenceSuggestionsInfo(sis, offsets, lengths);
                    retval.add(ssi);
                }

                return retval.toArray(new SentenceSuggestionsInfo[0]);
            } catch (Exception e) {
                Log.e(TAG, "onGetSentenceSuggestionsMultiple" + e);
                return null;
            }
        }

        private SentenceSuggestionsInfo getSentenceSuggestionsInfo(ArrayList<SuggestionsInfo> sis,
                                                                   ArrayList<Integer> offsets,
                                                                   ArrayList<Integer> lengths) {
            SuggestionsInfo[] s = sis.toArray(new SuggestionsInfo[0]);
            int[] o = convertIntegers(offsets);
            int[] l = convertIntegers(lengths);
            return new SentenceSuggestionsInfo(s, o, l);
        }

        private void getSuggestionsFromLT(TextInfo ti, ArrayList<SuggestionsInfo> sis,
                                    ArrayList<Integer> offsets, ArrayList<Integer> lengths) {

            final String input = ti.getText();

            LanguageToolRequest languageToolRequest = new LanguageToolRequest(mLocale);
            Suggestion[] suggestions = languageToolRequest
                    .GetSuggestions(input);

            for (Suggestion suggestion : suggestions) {
                SuggestionsInfo si = new SuggestionsInfo(
                        SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO,
                        suggestion.Text);

                si.setCookieAndSequence(ti.getCookie(), ti.getSequence());
                sis.add(si);
                offsets.add(suggestion.Position);
                lengths.add(suggestion.Length);
                String incorrectText = input.substring(suggestion.Position,
                        suggestion.Position + suggestion.Length);

                if (mReportedErrors.size() < MAX_REPORTED_ERRORS_STORED) {
                    mReportedErrors.add(incorrectText);
                    Log.d(TAG, String.format("mReportedErrors size: %d", mReportedErrors.size()));
                }
            }
        }

        /**
         * Let's imagine that you have the text:  Hi ha "cotxes" blaus
         * In the first request we get the text 'Hi ha "cotxes'. We return the error CA_UNPAIRED_BRACKETS
         * because the sentence is not completed and the ending commas are not introduced yet.
         *
         * In the second request we get the text 'Hi ha "cotxes" blaus al carrer', now with both commas
         * there is no longer an error. However, since we sent the error as answer to the first request
         * the error marker will be there since they are not removed.
         *
         * This function asks the spell checker to remove previously marked errors (all of them for the given string)
         * since we spell check the string every time.
         *
         * Every time that we get a request we do not know how this related to the full sentence or
         * if is it a sentence previously given. As result, we may ask to remove previously marked errors,
         * but this is fine since we evaluate the sentence every time. We only clean the list of reported
         * errors once per session because we do not when a sentence with a previously marked error
         * will be requested again and if the words /marks  that we asked to cleanup previously correspond
         * to that fragment of text.
         *
         */
        private void removePreviouslyMarkedErrors(TextInfo ti, ArrayList<SuggestionsInfo> sis,
                                                  ArrayList<Integer> offsets, ArrayList<Integer> lengths) {
            final int REMOVE_SPAN = 0;
            final String input = ti.getText();
            for (String txt : mReportedErrors) {
                int idx = input.indexOf(txt);
                while (idx != -1) {

                    SuggestionsInfo siNone = new SuggestionsInfo(REMOVE_SPAN,
                            new String[]{""});
                    siNone.setCookieAndSequence(ti.getCookie(), ti.getSequence());
                    sis.add(siNone);
                    int len = txt.length();
                    offsets.add(idx);
                    lengths.add(len);

                    Log.d(TAG, String.format("Asking to remove: '%s' at %d, %d", txt, idx, len));
                    idx = input.indexOf(txt, idx + 1);
                }
            }
        }
    }
}
