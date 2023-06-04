package utils.data;

import java.util.List;
import java.util.Objects;

public class CleaningRobotIDsHTTPResponse {
    private List<String> ids;

    public CleaningRobotIDsHTTPResponse(List<String> ids) {
        this.ids = ids;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;

        var other = (CleaningRobotIDsHTTPResponse) obj;

        return Objects.equals(this.ids, other.ids);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ids);
    }

    @Override
    public String toString() {
        return "CleaningRobotIDsHTTPResponse[" +
                "ids=" + ids + ']';
    }
}