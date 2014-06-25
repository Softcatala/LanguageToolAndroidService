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
			return ReadXml(result);
		} catch (Exception e) {
			Log.e("error", "Error reading stream from URL.", e);
		}
		Suggestion[] suggestions = {};
		return suggestions;
	}

	private Suggestion[] ReadXml(String xml) {
		ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		InputSource is;

		try {
			builder = factory.newDocumentBuilder();
			is = new InputSource(new StringReader(xml));
			org.w3c.dom.Document doc = builder.parse(is);
			NodeList list = doc.getElementsByTagName("error");		

			for (int i = 0; i < list.getLength(); i++) {
				NamedNodeMap nodeMap = list.item(i).getAttributes();
				Node fromX = nodeMap.getNamedItem("fromx");
				Node replacements = nodeMap.getNamedItem("replacements");
				Node toX = nodeMap.getNamedItem("tox");				
				
				Suggestion suggestion = new Suggestion();
				suggestion.Text = replacements.getNodeValue();
				String value = fromX.getNodeValue();
				suggestion.Position = Integer.parseInt(value);
				value = toX.getNodeValue();
				int to = Integer.parseInt(value);
				suggestion.Length = to - suggestion.Position;				
				suggestions.add(suggestion);
			}

		} catch (Exception e) {
			Log.e("error", "Parsing XML response", e);
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
		public String Text;
		public int Length;

	}

}
