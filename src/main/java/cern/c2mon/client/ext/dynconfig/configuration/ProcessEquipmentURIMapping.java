package cern.c2mon.client.ext.dynconfig.configuration;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessEquipmentURIMapping {
	public Long processId;
	public String processName;
	public String equipmentName;
	
	public String processDescription = "DynConfig Process";
	
	public String equipmentDescription;

	
	public String uriPattern;
	
	public Map<String, String> metadata;

}
