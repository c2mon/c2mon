package cern.c2mon.shared.client.configuration.api.metaData;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fritter on 30/11/15.
 */
@Data
public class MetaData {

  private Map<String, Object> metaDataProperties = new HashMap<>();

  private List<String> removeMetaData = new ArrayList<>();

  @Builder
  public MetaData(@Singular("addMetaData") Map<String, Object> metaDataProperties, @Singular("removeMetaData") List<String> removeMetaData) {
    this.metaDataProperties = metaDataProperties;
    this.removeMetaData = removeMetaData;
  }

  public MetaData() {
  }

}
