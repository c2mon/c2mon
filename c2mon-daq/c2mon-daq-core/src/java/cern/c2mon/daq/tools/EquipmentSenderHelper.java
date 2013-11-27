package cern.c2mon.daq.tools;

import java.util.regex.Pattern;

import cern.c2mon.shared.daq.datatag.ISourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.c2mon.shared.daq.datatag.SourceDataTag;

/**
 * 
 * @author vilches
 *
 */
public class EquipmentSenderHelper {
	
    /**
     * maximum allowed length of the tag quality description (will be truncated to this if too long)
     */
    private static final int MAX_QUALITY_DESC_LENGHT = 300;

    /**
     * Invalid XML chars that should not be send to the server
     */
    private static final Pattern INVALID_XML_CHARS = Pattern
            .compile("[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD\uD800\uDC00-\uDBFF\uDFFF]");
	
	
	/**
     * Converts the input value to the expected type
     * 
     * @param tag
     * @param inValue
     * @return
     */
    public Object convertValue(ISourceDataTag tag, Object inValue) {
        Object outValue = null;
        if (inValue instanceof Number) {
            outValue = TIMDriverSimpleTypeConverter.convert(tag, (Number) inValue);
        } else if (inValue instanceof Boolean) {
            outValue = inValue;
        } else {
            outValue = inValue.toString();
        }

        return outValue;
    }
    
    /**
     * Creates a new quality object for a {@link SourceDataTag}
     * 
     * @param pQualityCode A quality code which is defined as constant in SourceDataQuality
     * @param pDescription A quality description
     * 
     * @return A new instance of {@link SourceDataQuality}
     */
    public SourceDataQuality createTagQualityObject(final short pQualityCode, final String pDescription) {
      SourceDataQuality sdQuality;
      if (pDescription == null) {
          sdQuality = new SourceDataQuality(pQualityCode);
      } else {
          String pQualityDesc;
          if (pDescription.length() > MAX_QUALITY_DESC_LENGHT) {
              pQualityDesc = pDescription.substring(0, MAX_QUALITY_DESC_LENGHT - 1);
          } else {
              pQualityDesc = pDescription;
          }

          // Strip out all invalid XML characters
          pQualityDesc = INVALID_XML_CHARS.matcher(pQualityDesc).replaceAll("");

          sdQuality = new SourceDataQuality(pQualityCode, pQualityDesc);
      }
      
      return sdQuality;
    }
}
