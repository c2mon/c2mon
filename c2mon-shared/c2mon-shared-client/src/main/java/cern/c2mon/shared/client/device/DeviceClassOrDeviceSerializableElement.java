package cern.c2mon.shared.client.device;

import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import java.io.StringWriter;

public abstract class DeviceClassOrDeviceSerializableElement {

    public String toConfigXml() throws Exception {
        Persister serializer = new Persister(new AnnotationStrategy());
        try (StringWriter fw = new StringWriter()) {
            serializer.write(this, fw);
            return fw.toString();
        }
    }
}
