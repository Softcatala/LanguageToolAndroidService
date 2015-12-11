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

import java.io.StringReader;
import java.util.ArrayList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import android.util.Log;

public class LanguageToolParsing {

    private static final String TAG = SampleSpellCheckerService.class
            .getSimpleName();


    public Suggestion[] GetSuggestions(String xml, String text) {
        ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        InputSource is;

        // Stores the position in chars for every Y value
        ArrayList <Integer> linelen = GetLinesLength(text);

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
            Log.e(TAG, "GetSuggestions", e);
        }

        return suggestions.toArray(new Suggestion[0]);
    }

    private ArrayList <Integer> GetLinesLength(String text) {
        ArrayList <Integer> linelen = new ArrayList<Integer>();
        String [] lines = text.split("\n");
        int currentY = 0;
        for (int i = 0; i < lines.length; i++) {
            linelen.add(i, currentY);
            currentY += lines[i].length() + 1;
            Log.i(TAG, "Line len: " + currentY);
        }
        return linelen;
    }
}
