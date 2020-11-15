package evaluation;/*
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

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.*;
import java.util.*;

/** Simple command-line based search demo. */
public class Evaluation {

  private static List<List<Integer>> resultados;
  private static List<Map<Integer, Boolean>> relevancias;
  private Evaluation() {}

  /** Simple command-line based search demo. */
  public static void main(String[] args) throws Exception {
    String usage = "Usage:\tjava Evaluation -qrels <qrelsFileName> -results <resultsFileName> -output <outputFileName>";
    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
      System.out.println(usage);
      System.exit(0);
    }

    String qrelsFileN = "qrels.txt";
    String resultsFileN = "results.txt";
    String outputFileN = "output.txt";
    
    for(int i = 0;i < args.length;i++) {
      if ("-qrels".equals(args[i])) {
        qrelsFileN = args[i+1];
        i++;
      } else if ("-results".equals(args[i])) {
        resultsFileN = args[i+1];
        i++;
      } else if ("-output".equals(args[i])) {
        outputFileN = args[i+1];
        i++;
      }
    }
    relevancias = parseQRels(qrelsFileN);
    /*for (var eval : relevancias) {
      System.out.println(eval);
    }*/
    resultados = parseResults(resultsFileN);
    /*for (var res : resultados) {
      System.out.println(res);
    }*/
    List<EvaluacionNeed> evaluaciones = evaluar(relevancias, resultados);
    escribirEvaluaciones(outputFileN, evaluaciones);
  }

  private static void escribirEvaluaciones(String outputFileN, List<EvaluacionNeed> evaluaciones) {
    // TODO: escribir al fichero como en el enunciado
    try {
      FileWriter myWriter = new FileWriter(outputFileN);
      int iNeed = 1; // cuenta de infoneed
      for (EvaluacionNeed evaluacion : evaluaciones) {
        escribirEvaluacionNeed(myWriter, evaluacion, iNeed++);
      }
      myWriter.close();
    } catch (IOException e) {
      System.out.println("Error de escritura!");
      e.printStackTrace();
    }

  }

  private static void escribirEvaluacionNeed(FileWriter myWriter, EvaluacionNeed evaluacion, int iNeed) throws IOException {
    myWriter.write("INORMATION NEED " + iNeed + "\n");
    myWriter.write(evaluacion.toString() + "\n\n");
  }

  // TODO:
  /*
     * precisionatK - C
     * avgPrecision - C
     * promedios de otros para total (prec, rec, f1, map, at10....) - C
     * ptos prec_recall - N
     * ptos interpolados - N
     * interpolacion total - N
                                            * escribir a fichero - N
   */

  private static List<EvaluacionNeed> evaluar(List<Map<Integer, Boolean>> relevancias, List<List<Integer>> resultados) {
    List<EvaluacionNeed> evaluaciones = new ArrayList<>();
    for (int need = 0; need<relevancias.size();need++) {
      List<Integer> recuperadosNeed = resultados.get(need);
      Map<Integer, Boolean> relevanciasNeed = relevancias.get(need);
      double precision = precision(recuperadosNeed, relevanciasNeed);
      double recall = recall(recuperadosNeed, relevanciasNeed);
      double f1score = 2f * precision * recall / (precision + recall);
      List<Double[]> prec_recall = puntosPR(precision, recall, f1score);
      double precisionAt10 = precisionAtK(recuperadosNeed, relevanciasNeed, 10);
      double avgPrecision = avgPrecision(recuperadosNeed, relevanciasNeed);
      System.out.println("Need " + (need+1) + "\nprecision: " + precision + "\nrecall: " + recall + "\nF1: " + f1score);
      evaluaciones.add(new EvaluacionNeed(precision, recall, f1score, f1score,f1score, prec_recall)); // TODO: parametros bien
      // TODO: sacar el resto de medidas
    }
    EvaluacionNeed total = new EvaluacionNeed(evaluaciones);
    // evaluaciones.add(total); // TODO: descomentar
    return evaluaciones;
  }

  private static List<Double[]> puntosPR(double precision, double recall, double f1score) {
    List<Double[]> ptos = new ArrayList<>();
    return ptos;
  }

  private static double avgPrecision(List<Integer> recuperadosNeed, Map<Integer, Boolean> relevanciasNeed) {
    // TODO:
    return 0;
  }

  private static double precisionAtK(List<Integer> recuperadosNeed, Map<Integer, Boolean> relevanciasNeed, int i) {
    // TODO
    return 0;
  }

  private static double precision(List<Integer> recuperadosNeed, Map<Integer, Boolean> relevanciasNeed) {
    int precision = 0;
    for (int documento : recuperadosNeed) { // para cada doc recuperado
      if (relevanciasNeed.get(documento)) { // si es relevante
        precision++; // se cuenta
      }
    }
    return Double.valueOf(precision)/Double.valueOf(recuperadosNeed.size()); // se devuelve entre 0 y 1
  }

  private static double recall(List<Integer> recuperadosNeed, Map<Integer, Boolean> relevanciasNeed) {
    int recall = 0;
    int nRelevantes = 0;
    for (Map.Entry<Integer, Boolean> entry : relevanciasNeed.entrySet()) {
      boolean relevante = entry.getValue();
      if (relevante) { // es relevante
        nRelevantes++; // lo contamos
        int docId = entry.getKey();
        if (recuperadosNeed.contains(docId)) { // Relevante y recuperado
          recall++;
        }
      }
    }
    return Double.valueOf(recall)/Double.valueOf(nRelevantes); // porcentaje de relevantes recuperados
  }

  private static List<List<Integer>> parseResults(String resultsFileN) {
    List<List<Integer>> resultados= new ArrayList<>();
    File resultsFile = new File(resultsFileN);
    Scanner scan = null;
    try {
      scan = new Scanner(resultsFile);
      while(scan.hasNext()){
        String linea = scan.nextLine();
        String[] tokens = linea.split("\t");
        if (tokens.length<2) {
          System.out.println("Linea con menos de 2 elementos en fichero " + resultsFileN);
          System.exit(1);
        }
        /*for (var s : tokens) {
          System.out.println(s);
          System.out.println("------------------");
        }*/
        int need = Integer.parseInt(tokens[0].trim());
        if (resultados.size()<need){
          resultados.add(new ArrayList<Integer>());
        }
        int docId = Integer.parseInt(tokens[1].trim());
        resultados.get(need-1).add(docId);
        //documentos.add(new Documento(Integer.parseInt(tokens[0].trim()), Integer.parseInt(tokens[1].trim())));
      }
      scan.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return resultados;
  }


  private static List<Map<Integer, Boolean>> parseQRels(String qrelsFileN) {
    List<Map<Integer, Boolean>> relevancias = new ArrayList<Map<Integer,Boolean>>();//[{},,,,]
    File qrelsFile = new File(qrelsFileN);
    Scanner scan = null;
    try {
      scan = new Scanner(qrelsFile);
      while(scan.hasNext()){
        String linea = scan.nextLine();
        String[] tokens = linea.split("\t");
        if (tokens.length<3) {
          System.out.println("Linea con menos de 3 elementos en fichero qrels");
          System.exit(1);
        }
        int need = Integer.parseInt(tokens[0].trim());
        if (relevancias.size()<need){
          relevancias.add(new HashMap<Integer, Boolean>());
        }
        int docId = Integer.parseInt(tokens[1].trim());
        boolean relevante = tokens[2].trim().equals("1");
        relevancias.get(need-1).put(docId, relevante);
        /*for (var s : tokens) {
          System.out.println(s);
          System.out.println("------------------");
        }*/
        //documentos.add(new Documento(Integer.parseInt(tokens[0].trim()), Integer.parseInt(tokens[1].trim()), tokens[2].trim().equals("1")));
      }
      scan.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return relevancias;
  }

  /**
   * This demonstrates a typical paging search scenario, where the search engine presents 
   * pages of size n to the user. The user can then go to the next page if interested in
   * the next hits.
   * 
   * When the query is executed for the first time, then only enough results are collected
   * to fill 5 result pages. If the user wants to page beyond this limit, then the query
   * is executed another time and all hits are collected.
   * 
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
        System.out.println("Only results 1 - " + hits.length +" of " + numTotalHits + " total matching documents collected.");
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
          System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
          continue;
        }

        Document doc = searcher.doc(hits[i].doc);
        String path = doc.get("path");
        if (path != null) {
          System.out.println((i+1) + ". " + path);
        } else {
          System.out.println((i+1) + ". " + "No path for this document");
        }
        if (!raw){
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
          if (line.length() == 0 || line.charAt(0)=='q') {
            quit = true;
            break;
          }
          if (line.charAt(0) == 'p') {
            start = Math.max(0, start - hitsPerPage);
            break;
          } else if (line.charAt(0) == 'n') {
            if (start + hitsPerPage < numTotalHits) {
              start+=hitsPerPage;
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
}