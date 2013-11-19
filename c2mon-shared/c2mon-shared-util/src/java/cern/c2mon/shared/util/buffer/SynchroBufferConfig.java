package cern.c2mon.shared.util.buffer;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;

/**
 * Service class. Provides property definitions and initialization facility.
 * @author fracalde
 */

public final class SynchroBufferConfig {
    
    static private Properties sbProperties = null;
    
    /** Field CONFIG_FILE_LOCATION_PROPERTY           */
    public static final String CONFIGURATION_PROPERTY = "synchrobuffer.config";
    /** Field CONFIGURATION_PROPERTY_FILE           */
    public static final String CONFIGURATION_PROPERTY_FILE = "synchrobuffer-config.properties";
    /** Field CONFIGURATION_FILE           */
    public static final String CONFIGURATION_FILE = "synchrobuffer.properties";
    /** Field MIN_WINDOW_SIZE_PROPERTY           */
    public static final String  MIN_WINDOW_SIZE_PROPERTY = "synchrobuffer.minwindowsize";
    /** Field MAX_WINDOW_SIZE_PROPERTY           */
    public static final String  MAX_WINDOW_SIZE_PROPERTY = "synchrobuffer.maxwindowsize";
    /** Field WINDOW_GROWTH_FACTOR_PROPERTY           */
    public static final String  WINDOW_GROWTH_FACTOR_PROPERTY = "synchrobuffer.windowgrowthfactor";
    /** Field DUPLCATE_POLICY_PROPERTY           */
    public static final String  DUPLICATE_POLICY_PROPERTY = "synchrobuffer.duplicatepolicy";
    
    private static final String DEFAULT_MIN_WINDOW_SIZE  = "500";
    private static final String DEFAULT_MAX_WINDOW_SIZE  = "5000";
    private static final String DEFAULT_WINDOW_GROWTH_FACTOR = "100";
    private static final String DEFAULT_DUPLICATE_POLICY = String.valueOf(SynchroBuffer.DUPLICATE_OK);
    
    /**
     * Returns properties loaded from the SynchroBuffer configuration file.
     * @param loader the class loader
     * @return The configuration properties.
     */
    public static Properties getProperties(ClassLoader loader) {
        if (sbProperties == null) {
            sbProperties = new Properties();
            InputStream in_stream = null;
            try {
                // try to open the resource from a system property
                in_stream = getInputStream(loader, System.getProperty(CONFIGURATION_PROPERTY));
            } catch (Exception e1) {
            }
            if (in_stream == null) {
                try {
                    // try to open the default config file
                    in_stream = getInputStream(loader, CONFIGURATION_FILE);
                } catch (Exception e2) {
                }
                if (in_stream == null) {
                    try {
                        // try to open the resource from a property file
                        InputStream property_stream = getInputStream(loader, CONFIGURATION_PROPERTY_FILE);
                        Properties prop = new Properties();
                        prop.load(property_stream);
                        in_stream = getInputStream(loader, prop.getProperty(CONFIGURATION_PROPERTY));
                    } catch (Exception e3) {
                    }
                }
            }
            if (in_stream != null) {
                try {
                    BufferedInputStream bin_stream = new BufferedInputStream(in_stream);
                    sbProperties.load(bin_stream);
                    in_stream.close();
                } catch (IOException ex) {
                }
            }
            setProperties();
        }
        
        return sbProperties;
    }
    
    /**
     * Set the right values for the properties.
     */
    private static void setProperties() {
        sbProperties.setProperty(MIN_WINDOW_SIZE_PROPERTY, System.getProperty(MIN_WINDOW_SIZE_PROPERTY, sbProperties.getProperty(MIN_WINDOW_SIZE_PROPERTY, DEFAULT_MIN_WINDOW_SIZE)));
        sbProperties.setProperty(MAX_WINDOW_SIZE_PROPERTY, System.getProperty(MAX_WINDOW_SIZE_PROPERTY, sbProperties.getProperty(MAX_WINDOW_SIZE_PROPERTY, DEFAULT_MAX_WINDOW_SIZE)));
        sbProperties.setProperty(WINDOW_GROWTH_FACTOR_PROPERTY, System.getProperty(WINDOW_GROWTH_FACTOR_PROPERTY, sbProperties.getProperty(WINDOW_GROWTH_FACTOR_PROPERTY, DEFAULT_WINDOW_GROWTH_FACTOR)));
        sbProperties.setProperty(DUPLICATE_POLICY_PROPERTY, System.getProperty(DUPLICATE_POLICY_PROPERTY, sbProperties.getProperty(DUPLICATE_POLICY_PROPERTY, DEFAULT_DUPLICATE_POLICY)));
        StringWriter s_writer = new StringWriter();
        sbProperties.list(new PrintWriter(s_writer));
    }
    
    /**
     * Open the resource as Input Stream.
     */
    private static InputStream getInputStream(ClassLoader loader, String resource) throws IOException {
        InputStream in_stream = null;
        // Try to open a resource
        if (loader != null) {
            in_stream = loader.getResourceAsStream(resource);
        }
        if (in_stream == null) {
            // Try to open URL
            try {
                URL url = new URL(resource);
                in_stream = url.openStream();
            } catch (Exception e1) {
                // Try to open plain file
                try {
                    in_stream = new FileInputStream(resource);
                } catch (Exception e2) {
                    throw new IOException("unable to get the configuration");
                }
            }
        }
        return in_stream;
    }
}

