import com.google.api.client.util.Key;
import com.google.api.client.json.GenericJson;
import com.google.api.services.gmail.model.Label;
import com.google.api.client.json.JsonFactory;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class LabelRules extends GenericJson {
    @Key private List<Rules> label_rules;

    public LabelRules(){}

    public void setRules(List<Rules> label_rules) {
        this.label_rules = label_rules;
    }

    public void set(int index, Rules rules) {
        label_rules.set(index,rules);
    }

    public List<Rules> getRules() {
        return label_rules;
    }

    public Rules get(int index) {
        return label_rules.get(index);
    }

    public int size() {
        return label_rules.size();
    }

    public void loadLabelIds(List<Label> labels) throws RuntimeException {
        for (Rules r: label_rules) {
            if (r.getMarker()!=null)
                r.setMarkerId(search(labels,r.getMarker()));

            if (r.getDestination()!=null)
                r.setDestinationId(search(labels,r.getDestination()));

            if (r.getRemove()!=null)
                r.setRemoveId(search(labels,r.getRemove()));
        }
    }

    private String search(List<Label> labels, String label_name) throws RuntimeException {
        for (Label label : labels) {
            if (label.getName().equals(label_name)) {
                return label.getId();
            }
        }
        throw new RuntimeException("Failed to find "+label_name+" in labels");
    }

    public static final class Rules extends GenericJson {
        /** save authors from marking or discard every run */
        @Key("keep_authors") private boolean keepAuthors;

        /** Label to add messages to author list */
        @Key private String marker;
        
        /** Label to send messages from author list to */
        @Key private String destination;
        
        /** Additional search query terms, such as in:inbox */
        @Key private String q;
        
        /** Label to remove from messages in author list */
        @Key private String remove;

        public Rules(){}

        private String markerId;
        private String destinationId;
        private String removeId;

        public void setKeepAuthors(boolean keepAuthors) {
            this.keepAuthors = keepAuthors;
        }

        public void setMarker(String marker) {
            this.marker = marker;
        }

        public void setMarkerId(String id) {
            this.markerId = id;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public void setDestinationId(String id) {
            this.destinationId = id;
        }

        public void setQ(String q) {
            this.q = q;
        }

        public void setRemove(String remove) {
            this.remove = remove;
        }

        public void setRemoveId(String id) {
            this.removeId = id;
        }

        public boolean getKeepAuthors() {
            return keepAuthors;
        }

        public String getMarker() {
            return marker;
        }

        public String getMarkerId() {
            return markerId;
        }

        public String getDestination() {
            return destination;
        }

        public String getDestinationId() {
            return destinationId;
        }

        public String getQ() {
            return q;
        }

        public String getRemove() {
            return remove;
        }

        public String getRemoveId() {
            return removeId;
        }

        @Override
        public Rules set(String fieldName, Object value) {
        return (Rules) super.set(fieldName, value);
        }

        @Override
        public Rules clone() {
        return (Rules) super.clone();
        }
    }

    public String toString() {
        String out = "{\"label_rules\":[";
        boolean prev = false;
        for (int i = 0; i<label_rules.size(); i++) {
            Rules d = get(i);
            if (prev) {
                out += ",";
            }
            out += "{";
            prev = false;
            if (d.getMarker()!=null) {
                out += "\"marker\":\"" + d.getMarker() + "\"";
                prev = true;
            }
            if (d.getDestination()!=null) {
                out += "\"destination\":\"" + d.getDestination() + "\"";
                if (prev) {
                    out += ",";
                }
                prev = true;
            }
            if (d.getQ()!=null) {
                out += "\"q\":\"" + d.getQ() + "\"";
                if (prev) {
                    out += ",";
                }
                prev = true;
            }
            if (d.getRemove()!=null) {
                out += "\"q\":\"" + d.getRemove() + "\"";
                if (prev) {
                    out += ",";
                }
            }
            out += "}";
            prev = true;
        }
        out += "]}";
        return out;
    }

    @Override
    public LabelRules set(String fieldName, Object value) {
      return (LabelRules) super.set(fieldName, value);
    }

    @Override
    public LabelRules clone() {
      return (LabelRules) super.clone();
    }

    public static LabelRules load(JsonFactory jsonFactory, Reader reader) throws IOException {
        return jsonFactory.fromReader(reader, LabelRules.class);
    }

}
