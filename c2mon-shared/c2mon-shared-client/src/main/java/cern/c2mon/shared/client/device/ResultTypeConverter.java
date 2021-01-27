package cern.c2mon.shared.client.device;

import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

/**
 * A converter for SimpleXML to serialize {@link ResultType} values as their labels as they are stored in the database.
 */
public class ResultTypeConverter implements Converter<ResultType> {

    @Override
    public ResultType read(InputNode inputNode) throws Exception {
        String value = inputNode.getValue();
        return ResultType.getOrDefault(value);
    }

    @Override
    public void write(OutputNode outputNode, ResultType deviceElementField) {
        outputNode.setValue(deviceElementField.getLabel());
    }
}
