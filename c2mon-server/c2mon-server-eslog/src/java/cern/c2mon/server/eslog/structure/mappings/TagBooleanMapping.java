package cern.c2mon.server.eslog.structure.mappings;

/**
 * Mapping that a TagBoolean will use in the ElasticSearch cluster.
 * @author Alban Marguet.
 */
public class TagBooleanMapping extends TagESMapping implements Mapping {
	public TagBooleanMapping(String type) {
		super();
		setProperties(type);
	}

	@Override
	public void setProperties(String tagValueType) {
		if (tagValueType.compareTo(boolType) != 0) {
			throw new IllegalArgumentException("Type for TagBoolean must be boolean.");
		} else {
			properties = new Properties(tagValueType);
		}
	}
}