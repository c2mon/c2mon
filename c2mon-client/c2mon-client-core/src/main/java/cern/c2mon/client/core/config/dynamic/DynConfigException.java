package cern.c2mon.client.core.config.dynamic;

import lombok.AllArgsConstructor;

/**
 * A custom wrapper for exceptions occurring within the C2MON DynConfig module to allow a generic handling outside the class.
 */
public class DynConfigException extends Exception {

    /**
     * The Context enum documents the different settings and situations during which an exception may occur. The
     * corresponding error messages are centrally defined.
     */
    @AllArgsConstructor
    public enum Context {
        COMMAND_NOT_SUPPORTED("Dynamic configuration is not currently supported for CommandTags. "),
        CREATE_TAG("Error creating the data tag. "),
        DELETE_TAG("Error deleting the data tag. "),
        URI_MISSING_REQUIRED_PROPERTIES("URI is missing required property: "),
        UNSUPPORTED_SCHEME("The protocol is not supported: "),
        MISSING_SCHEME("Please specify a protocol"),
        NO_MATCHING_MAPPING( "No matching Process could be found for URI: "),
        INVALID_URI_PROPERTY("Invalid value for property:");

        String message;
    }

    /**
     * Creates a new DynConfigException detailing the context that it occurred in.
     * @param context the context that the error occurred in.
     */
    public DynConfigException(Context context) {
        super(context.message);
    }

    /**
     * Creates a new DynConfigException detailing the context that it occurred in as well as an error message.
     * @param context the context that the error occurred in.
     * @param message further information surrounding the error.
     */
    public DynConfigException(Context context, String message) {
        super(context.message + message);
    }

    /**
     * A DynConfigException wrapping another exception e.
     * @param context the context that the error occurred in.
     * @param e the original exception
     */
    public DynConfigException(Context context, Throwable e) {
        super(context.message, e);
    }

    /**
     * A DynConfigException wrapping another exception e.
     * @param context the context that the error occurred in.
     * @param message further information surrounding the error.
     * @param e the original exception
     */
    public DynConfigException(Context context, String message, Throwable e) {
        super(context.message + ", " + message, e);
    }
}
