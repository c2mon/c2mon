<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>


<f:view>
<html>
  <body>
    <h2>TIM Quality Codes</h2>
    <p>The table below displays the quality codes used by the application server.
    These codes appear for example in the Short-Term-Log and are used in all the pie charts
    showing the quality breakdown of updates to the application server. The codes themselves
    are powers of 2 and can be combined. That is, a single tag value can have multiple invalid codes
    at the same time.</p>
    
    <p>For example, a quality code of 66 is the combination of quality codes 64 and 2 (use unique binary
    decomposition in general).</p>
    
    <h:dataTable border="1" cellpadding="1" value="#{asQualityTable.qualityCodes}" var="quality">   
      <h:column>
        <f:facet name="header">
          <h:outputText>Code</h:outputText>
        </f:facet>
        <h:outputText value="#{quality.code}"/>
      </h:column>
      <h:column>
        <f:facet name="header">
          <h:outputText>Quality</h:outputText>
        </f:facet>
        <h:outputText value="#{quality.name}"/>
      </h:column>
      <h:column>
        <f:facet name="header">
          <h:outputText>Description</h:outputText>
        </f:facet>
        <h:outputText value="#{quality.description}"/>
      </h:column>
    </h:dataTable>
    
    <p>The following table gives the quality codes used for the updates sent from the 
    DAQ layer to the application server. These codes cannot be combined: that is, 
    an update is flagged with a unique such quality code.</p>
    <h:dataTable border="1" cellpadding="1" value="#{daqQualityTable.qualityCodes}" var="quality">   
      <h:column>
        <f:facet name="header">
          <h:outputText>Code</h:outputText>
        </f:facet>
        <h:outputText value="#{quality.code}"/>
      </h:column>
      <h:column>
        <f:facet name="header">
          <h:outputText>Quality</h:outputText>
        </f:facet>
        <h:outputText value="#{quality.name}"/>
      </h:column>
      <h:column>
        <f:facet name="header">
          <h:outputText>Description</h:outputText>
        </f:facet>
        <h:outputText value="#{quality.description}"/>
      </h:column>
    </h:dataTable> 
    
  </body>
</html>
</f:view>