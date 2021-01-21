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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/** Simple command-line based search demo. */
public class Evaluation {
  private static int MAX_RES_NEED = 45; // num de resultados de cada need tenidos en cuenta

  //private static List<List<Integer>> resultados;
  //private static List<Map<String, Integer>> resultados;
  private static Map<String, List<String>> resultados; // para cada need (str), lista de docs (ints)
  //private static List<Map<Integer, Boolean>> relevancias;
  private static Map<String, Map<String, Boolean>> relevancias;
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
    /*for (var eval : relevancias.keySet()) {
      System.out.println(relevancias.get(eval));
    }*/
    resultados = parseResults(resultsFileN);
    /*for (var res : resultados.keySet()) {
      System.out.println(resultados.get(res));
    }*/
    Map<String, EvaluacionNeed> evaluaciones = evaluar(relevancias, resultados);
    escribirEvaluaciones(outputFileN, evaluaciones);
  }

  private static void escribirEvaluaciones(String outputFileN, Map<String, EvaluacionNeed> evaluaciones) {
    try {
      FileWriter myWriter = new FileWriter(outputFileN);
      //int iNeed = 1; // cuenta de infoneed
      for (Map.Entry<String, EvaluacionNeed> entry : evaluaciones.entrySet()) {
        String need = entry.getKey();
        escribirEvaluacionNeed(myWriter, entry.getValue(), need);
      }
      myWriter.close();
    } catch (IOException e) {
      System.out.println("Error de escritura!");
      e.printStackTrace();
    }

  }

  private static void escribirEvaluacionNeed(FileWriter myWriter, EvaluacionNeed evaluacion, String iNeed) throws IOException {
    if (evaluacion.isTotal()) {
      myWriter.write("TOTAL:\n");
    }
    else {
      myWriter.write("INORMATION NEED " + iNeed + "\n");
    }
    myWriter.write(evaluacion.toString() + "\n\n");
  }

  // TODO:
  /*                                                  HECHO
     * precisionatK - C
     * avgPrecision - C
     * promedios de otros para total (prec, rec, f1, map, at10....) - C
                                                   * ptos prec_recall - N
                                                   * ptos interpolados - N
     * interpolacion total - N
                                                    * escribir a fichero - N
   */

  private static Map<String, EvaluacionNeed> evaluar(Map<String, Map<String, Boolean>> relevancias, Map<String, List<String>> resultados) {
    Map<String, EvaluacionNeed> evaluaciones = new HashMap<>();
    //((for (int need = 0; need<relevancias.size(); need++) {
    for (Map.Entry<String, Map<String, Boolean>> entry : relevancias.entrySet()) {
      String need = entry.getKey();
      Map<String, Boolean> relevanciasNeed = entry.getValue();

      List<String> recuperadosNeed = resultados.get(need);

      List<Double[]> prec_recall = puntosPR (relevanciasNeed, recuperadosNeed);
      // La prec y recall son los del ultimo punto:
      double precision = prec_recall.get(prec_recall.size()-1)[1];//precision(recuperadosNeed, relevanciasNeed);
      double recall = prec_recall.get(prec_recall.size()-1)[0];//recall(recuperadosNeed, relevanciasNeed);
      double f1score = 2f * precision * recall / (precision + recall);

              //puntosPR(precision, recall, f1score);
      double precisionAt10 = precisionAtK(recuperadosNeed, relevanciasNeed, 10);
      double avgPrecision = avgPrecision(recuperadosNeed, relevanciasNeed);
      //System.out.println("Need " + (need+1) + "\nprecision: " + precision + "\nrecall: " + recall + "\nF1: " + f1score);
      evaluaciones.put(need, new EvaluacionNeed(precision, recall, f1score, precisionAt10,avgPrecision, prec_recall)); // TODO: parametros bien

    }
    EvaluacionNeed total = new EvaluacionNeed(evaluaciones);
    evaluaciones.put("TOTAL",total);
    return evaluaciones;
  }

  private static List<Double[]> puntosPR(Map<String, Boolean> relevanciasNeed, List<String> recuperadosNeed) {
    List<Double[]> ptos = new ArrayList<>();
    int recall = 0;
    int nRelevantes = 0;
    //System.out.println(recuperadosNeed);
    for (Map.Entry<String, Boolean> entry : relevanciasNeed.entrySet()) {
      //System.out.println(entry.getKey() +" "+ entry.getValue());
      //System.out.println("---" + recall + " " + nRelevantes);
      boolean relevante = entry.getValue();
      //System.out.println(relevante);
      if (relevante) { // es relevante
        nRelevantes++; // lo contamos
        String docId = entry.getKey();
        //System.out.println(docId);
        if (recuperadosNeed.contains(docId)) { // Relevante y recuperado
          recall++;
          //System.out.println(recall + " " + nRelevantes);
          ptos.add(new Double[]{Double.valueOf(recall), 0.0});
        }
      }
    }
    List <Double> precs = precisiones(recuperadosNeed, relevanciasNeed);
    int i=0;
    for (var pto : ptos) {
      pto[0] = pto[0]/nRelevantes;
      pto[1] = precs.get(i++);
    }

    return ptos; // porcentaje de relevantes recuperados
  }



  private static double precParaRecallYF1(double r, double f1) {
    return -(r*f1)/(f1-2*r);
  }

  private static double avgPrecision(List<String> recuperadosNeed, Map<String, Boolean> relevanciasNeed) {
    int i=0;
    double p=0;
    int relevantes=0;
    while(i<recuperadosNeed.size()){
      String documento=recuperadosNeed.get(i);
      if (relevanciasNeed.containsKey(documento) && relevanciasNeed.get(documento)) { // si es relevante
        p+=precisionAtK(recuperadosNeed, relevanciasNeed, i+1);
        relevantes++;
      }
      i++;
    }
    return p/(relevantes);
  }

  private static double precisionAtK(List<String> recuperadosNeed, Map<String, Boolean> relevanciasNeed, int k) {
    double p=0;
    int i=0;
    while(i<k){
      String documento=recuperadosNeed.get(i);
      if (relevanciasNeed.containsKey(documento) && relevanciasNeed.get(documento)) { // si es relevante
        p++; // se cuenta
      }
      i++;
    }
    return (p/k);
  }

  private static List<Double> precisiones(List<String> recuperadosNeed, Map<String, Boolean> relevanciasNeed) {
    List<Double> precisiones = new ArrayList<>();
    int precision = 0;
    int total = 0;
    for (String documento : recuperadosNeed) { // para cada doc recuperado
      total++;
      if (relevanciasNeed.containsKey(documento) && relevanciasNeed.get(documento)) { // si es relevante
        precision++; // se cuenta
        precisiones.add(Double.valueOf(precision)/Double.valueOf(total));
      }
    }

    return precisiones;//Double.valueOf(precision)/Double.valueOf(recuperadosNeed.size()); // se devuelve entre 0 y 1
  }

  private static double recall(List<Integer> recuperadosNeed, Map<String, Boolean> relevanciasNeed) {
    int recall = 0;
    int nRelevantes = 0;
    for (Map.Entry<String, Boolean> entry : relevanciasNeed.entrySet()) {
      boolean relevante = entry.getValue();
      if (relevante) { // es relevante
        nRelevantes++; // lo contamos
        String docId = entry.getKey();
        if (recuperadosNeed.contains(docId)) { // Relevante y recuperado
          recall++;
        }
      }
    }
    return Double.valueOf(recall)/Double.valueOf(nRelevantes); // porcentaje de relevantes recuperados
  }

  private static Map<String, List<String>> parseResults(String resultsFileN) {
    Map<String, List<String>> resultados = new HashMap<>();
    File resultsFile = new File(resultsFileN);
    Scanner scan = null;
    try {
      scan = new Scanner(resultsFile);
      while(scan.hasNext()){
        String linea = scan.nextLine();
        String[] tokens = linea.split("[\t ]+");
        if (tokens.length<2) {
          System.out.println("Linea con menos de 2 elementos en fichero " + resultsFileN+" los tokens de mierda son: "+tokens[0]);
          System.exit(1);
        }
        /*for (var s : tokens) {
          System.out.println(s);
          System.out.println("------------------");
        }*/
        String need = (tokens[0].trim());
        // {105: [1 2 3 8], }
        if (!resultados.containsKey(need)) {
          resultados.put(need, new ArrayList<String>());
        }
        String docId = (tokens[1]);
        List<String> resNeed = resultados.get(need);
        System.out.println(tokens[0]+" id:  "+docId);
        if (resNeed.size() < MAX_RES_NEED) { // Solo MAX_RES_NEED por nee
          resNeed.add(docId);
        }
        //documentos.add(new Documento(Integer.parseInt(tokens[0].trim()), Integer.parseInt(tokens[1].trim())));
      }
      scan.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return resultados;
  }


  private static Map<String, Map<String, Boolean>> parseQRels(String qrelsFileN) {
    Map<String, Map<String, Boolean>> relevancias = new HashMap<>();//[{},,,,]
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
        String need = (tokens[0].trim());
        if (!relevancias.containsKey(need)){
          relevancias.put(need, new HashMap<String, Boolean>());
        }
        String docId = (tokens[1].trim());
        boolean relevante = tokens[2].trim().equals("1");
        relevancias.get(need).put(docId, relevante);
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


}