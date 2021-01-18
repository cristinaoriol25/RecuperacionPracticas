package IR.Practica5;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;

import java.io.*;

public class SemanticSearcher {
    static String raiz = "http://nuestraraiz/";
    // Genera el rdf a partir del modelo skos, el owl y el directorio de los documentos en xml
    public static void main(String[] args) throws IOException {
        String usage = "java SemanticGenerator"
                + " [-rdf RDF_PATH] [-skos SKOS_PATH] [-owl OWL_PATH] [-docs DOCS_PATH]\n\n";
        String rdfPath = null;
        String needsPath = null;
        String outputPath = null;
        for (int i = 0; i < args.length; i++) {
            if ("-rdf".equals(args[i])) {
                rdfPath = args[i + 1];
                i++;
            } else if ("-infoNeeds".equals(args[i])) {
                needsPath = args[i + 1];
                i++;
            } else if ("-output".equals(args[i])) {
                outputPath = args[i + 1];
                i++;
            }
        }
        if(rdfPath==null || needsPath==null || outputPath==null) {
            System.out.println("Invocación incorrecta :" + usage);
        }
        Model modelo= cargarModelo(rdfPath, "RDF/XML");
        BufferedReader in = null;
        in = new BufferedReader(new InputStreamReader(new FileInputStream(needsPath), "UTF-8"));
        while(true){
            String line = in.readLine();

            if (line == null || line.length() == -1) {
                break;
            }

            line = line.trim();
            if (line.length() == 0) {
                break;
            }
            procesarConsulta(line, outputPath, modelo);
            break;
        }

    }


    private static Model cargarModelo(String skosPath, String rdfSyntax) {
        return FileManager.get().loadModel(skosPath,rdfSyntax);
    }

    /*Queries viejas:
    String query = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
                "PREFIX raiz:<http://nuestraraiz/> " +
                "PREFIX skos:<http://www.w3.org/2004/02/skos/core#>" +
                "PREFIX documento:<http://nuestraraiz/Documento> " +
                "SELECT ?x WHERE { " +
                "?x skos:prefLabel \"alzheimer\" . " +
                "?x rdf:type skos:Concept } ";
        String query_2 = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
                "PREFIX raiz:<http://nuestraraiz/> " +
                "PREFIX skos:<http://www.w3.org/2004/02/skos/core#>" +
                "PREFIX documento:<http://nuestraraiz/Documento> " +
                "SELECT ?x " +
                "WHERE { ?x rdf:type ?tipo . " +
                "?tipo rdfs:subClassOf raiz:Documento . " +
                "?x raiz:Tema ?concepto . " +
                "?concepto skos:prefLabel \"alzheimer\" } ";// +
        //Estoy interesado en trabajos académicos sobre Bioinformática (también conocida como Biología Computacional,
        // Bioinformatics o Computational Biology) o Filogenética (Phylogenetics), publicados entre 2010 y 2018.
        String queryNecesidad4 = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "+
                "PREFIX raiz:<http://nuestraraiz/> " +
                "PREFIX skos:<http://www.w3.org/2004/02/skos/core#>" +
                "PREFIX documento:<http://nuestraraiz/Documento> " +
                "SELECT DISTINCT ?x ?date WHERE { " +
                "?x raiz:date ?date . " +
                "FILTER ( ?date >= \"2010\" ) . " +
                "FILTER ( ?date <= \"2018\" ) . " +
                "?x raiz:Tema ?concepto . " +
                "{" +
                "{?concepto skos:prefLabel \"bioinformatica\"@es } UNION " +
                "{?concepto skos:prefLabel \"bioinformatics\"@en } UNION " +
                "{?concepto skos:prefLabel \"phylogenetics\"@en } UNION " +
                "{?concepto skos:prefLabel \"filogenetica\"@es } UNION " +
                "{?concepto skos:prefLabel \"computational biology\"@en } UNION " +
                "{?concepto skos:prefLabel \"bioinformatics\"@en }" +
                "} }";
        String queryNecesidad5 = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "+
                "PREFIX raiz:<http://nuestraraiz/> " +
                "PREFIX skos:<http://www.w3.org/2004/02/skos/core#>" +
                "PREFIX documento:<http://nuestraraiz/Documento> " +
                "SELECT ?x ?date WHERE { " +
                "?x raiz:date ?date . " +
                "FILTER ( ?date >= \"2012\" ) . " +
                "?x raiz:Idioma-documento raiz:eng . " +
                "?x rdf:type raiz:TFG . " +
                "{" +
                "{?x raiz:creado ?autor . ?autor foaf:firstName \"Javier\" } " +
                "UNION " +
                "{?x raiz:contribuido ?cont . ?cont foaf:firstName \"Javier\" }" +
                "} . " +
                "?x raiz:Tema ?concepto . " +
                "?concepto skos:prefLabel \"informatica\"@es } ";*/


    private static void procesarConsulta(String consulta, String output, Model model) throws IOException {
        FileWriter myWriter = new FileWriter(output);
        String[] Consulta=consulta.split(" ", 2);
        String nConsulta=Consulta[0];
        String query=Consulta[1];
        System.out.println(query);
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        try {
            ResultSet results = qexec.execSelect() ;
            for ( ; results.hasNext() ; )
            {
                System.out.println("tengo cosas");
                QuerySolution soln = results.nextSolution() ;
                Resource x = soln.getResource("x");
                Literal date = soln.getLiteral("date");
                String uri = x.getURI();
                myWriter.write(nConsulta +": "+ uri + " " + date.getValue() + " " + "\n");
            }
        } finally { qexec.close() ; }
        myWriter.close();
    }

}
