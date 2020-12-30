package IR.Practica5;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Ejemplo de como construir un modelo de Jena y añadir nuevos recursos 
 * mediante la clase Model
 */
public class A_CreacionRDF {
	
	/**
	 * muestra un modelo de jena de ejemplo por pantalla
	 */
	public static void main (String args[]) throws FileNotFoundException {
        Model model = A_CreacionRDF.generarmodelo();
        // write the model in the standar output
        model.write(new FileOutputStream(new File("rdfs.xml")),"RDF/XML");
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

	public static Model generarmodelo(){
        String root = "http://nuestraraiz/";
        Model model=ModelFactory.createDefaultModel();

        //Añadimos nuestras clases:
        //Tipos de documentos
        Resource documento=model.createResource(root+"Documento");
        Resource tesis=model.createResource(root+"Tesis")
                .addProperty(RDFS.subClassOf, documento);
        Resource tfg=model.createResource(root+"TFG")
                .addProperty(RDFS.subClassOf, documento);
        Resource tfm=model.createResource(root+"TFM")
                .addProperty(RDFS.subClassOf, documento);
        Resource tfc=model.createResource(root+"TFC")
                .addProperty(RDFS.subClassOf, documento);

        //Personas:
        Resource contributor=model.createResource(root+"Contributor")
                .addProperty(RDFS.subClassOf, FOAF.Person);

        Resource creator=model.createResource(root+"Creator")
                .addProperty(RDFS.subClassOf, FOAF.Person);

        //Departamento
        Resource departamento=model.createResource(root+"Departamento");

        //Idioma
        Resource idioma=model.createResource(root+"Idioma");

        //Añadimos nuestras propiedades:

        Resource contribuido= model.createProperty(root+"contribuido") //No estoy segura si está bien dejarlo como resource sin indicar tipo propiedad y tal pero es que no se ocmo
                .addProperty(RDFS.range, contributor)
                .addProperty(RDFS.domain, documento);

        Resource creado= model.createProperty(root+"creado")
                .addProperty(RDFS.range, creator)
                .addProperty(RDFS.domain, documento);

        Resource publisher= model.createProperty(root+"publisher")
                .addProperty(RDFS.range, departamento)
                .addProperty(RDFS.domain, documento);

        Resource idiomaDoc=model.createProperty(root+"Idioma-documento")
                .addProperty(RDFS.range, idioma)
                .addProperty(RDFS.domain, documento);

        Resource tema=model.createProperty(root+"Tema")
                .addProperty(RDFS.range, SKOS.Concept)
                .addProperty(RDFS.domain, documento);

        Resource date=model.createProperty(root+"Date")
                .addProperty(RDFS.range, XSD.gYear)
                .addProperty(RDFS.domain, documento);
        Resource title=model.createProperty(root+"Title")
                .addProperty(RDFS.range, RDFS.Literal)
                .addProperty(RDFS.domain, documento);
        Resource description=model.createProperty(root+"Date")
                .addProperty(RDFS.range, RDFS.Literal)
                .addProperty(RDFS.domain, documento);
        Resource subject=model.createProperty(root+"Subject")
                .addProperty(RDFS.range, RDFS.Literal)
                .addProperty(RDFS.domain, documento);
        Resource nomDepartamento=model.createProperty(root+"Nombre-departamento")
                .addProperty(RDFS.range, RDFS.Literal)
                .addProperty(RDFS.domain, departamento);


        return model;
    }
	
}
