package greenfield.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an entity in the game world, composed of components.
 */
public class Entity {

    /**
     * The components attached to this entity, stored as a map with their class types as keys.
     */
    protected Map<Class<? extends Component>, Component> components;

    public Entity() {
        components = new HashMap<>();
    }

    /**
     * Adds a component to this entity.
     *
     * @param component the component to add
     * @param <T> the type of the component
     */
    public <T extends Component> void addComponent(T component) {
        components.put(component.getClass(), component);
    }

    /**
     * Returns the component of the specified type attached to this entity.
     *
     * @param componentClass the class of the component to retrieve
     * @param <T> the type of the component
     * @return the component of the specified type, or null if it is not attached to this entity
     */
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return componentClass.cast(components.get(componentClass));
    }

    /**
     * Returns whether this entity has a component of the specified type attached to it.
     *
     * @param componentClass the class of the component to check for
     * @param <T> the type of the component
     * @return true if a component of the specified type is attached to this entity, false otherwise
     */
    public <T extends Component> boolean hasComponent(Class<T> componentClass) {
        return components.containsKey(componentClass);
    }
}

