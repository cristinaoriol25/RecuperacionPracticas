package IR.Practica5;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
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
                String uri = x.getURI();
                myWriter.write(nConsulta +": "+ x);
            }
        } finally { qexec.close() ; }
        myWriter.close();
    }

}
