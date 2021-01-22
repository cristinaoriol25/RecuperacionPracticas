package IR.Practica5;

import openllet.jena.PelletReasonerFactory;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.vocabulary.XSD;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

import static java.lang.System.exit;

public class SemanticGenerator {
    static String raiz = "http://nuestraraiz/";
    // Genera el rdf a partir del modelo skos, el owl y el directorio de los documentos en xml
    public static void main(String[] args) throws FileNotFoundException {
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
        System.out.println("A cargar los modelos " + skosPath+ " "+owlPath);
        // cargar skos:
        Model modeloSkos = cargarModelo(skosPath, "TURTLE");
        Model modeloOwl = cargarModelo(owlPath, "TURTLE");
        // ----------------------------------- PRUEBA Inferencia:
        //InfModel inf2 = ModelFactory.createInfModel(PelletReasonerFactory.theInstance().create(), modeloOwl.union(modeloSkos));//
        //inf2.write(new FileOutputStream(new File("test-"+rdfPath)),"RDF/XML");
        //exit(0);
        // ----------------------------------------------------------------------
        System.out.println("A procesar directorios:");
        Model rdfOut = procesarDirectorio(modeloSkos, modeloOwl, docDir);
        //rdfOut.write(new FileOutputStream(new File(rdfPath)),"RDF/XML");
        // ------------------------- Se pueden unir los tres modelos en un solo fichero:
        //Model unido = rdfOut.union(modeloSkos);
        //unido = unido.union(modeloOwl);
        rdfOut.write(new FileOutputStream(new File("sininf-"+rdfPath)),"RDF/XML");
        //System.out.println("a inferir");
        // Inferencia:
        //InfModel inf = ModelFactory.createInfModel(PelletReasonerFactory.theInstance().create(), rdfOut);
        // borramos elementos del modelo para facilitar la visualizacion de lo que nos interesa
        //Model model2 = borrarRecursosOWL(inf);
        //model2.write(new FileOutputStream(new File("inf-"+rdfPath)),"RDF/XML");
    }


    private static Model cargarModelo(String skosPath, String rdfSyntax) {
        return FileManager.get().loadModel(skosPath,rdfSyntax);
    }


    private static Model procesarDirectorio(Model modeloSkos, Model modeloOwl, File dir) {
        Model rdfOut = addConstantes(modeloSkos, modeloOwl);
        Model unido = rdfOut.union(modeloSkos);
        rdfOut = unido.union(modeloOwl);
        //Model rdfOut = ModelFactory.createDefaultModel();
        if (dir.canRead()) {
            if (dir.isDirectory()) {
                String[] files = dir.list();
                // an IO error could occur
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        rdfOut = procesarFichero(modeloSkos, modeloOwl, rdfOut, new File(dir, files[i]));
                        //if (i>500)
                            //break; // TODO: BORRAR, DEBUG
                    }
                }
            } else {
                System.out.println(dir + " no es un directorio");
                System.exit(1);
            }
        }
        return rdfOut;
    }

    private static Model addConstantes(Model modeloSkos, Model modeloOwl) {
        Model rdfOut = ModelFactory.createDefaultModel();
        Resource ingles = rdfOut.createResource(raiz+"eng")
                .addProperty(RDF.type, modeloOwl.getResource(raiz+"Idioma"));
        Resource esp = rdfOut.createResource(raiz+"spa")
                .addProperty(RDF.type, modeloOwl.getResource(raiz+"Idioma"));
        Resource fra = rdfOut.createResource(raiz+"fre")
                .addProperty(RDF.type, modeloOwl.getResource(raiz+"Idioma"));
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
            List<String> listaString = Arrays.asList("identifier", "type", "date", "language");

            Map<String, List<String>> campos = new HashMap<>();

            for (String nom : listaTexto) { // nom es el nombre del campo

                NodeList nList = documento.getElementsByTagName("dc:" + nom);
                List<String> vals = new ArrayList<>();
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    Node datoContenido = nNode.getFirstChild();

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        //Element eElement = (Element) nNode;
                        String s = datoContenido.getNodeValue(); // s es el valor
                        //rdfOut = procesarTexto(modeloSkos, modeloOwl, rdfOut, nom, s);
                        vals.add(s);
                    }
                }
                campos.put(nom, vals);
            }
            for (String nom : listaString) {

                NodeList nList = documento.getElementsByTagName("dc:" + nom);

                List<String> vals = new ArrayList<>();
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    Node datoContenido = nNode.getFirstChild();

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        //Element eElement = (Element) nNode;
                        String s = datoContenido.getNodeValue();
                        vals.add(s);

                        //rdfOut = procesarString(modeloSkos, modeloOwl, rdfOut, nom, s);
                        //System.out.println("");
                    }
                }
                campos.put(nom, vals);
            }
            rdfOut = procesarCampos(modeloSkos, modeloOwl, rdfOut, campos);
            //System.out.print(campos.get("identifier"));
            //InfModel inf = ModelFactory.createInfModel(PelletReasonerFactory.theInstance().create(), rdfOut);
            //System.out.println(" " + inf.toString().charAt(0));
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
    private static Model procesarCampos(Model modeloSkos, Model modeloOwl, Model rdfOut, Map<String, List<String>> campos) {
        String uri = uriFromIdentifier(campos.get("identifier").get(0));
        // Campos {"identifier", "date", "format", "language"} (Strings cortas)
        // + {"title", "contributor", "subject", "description", "creator", "publisher"} (Textos)

        String tipo = parseTipo(campos.get("type").get(0));
        //System.out.println(date);
        Resource doc = rdfOut.createResource(uri)
                .addProperty(RDF.type, modeloOwl.getResource(tipo))
                .addProperty(modeloOwl.getProperty(raiz+"Idioma-documento"), rdfOut.getResource(raiz+campos.get("language").get(0)))
                .addProperty(modeloOwl.getProperty(raiz+"title"), campos.get("title").get(0));
        for (String contributor : campos.get("contributor")) {
            String uriCont = crearUri(contributor);
            String[] nombres = parseNombre(contributor);

            // System.out.println(contributor + " ---------- " + uriCont);
            Resource contributorRes = rdfOut.createResource(uriCont)
                    .addProperty(RDF.type, modeloOwl.getResource(raiz+"Contributor"));
            if (nombres.length > 0) {
                contributorRes.addProperty(FOAF.family_name, nombres[0]);
            }
            if (nombres.length > 1) {
                contributorRes.addProperty(FOAF.firstName, nombres[1]);
            }
            if (nombres.length == 0) {
                System.out.println(contributor + " no tiene nombres!!");
                exit(1);
            }
            doc.addProperty(modeloOwl.getProperty(raiz+"contribuido"), contributorRes);
        }
        for (String creator : campos.get("creator")) {
            String uriCreator = crearUri(creator);
            Resource creatorRes = rdfOut.createResource(uriCreator);

            String[] nombres = parseNombre(creator);

            // System.out.println(contributor + " ---------- " + uriCont);
            Resource contributorRes = rdfOut.createResource(uriCreator)
                    .addProperty(RDF.type, modeloOwl.getResource(raiz+"Creator"));
            if (nombres.length > 0) {
                contributorRes.addProperty(FOAF.family_name, nombres[0]);
            }
            if (nombres.length > 1) {
                contributorRes.addProperty(FOAF.firstName, nombres[1]);
            }
            if (nombres.length == 0) {
                System.out.println(creator + " no tiene nombres!!");
                exit(1);
            }
            doc.addProperty(modeloOwl.getProperty(raiz+"creado"), creatorRes);
        }
        for (String publisher : campos.get("publisher")) {
            String uriPublisher = crearUri(publisher);
            Resource nombreDep = rdfOut.createResource(uriPublisher+"-nombre")
                    .addProperty(RDF.type, modeloOwl.getResource(raiz+"Nombre-departamento"));
            Resource departamentoRes = rdfOut.createResource(uriPublisher)
                    .addProperty(modeloOwl.getProperty(raiz+"Nombre-departamento"), publisher);
            doc.addProperty(modeloOwl.getProperty(raiz+"publisher"), departamentoRes);
        }
        for (String subject : campos.get("subject")) { // Para cada subject
            doc.addProperty(modeloOwl.getProperty(raiz+"Subject"), subject);
        }
        for (String description : campos.get("description")) { // Para cada subject
            doc.addProperty(modeloOwl.getProperty(raiz+"description"), description);
        }

        if (!campos.get("date").isEmpty()) {
            String date = campos.get("date").get(0);
            rdfOut.createTypedLiteral(date, XSDDatatype.XSDgYear);
            doc.addProperty(modeloOwl.getProperty(raiz + "date"), rdfOut.createTypedLiteral(date, XSDDatatype.XSDgYear));
        }
        rdfOut = addConceptos(modeloSkos, modeloOwl, doc, rdfOut, campos);

        return rdfOut;
    }

    private static Model addConceptos(Model modeloSkos, Model modeloOwl, Resource doc, Model rdfOut,
                                      Map<String, List<String>> campos)
    {
        for (var clave : campos.keySet()) { // para cada lista de textos
            for (String texto : campos.get(clave)) { // para cada texto en el doc de entrada
                // Tokenizar (obtener palabras o raices):
                List<String> tokens = tokenizar(texto);
                rdfOut = addConceptosTokens(modeloSkos, modeloOwl, doc, rdfOut, tokens);

            }
        }
        return rdfOut;
    }

    private static Model addConceptosTokens(Model modeloSkos, Model modeloOwl, Resource doc, Model rdfOut, List<String> tokens) {
        for (String token : tokens) {
            //System.out.println("--"+token+"--");

            List<String> urisConceptos = getURIsConcepto(modeloSkos, token);
            //System.out.println(token);
            for (var uri : urisConceptos) {
                //System.out.println(token + " ... " + uri);
                doc.addProperty(modeloOwl.getProperty(raiz+"Tema"), modeloSkos.getResource(uri));
            }

        }
        return rdfOut;
    }
    // Fuente: https://stackoverflow.com/a/1102916/14997419
    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
    private static List<String> getURIsConcepto(Model modeloSkos, String token) {
        List<String> uris = new ArrayList<>();
        // PREF LABELS:
        StmtIterator it = modeloSkos.listStatements(null, SKOS.prefLabel, (RDFNode) null);
        while (it.hasNext()) {
            Statement st = it.next();
            if (st.getObject().isLiteral() && st.getLiteral().getLexicalForm().equals(token)) {
                uris.add(st.getSubject().getURI());
            }
        }
        // ALT LABELS:
        it = modeloSkos.listStatements(null, SKOS.altLabel, (RDFNode) null);
        while (it.hasNext()) {
            Statement st = it.next();
            if (st.getObject().isLiteral() && st.getLiteral().getLexicalForm().equals(token)) {
                uris.add(st.getSubject().getURI());
            }
        }
        return uris;
    }

    private static List<String> getURIsConceptoSPARQL(Model modeloSkos, String token) {
        /*String queryString = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                "SELECT ?x ?y ?z WHERE { " +
                "?x foaf:maker " + uri + ". }";*/
        List<String> uris = new ArrayList<>();
        String tokenComs = "\""+token+"\"";
        //System.out.println(tokenComs + " " + tokenComs.length());
        String queryString = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
                "SELECT ?x WHERE {{?x <http://www.w3.org/2004/02/skos/core#prefLabel> " + tokenComs +
                " } UNION {?x <http://www.w3.org/2004/02/skos/core#altLabel> " + tokenComs + " }. }";
        //System.out.println("Querystring: " +queryString);
        //ejecutamos la consulta y obtenemos los resultados
        Query query = QueryFactory.create(queryString) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, modeloSkos);
        try {
            ResultSet results = qexec.execSelect();
            for (; results.hasNext(); ) {
                System.out.println("RESULTADO");
                QuerySolution soln = results.nextSolution();
                Resource x = soln.getResource("x");
                if (x.isLiteral()) {
                    uris.add(x.getURI());
//                    System.out.println(x.getURI() + " - "
//                            + x.getURI() + " - "
//                            + x.toString());
                }
                else {
                    System.out.println("no es literal");
                    uris.add(x.getURI());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uris;
    }




    private static String sinTildes(String input) {
        return input.replaceAll("[áàäâ]", "a").replaceAll("[éèêë]", "e")
                .replaceAll("[îìíï]", "i").replaceAll("[óöòô]", "o")
                .replaceAll("[úüùû]", "u").replaceAll("ñ", "n");
    }

    private static List<String> tokenizar(String texto) {
        String regexTildes = "[]";
        return Arrays.asList(sinTildes(texto).toLowerCase()
                .replaceAll("[()\"\\\\',;./:?!¿¡\\-\\[\\]]+", " ").strip().split("[ \n]+"));
    }


    private static String[] parseNombre(String nombreCompleto) {
        String[] tokens = nombreCompleto.split("[ ]*,[ ]*");
        return tokens;
    }

    private static String crearUri(String valor) {
        return raiz+valor.hashCode();//.replaceAll("[ ]+", "-");
    }


    private static String parseTipo(String type) {
        String fin ="";
        type = type.strip();
        //System.out.println("--"+type+"--");
        if (type.equals("TESIS")) {
            fin = "Tesis";
        } else if (type.equals("TAZ-TFG")) {
            fin = "TFG";
        }else if (type.equals("TAZ-TFM")) {
            fin = "TFM";
        }else if (type.equals("TAZ-PFC")) {
            fin = "TFC";
        } else {
            System.out.println("Tipo " + type + " desconocido!");
            exit(1);
        }
        return raiz+fin;
    }

    private static String uriFromIdentifier(String identifier) {
        // TODO: revisar +1 ????
        int lenIni = "http://zaguan.unizar.es/".length();
        String fin = identifier.substring(lenIni);
        return raiz+fin;
    }

    /** De la practica 5
     * borramos las clases del modelo rdfs que se añaden automáticamene al hacer la inferencia
     * simplemente para facilitar la visualización de la parte que nos interesa
     * si quieres ver todo lo que genera el motor de inferencia comenta estas lineas
     */
    private static Model borrarRecursosOWL(Model inf) {
        //hacemos una copia del modelo ya que el modelo inferido es inmutable
        Model model2 = ModelFactory.createDefaultModel();
        model2.add(inf);
        model2.removeAll(inf.createResource("http://www.w3.org/2002/07/owl#topDataProperty"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/2002/07/owl#topObjectProperty"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/2002/07/owl#Thing"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/2002/07/owl#bottomObjectProperty"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/2002/07/owl#Nothing"), null, (RDFNode)null);
        model2.removeAll(inf.createResource("http://www.w3.org/2002/07/owl#bottomDataProperty"), null, (RDFNode)null);

        return model2;
    }

}
