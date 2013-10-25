package cern.c2mon.driver.common.vcm.testhandler;

public class TagOperationException extends Exception {

    private static final long serialVersionUID = 7906984509174711852L;

    public TagOperationException(final String errMessage) {
        super(errMessage);
    }
}
