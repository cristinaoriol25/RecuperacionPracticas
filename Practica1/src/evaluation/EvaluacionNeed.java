package evaluation;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EvaluacionNeed {
    private boolean esTotal;
    private double precision;
    private double recall;
    private double f1score;
    private double precAt10;
    private double avgPrecision; // en la final esto será el MAP (porque viene a ser lo puto mismo)
    private List<Double[]> ptosPR;//TODO: ver como hacer los puntos de precision-recall
    private List<Double[]> ptosPRInterpolados;
    private double map;
    public EvaluacionNeed(double precision, double recall, double f1score, double precAt10, double avgPrecision, List<Double[]> ptosPR) {
        this.precision = precision;
        this.recall = recall;
        this.f1score = f1score;
        this.precAt10 = precAt10;
        this.avgPrecision = avgPrecision;
        this.ptosPR=ptosPR;
        setInterpolarPR();
        esTotal = false;
    }

    private void setInterpolarPR() {
        //this.ptosPRInterpolados = ptosPR; // TODO
        this.ptosPRInterpolados = new ArrayList<>();
        this.ptosPRInterpolados.addAll(ptosPR);
        // TODO
    }

    public EvaluacionNeed(List<EvaluacionNeed> evaluaciones) {
        esTotal = true;
        precision=0;
        recall=0;
        f1score=0;
        precAt10=0;
        avgPrecision=0;
        for(EvaluacionNeed ev : evaluaciones){
            precision+=ev.getPrecision();
            recall+=ev.getRecall();
            f1score+=ev.getF1score();
            precAt10+=ev.getPrecAt10();
            avgPrecision+=getAvgPrecision();
        }
        precision=precision/ evaluaciones.size();
        recall=recall/ evaluaciones.size();
        f1score=f1score/ evaluaciones.size();
        precAt10=precAt10/ evaluaciones.size();
        avgPrecision=avgPrecision/ evaluaciones.size();
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getRecall() {
        return recall;
    }

    public void setRecall(double recall) {
        this.recall = recall;
    }

    public double getF1score() {
        return f1score;
    }

    public void setF1score(double f1score) {
        this.f1score = f1score;
    }

    public double getPrecAt10() {
        return precAt10;
    }

    public void setPrecAt10(double precAt10) {
        this.precAt10 = precAt10;
    }

    public double getAvgPrecision() {
        return avgPrecision;
    }

    public void setAvgPrecision(double avgPrecision) {
        this.avgPrecision = avgPrecision;
    }

    private String decimal(double val) {
        return String.format("%.3f", val).replace(',', '.'); // Ponia comas por el español
    }

    @Override
    public String toString() {
        NumberFormat nf_out = NumberFormat.getNumberInstance(Locale.UK);
        nf_out.setMaximumFractionDigits(3);
        String s = "precision " + decimal(precision) +
                "\nrecall " + decimal(recall) +
                "\nF1 " + decimal(f1score) +
                "\nprec@10 " + decimal(precAt10);
        if (!esTotal) {
            s +=  "\naverage_precision " + decimal(avgPrecision) +
                    "\nrecall_precision ";
            for (Double[] pto : ptosPR) {
                s+= "\n" + decimal(pto[0]) + decimal(pto[1]);
            }
        }
        else {
            s += "\nMAP " + decimal(map);
        }
        s+= "\ninterpolated_recall_precision";
        for (Double[] pto : ptosPRInterpolados) {
            s+= "\n" + decimal(pto[0]) + " " + decimal(pto[1]);
        }
        return s;
    }
}
