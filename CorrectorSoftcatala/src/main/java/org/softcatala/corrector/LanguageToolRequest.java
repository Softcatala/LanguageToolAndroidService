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
import org.apache.http.client.HttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.BufferedReader;
import java.util.Date;

import android.util.Log;

public class LanguageToolRequest {

	//private static final String SERVER_URL = "https://www.softcatala.org/languagetool/api/";
	private static final String SERVER_URL = "https://languagetool.org:8081";
	private static final String ENCODING = "UTF-8";
	private static final String TAG = LanguageToolRequest.class.getSimpleName();

	private final LanguageToolParsing languageToolParsing = new LanguageToolParsing();
	private String m_language;

    public LanguageToolRequest(String language)
    {
        m_language = ConvertLanguage(language);
    }

    String[][] mAndroidToLTLangMap = new String[][]{
            {"en", "en-US"},
            {"de", "de-DE"},
            {"pl", "pl"},
            {"fr", "fr"},
            {"ca", "ca"}
    };

    private String ConvertLanguage(String language)
    {
        String lang = "";

        for (int i = 0; i < mAndroidToLTLangMap.length; i++) {
            if (language.startsWith(mAndroidToLTLangMap[i][0])) {
                lang =  mAndroidToLTLangMap[i][1];
                break;
            }
        }
        Log.d(TAG, String.format("ConvertLanguage from Android %s to LT %s", language, lang));
        return lang;
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

	public Suggestion[] GetSuggestions(String text) {
		return Request(text);
	}

	public Suggestion[] Request(String text) {
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); // Timeout
																				// Limit
		HttpURLConnection uc = null;

		try {

			String url = BuildURL(text);
            Log.d(TAG, "Request start:" + url);
			uc = (HttpURLConnection) new URL(url).openConnection();

            /* This a specific HTTP header parameter (not the standard User-Agent) to allow
            * languagetool.org to distingish the origin of the request */
            uc.setRequestProperty("useragent","androidspell");

			InputStream is = uc.getInputStream();
			String result = toString(is);

			Configuration.getInstance().incConnections();
			Configuration.getInstance().setLastConnection(new Date());
			Log.d(TAG, "Request result: " + result);
			return languageToolParsing.GetSuggestions(result, text);
		} catch (Exception e) {
			Log.e(TAG, "Error reading stream from URL.", e);
		}
		Suggestion[] suggestions = {};
		return suggestions;
	}

	private String BuildURL(final String text) {
		StringBuilder sb = new StringBuilder();
		sb.append(SERVER_URL);
		//String lang = Configuration.getInstance().getDialect() ? "ca-ES-valencia" : "ca-ES";
		sb.append("?language=" + m_language);
		sb.append(AddQueryParameter("text", text));
		return sb.toString();
	}

	String AddQueryParameter(String key, String value) {
		StringBuilder sb = new StringBuilder();
		sb.append("&");
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
