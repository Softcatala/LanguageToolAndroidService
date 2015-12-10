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
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import android.util.Log;

public class LanguageToolRequest {

	private static final String SERVER_URL = "http://www.softcatala.org/languagetool/api/";
	private static final String ENCODING = "UTF-8";
	private static final String TAG = SampleSpellCheckerService.class
			.getSimpleName();

	// private static final boolean DBG = true;

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
			uc = (HttpURLConnection) new URL(url).openConnection();

			InputStream is = uc.getInputStream();
			String result = toString(is);
			Log.d(TAG, "Request result: " + result);
			return ReadXml(result, text);
		} catch (Exception e) {
			Log.e(TAG,  "Error reading stream from URL.", e);
		}
		Suggestion[] suggestions = {};
		return suggestions;
	}

	private Suggestion[] ReadXml(String xml, String text) {
		ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		InputSource is;

		String [] lines = text.split("\n");
		ArrayList <Integer> linelen = new ArrayList<Integer>();

		// Stores the position in chars for every Y value
		int currentY = 0;
		for (int i = 0; i < lines.length; i++) {
			linelen.add(i, currentY);
			currentY += lines[i].length() + 1;
            Log.i(TAG, "Line len: " + currentY);
		}

		try {
			builder = factory.newDocumentBuilder();
			is = new InputSource(new StringReader(xml));
			org.w3c.dom.Document doc = builder.parse(is);
			NodeList list = doc.getElementsByTagName("error");

			for (int i = 0; i < list.getLength(); i++) {
				NamedNodeMap nodeMap = list.item(i).getAttributes();
				Node fromX = nodeMap.getNamedItem("fromx");
				Node fromY = nodeMap.getNamedItem("fromy");
				Node replacements = nodeMap.getNamedItem("replacements");
				Node ruleId = nodeMap.getNamedItem("ruleId");
                Node errorLength = nodeMap.getNamedItem("errorlength");

				// Since we process fragments we need to skip the upper case
				// suggestion
				if (ruleId.getNodeValue().equals("UPPERCASE_SENTENCE_START") == true)
					continue;

				Suggestion suggestion = new Suggestion();
				String value = replacements.getNodeValue();

				if (value.length() == 0) {
					suggestion.Text = new String[] { "(sense suggeriment correcció)" };
				} else {
					suggestion.Text = value.split("#");
				}

				value = fromY.getNodeValue();
				Integer valueY = Integer.parseInt(value);
				valueY = linelen.get(valueY);

				value = fromX.getNodeValue();
				suggestion.Position = valueY + Integer.parseInt(value);
				suggestion.Length = Integer.parseInt(errorLength.getNodeValue());
				suggestions.add(suggestion);

                Log.d(TAG, "Request result: " + suggestion.Position + " Len:" + suggestion.Length);
			}

		} catch (Exception e) {
			Log.e(TAG, "ReadXml", e);
		}

		return suggestions.toArray(new Suggestion[0]);
	}

	private String BuildURL(final String text) {
		StringBuilder sb = new StringBuilder();
		sb.append(SERVER_URL);
		sb.append("?language=ca-ES");
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
			Log.e("error", "Error reading translation stream.", ex);
		}
		return outputBuilder.toString();
	}

	public class Suggestion {
		public int Position;
		public String[] Text;
		public int Length;
	}

}
