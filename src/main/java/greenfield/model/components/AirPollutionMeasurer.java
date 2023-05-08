package greenfield.model.components;

import greenfield.model.Component;

public class AirPollutionMeasurer extends Component {
    public AirPollutionMeasurer () {
        setName("AirPollutionMeasurer");
        setID("3");
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
