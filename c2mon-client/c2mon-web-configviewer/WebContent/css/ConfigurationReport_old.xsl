<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="ConfigurationReport">
 <html>
   <head>
       <link rel="stylesheet" href="tim.css"/>
   </head>
   <body>
     
     <h1>Configuration Report</h1>

     <p>
     <h2>Overview</h2>
     <table class="inline" border="1">
       <tr>
         <th>Identifier</th>
         <td><xsl:value-of select="id" /></td>
       </tr>
       <tr>
         <th>Name</th>
         <td><xsl:value-of select="name" /></td>
       </tr>
       <tr>
         <th>Applied by</th>
         <td><xsl:value-of select="user" /></td>
       </tr>
       <tr>
         <th>Applied on</th>
         <td><xsl:value-of select="timestamp" /></td>
       </tr>
       <tr>
         <th>Status</th>
         <xsl:choose>
         <xsl:when test="status='OK'">
           <td bgcolor="green"><xsl:value-of select="status" /></td>
         </xsl:when>  
         <xsl:when test="status='WARNING'">
           <td bgcolor="yellow"><xsl:value-of select="status" /></td>
         </xsl:when>  
         <xsl:otherwise>
           <td bgcolor="red"><xsl:value-of select="status" /></td>
         </xsl:otherwise>
         </xsl:choose>
       </tr>
       <tr>
         <th>Message</th>
         <td><pre><xsl:value-of select="status-description" /></pre></td>
       </tr>
       <tr>
         <th>DAQs to reboot</th>
         <td><xsl:value-of select="daq-reboot" /></td>
       </tr>
     </table>
     </p>
     <xsl:apply-templates select="ConfigurationElementReports"/>
   </body>
  </html>
</xsl:template>


<xsl:template match="ConfigurationElementReports">
     <hr/>
     <p>
     <h2>Detailed Report</h2>
     <table class="inline" border="1">
       <tr>
         <th>Action</th>
         <th>Entity</th>
         <th>Id</th>
         <th>Status</th>
         <th>Report</th>
       </tr>
       <xsl:for-each select="ConfigurationElementReport">
         <tr>
         <td><xsl:value-of select="action" /></td>
         <td><xsl:value-of select="entity" /></td>
         <td><xsl:value-of select="id" /></td>
         <xsl:choose>
         <xsl:when test="status='OK'">
           <td bgcolor="green"><xsl:value-of select="status" /></td>
         </xsl:when>  
         <xsl:when test="status='WARNING'">
           <td bgcolor="yellow"><xsl:value-of select="status" /></td>
         </xsl:when>  
         <xsl:otherwise>
           <td bgcolor="red"><xsl:value-of select="status" /></td>
         </xsl:otherwise>
         </xsl:choose>

         <td>
	   <pre><xsl:value-of select="status-message" /></pre>
           <xsl:apply-templates select="sub-reports"/>
	 </td>
         </tr>
       </xsl:for-each>
     </table>
     </p>
</xsl:template>

<xsl:template match="sub-reports">
    <ul>   
         <xsl:for-each select="ConfigurationElementReport">
	   <li>
	     <xsl:value-of select="action" /> -  
	     <xsl:value-of select="entity" /> - 
	     <xsl:value-of select="id" /> -  
	     <xsl:value-of select="status"/> - 
             <xsl:if test="status-message!=''">
	       <pre>
    	       <xsl:value-of select="status-message"/>
	       </pre>
	     </xsl:if>  
	     
	   </li>
	   <xsl:apply-templates select="sub-reports"/>
	 </xsl:for-each>
    </ul>	 
</xsl:template>



</xsl:stylesheet>
