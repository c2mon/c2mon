package cern.c2mon.client.ext.dynconfig;

import java.net.URI;

public enum SupportedProtocolsEnum {
	 PROTOCOL_DIP("dip",false)
//	,PROTOCOL_OPCDA("opcda",false)
	,PROTOCOL_OPCUA("opcua",false)
//	,PROTOCOL_SIMU("simu", false)
//	,PROTOCOL_HEARTBEAT("heartbeat", false)
//	,PROTOCOL_RDA("rda", false)
//	,PROTOCOL_JAPC("japc", false)
	;
	
	
	SupportedProtocolsEnum(String urlScheme,boolean supportsAuthentication){
		m_supportsAuthentication = supportsAuthentication;
		m_urlScheme = urlScheme;
	}
	
	public boolean isSupportsAuthentication(){
		return m_supportsAuthentication;
	}
	
   public String getUrlScheme() {
		return m_urlScheme;
	}
   
   public String toString(){
	   return m_urlScheme;
   }

   private final boolean m_supportsAuthentication;
   private final String m_urlScheme;
   
   public static SupportedProtocolsEnum getEnumForProtocolScheme(String scheme){
	   if(scheme.equalsIgnoreCase(PROTOCOL_DIP.m_urlScheme)){
		   return PROTOCOL_DIP;
//	   }else if(scheme.equalsIgnoreCase(PROTOCOL_OPCDA.m_urlScheme)){
//		   return PROTOCOL_OPCDA;
	   }else if(scheme.equalsIgnoreCase(PROTOCOL_OPCUA.m_urlScheme)){
		   return PROTOCOL_OPCUA;
//	   }else if(scheme.equalsIgnoreCase(PROTOCOL_SIMU.m_urlScheme)){
//		   return PROTOCOL_SIMU;
//	   }else if(scheme.equalsIgnoreCase(PROTOCOL_HEARTBEAT.m_urlScheme)){
//		   return PROTOCOL_HEARTBEAT;
//	   }else if(scheme.equalsIgnoreCase(PROTOCOL_RDA.m_urlScheme)){
//		   return PROTOCOL_RDA;
//	   }else if(scheme.equalsIgnoreCase(PROTOCOL_JAPC.m_urlScheme)){
//		   return PROTOCOL_JAPC;
	   }else{
		   throw new UnsupportedOperationException("Protocol '"+scheme+"' is not supported");
	   }
   }

public static String convertToTagName(URI uri) {
	return uri.toString();
}
}
