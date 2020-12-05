package IR.Practica5;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VCARD;

/**
 * Ejemplo de como construir un modelo de Jena y añadir nuevos recursos 
 * mediante la clase Model
 */
public class A_CreacionRDF {
	
	/**
	 * muestra un modelo de jena de ejemplo por pantalla
	 */
	public static void main (String args[]) {
        Model model = A_CreacionRDF.generarEjemplo();
        // write the model in the standar output
        model.write(System.out); 
    }
	
	/**
	 * Genera un modelo de jena de ejemplo
	 */
	public static Model generarEjemplo(){
		// definiciones
        String root    = "http://somewhere/";
        String personURI    = "http://somewhere/JohnSmith";
        String givenName    = "John";
        String familyName   = "Smith";
        String fullName     = givenName + " " + familyName;

        // crea un modelo vacio
        Model model = ModelFactory.createDefaultModel();

        // le a�ade las propiedades
        Resource johnSmith  = model.createResource(personURI)
             .addProperty(VCARD.FN, fullName)
             .addProperty(VCARD.N, 
                      model.createResource()
                           .addProperty(VCARD.Given, givenName)
                           .addProperty(VCARD.Family, familyName));

        johnSmith.addProperty(RDF.type, FOAF.Person);

        // Personas "persona1" y "persona2" (ej ?)
        Resource []personas = new Resource[2];
        for (int i=0; i<2; i++) {
            String uri_i    = root + "persona"+ i;
            String givenName_i    = "persona"+ i;
            String familyName_i   = "persona"+ i;
            String fullName_i     = givenName + " " + familyName;

            personas[i] = model.createResource(uri_i)
                    .addProperty(VCARD.FN, fullName_i)
                    .addProperty(VCARD.N,
                            model.createResource()
                                    .addProperty(VCARD.Given, givenName_i)
                                    .addProperty(VCARD.Family, familyName_i));
        }
        personas[0].addProperty(FOAF.knows, personas[1]);

        // ej (?)
        model.add(personas[1], FOAF.knows, personas[0]);

        return model;
	}
	
	
}
