<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!--  link variables  -->
  <xsl:variable name="base_url">../</xsl:variable>
  <xsl:variable name="alarm_url">alarmviewer/</xsl:variable>
  <xsl:variable name="command_url">commandviewer/</xsl:variable>
  <xsl:variable name="datatag_url">tagviewer/</xsl:variable>
  <xsl:variable name="process_xml_url">process/xml/</xsl:variable>
  <xsl:variable name="alarm_xml_url">alarmviewer/xml/</xsl:variable>
  <xsl:variable name="command_xml_url">commandviewer/xml/</xsl:variable>
  <xsl:variable name="report_xml_url">configloader/progress/finalReport/xml/</xsl:variable>
  
  <xsl:variable name="help_alarm_url">http://oraweb.cern.ch/pls/timw3/helpalarm.AlarmForm?p_alarmid=</xsl:variable>

  <!-- process the XML element ConfigurationReport -->
  <xsl:template match="ConfigurationReport">
      
    <div class="container-fluid" style="padding-left:150px; padding-right:150px;">
      <div class="row">

        <p class="tagName">
          <a href="../../{$base_url}{$report_xml_url}{id}/" style="margin-top: 20px;" class="btn btn-default btn-large pull-right">View
            Configuration Report XML >>
          </a>
        </p>
        <p>
          <h2>Overview</h2>
          <table class="table table-striped table-bordered">
            <tr>
              <th class="bold">Identifier</th>
              <td>
                <xsl:value-of select="id" />
              </td>
            </tr>
            <tr>
              <th class="bold">Name</th>
              <td>
                <xsl:value-of select="name" />
              </td>
            </tr>
            <tr>
              <th class="bold">Applied by</th>
              <td>
                <xsl:value-of select="user" />
              </td>
            </tr>
            <tr>
              <th class="bold">Applied on</th>
              <td>
                <xsl:value-of select="timestamp" />
              </td>
            </tr>
            <tr>
              <th class="bold">Status</th>
              <xsl:choose>
                <xsl:when test="status='OK'">
                  <td class="success">
                    <xsl:value-of select="status" />
                  </td>
                </xsl:when>
                <xsl:when test="status='WARNING' or status='RESTART'">
                  <td class="warning">
                    <xsl:value-of select="status" />
                  </td>
                </xsl:when>
                <xsl:otherwise>
                  <td class="danger">
                    <xsl:value-of select="status" />
                  </td>
                </xsl:otherwise>
              </xsl:choose>
            </tr>
            <tr>
              <th class="highlight bold">Message</th>
              <td>
                <xsl:value-of select="status-description" />
              </td>
            </tr>
            <tr>
              <th class="highlight bold">DAQs to reboot</th>
              <td>
                <xsl:value-of select="daq-reboot" />
              </td>
            </tr>
          </table>
        </p>
        <xsl:apply-templates select="ConfigurationElementReports" />

      </div>
    </div>

  </xsl:template>

  <xsl:template match="ConfigurationElementReports">
    <hr />
    <p>
      <h2>Detailed Report</h2>
      <table class="table table-striped table-bordered">
        <thead>
          <tr>
            <th class="col-md-1">Action</th>
            <th class="col-md-1">Entity</th>
            <th class="col-md-1">Id</th>
            <th class="col-md-1">Status</th>
            <th>Report</th>
          </tr>
        </thead>
        <tbody>
          <xsl:for-each select="ConfigurationElementReport">
            <tr>
              <td>
                <xsl:value-of select="action" />
              </td>
              <td>
                <xsl:value-of select="entity" />
              </td>
              <td>
                <xsl:value-of select="id" />
              </td>
              <xsl:choose>
                <xsl:when test="status='OK'">
                  <td class="success">
                    <xsl:value-of select="status" />
                  </td>
                </xsl:when>
                <xsl:when test="status='WARNING' or status='RESTART'">
                  <td class="warning">
                    <xsl:value-of select="status" />
                  </td>
                </xsl:when>
                <xsl:otherwise>
                  <td class="danger">
                    <xsl:value-of select="status" />
                  </td>
                </xsl:otherwise>
              </xsl:choose>

              <td>

                <button type="button" class="btn btn-default" data-toggle="collapse" data-target="#collapseme-{action}-{id}">
                  Click to expand
                </button>

                <div id="collapseme-{action}-{id}" class="collapse out">
                  <xsl:if test="status-message!='' ">
                    <div>
                      <xsl:value-of select="status-message" />
                    </div>
                  </xsl:if>
                  <ul class="list-unstyled">
                    <xsl:apply-templates select="sub-reports" />
                  </ul>
                </div>

              </td>
            </tr>
          </xsl:for-each>
        </tbody>
      </table>
    </p>
  </xsl:template>

  <xsl:template match="sub-reports">

    <xsl:for-each select="ConfigurationElementReport">
      <li>
        <xsl:value-of select="action" /> -
        <xsl:value-of select="entity" /> -
        <xsl:value-of select="id" /> -
        <xsl:value-of select="status" />
        
        <xsl:if test="status-message!=''">
          <p>
            <xsl:value-of select="status-message" />
          </p>
        </xsl:if>
      </li>
      <xsl:apply-templates select="sub-reports" />
    </xsl:for-each>

  </xsl:template>
</xsl:stylesheet>