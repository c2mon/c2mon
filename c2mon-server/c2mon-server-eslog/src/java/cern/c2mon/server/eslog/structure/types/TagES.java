package cern.c2mon.server.eslog.structure.types;

import cern.c2mon.server.eslog.structure.mappings.TagESMapping;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Class that represents a Tag for ElasticSearch.
 * Used as "type" in ElasticSearch.
 * @author Alban Marguet.
 */
@Slf4j
public class TagES implements TagESInterface {
    private Map<String, String> metadataProcess;
    private long tagId;
    private String tagName;
    private String dataType;
    private long tagTime;
    private long tagServerTime;
    private long tagDaqTime;
    private int tagStatus;
    private String quality; //tagstatusdesc
    protected Object tagValue;
    private String tagValueDesc;
    protected transient TagESMapping mapping;


    public TagES() {
        this.metadataProcess = new HashMap<>();
        this.mapping = new TagESMapping();
    }

    /**
     * Return a JSON representing the Tag for indexing in ElasticSearch.
     * @return XContentBuilder
     */
    public String build() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(this);
        log.info(json);
        return json;
    }

    public void setMapping(String tagValueType) {
        //TODO: check type
        mapping.setProperties(tagValueType);
    }

    /**
     * ElasticSearch will give a default mapping if it is another type of tag.
     * Should not happen.
     * @return nullXContentBuilder
     */
    public String getMapping() {
        return mapping.getMapping();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(getTagId());
        str.append('\t');
        str.append(getTagName());
        str.append('\t');
        str.append("tagValue");
        str.append('\t');
        str.append(getTagValueDesc());
        str.append('\t');
        str.append(getDataType());
        str.append('\t');
        str.append(getTagTime());
        str.append('\t');
        str.append(getTagDaqTime());
        str.append('\t');
        str.append(getTagServerTime());
        str.append('\t');
        str.append(getTagStatus());
        str.append('\t');
        if ((getQuality() != null) && (getQuality().equals(""))) {
            str.append("null");
        } else {
            str.append(getQuality());
        }
        str.append('\t');
        for (String metadata : getMetadataProcess().values()) {
            str.append(metadata);
            str.append('\t');
        }
        return str.toString();
    }

    public Map<String, String> getMetadataProcess() {
        return metadataProcess;
    }

    public long getTagId() {
        return tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public String getDataType() {
        return dataType;
    }

    public long getTagTime() {
        return tagTime;
    }

    public long getTagServerTime() {
        return tagServerTime;
    }

    public long getTagDaqTime() {
        return tagDaqTime;
    }

    public int getTagStatus() {
        return tagStatus;
    }

    public String getTagValueDesc() {
        return tagValueDesc;
    }

    public void setMetadataProcess(Map<String, String> metadataProcess) {
        this.metadataProcess = metadataProcess;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setTagTime(long tagTime) {
        this.tagTime = tagTime;
    }

    public void setTagServerTime(long tagServerTime) {
        this.tagServerTime = tagServerTime;
    }

    public void setTagDaqTime(long tagDaqTime) {
        this.tagDaqTime = tagDaqTime;
    }

    public void setTagStatus(int tagStatus) {
        this.tagStatus = tagStatus;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public void setTagValue(Object tagValue) {
        this.tagValue = tagValue;
    }

    public void setTagValueDesc(String tagValueDesc) {
        this.tagValueDesc = tagValueDesc;
    }

    public String getQuality() {
        return quality;
    }

    public Object getTagValue() {
        return tagValue;
    }
}