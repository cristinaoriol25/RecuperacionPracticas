package evaluation;

public class EvaluacionNeed {
    private double precision;
    private double recall;
    private double f1score;
    private double precAt10;
    private double avgPrecision;
    //private List<> //TODO: ver como hacer los puntos de precision-recall
    public EvaluacionNeed(double precision, double recall, double f1score, double precAt10, double avgPrecision) {
        this.precision = precision;
        this.recall = recall;
        this.f1score = f1score;
        this.precAt10 = precAt10;
        this.avgPrecision = avgPrecision;
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
}
