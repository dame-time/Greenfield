package greenfield.model.components;

import greenfield.model.Component;

public class District extends Component {
    public District() {
        setName("District");
        setID("2");
    }

    @Override
    protected void setName(String name) {
        this.name = name;
    }

    @Override
    protected void setID(String id) {
        this.ID = id;
    }
}
