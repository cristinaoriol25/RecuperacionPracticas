/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static java.util.Map.entry;

/** Simple command-line based search demo. */
public class SearchFiles {

  static boolean verbose = false;
  private SearchFiles() {
  }

  /**
   * Simple command-line based search demo.
   */
  public static void main(String[] args) throws Exception {
    String usage =
            "Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\nSee http://lucene.apache.org/core/4_1_0/demo/ for details.";
    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
      System.out.println(usage);
      System.exit(0);
    }

    String index = "index";
    String field = "contents";
    String infoNeedsFile = "information-needs"; // por ej
    String output = "output.txt"; // por ej
    String queries = null;
    int repeat = 0;
    boolean raw = false;
    String queryString = null;
    int hitsPerPage = 10;

    for (int i = 0; i < args.length; i++) {
      if ("-index".equals(args[i])) {
        index = args[i + 1];
        i++;
      } else if ("-v".equals(args[i])) {
        verbose = true;
        i++;
      } else if ("-field".equals(args[i])) {
        field = args[i + 1];
        i++;
      } else if ("-infoNeeds".equals(args[i])) {
        infoNeedsFile = args[i + 1];
        i++;
      } else if ("-output".equals(args[i])) {
        output = args[i + 1];
        i++;
      } else if ("-queries".equals(args[i])) {
        queries = args[i + 1];
        i++;
      } else if ("-query".equals(args[i])) {
        queryString = args[i + 1];
        i++;
      } else if ("-repeat".equals(args[i])) {
        repeat = Integer.parseInt(args[i + 1]);
        i++;
      } else if ("-raw".equals(args[i])) {
        raw = true;
      } else if ("-paging".equals(args[i])) {
        hitsPerPage = Integer.parseInt(args[i + 1]);
        if (hitsPerPage <= 0) {
          System.err.println("There must be at least 1 hit per page.");
          System.exit(1);
        }
        i++;
      }
    }

    File infoNeedsFileFile = new File(infoNeedsFile);
    List<Map<String, String>> infoNeeds = parseNeeds(infoNeedsFileFile);
    // Resetear fichero...
    FileWriter myWriter = new FileWriter(output);
    myWriter.close();
    // Modelos de tokenizers, NER de OpenNLP:
    try {
      InputStream inputStreamTokenizer;
      inputStreamTokenizer = new FileInputStream("en-token.bin"); // Cargar el "tokenizador" (ingles, no hemos encontrado es)
      TokenizerModel tokenModel = new TokenizerModel(inputStreamTokenizer);
      //Instantiating the TokenizerME class
      TokenizerME tokenizer = new TokenizerME(tokenModel);
      //      for (String token : tokensNeed) {
      //        System.out.println(token);
      //      }
      // Cargar el modelo de NameFinder:
      InputStream modelIn = new FileInputStream("en-ner-person.bin"); // español
      TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
      NameFinderME nameFinder = new NameFinderME(model);
      for (var need : infoNeeds) {
        System.out.println("------------");
        System.out.println(need.toString());
        List<String> docsNeed = buscarMatch(index, tokenizer, nameFinder, need.get("text"));
        outputDocs(output, need.get("identifier"), docsNeed); // saca los docs de un need
      }
    } catch (FileNotFoundException fileNotFoundException) {
      fileNotFoundException.printStackTrace();
    } catch (IOException ioException) {
      ioException.printStackTrace();
    }
  }

  private static void outputDocs(String outputFileS, String identifier, List<String> docsNeed) {
    try {
      FileWriter myWriter = new FileWriter(outputFileS, true); // true para append
      for (var doc : docsNeed) {
        myWriter.write(identifier + "\t" + doc + "\n");
      }
      myWriter.close();
    } catch (IOException e) {
      System.out.println("Error de escritura!");
      e.printStackTrace();
    }
  }


  // Devuelve la lista ordenada de documentos por sus puntuaciones
  private static List<String> buscarMatch(String index, TokenizerME tokenizer, NameFinderME nameFinder, String textNeed) throws IOException {

    IndexReader reader = null;
    try {
      reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
    } catch (IOException e) {
      e.printStackTrace();
    }
    IndexSearcher searcher = new IndexSearcher(reader);

    Analyzer analyzer = new Nuestroanalyzer();

    List<String> camposGenerales = Arrays.asList("title", "description", "subject");
    BooleanQuery.Builder builder = construirConsultaGeneral(camposGenerales, textNeed, analyzer, 1f);
    //BooleanQuery.Builder builder = new BooleanQuery.Builder();
    List<String> camposNombres = Arrays.asList("contributor", "creator");

    String[] tokensNeed = tokenizer.tokenize(textNeed);
    //builder = construirConsultaNombresPropios(builder, nameFinder, camposNombres, tokensNeed, analyzer);


    String[] otrosTokens = tokenizer.tokenize("Pepe María es José, Cristina y Mike");
     //construirConsultaNombresPropios(builder, nameFinder, camposNombres, otrosTokens, analyzer); // No funciona por el es-ner
    builder = construirConsultaTipo(builder, textNeed, analyzer, 0.7f);

    builder = construirConsultaLenguaje(builder, textNeed, 10.0f);
//
    builder = construirConsultaTemporal(builder, analyzer, camposGenerales, tokensNeed, 1f);
    builder = construirConsultaPublicado(builder, textNeed, analyzer, 5.0f);

    BooleanQuery booleanQuery = builder.build();

    System.out.println("Searching for: " + booleanQuery.toString());
    TopDocs resultados = null;
    try {
      resultados = searcher.search(booleanQuery, 100); // TODO: revisar para que sean todos
    } catch (IOException e) {
      e.printStackTrace();
    }
    ScoreDoc[] hits = resultados.scoreDocs;
    System.out.println("topScores... " );
    List<String> nombresHits = new ArrayList<String>();
    for (var hit : hits) {
      //System.out.println(hit.toString());
      String nombre = searcher.doc(hit.doc).get("name");
      nombresHits.add(nombre);
      try {
        if (verbose) {
          System.out.println(nombre);
          System.out.println(searcher.explain(booleanQuery, hit.doc));
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return nombresHits;

  }


  // Fuente: https://www.tutorialspoint.com/opennlp/opennlp_named_entity_recognition.htm
  private static BooleanQuery.Builder construirConsultaNombresPropios(BooleanQuery.Builder builder, NameFinderME nameFinder, List<String> camposNombres, String[] tokensNeed, Analyzer analyzer) {
    System.out.println("Buscando nombres en need: " + tokensNeed);
    Span nombresSpans[] = nameFinder.find(tokensNeed);
    for(Span s: nombresSpans) { // TODO: por que co&%$$%& encuentra Biologia como nombre y no Javier??
      //System.out.println(s.toString() + "  " + tokensNeed[s.getStart()]);
      String nombre = ""; // almacenara el nombre completo (+apellidos)
      for (int i = s.getStart(); i<s.getEnd(); i++) { // cada span puede tener varios tokens (ej: "Mariano Rajoy" es un solo span)
        System.out.println(s.toString() + "  " + tokensNeed[i]);
        nombre += tokensNeed[i] + ((i<s.getEnd()-1) ? " " : "");// token + espacio (si no es el ultimo nombre)
      }
      for (var campo: camposNombres) { // se busca en cada campo
        Query query = new TermQuery(new Term(campo, nombre));
        builder.add(query, BooleanClause.Occur.SHOULD); // TODO: MUST, supongo. Revisar cuando funcione
      }
    }
    nameFinder.clearAdaptiveData();

    return builder;
  }

  private static BooleanQuery.Builder construirConsultaLenguaje(BooleanQuery.Builder builder, String textNeed, float boost) {
    String needLower = textNeed.toLowerCase();
    if (needLower.contains("en inglés")) {
      //System.out.println("en inglessssssssssssssssssssssssssssssssss");
      Query query = new TermQuery(new Term("language", "eng"));
      BoostQuery b = new BoostQuery(query, boost); // peso
      builder.add(b, BooleanClause.Occur.SHOULD);
    }
    if (needLower.contains("en español")) {
      Query query = new TermQuery(new Term("language", "spa"));
      BoostQuery b = new BoostQuery(query, boost); // peso
      builder.add(b, BooleanClause.Occur.SHOULD);
    }
    return builder;
  }

  private static BooleanQuery.Builder construirConsultaGeneral(List<String> fields, String textNeed, Analyzer analyzer, float boost) {
    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    for (var field : fields) {
      QueryParser parser = new QueryParser(field, analyzer);
      // Query query = new TermQuery(new Term(field, "eto que e"));
      Query query = null;
      try {
        query = parser.parse(textNeed);
      } catch (ParseException e) {
        e.printStackTrace();
      }
      BoostQuery bq = new BoostQuery(query, boost);
      builder.add(bq, BooleanClause.Occur.SHOULD);
    }
    return builder;
  }
  private static BooleanQuery.Builder construirConsultaTipo(BooleanQuery.Builder builder, String textNeed, Analyzer analyzer, float boost) {
    if(textNeed.contains("Tesis") || textNeed.contains("tesis") ) {
      Query query = new TermQuery(new Term("type", "TESIS"));
      BoostQuery b = new BoostQuery(query, boost);
      builder.add(b, BooleanClause.Occur.SHOULD);
    }
    if (textNeed.contains("Trabajo de fin de grado") || textNeed.contains("trabajo de fin de grado") || textNeed.contains("Trabajos académicos") || textNeed.contains("trabajos académicos")) {
      Query query = new TermQuery(new Term("type", "TAZ-TFG"));
      BoostQuery b = new BoostQuery(query, boost);
      builder.add(b, BooleanClause.Occur.SHOULD);
    }
    if (textNeed.contains("Trabajo de fin de master") || textNeed.contains("trabajo de fin de master")|| textNeed.contains("Trabajos académicos") || textNeed.contains("trabajos académicos")) {
      Query query = new TermQuery(new Term("type", "TAZ-TFM"));
      BoostQuery b = new BoostQuery(query, boost);
      builder.add(b, BooleanClause.Occur.SHOULD);
    }
    if (textNeed.contains("Proyectos fin de carrera") || textNeed.contains("proyectos fin de carrera")|| textNeed.contains("Trabajos académicos") || textNeed.contains("trabajos académicos")) {
      Query query = new TermQuery(new Term("type", "TAZ-TFC"));
      BoostQuery b = new BoostQuery(query, boost);
      builder.add(b, BooleanClause.Occur.SHOULD);
    }
    return builder;
  }
  // Si es un siglo, lo devuelve como año (Siglo XX -> 1900).
  // TODO: Si no, devuelve -1, de momento asumimos que el siglo esta bien...
  private static int romanoToInt(String siglo) {
    Map<Character, Integer> valores = Map.ofEntries(
            entry('M', 1000),
            entry('D', 500),
            entry('C', 100),
            entry('L', 50),
            entry('X', 10),
            entry('V', 5),
            entry('I', 1)
    );//new HashMap<String, Integer>();
    int val = 0;
    int ultimoVal = 0;
    for (int i = siglo.length()-1; i>=0; i--) {// dcha a izq
      Character c = siglo.charAt(i);
      int valI = valores.get(c);
      if (valI >= ultimoVal) {// si es >= se suma
        val += valI;
      }
      else { // Si no, se resta (ej: IX)
        val -= valI;
      }
      ultimoVal=valI;
    }
    return val;
  }

  private static BooleanQuery.Builder construirConsultaPublicado(BooleanQuery.Builder builder, String need, Analyzer analyzer, float boost) {
    Pattern publicacion=Pattern.compile(".*publicados? en ([0-9]+).*");

    Pattern publicacion1=Pattern.compile(".*publicados? entre ([0-9]+) y ([0-9]+).*");

    Pattern publicacion2= Pattern.compile(".*los últimos ([0-9]+) años.*"); //".*los últimos ([0-9]+) años.*"

      need=need.toLowerCase();
      Matcher m = publicacion.matcher(need);
      Matcher m1 = publicacion1.matcher(need);
      Matcher m2= publicacion2.matcher(need);
      if (m1.matches()) {
        System.out.println("Yep: " + m1.group(1) + "..."+m1.group(2));
        BytesRef n1= new BytesRef(m1.group(1)); // 1er año
        BytesRef n2= new BytesRef(m1.group(2)); // 2o
        TermRangeQuery t=new TermRangeQuery("date", n1, n2, true, true);
        BoostQuery b = new BoostQuery(t, boost);
        builder.add(b, BooleanClause.Occur.SHOULD);
      }
      if (m.matches()) {
        Query query = new TermQuery(new Term("date", m.group(1)));
        BoostQuery b = new BoostQuery(query, boost);
        builder.add(b, BooleanClause.Occur.SHOULD);
      }
      if(m2.matches()){
        Calendar c = Calendar.getInstance();
        int annio=c.get(Calendar.YEAR)- Integer.valueOf (m2.group(1));
        BytesRef n1= new BytesRef(Integer.toString(annio)); // 1er año
        BytesRef n2= new BytesRef(Integer.toString(c.get(Calendar.YEAR))); // 2o
        TermRangeQuery t=new TermRangeQuery("date", n1, n2, true, true);
        BoostQuery b = new BoostQuery(t, boost);
        builder.add(b, BooleanClause.Occur.SHOULD);
      }
      return builder;
  }

  // Añade las queries correspondientes a los periodos o años que se quieren buscar
  private static BooleanQuery.Builder construirConsultaTemporal(BooleanQuery.Builder builder, Analyzer analyzer, List<String> fields, String[] tokensNeed, float boost) {

    Pattern pSiglo = Pattern.compile("[mdclxvi]+");
    for (String token : tokensNeed) {
      token = token.toLowerCase();
      Matcher m = pSiglo.matcher(token);
      // boolean b = m.matches();
      if (m.matches())
      {
        System.out.println("Siglo: " + token + " = " + romanoToInt(token.toUpperCase()));
        int siglo = romanoToInt(token.toUpperCase());
        for (var campo: fields) { // se busca en cada campo
// TODO: ????????????????????????????????????
//          QueryParser parser = new QueryParser(campo, analyzer);
//          Query q= null;
//          try {
//            q = parser.parse("año " + (siglo-1)+"??");
//          } catch (ParseException e) {
//            e.printStackTrace();
//          }
//          BoostQuery b = new BoostQuery(q, boost);
//          builder.add(b, BooleanClause.Occur.SHOULD);
          Query query = new TermQuery(new Term(campo, token)); // se busca el siglo tal cual
          var b = new BoostQuery(query, boost);
          builder.add(b, BooleanClause.Occur.SHOULD);
        }
      }
    }

    return builder;
  }


  /**
   * This demonstrates a typical paging search scenario, where the search engine presents
   * pages of size n to the user. The user can then go to the next page if interested in
   * the next hits.
   * <p>
   * When the query is executed for the first time, then only enough results are collected
   * to fill 5 result pages. If the user wants to page beyond this limit, then the query
   * is executed another time and all hits are collected.
   */
  public static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query,
                                    int hitsPerPage, boolean raw, boolean interactive) throws IOException {

    // Collect enough docs to show 5 pages
    TopDocs results = searcher.search(query, 5 * hitsPerPage);
    ScoreDoc[] hits = results.scoreDocs;

    int numTotalHits = Math.toIntExact(results.totalHits.value);
    System.out.println(numTotalHits + " total matching documents");

    int start = 0;
    int end = Math.min(numTotalHits, hitsPerPage);

    while (true) {
      if (end > hits.length) {
        System.out.println("Only results 1 - " + hits.length + " of " + numTotalHits + " total matching documents collected.");
        System.out.println("Collect more (y/n) ?");
        String line = in.readLine();
        if (line.length() == 0 || line.charAt(0) == 'n') {
          break;
        }

        hits = searcher.search(query, numTotalHits).scoreDocs;
      }

      end = Math.min(hits.length, start + hitsPerPage);

      for (int i = start; i < end; i++) {
        System.out.println(searcher.explain(query, hits[i].doc));
        if (raw) {                              // output raw format
          System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score);
          continue;
        }

        Document doc = searcher.doc(hits[i].doc);
        String path = doc.get("path");
        if (path != null) {
          System.out.println((i + 1) + ". " + path);
        } else {
          System.out.println((i + 1) + ". " + "No path for this document");
        }
        if (!raw) {
          Date currentDate = new Date(Long.parseLong(doc.get("modified")));
          System.out.println(currentDate);
        }
      }

      if (!interactive || end == 0) {
        break;
      }

      if (numTotalHits >= end) {
        boolean quit = false;
        while (true) {
          System.out.print("Press ");
          if (start - hitsPerPage >= 0) {
            System.out.print("(p)revious page, ");
          }
          if (start + hitsPerPage < numTotalHits) {
            System.out.print("(n)ext page, ");
          }
          System.out.println("(q)uit or enter number to jump to a page.");

          String line = in.readLine();
          if (line.length() == 0 || line.charAt(0) == 'q') {
            quit = true;
            break;
          }
          if (line.charAt(0) == 'p') {
            start = Math.max(0, start - hitsPerPage);
            break;
          } else if (line.charAt(0) == 'n') {
            if (start + hitsPerPage < numTotalHits) {
              start += hitsPerPage;
            }
            break;
          } else {
            int page = Integer.parseInt(line);
            if ((page - 1) * hitsPerPage < numTotalHits) {
              start = (page - 1) * hitsPerPage;
              break;
            } else {
              System.out.println("No such page");
            }
          }
        }
        if (quit) break;
        end = Math.min(numTotalHits, start + hitsPerPage);
      }

    }
  }

  public static List<Map<String, String>> parseNeeds(File file) throws ParserConfigurationException, IOException, SAXException {
    List<Map<String, String>> iNeedsList = new ArrayList<>();
    if (file.canRead() && !file.isDirectory()) {
      FileInputStream fis;
      try {
        fis = new FileInputStream(file);
      } catch (FileNotFoundException fnfe) {
        // at least on windows, some temporary files raise this exception with an "access denied" message
        // checking if the file can be read doesn't help
        return null;
      }
      DocumentBuilderFactory creadorDoc = DocumentBuilderFactory.newInstance();
      DocumentBuilder docB = creadorDoc.newDocumentBuilder();
      org.w3c.dom.Document documento = docB.parse(fis);
      //Obtener el elemento raíz del documento
      // Element raiz = (Element) ((org.w3c.dom.Document) documento).getDocumentElement();
      documento.getDocumentElement().normalize();
      NodeList nList = documento.getElementsByTagName("informationNeed");
      for (int temp = 0; temp < nList.getLength(); temp++) {
        Node nNode = nList.item(temp);

        iNeedsList.add(processNeed(nNode));
      }
    } else {
      System.out.println(file.toString() + " debe ser un archivo accesible");
      System.exit(1);
    }
    return iNeedsList;
  }


  private static Map<String, String> processNeed(Node nNode) {
    Map<String,String> conts = new HashMap<>();
    NodeList iNeedL = nNode.getChildNodes();
    for (int temp = 0; temp < iNeedL.getLength(); temp++) {
      Node nodo = iNeedL.item(temp);
      Node datoContenido = nodo.getFirstChild();
      if (nodo.getNodeType() == Node.ELEMENT_NODE) {
        //Element eElement = (Element) nNode;
        //System.out.println(temp + " Metiendo " + nodo.getNodeName() + " : " + datoContenido.getNodeValue());
        conts.put(nodo.getNodeName(),datoContenido.getNodeValue());
      }
    }
    return conts;
  }
}
