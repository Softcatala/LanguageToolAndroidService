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

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class LanguageToolParsing {

    private static final String TAG = LanguageToolParsing.class
            .getSimpleName();


    public Suggestion[] GetSuggestions(String jsonText) {
        ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();

        try {

            JSONObject json = new JSONObject(jsonText);
            JSONArray matches = json.getJSONArray("matches");

            for (int i = 0; i < matches.length(); i++) {

                JSONObject match = matches.getJSONObject(i);

                JSONArray replacements = match.getJSONArray("replacements");
                JSONObject rule = match.getJSONObject("rule");
                String ruleId = rule.getString("id");

                // Since we process fragments we need to skip the upper case
                // suggestion
                if (ruleId.equals("UPPERCASE_SENTENCE_START") == true)
                    continue;

                Suggestion suggestion = new Suggestion();

                if (replacements.length() == 0) {
                    String message = match.getString("message");
                    String msgText = String.format("(%s)", message);
                    suggestion.Text = new String[]{msgText};
                } else {
                    ArrayList<String> list = new ArrayList<String>();
                    for (int r = 0; r < replacements.length(); r++) {
                        JSONObject replacement = replacements.getJSONObject(r);
                        String value = replacement.getString("value");
                        list.add(value);
                    }
                    suggestion.Text = list.toArray(new String[list.size()]);
                }

                suggestion.Position = match.getInt("offset");
                suggestion.Length = match.getInt("length");
                suggestions.add(suggestion);

                Log.d(TAG, "Request result: " + suggestion.Position + " Len:" + suggestion.Length);
            }

        } catch (Exception e) {
            Log.e(TAG, "GetSuggestions", e);
        }

        return suggestions.toArray(new Suggestion[0]);
    }
}
