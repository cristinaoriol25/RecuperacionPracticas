package IR.Practica5;




import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;

/**
 * Ejemplo de lectura de un modelo RDF de un fichero de texto 
 * y como acceder con SPARQL a los elementos que contiene
 */
public class E_AccesoSPARQL {

	public static void main(String args[]) {
		codigoInicial();
		String str = "Berners-Lee";
		//System.out.println("Contienen " + str +":");
		//contienen(str);


		String uri = "http://www.w3.org/People/Berners-Lee/card#i";
		System.out.println("Titulos de docs creados por " + uri +":");
		creadosPor(uri);
	}

	private static void creadosPor(String uri) {
		// cargamos el fichero deseado
		Model model = FileManager.get().loadModel("card.rdf");
		//PREFIX foaf: <http://xmlns.com/foaf/0.1/maker>
		String queryString = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
				"SELECT ?x ?y ?z WHERE { " +
				"?x foaf:maker " + uri + ". }";
		System.out.println("Querystring: " +queryString);
		//ejecutamos la consulta y obtenemos los resultados
		Query query = QueryFactory.create(queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext(); ) {
				QuerySolution soln = results.nextSolution();
				Resource x = soln.getResource("x");
				if (x.isLiteral()) {
					System.out.println(x.getURI() + " - "
							+ x.getURI() + " - "
							+ x.toString());
				}else {
					System.out.println(x.getURI() + " - ");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void contienen(String str) {
		// cargamos el fichero deseado
		Model model = FileManager.get().loadModel("card.rdf");
		String queryString = "SELECT ?x ?y ?z  WHERE { ?x ?y ?z . " +
				" FILTER contains(str(?x), \"" + str + "\") }";
		String queryString2 = "SELECT ?x ?y ?z  WHERE { ?x ?y ?z . " +
				" FILTER ( contains(str(?x), \"" + str + "\")" +
				"|| contains(str(?y), \"" + str + "\")" +
				"|| contains(str(?z), \"" + str + "\") ) }";
		System.out.println("Querystring: " +queryString);
		//ejecutamos la consulta y obtenemos los resultados
		Query query = QueryFactory.create(queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext(); ) {
				QuerySolution soln = results.nextSolution();
				Resource x = soln.getResource("x");
				if (x.isLiteral()) {
					System.out.println(x.getURI() + " - "
							+ x.getURI() + " - "
							+ x.toString());
				}else {
					System.out.println(x.getURI() + " - ");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void codigoInicial() {
		// cargamos el fichero deseado
		Model model = FileManager.get().loadModel("card.rdf");

		//definimos la consulta (tipo query)
		String queryString = "Select ?x ?y ?z WHERE  {?x ?y ?z }" ;

		//ejecutamos la consulta y obtenemos los resultados
		Query query = QueryFactory.create(queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
		try {
			ResultSet results = qexec.execSelect() ;
			for ( ; results.hasNext() ; )
			{
				QuerySolution soln = results.nextSolution() ;
				Resource x = soln.getResource("x");
				Resource y = soln.getResource("y");
				RDFNode z = soln.get("z") ;
				if (z.isLiteral()) {
					System.out.println(x.getURI() + " - "
							+ y.getURI() + " - "
							+ z.toString());
				} else {
					System.out.println(x.getURI() + " - "
							+ y.getURI() + " - "
							+ z.asResource().getURI());
				}
			}
		} finally { qexec.close() ; }

		System.out.println("----------------------------------------");

		//definimos la consulta (tipo describe)
		queryString = "Describe <http://www.w3.org/People/Berners-Lee/card#i>" ;
		query = QueryFactory.create(queryString) ;
		qexec = QueryExecutionFactory.create(query, model) ;
		Model resultModel = qexec.execDescribe() ;
		qexec.close() ;
		resultModel.write(System.out);

		System.out.println("----------------------------------------");


		//definimos la consulta (tipo ask)
		queryString = "ask {<http://www.w3.org/People/Berners-Lee/card#i> ?x ?y}" ;
		query = QueryFactory.create(queryString) ;
		qexec = QueryExecutionFactory.create(query, model) ;
		System.out.println( qexec.execAsk()) ;
		qexec.close() ;

		System.out.println("----------------------------------------");

		//definimos la consulta (tipo cosntruct)
		queryString = "construct {?x <http://miuri/inverseSameAs> ?y} where {?y <http://www.w3.org/2002/07/owl#sameAs> ?x}" ;
		query = QueryFactory.create(queryString) ;
		qexec = QueryExecutionFactory.create(query, model) ;
		resultModel = qexec.execConstruct() ;
		qexec.close() ;
		resultModel.write(System.out);

	}

}
