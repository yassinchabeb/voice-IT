package com.palo_it.com.myapplication.text;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class SemanticParserMatcher {

    private InputStream ontologyFile;

    DocumentBuilderFactory factory;
    DocumentBuilder builder;
    Document document;
    Element racine;

    public SemanticParserMatcher(InputStream ontologyFile) {
        this.ontologyFile = ontologyFile;
        buildFactory();
        builder = getDoumentBuilder();
        document = getDocument();
        racine = getRacine();
    }

    public void buildFactory() {
        factory = DocumentBuilderFactory.newInstance();
    }

    public DocumentBuilder getDoumentBuilder() {
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return builder;
    }

    public Document getDocument() {
        Document document = null;
        try {
            document = builder.parse(ontologyFile);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
        return document;
    }

    public Element getRacine() {
        Element racine;
        // System.out.println("*************PROLOGUE************");
        // System.out.println("version : " + document.getXmlVersion());
        // System.out.println("encodage : " + document.getXmlEncoding());
        // System.out.println("standalone : " +
        // document.getXmlStandalone());
        racine = document.getDocumentElement();
        // System.out.println("\n*************RACINE************");
        // System.out.println(racine.getNodeName());
        return racine;
    }

    public String getApi(String textFromSpeech) {
        String apiOperation = "UNKNOWN";

        if (racine != null) {
            final NodeList racineNoeuds = racine.getChildNodes();
            final int nbRacineNoeuds = racineNoeuds.getLength();

            for (int i = 0; i < nbRacineNoeuds; i++) {
                if (racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    final Element element = (Element) racineNoeuds.item(i);
                    if (element != null) {
                        if (element.getNodeName() != null
                                && element.getNodeName().equals("owl:Class")) {

                            System.out.println("getNodeName : "
                                    + element.getNodeName());

                            if (element.getAttribute("rdf:about") != null) {
                                System.out.println("rdf:about : "
                                        + element.getAttribute("rdf:about"));
                            }
                            final Element subClassOf = (Element) element
                                    .getElementsByTagName("rdfs:subClassOf")
                                    .item(0);

                            final Element command = (Element) element
                                    .getElementsByTagName("command").item(0);

                            final Element posture = (Element) element
                                    .getElementsByTagName("posture").item(0);

                            if (subClassOf != null
                                    && subClassOf.getAttribute("rdf:resource") != null) {
                                System.out.println("rdf:resource : "
                                        + subClassOf
                                        .getAttribute("rdf:resource"));
                            }

                            if (command != null) {
                                apiOperation = match(textFromSpeech, element,
                                        command, apiOperation);
                            }

                            if (posture != null) {
                                System.out.println("posture : "
                                        + posture.getTextContent());
                            }
                        }
                    }
                }
            }
        }
        System.out.println("apiOperation : " + apiOperation);
        return apiOperation;
    }

    public String match(String textFromSpeech, Element element, Element command, String apiOperation) {
        String apiOperationTmp = apiOperation;

        System.out.println("command : " + command.getTextContent());
        // String textFromSpeech = "arrête";
        if (command.getTextContent().contains(textFromSpeech)) {
            System.out.println("call API : #########");
            if (element.getAttribute("rdf:about") != null) {
                int index = element.getAttribute("rdf:about").indexOf("#");
                apiOperationTmp = element.getAttribute("rdf:about").substring(
                        index + 1);
                System.out.println("rdf:about : " + apiOperationTmp);
            }
            System.out.println("####################");
        }
        return apiOperationTmp;
    }
}
