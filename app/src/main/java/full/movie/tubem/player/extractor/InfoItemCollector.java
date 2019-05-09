package full.movie.tubem.player.extractor;

import full.movie.tubem.player.extractor.exceptions.ExtractionException;
import java.util.List;
import java.util.Vector;

public class InfoItemCollector {
    private List<Throwable> errors = new Vector();
    private List<InfoItem> itemList = new Vector();
    private int serviceId = -1;

    public InfoItemCollector(int serviceId2) {
        this.serviceId = serviceId2;
    }

    public List<InfoItem> getItemList() {
        return this.itemList;
    }

    public List<Throwable> getErrors() {
        return this.errors;
    }

    /* access modifiers changed from: protected */
    public void addFromCollector(InfoItemCollector otherC) throws ExtractionException {
        if (this.serviceId != otherC.serviceId) {
            throw new ExtractionException("Service Id does not equal: " + Newapp.getNameOfService(this.serviceId) + " and " + Newapp.getNameOfService(otherC.serviceId));
        }
        this.errors.addAll(otherC.errors);
        this.itemList.addAll(otherC.itemList);
    }

    /* access modifiers changed from: protected */
    public void addError(Exception e) {
        this.errors.add(e);
    }

    /* access modifiers changed from: protected */
    public void addItem(InfoItem item) {
        this.itemList.add(item);
    }

    /* access modifiers changed from: protected */
    public int getServiceId() {
        return this.serviceId;
    }
}
