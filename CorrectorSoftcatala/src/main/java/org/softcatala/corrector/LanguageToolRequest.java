/*
 * Copyright (C) 2014-2017 Jordi Mas i Hernàndez <jmas@softcatala.org>
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

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.Set;

import android.util.Log;

public class LanguageToolRequest {

    private static final String ENCODING = "UTF-8";
    private static final String TAG = LanguageToolRequest.class.getSimpleName();
    private static final String m_sessionId = GetSessionID();

    private final LanguageToolParsing languageToolParsing = new LanguageToolParsing();
    String[][] mAndroidToLTLangMap = new String[][]{
            {"ar", "ar"},
            {"ast", "ast-ES"},
            {"be", "be-BY"},
            {"br", "br-FR"},
            {"ca", "ca-ES"},
            {"zh", "zh-CN"},
            {"da", "da-DK"},
            {"nl", "nl"},
            {"en", "en-US"},
            {"eo", "eo"},
            {"fr", "fr"},
            {"gl", "gl-ES"},
            {"de", "de-DE"},
            {"el", "el-GR"},
            {"ga", "ga-IE"},
            {"it", "it"},
            {"ja", "ja-JP"},
            {"km", "km-KH"},
            {"nb", "nb"},
            {"no", "no"},
            {"fa", "fa"},
            {"pl", "pl-PL"},
            {"pt", "pt"},
            {"ro", "ro-RO"},
            {"ru", "ru-RU"},
            {"sk", "sk-SK"},
            {"sl", "sl-SI"},
            {"es", "es"},
            {"sv", "sv"},
            {"tl", "tl-PH"},
            {"ta", "ta-IN"},
            {"uk", "uk-UA"},
    };
    private final String system_language;

    public LanguageToolRequest(String language) {
        system_language = language;
    }

    static private String GetSessionID() {
        Random rand = new Random();
        int MAX_NUM = 999999;
        int id = rand.nextInt(MAX_NUM);
        return Integer.toString(id);
    }

    private String ConvertLanguage(String language) {
        String lang = "";

        for (String[] strings : mAndroidToLTLangMap) {
            if (language.startsWith(strings[0])) {
                lang = strings[1];
                break;
            }
        }
        Log.d(TAG, String.format("ConvertLanguage from Android %s to LT %s", language, lang));
        return lang;
    }

    public Suggestion[] GetSuggestions(String text) {
        return Request(text);
    }

    private String GetFillPostFields(String text) {
        StringBuilder queryParameter = new StringBuilder();
        queryParameter.append(AddQueryParameter("", "useragent", "androidspell"));
        queryParameter.append(AddQueryParameter("&", "text", text));

        String settings_language = Configuration.getInstance().getLanguage();
        if (settings_language.equals("system")) {
            queryParameter.append(AddQueryParameter("&", "language", ConvertLanguage(system_language)));
        } else {
            queryParameter.append(AddQueryParameter("&", "language", settings_language));

            if (settings_language.equals("auto")) {
                Set<String> settings_preferred_variants = Configuration.getInstance().getPreferredVariants();
                if (!settings_preferred_variants.isEmpty()) {
                    queryParameter.append(AddQueryParameter("&", "preferredVariants", String.join(",", settings_preferred_variants)));
                } else {
                    Log.d(TAG, "preferredVariants are empty");
                }
            }
        }

        String settings_mother_tongue = Configuration.getInstance().getMotherTongue();
        if (!settings_mother_tongue.isEmpty()) {
            queryParameter.append(AddQueryParameter("&", "motherTongue", settings_mother_tongue));
        } else {
            Log.d(TAG, "mother_tongue is empty");
        }

        return queryParameter.toString();
    }

    // HTTP POST request
    private String sendPost(String text) {
        try {

            String url = BuildURL();
            Log.d("softcatala", "URL: " + url);

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");

            String urlParameters = GetFillPostFields(text);
            Log.d("softcatala", "Parameters : " + urlParameters);

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();

            Log.d("softcatala", "Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            Log.d("softcatala", "Response : " + response);

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (Exception e) {
            Log.e("softcatala", "Exception ", e);
        }

        return "";
    }


    public Suggestion[] Request(String text) {
        try {

            String result = sendPost(text);

            Configuration.getInstance().incConnections();
            Configuration.getInstance().setLastConnection(new Date());
            Log.d(TAG, "Request result: " + result);
            return languageToolParsing.GetSuggestions(result);
        } catch (Exception e) {
            Log.e(TAG, "Error reading stream from URL.", e);
        }
        return new Suggestion[]{};
    }

    private String BuildURL() {
        return Configuration.getInstance().getServer() +
                "/v2/check" +
                /* Parameter to help to track requests from the same IP */
                AddQueryParameter("?", "sessionID", m_sessionId);
    }

    String AddQueryParameter(String separator, String key, String value) {
        StringBuilder sb = new StringBuilder();
        sb.append(separator);
        sb.append(key);
        sb.append("=");
        try {
            sb.append(URLEncoder.encode(value, ENCODING));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

}
