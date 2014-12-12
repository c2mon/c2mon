<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="EquipmentUnits">
    <xsl:apply-templates select="EquipmentUnit" />
  </xsl:template>

  <xsl:template match="EquipmentUnit">
  
    <style>th {width:25%;}</style>
  
    <h2 class="tagName">
      Equipment: 
      <a name="{@name}">
        <xsl:value-of select="@name" />
      </a>
      : (<xsl:value-of select="@id" />)
    </h2>
    
    <div class="row">
      <table class="table table-striped table-bordered">
        <th colspan="2">EquipmentUnit</th>
        <tr>
          <th> id </th>
          <td>
            <xsl:value-of select="@id" />
          </td>
        </tr>
        <tr>
          <th> name </th>
          <td>
            <xsl:value-of select="@name" />
          </td>
        </tr>

        <xsl:for-each select="*[not(local-name() = 'DataTags' or local-name() = 'SubEquipmentUnits' 
        or local-name() = 'CommandTags')]">
          <tr>
            <th>
              <xsl:value-of select="local-name()" />
            </th>
            <td>
              <xsl:value-of select="." />
            </td>
          </tr>
        </xsl:for-each>

      </table>
    </div>
    <xsl:apply-templates select="SubEquipmentUnits" />
    <xsl:apply-templates select="DataTags" />
    <xsl:apply-templates select="CommandTags" />
  </xsl:template>

  <xsl:template match="SubEquipmentUnits">
    <xsl:apply-templates select="SubEquipmentUnit" />
  </xsl:template>

  <xsl:template match="SubEquipmentUnit">
    <h3 class="tagName">
      <a name="{@name}">
        <xsl:value-of select="@name" />
      </a>
      : (
      <xsl:value-of select="@id" />
      )
    </h3>
    <div class="row">
      <table class="table table-striped table-bordered">
        <th colspan="2">SubEquipmentUnit</th>
        <tr>
          <th> id </th>
          <td>
            <xsl:value-of select="@id" />
          </td>
        </tr>
        <tr>
          <th> name </th>
          <td>
            <xsl:value-of select="@name" />
          </td>
        </tr>

        <xsl:for-each select="*">
          <tr>
            <th>
              <xsl:value-of select="local-name()" />
            </th>
            <td>
              <xsl:value-of select="." />
            </td>
          </tr>
        </xsl:for-each>

      </table>
    </div>
  </xsl:template>

  <xsl:template match="CommandTags">
    <xsl:apply-templates select="CommandTag" />
  </xsl:template>

  <xsl:template match="CommandTag">
    <h4 class="tagName">
      <a href="{$base_url}{$command_url}{@id}/">
        <xsl:value-of select="@name" />
        :(
        <xsl:value-of select="@id" />
        )
      </a>
      &#160;
    </h4>
    <p>
      This CommandTag belongs to Equipment
      <a href="#{../../@name}">
        <xsl:value-of select="../../@name" />
      </a>
    </p>
    <div class="row">
      <table class="table table-striped table-bordered">
        <th colspan="2">CommandTag</th>

        <tr>
          <th> id </th>
          <td>
            <xsl:value-of select="@id" />
          </td>
        </tr>
        <tr>
          <th> name </th>
          <td>
            <xsl:value-of select="@name" />
          </td>
        </tr>

        <xsl:for-each select="*">

          <tr>
            <th>
              <xsl:value-of select="local-name()" />
            </th>
            <td>
              <xsl:value-of select="." />
            </td>
          </tr>
        </xsl:for-each>

      </table>
    </div>
    <xsl:apply-templates select="HardwareAddress" />
  </xsl:template>

  <xsl:template match="DataTags">
    <xsl:apply-templates select="DataTag" />
  </xsl:template>

  <xsl:template match="DataTag">
    <h4 class="tagName">
      <a href="{$base_url}{$datatag_url}{@id}">
        <xsl:value-of select="@name" />
        :(
        <xsl:value-of select="@id" />
        )
      </a>
      &#160;
    </h4>
    <p>
      This DataTag belongs to Equipment
      <a href="#{../../@name}">
        <xsl:value-of select="../../@name" />
      </a>
    </p>
    <div class="row">
      <table class="table table-striped table-bordered">
        <th colspan="2">DataTag</th>
        <tr>
          <th> id </th>
          <td>
            <xsl:value-of select="@id" />
          </td>
        </tr>
        <tr>
          <th> name </th>
          <td>
            <xsl:value-of select="@name" />
          </td>
        </tr>

        <xsl:for-each select="*[not(local-name() = 'DataTagAddress')]">
          <tr>
            <th>
              <xsl:value-of select="local-name()" />
            </th>
            <td>
              <xsl:value-of select="." />
            </td>
          </tr>
        </xsl:for-each>

      </table>
    </div>
    <xsl:apply-templates select="DataTagAddress" />
  </xsl:template>

  <xsl:template match="DataTagAddress">
    <p class="tagName"></p>
    <div class="row">
      <table class="table table-striped table-bordered">
        <th colspan="2">DataTagAddress</th>

        <xsl:for-each select="*[not(local-name() = 'HardwareAddress')]">

          <tr>
            <th>
              <xsl:value-of select="local-name()" />
            </th>
            <td>
              <xsl:value-of select="." />
            </td>
          </tr>
        </xsl:for-each>
      </table>
    </div>
    
    <xsl:apply-templates select="HardwareAddress" />
  </xsl:template>

  <xsl:template match="HardwareAddress">
    <p class="tagName"></p>
    <div class="row">
      <table class="table table-striped table-bordered">
        <th colspan="2">HardwareAddress</th>

        <xsl:for-each select="*">
          <tr>
            <th>
              <xsl:value-of select="local-name()" />
            </th>
            <td>
              <xsl:value-of select="." />
            </td>
          </tr>
        </xsl:for-each>

      </table>
    </div>
  </xsl:template>

</xsl:stylesheet>