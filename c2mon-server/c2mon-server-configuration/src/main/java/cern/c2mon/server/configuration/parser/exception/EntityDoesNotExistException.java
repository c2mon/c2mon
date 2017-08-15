package cern.c2mon.server.configuration.parser.exception;

import lombok.Getter;

public class EntityDoesNotExistException extends ConfigurationParseException {

    @Getter
    private long id;
    @Getter
    private String type;
    @Getter
    private String name;

    public EntityDoesNotExistException(long id, String type, String name) {
        super("Error updating entity: " + type + " (name = " + name + ", id = " + id + ") does not exist!");
        this.id = id;
        this.type = type;
        this.name = name;
    }

}
