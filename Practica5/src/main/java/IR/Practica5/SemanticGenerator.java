package IR.Practica5;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.exit;

public class SemanticGenerator {
    static String raiz = "http://nuestraraiz/";
    // Genera el rdf a partir del modelo skos, el owl y el directorio de los documentos en xml
    public static void main(String[] args) {
        String usage = "java SemanticGenerator"
                + " [-rdf RDF_PATH] [-skos SKOS_PATH] [-owl OWL_PATH] [-docs DOCS_PATH]\n\n";
        String rdfPath = "index";
        String docsPath = null;
        String skosPath = null;
        String owlPath = null;
        for (int i = 0; i < args.length; i++) {
            if ("-rdf".equals(args[i])) {
                rdfPath = args[i + 1];
                i++;
            } else if ("-docs".equals(args[i])) {
                docsPath = args[i + 1];
                i++;
            } else if ("-skos".equals(args[i])) {
                skosPath = args[i + 1];
                i++;
            } else if ("-owl".equals(args[i])) {
                owlPath = args[i + 1];
                i++;
            }
        }

        if (docsPath == null) {
            System.err.println("Usage: " + usage);
            exit(1);
        }

        final File docDir = new File(docsPath);
        if (!docDir.exists() || !docDir.canRead()) {
            System.out.println("Document directory '" + docDir.getAbsolutePath() + "' does not exist or is not readable, please check the path");
            exit(1);
        }
        // cargar skos:
        Model modeloSkos = cargarModelo(skosPath, "TURTLE");
        Model modeloOwl = cargarModelo(owlPath, "RDF/XML");
        Model rdfOut = procesarDirectorio(modeloSkos, modeloOwl, docDir);
    }


    private static Model cargarModelo(String skosPath, String rdfSyntax) {
        return FileManager.get().loadModel(skosPath,rdfSyntax);
    }


    private static Model procesarDirectorio(Model modeloSkos, Model modeloOwl, File dir) {
        Model rdfOut = ModelFactory.createDefaultModel();
        if (dir.canRead()) {
            if (dir.isDirectory()) {
                String[] files = dir.list();
                // an IO error could occur
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        rdfOut = procesarFichero(modeloSkos, modeloOwl, rdfOut, new File(dir, files[i]));
                    }
                }
            } else {
                System.out.println(dir + " no es un directorio");
                System.exit(1);
            }
        }
        return rdfOut;
    }

    private static Model procesarFichero(Model modeloSkos, Model modeloOwl, Model rdfOut, File file) {

        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException fnfe) {
            // at least on windows, some temporary files raise this exception with an "access denied" message
            // checking if the file can be read doesn't help
            return null;
        }
        try {
            // Parser del documento:
            DocumentBuilderFactory creadorDoc = DocumentBuilderFactory.newInstance();
            DocumentBuilder docB = creadorDoc.newDocumentBuilder();
            org.w3c.dom.Document documento = docB.parse(fis);

            documento.getDocumentElement().normalize();

            //List<String> listaTexto=new ArrayList<String>();
            List<String> listaTexto = Arrays.asList("title", "contributor", "subject", "description", "creator", "publisher");
            List<String> listaString = Arrays.asList("identifier", "type", "date", "format", "language");

            Map<String, String> campos = new HashMap<>();

            for (String nom : listaTexto) { // nom es el nombre del campo

                NodeList nList = documento.getElementsByTagName("dc:" + nom);

                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    Node datoContenido = nNode.getFirstChild();

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        //Element eElement = (Element) nNode;
                        String s = datoContenido.getNodeValue(); // s es el valor
                        //rdfOut = procesarTexto(modeloSkos, modeloOwl, rdfOut, nom, s);
                        campos.put(nom, s);
                    }
                }
            }
            for (String nom : listaString) {

                NodeList nList = documento.getElementsByTagName("dc:" + nom);

                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    Node datoContenido = nNode.getFirstChild();

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        //Element eElement = (Element) nNode;
                        String s = datoContenido.getNodeValue();
                        campos.put(nom, s);
                        //rdfOut = procesarString(modeloSkos, modeloOwl, rdfOut, nom, s);
                        //System.out.println("");
                    }
                }
            }
            rdfOut = procesarCampos(modeloSkos, modeloOwl, rdfOut, campos);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return rdfOut;
    }

    // nom es uno de estos {"identifier", "type", "date", "format", "language"} (Strings cortas)
    // o estos {"title", "contributor", "subject", "description", "creator", "publisher"} (Textos)
    private static Model procesarCampos(Model modeloSkos, Model modeloOwl, Model rdfOut, Map<String, String> campos) {
        String uri = uriFromIdentifier(campos.get("identifier"));
        Resource doc = rdfOut.createResource(uri)
                .addProperty(RDF.type, modeloOwl.getResource(raiz+"Documento"))
                .addProperty(SKOS.prefLabel, "ciencia","es")
                //.addProperty(SKOS.prefLabel, "ciencia", "es")
                .addProperty(SKOS.prefLabel, "science", "en")
                .addProperty(SKOS.altLabel, "100cia", "en");
        /*for (Map.Entry<String, String> entry : campos.entrySet()) {
            String clave = entry.getKey();
            String valor = entry.getValue();
            switch (clave) {
                case
            }
        }*/
        return rdfOut;
    }

    private static String uriFromIdentifier(String identifier) {
        // TODO: revisar +1 ????
        int lenIni = "http://zaguan.unizar.es/".length();
        String fin = identifier.substring(lenIni);
        return raiz+fin;
    }

}
