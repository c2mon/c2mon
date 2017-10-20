package cern.c2mon.server.configuration.parser.exception;

import lombok.Getter;

public class EntityDoesNotExistException extends ConfigurationParseException {

    @Getter
    private final Long id;
    @Getter
    private final String type;
    @Getter
    private final String name;

    public EntityDoesNotExistException(final Long id, final String type, final String name) {
        super("Error updating entity: " + type + " (name = " + name + ", id = " + id + ") does not exist!");
        this.id = id;
        this.type = type;
        this.name = name;
    }

}
