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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/** Simple command-line based search demo. */
public class SearchFiles {

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


    File outputFile = new File(output);
    for (var need : infoNeeds) {
      //System.out.println("------------");
      //System.out.println(need.toString());
      TopDocs docsNeed = buscarMatch(index, need.get("text"));
      outputDocs(outputFile, need.get("identifier"), docsNeed); // saca los docs de un need
      // TODO: enseñar lo que sacamos o algo no? xdd

    }
    System.exit(0); // TODO: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
    IndexSearcher searcher = new IndexSearcher(reader);
    searcher.setSimilarity(new ClassicSimilarity());

    Analyzer analyzer = new SpanishAnalyzer2();

    BufferedReader in = null;
    if (queries != null) {
      in = new BufferedReader(new InputStreamReader(new FileInputStream(queries), "UTF-8"));
    } else {
      in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
    }
    QueryParser parser = new QueryParser(field, analyzer);
    while (true) {
      if (queries == null && queryString == null) {                        // prompt the user
        System.out.println("Enter query: ");
      }

      String line = queryString != null ? queryString : in.readLine();

      if (line == null || line.length() == -1) {
        break;
      }

      line = line.trim();
      if (line.length() == 0) {
        break;
      }

      Query query = parser.parse(line);
      System.out.println("Searching for: " + query.toString(field));

      if (repeat > 0) {                           // repeat & time as benchmark
        Date start = new Date();
        for (int i = 0; i < repeat; i++) {
          searcher.search(query, 100);
        }
        Date end = new Date();
        System.out.println("Time: " + (end.getTime() - start.getTime()) + "ms");
      }

      doPagingSearch(in, searcher, query, hitsPerPage, raw, queries == null && queryString == null);

      if (queryString != null) {
        break;
      }
    }
    reader.close();
  }

  private static void outputDocs(File outputFile, String identifier, TopDocs docsNeed) {
    // TODO
  }

  // Devuelve la lista ordenada de documentos por sus puntuaciones
  private static TopDocs buscarMatch(String index, String textNeed) {

    IndexReader reader = null;
    try {
      reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
    } catch (IOException e) {
      e.printStackTrace();
    }
    IndexSearcher searcher = new IndexSearcher(reader);

    Analyzer analyzer = new SpanishAnalyzer2();
    String field = "title"; // ????????
    QueryParser parser = new QueryParser(field, analyzer);
    Query query = null;
    try {
      query = parser.parse(textNeed);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    System.out.println("Searching for: " + query.toString(field));
    TopDocs resultados = null;
    try {
      resultados = searcher.search(query, 100); // TODO: revisar para que sean todos
    } catch (IOException e) {
      e.printStackTrace();
    }
    return resultados;

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
    List<Map<String, String>> iNeedsList = new ArrayList<Map<String, String>>();
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
    Map<String,String> conts = new HashMap<String,String>();
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
