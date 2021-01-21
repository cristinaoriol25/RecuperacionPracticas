package evaluation;

// RIP Documento

public class Documento {
    private int docId;
    private int informationNeed;
    private boolean relevant;

    private boolean sabemosRelevancia = false;

    public Documento() {
    }

    public Documento(int docId, int informationNeed, boolean relevant) {
        this.docId = docId;
        this.informationNeed = informationNeed;
        this.relevant = relevant;
        this.sabemosRelevancia=true;
    }

    public Documento(int docId, int informationNeed) {
        this.docId = docId;
        this.informationNeed = informationNeed;
    }

    public int getDocId() {
        return docId;
    }

    public int getInformationNeed() {
        return informationNeed;
    }

    public void setInformationNeed(int informationNeed) {
        this.informationNeed = informationNeed;
    }

    public boolean isRelevant() {
        return relevant;
    }

    public void setRelevant(boolean relevant) {
        this.relevant = relevant;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    @Override
    public String toString() {
        return "Documento{" +
                "docId=" + docId +
                ", informationNeed=" + informationNeed +
                ", relevant=" + relevant +
                '}';
    }

}
