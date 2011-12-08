<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
  <html>
  <body>
  <h4>registered TIM-DIPPublisher's data-tags:</h4>
    <table border="1">
      <tr bgcolor="#a0a0fc">
        <th align="left">Tag id</th>
        <th align="left">dip topic</th>
        <th align="left">comment</th>
      </tr>
      <xsl:for-each select="DataTags/DataTag">
      <tr>
        <td><xsl:value-of select="id"/></td>
        <td><xsl:value-of select="dip-topic"/></td>
        <td><xsl:value-of select="comment"/></td>
      </tr>
      </xsl:for-each>
    </table>
  </body>
  </html>
</xsl:template>
</xsl:stylesheet>
