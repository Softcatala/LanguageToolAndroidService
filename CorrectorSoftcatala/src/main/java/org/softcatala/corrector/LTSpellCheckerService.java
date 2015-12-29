/*
 * Copyright (C) 2014 Jordi Mas i Hernàndez <jmas@softcatala.org>
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

import android.os.Build;
import android.service.textservice.SpellCheckerService;
import android.util.Log;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import java.util.ArrayList;
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

		public static int[] convertIntegers(ArrayList<Integer> integers) {
			int[] ret = new int[integers.size()];
			Iterator<Integer> iterator = integers.iterator();
			for (int i = 0; i < ret.length; i++) {
				ret[i] = iterator.next().intValue();
			}
			return ret;
		}

		private boolean isSentenceSpellCheckApiSupported() {
			// Note that the sentence level spell check APIs work on Jelly Bean
			// or later.
			boolean rslt = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
			return rslt;
		}

		@Override
		public void onCreate() {
			if (DBG) {
				Log.d(TAG, "onCreate");
			}
			// TODO: To allow debugging a service
			// android.os.Debug.waitForDebugger();
			mLocale = getLocale();
		}

		@Override
		public void onCancel() {
			if (DBG) {
				Log.d(TAG, "onCancel");
			}
		}

		@Override
		public void onClose(){
			if (DBG) {
				Log.d(TAG, "onClose");
			}
		}

		/**
		 * This method should have a concrete implementation in all spell
		 * checker services. Please note that the default implementation of
		 * {@link SpellCheckerService.Session#onGetSuggestionsMultiple(TextInfo[], int, boolean)}
		 * calls up this method. You may want to override
		 * {@link SpellCheckerService.Session#onGetSuggestionsMultiple(TextInfo[], int, boolean)}
		 * by your own implementation if you'd like to provide an optimized
		 * implementation for
		 * {@link SpellCheckerService.Session#onGetSuggestionsMultiple(TextInfo[], int, boolean)}
		 * .
		 */
		@Override
		public SuggestionsInfo onGetSuggestions(TextInfo textInfo,
				int suggestionsLimit) {
			if (DBG) {
				Log.d(TAG, "onGetSuggestions: " + textInfo.getText());
			}
			final String input = textInfo.getText();
			final int length = input.length();
			// Just a fake logic:
			// length <= 3 for short words that we assume are in the fake
			// dictionary
			// length > 20 for too long words that we assume can't be recognized
			// (such as CJK words)
			final int flags = length <= 3 ? SuggestionsInfo.RESULT_ATTR_IN_THE_DICTIONARY
					: length <= 20 ? SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO
							: 0;
			return new SuggestionsInfo(flags, new String[] { "aaa", "bbb",
					"Candidate for " + input, mLocale });
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
                    Log.e(TAG,
                            "Sentence spell check is not supported on this platform, "
                                    + "but accidentially called.");
                    return null;
                }

                final ArrayList<SentenceSuggestionsInfo> retval = new ArrayList<SentenceSuggestionsInfo>();
                for (int i = 0; i < textInfos.length; ++i) {
                    final TextInfo ti = textInfos[i];
                    if (DBG) {
                        Log.d(TAG,
                                "onGetSentenceSuggestionsMultiple: " + ti.getText());
                    }
                    final String input = ti.getText();

                    LanguageToolRequest languageToolRequest = new LanguageToolRequest(mLocale);
                    Suggestion[] suggestions = languageToolRequest
                            .GetSuggestions(input);
                    ArrayList<SuggestionsInfo> sis = new ArrayList<SuggestionsInfo>();
                    ArrayList<Integer> offsets = new ArrayList<Integer>();
                    ArrayList<Integer> lengths = new ArrayList<Integer>();

                    for (int s = 0; s < suggestions.length; s++) {
                        SuggestionsInfo si = new SuggestionsInfo(
                                SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO,
                                suggestions[s].Text);

                        si.setCookieAndSequence(ti.getCookie(), ti.getSequence());
                        sis.add(si);
                        offsets.add(suggestions[s].Position);
                        lengths.add(suggestions[s].Length);
                    }

                    SuggestionsInfo[] s = sis.toArray(new SuggestionsInfo[0]);
                    int[] o = convertIntegers(offsets);
                    int[] l = convertIntegers(lengths);

                    final SentenceSuggestionsInfo ssi = new SentenceSuggestionsInfo(
                            s, o, l);
                    retval.add(ssi);
                }

                return retval.toArray(new SentenceSuggestionsInfo[0]);
            } catch (Exception e) {
                Log.e(TAG, "onGetSentenceSuggestionsMultiple" + e);
                return null;
            }
		}
	}
}
