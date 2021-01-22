package IR.Practica5;

import org.apache.jena.query.*;
import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextDatasetFactory;
import org.apache.jena.query.text.TextIndexConfig;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

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
       // Model modelo= cargarModelo(rdfPath, "RDF/XML");

        EntityDefinition entDef = new EntityDefinition("uri", "name", ResourceFactory.createProperty("http://nuestraraiz/","title"));
        entDef.set("firstName", ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/","firstName").asNode());
        entDef.set("prefLabel", ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#","prefLabel").asNode());
        entDef.set("description", ResourceFactory.createProperty("http://nuestraraiz/","description").asNode());
        entDef.set("Documento", ResourceFactory.createProperty("http://nuestraraiz/","Documento").asNode());
        entDef.set("tema", ResourceFactory.createProperty("http://nuestraraiz/","Tema").asNode());
        entDef.set("idioma", ResourceFactory.createProperty("http://nuestraraiz/","Idioma").asNode());
        entDef.set("Contribuidor", ResourceFactory.createProperty("http://nuestraraiz/","Contribuidor").asNode());
        entDef.set("contribuido", ResourceFactory.createProperty("http://nuestraraiz/","contribuido").asNode());
        entDef.set("publisher", ResourceFactory.createProperty("http://nuestraraiz/","publisher").asNode());
        entDef.set("Creator", ResourceFactory.createProperty("http://nuestraraiz/","Creator").asNode());
        entDef.set("creado", ResourceFactory.createProperty("http://nuestraraiz/","creado").asNode());
        entDef.set("Departamento", ResourceFactory.createProperty("http://nuestraraiz/","Departamento").asNode());
        entDef.set("Nombre-departamento", ResourceFactory.createProperty("http://nuestraraiz/","Nombre-departamento").asNode());
        entDef.set("Subject", ResourceFactory.createProperty("http://nuestraraiz/","Subject").asNode());
        entDef.set("date", ResourceFactory.createProperty("http://nuestraraiz/","date").asNode());


        TextIndexConfig config = new TextIndexConfig(entDef);
        config.setAnalyzer(new SpanishAnalyzer());
        config.setQueryAnalyzer(new SpanishAnalyzer());
        config.setMultilingualSupport(true);

        //definimos el repositorio indexado todo en memoria
        Dataset ds1 = DatasetFactory.createGeneral() ;
        //Directory dir =  new MMapDirectory(Paths.get("/home/cris/Escritorio/Universidad/RecuperacionPracticas/Practica5/index"));
        Directory dir =  new RAMDirectory();
        Dataset ds = TextDatasetFactory.createLucene(ds1, dir, config) ;

        // cargamos el fichero deseado y lo almacenamos en el repositorio indexado
        RDFDataMgr.read(ds.getDefaultModel(), rdfPath) ;

        BufferedReader in = null;
        FileWriter myWriter = new FileWriter(outputPath);


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
            procesarConsulta(line, myWriter, ds);
        }
        myWriter.close();


    }


    private static Model cargarModelo(String skosPath, String rdfSyntax) {
        return FileManager.get().loadModel(skosPath,rdfSyntax);
    }

    private static void procesarConsulta(String consulta, FileWriter myWriter, Dataset model) throws IOException {
        String[] Consulta=consulta.split(" ", 2);
        String nConsulta=Consulta[0];
        String query=Consulta[1];
        System.out.println(query);
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        try {
            ResultSet results = qexec.execSelect() ;
            for ( ; results.hasNext() ; )
            {
                QuerySolution soln = results.nextSolution() ;
                Resource x = soln.getResource("x");
                String uri = x.getURI();
                    String[] parseuri=uri.split("/");
                    Literal t = soln.getLiteral("t");
                    Resource tema = soln.getResource("tema");
                    Literal score = soln.getLiteral("scoreTot");
                    //System.out.println(score);
                    myWriter.write(nConsulta + "   " + "oai_zaguan.unizar.es_"+parseuri[4]+".xml" + "\n");
            }
        } finally { qexec.close() ; }
    }

}
