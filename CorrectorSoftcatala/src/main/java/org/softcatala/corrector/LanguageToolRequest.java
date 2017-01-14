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
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.util.Date;
import java.util.Random;

import android.util.Log;

import javax.net.ssl.HttpsURLConnection;

public class LanguageToolRequest {

    //private static final String SERVER_URL = "https://www.softcatala.org/languagetool/api/";
    private static final String SERVER_URL = "https://languagetool.org/api/v2/check";
    private static final String ENCODING = "UTF-8";
    private static final String TAG = LanguageToolRequest.class.getSimpleName();
    private static final String m_sessionId = GetSessionID();

    private final LanguageToolParsing languageToolParsing = new LanguageToolParsing();
    String[][] mAndroidToLTLangMap = new String[][]{
            {"en", "en-US"},
            {"de", "de-DE"},
            {"pl", "pl"},
            {"fr", "fr"},
            {"ca", "ca"},
            {"uk", "uk"},
            {"es", "es"},
            {"br", "br-FR"},
            {"eo", "eo"},
            {"ru", "ru-RU"},
    };
    private String m_language;

    public LanguageToolRequest(String language) {
        m_language = ConvertLanguage(language);
    }

    static private String GetSessionID() {
        Random rand = new Random();
        int MAX_NUM = 999999;
        int id = rand.nextInt(MAX_NUM);
        return Integer.toString(id);
    }


    private String ConvertLanguage(String language) {
        String lang = "";

        for (int i = 0; i < mAndroidToLTLangMap.length; i++) {
            if (language.startsWith(mAndroidToLTLangMap[i][0])) {
                lang = mAndroidToLTLangMap[i][1];
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

        StringBuilder sb = new StringBuilder();

        sb.append(AddQueryParameter("", "language", "ca"));
        sb.append(AddQueryParameter("&", "text", text));
        /* Parameter to allow languagetool.org to distingish the origin of the request */
        sb.append(AddQueryParameter("&", "useragent", "androidspell"));
        return sb.toString();
    }

    // HTTP POST request
    private String sendPost(String text) {

        try {

            String url = BuildURL();

            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
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
            StringBuffer response = new StringBuffer();
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
        Suggestion[] suggestions = {};
        return suggestions;
    }

    private String BuildURL() {
        StringBuilder sb = new StringBuilder();
        sb.append(SERVER_URL);
        /* Parameter to help to track requests from the same IP */
        sb.append(AddQueryParameter("?", "sessionID", m_sessionId));
        return sb.toString();
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
