package greenfield.model;

public abstract class Component {
    protected String name;
    protected String ID;

    /**
     * Gets the name of the component.
     *
     * @return the name of the component
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the ID of the component.
     *
     * @return the ID of the component
     */
    public String getID() {
        return ID;
    }

    /**
     * Sets the name of the component.
     *
     * @param name the name to set for the component
     */
    protected abstract void setName(String name);

    /**
     * Sets the ID of the component.
     *
     * @param id the ID to set for the component
     */
    protected abstract void setID(String id);
}
