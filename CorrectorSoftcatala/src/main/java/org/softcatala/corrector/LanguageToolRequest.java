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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.util.Log;

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

    private static String toString(InputStream inputStream) throws Exception {
        StringBuilder outputBuilder = new StringBuilder();
        try {
            String string;
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream, ENCODING));
                while (null != (string = reader.readLine())) {
                    outputBuilder.append(string).append('\n');
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error reading translation stream.", ex);
        }
        return outputBuilder.toString();
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

    private void FillPostFields(HttpPost httpPost, String text) {
        BasicNameValuePair languageBasicNameValuePair = new BasicNameValuePair("language", m_language);
        BasicNameValuePair textBasicNameValuePair = new BasicNameValuePair("text", text);
        /* Parameter to allow languagetool.org to distingish the origin of the request */
        BasicNameValuePair useragentBasicNameValuePair = new BasicNameValuePair("useragent", "androidspell");

        List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
        nameValuePairList.add(languageBasicNameValuePair);
        nameValuePairList.add(textBasicNameValuePair);
        nameValuePairList.add(useragentBasicNameValuePair);

        try {

            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairList, "UTF-8");
            httpPost.setEntity(urlEncodedFormEntity);
        } catch (Exception e) {
            Log.e(TAG, "Error reading stream from URL.", e);
        }
    }

    public Suggestion[] Request(String text) {
        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); // Timeout
        // Limit
        HttpURLConnection uc = null;

        try {

            String url = BuildURL();

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            FillPostFields(httpPost, text);
            Log.d(TAG, "Request start:" + url);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            InputStream inputStream = httpResponse.getEntity().getContent();

            String result = toString(inputStream);
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
