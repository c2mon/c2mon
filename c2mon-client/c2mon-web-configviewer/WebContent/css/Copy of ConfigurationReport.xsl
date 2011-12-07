
<xsl:template match="ConfigurationReport">
     
     <h1>Configuration Report</h1>


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





</xsl:stylesheet>
