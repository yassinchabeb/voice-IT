package com.palo_it.com.myapplication.text;

import android.util.Pair;
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
import java.util.ArrayList;
import java.util.List;

public class OntologySearcher {

    public static final String UNKNOWN = "UNKNOWN";
    private InputStream ontologyFile;

    private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder builder;
    private Document document;
    private Element racine;

    public OntologySearcher(final InputStream ontologyFile) {
        this.ontologyFile = ontologyFile;
        builder = getDoumentBuilder();
        document = getDocument();
        racine = getRacine();
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
        return document.getDocumentElement();
    }

    public Pair<String, String> getApi(String textFromSpeech) {
        Pair<String, String> apiOperation = null;
        if (racine != null) {
            final NodeList racineNoeuds = racine.getChildNodes();
            final int nbRacineNoeuds = racineNoeuds.getLength();

            for (int i = 0; i < nbRacineNoeuds; i++) {
                if (racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    final Element element = (Element) racineNoeuds.item(i);
                    if (element != null) {
                        if (element.getNodeName() != null && element.getNodeName().equals("owl:Class")) {

                            //                            System.out.println("getNodeName : " + element.getNodeName());

                            if (element.getAttribute("rdf:about") != null) {
                                //                                System.out.println("rdf:about : " + element.getAttribute("rdf:about"));
                            }
                            final Element subClassOf =
                                    (Element) element.getElementsByTagName("rdfs:subClassOf").item(0);
                            final Element command = (Element) element.getElementsByTagName("command").item(0);
                            final Element posture = (Element) element.getElementsByTagName("posture").item(0);
                            final Element message = (Element) element.getElementsByTagName("message").item(0);

                            if (subClassOf != null && subClassOf.getAttribute("rdf:resource") != null) {
                                //                                System.out.println("rdf:resource : " + subClassOf.getAttribute("rdf:resource"));
                            }

                            if (command != null) {
                                apiOperation = match(textFromSpeech, element, command, message);
                                if (!apiOperation.first.isEmpty()) {
                                    break;
                                }
                            }

                            if (posture != null) {
                                //                                System.out.println("posture : " + posture.getTextContent());
                            }
                        }
                    }
                }
            }
        }
        System.out.println("apiOperation : " + apiOperation.first);
        return apiOperation;
    }

    public Pair<String, String> match(String textFromSpeech, Element element, Element command, Element message) {
        String apiOperationTmp = "";
        String response = "";

        if (command.getTextContent().contains(textFromSpeech)) {
            if (element.getAttribute("rdf:about") != null) {
                int index = element.getAttribute("rdf:about").indexOf("#");
                apiOperationTmp = element.getAttribute("rdf:about").substring(index + 1);
            }
            if (message != null && message.getAttribute("xml:lang").equalsIgnoreCase("fr")) {
                response = message.getTextContent();
            }
        }
        return Pair.create(apiOperationTmp, response);
    }

    public List<String> getCommands() {
        ArrayList<String> commands = new ArrayList<>();
        if (racine != null) {
            final NodeList racineNoeuds = racine.getChildNodes();
            final int nbRacineNoeuds = racineNoeuds.getLength();

            for (int i = 0; i < nbRacineNoeuds; i++) {
                if (racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    final Element element = (Element) racineNoeuds.item(i);
                    if (element != null) {
                        if (element.getNodeName() != null && element.getNodeName().equals("owl:Class")) {
                            final Element command = (Element) element.getElementsByTagName("command").item(0);
                            if (command != null) {
                                commands.add(command.getTextContent());
                            }
                        }
                    }
                }
            }
        }
        return commands;
    }
}
