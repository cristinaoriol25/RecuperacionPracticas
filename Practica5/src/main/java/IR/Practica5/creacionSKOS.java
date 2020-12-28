package IR.Practica5;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.vocabulary.VCARD;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Ejemplo de como construir un modelo de Jena y añadir nuevos recursos 
 * mediante la clase Model
 */
public class creacionSKOS {
	
	/**
	 * muestra un modelo de jena de ejemplo por pantalla
	 */
	public static void main (String args[]) throws FileNotFoundException {
        Model model = creacionSKOS.generarEjemplo();
        // write the model in the standar output
        //model.write(System.out);
        // Guardamos como turtle (ttl)
        String fichero = "tesauro-skos.ttl";
        model.write(new FileOutputStream(new File(fichero)),"TURTLE");//"N-TRIPLE");
    }
	
	/**
	 * Genera un modelo de jena de ejemplo
	 */
	public static Model generarEjemplo(){
        // crea un modelo vacio
        Model model = ModelFactory.createDefaultModel();

        // Tesauro skos:
        String raiz = "http://nuestraraiz/";
        // Conceptos mas generales:
        Resource ciencia = model.createResource(raiz+"ciencia")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "ciencia","es")
                //.addProperty(SKOS.prefLabel, "ciencia", "es")
                .addProperty(SKOS.prefLabel, "science", "en")
                .addProperty(SKOS.altLabel, "100cia", "en");
        Resource ingenieria = model.createResource(raiz+"ingenieria")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "ingenieria", "es")
                .addProperty(SKOS.prefLabel, "engineering", "en");
        // politica y demas
        Resource politica = model.createResource(raiz+"politica")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "politica", "es")
                .addProperty(SKOS.prefLabel, "politics", "en");
        Resource economia = model.createResource(raiz+"economia")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "economia", "es")
                .addProperty(SKOS.prefLabel, "economy", "en");
        Resource autoritarismo = model.createResource(raiz+"autoritarismo")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "autoritarismo", "es")
                .addProperty(SKOS.prefLabel, "authoritarianism", "en")
                .addProperty(SKOS.broader, politica);
        Resource represion = model.createResource(raiz+"represion")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "represion", "es")
                .addProperty(SKOS.prefLabel, "repression", "en");
        Resource represionPolitica = model.createResource(raiz+"represion-politica")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "represion politica", "es")
                .addProperty(SKOS.prefLabel, "political repression", "en")
                .addProperty(SKOS.broader, politica)
                .addProperty(SKOS.broader, represion);
        Resource caciquismo = model.createResource(raiz+"caciquismo")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "caciquismo", "es")
                .addProperty(SKOS.broader, autoritarismo);
        Resource dictadura = model.createResource(raiz+"dictadura")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "dictadura", "es")
                .addProperty(SKOS.prefLabel, "dictatorship", "en")
                .addProperty(SKOS.broader, autoritarismo);
        // Partidos:
        Resource partidosPoliticos = model.createResource(raiz+"partidos-politicos")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "partidos politicos", "es")
                .addProperty(SKOS.prefLabel, "political parties", "en")
                .addProperty(SKOS.broader, politica);
        Resource gobierno = model.createResource(raiz+"gobierno")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "gobierno", "es")
                .addProperty(SKOS.prefLabel, "government", "en")
                .addProperty(SKOS.broader, politica);

        Resource social = model.createResource(raiz+"social")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "social", "es")
                .addProperty(SKOS.prefLabel, "social", "en");
        Resource relevanciaSocial = model.createResource(raiz+"relevancia-social")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "relevancia social", "es")
                .addProperty(SKOS.prefLabel, "social relevance", "en")
                .addProperty(SKOS.broader, social);


        // Mas Economia:
        Resource evolucion = model.createResource(raiz+"evolucion")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "evolucion", "es")
                .addProperty(SKOS.prefLabel, "evolution", "en");

        Resource evolucionEconomica = model.createResource(raiz+"evolucion-economica")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "evolucion economica", "es")
                .addProperty(SKOS.prefLabel, "economic evolution", "en")
                .addProperty(SKOS.broader, evolucion)
                .addProperty(SKOS.broader, economia);
        Resource crisis = model.createResource(raiz+"crisis")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "crisis", "es")
                .addProperty(SKOS.prefLabel, "crisis", "en");
        Resource crisisEconomica = model.createResource(raiz+"crisis-economica")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "crisis economica", "es")
                .addProperty(SKOS.prefLabel, "economic crisis", "en")
                .addProperty(SKOS.broader, crisis)
                .addProperty(SKOS.broader, economia);
        // Cuestionables:
        Resource espana = model.createResource(raiz+"espana")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "españa", "es")
                .addProperty(SKOS.prefLabel, "spain", "en");
        Resource huesca = model.createResource(raiz+"huesca")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "huesca", "es")
                .addProperty(SKOS.prefLabel, "uesca@fabla")
                .addProperty(SKOS.broader, espana);

        // 100cias:
        Resource biologia = model.createResource(raiz+"biologia")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "biologia", "es")
                .addProperty(SKOS.prefLabel, "biology", "en")
                .addProperty(SKOS.broader, ciencia);

        Resource genetica = model.createResource(raiz+"genetica")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "genetica", "es")
                .addProperty(SKOS.prefLabel, "genetics", "en")
                .addProperty(SKOS.broader, biologia);

        Resource filogenetica = model.createResource(raiz+"filogenetica")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "filogenetica", "es")
                .addProperty(SKOS.prefLabel, "phylogenetics", "en")
                .addProperty(SKOS.broader, genetica);
        Resource medicina = model.createResource(raiz+"medicina")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "medicina", "es")
                .addProperty(SKOS.prefLabel, "medicine", "en")
                .addProperty(SKOS.broader, biologia);
        Resource informatica = model.createResource(raiz+"informatica")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "informatica", "es")
                .addProperty(SKOS.prefLabel, "computer science", "en")
                .addProperty(SKOS.broader, ciencia);
        // biocosas:
        Resource bioinformatica = model.createResource(raiz+"bioinformatica")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "bioinformatica", "es")
                .addProperty(SKOS.prefLabel, "bioinformatics", "en")
                .addProperty(SKOS.altLabel, "computational biology", "en")
                .addProperty(SKOS.altLabel, "biologia computacional", "es")
                .addProperty(SKOS.broader, biologia)
                .addProperty(SKOS.broader, informatica);

        Resource biomedicina = model.createResource(raiz+"biomedicina")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "biomedicina", "es")
                .addProperty(SKOS.prefLabel, "biomedicine", "en")
                .addProperty(SKOS.broader, biologia)
                .addProperty(SKOS.broader, medicina);

        Resource ingenieriaBiomedica = model.createResource(raiz+"ingenieria-biomedica")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "ingenieria biomedica", "es")
                .addProperty(SKOS.prefLabel, "biomedical engineering", "en")
                .addProperty(SKOS.broader, biomedicina)
                .addProperty(SKOS.broader, ingenieria);

        // Medicina...
        Resource diagnostico = model.createResource(raiz+"diagnostico")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "diagnostico", "es")
                .addProperty(SKOS.prefLabel, "diagnosis", "en")
                .addProperty(SKOS.broader, medicina);

        Resource enfermedad = model.createResource(raiz+"enfermedad")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "enfermedad", "es")
                .addProperty(SKOS.prefLabel, "disease", "en")
                .addProperty(SKOS.broader, medicina);


        Resource neurologia = model.createResource(raiz+"neurologia")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "neurologia", "es")
                .addProperty(SKOS.prefLabel, "neurology", "en")
                .addProperty(SKOS.broader, medicina);

        Resource enfermedadNeurodegenerativa = model.createResource(raiz+"enfermedad-neurodegenerativa")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "enfermedad neurodegenerativa", "es")
                .addProperty(SKOS.prefLabel, "neurodegenerative disease", "en")
                .addProperty(SKOS.broader, enfermedad)
                .addProperty(SKOS.broader, neurologia);

        Resource alzheimer = model.createResource(raiz+"alzheimer")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "alzheimer")
                .addProperty(SKOS.broader, enfermedadNeurodegenerativa);

        Resource parkinson = model.createResource(raiz+"parkinson")
                .addProperty(RDF.type, SKOS.Concept)
                .addProperty(SKOS.prefLabel, "parkinson")
                .addProperty(SKOS.broader, enfermedadNeurodegenerativa);

        return model;
	}
	
	
}
