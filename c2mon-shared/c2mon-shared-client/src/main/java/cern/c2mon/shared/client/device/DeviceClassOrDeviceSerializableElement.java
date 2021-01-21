package cern.c2mon.shared.client.device;

import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import java.io.StringWriter;

/**
 * An interface providing a convenience method for serializing object to an XML string using the SimpleXML framework.
 */
public interface DeviceClassOrDeviceSerializableElement {

    /**
     * Serializes the implementing object into an XML string using SimpleXML
     * @return a String of the serialized object
     * @throws Exception if the serialization fails
     */
    default String toConfigXml() throws Exception {
        Persister serializer = new Persister(new AnnotationStrategy());
        try (StringWriter fw = new StringWriter()) {
            serializer.write(this, fw);
            return fw.toString();
        }
    }
}
