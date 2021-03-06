/**
 * Licensed to DigitalPebble Ltd under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * DigitalPebble licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.digitalpebble.storm.crawler.parse.filter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.digitalpebble.storm.crawler.parse.ParseData;
import com.digitalpebble.storm.crawler.parse.ParseFilter;
import com.digitalpebble.storm.crawler.parse.ParseResult;
import com.fasterxml.jackson.databind.JsonNode;

public class SubDocumentsParseFilter implements ParseFilter {
    private static final org.slf4j.Logger LOG = LoggerFactory
            .getLogger(SubDocumentsParseFilter.class);

    @Override
    public void filter(String URL, byte[] content, DocumentFragment doc,
            ParseResult parse) {

        InputStream stream = new ByteArrayInputStream(content);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            Document document = factory.newDocumentBuilder().parse(stream);
            Element root = document.getDocumentElement();

            XPath xPath = XPathFactory.newInstance().newXPath();
            XPathExpression expression = xPath.compile("//url");

            NodeList nodes = (NodeList) expression.evaluate(root,
                    XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);

                expression = xPath.compile("loc");
                Node child = (Node) expression.evaluate(node,
                        XPathConstants.NODE);

                // create a subdocument for each url found in the sitemap
                ParseData parseData = parse.get(child.getTextContent());

                NodeList childs = node.getChildNodes();
                for (int j = 0; j < childs.getLength(); j++) {
                    Node n = childs.item(j);
                    parseData.put(n.getNodeName(), n.getTextContent());
                }
            }
        } catch (Exception e) {
            LOG.error("Error processing sitemap from {}: {}", URL, e);
        }
    }

    @Override
    public void configure(Map stormConf, JsonNode filterParams) {
        // nothing to do here for now
    }

    @Override
    public boolean needsDOM() {
        return false;
    }
}
