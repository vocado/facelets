<?xml version="1.0" encoding="US-ASCII"?>
<!--This file was created automatically by html2xhtml-->
<!--from the HTML stylesheets. Do not edit this file.-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:func="http://exslt.org/functions" xmlns:i="urn:cz-kosek:functions:index" xmlns="http://www.w3.org/1999/xhtml" version="1.0" exclude-result-prefixes="i">

<!-- ********************************************************************
     $Id: autoidx-ng.xsl,v 1.1 2005-07-18 22:31:03 jhook Exp $
     ********************************************************************

     This file is part of the DocBook XSL Stylesheet distribution.
     See ../README or http://docbook.sf.net/ for copyright
     and other information.

     ******************************************************************** -->

<xsl:include href="../common/autoidx-ng.xsl"/>

<!-- Modified original code is using index group codes instead of just first letter 
     to gain better grouping -->
<xsl:template name="generate-index">
  <xsl:param name="scope" select="(ancestor::book|/)[last()]"/>

  <xsl:variable name="role">
    <xsl:if test="$index.on.role != 0">
      <xsl:value-of select="@role"/>
    </xsl:if>
  </xsl:variable>

  <xsl:variable name="type">
    <xsl:if test="$index.on.type != 0">
      <xsl:value-of select="@type"/>
    </xsl:if>
  </xsl:variable>

  <xsl:variable name="terms" select="//indexterm[count(.|key('group-code',                                                 i:group-index(normalize-space(concat(primary/@sortas, primary[not(@sortas)]))))[count(ancestor::node()|$scope) = count(ancestor::node())                 and ($role = @role or $type = @type or                 (string-length($role) = 0 and string-length($type) = 0))][1]) = 1                                     and not(@class = 'endofrange')]"/>

  <div class="index">
    <xsl:apply-templates select="$terms" mode="index-div">
      <xsl:with-param name="scope" select="$scope"/>
      <xsl:with-param name="role" select="$role"/>
      <xsl:with-param name="type" select="$type"/>
      <xsl:sort select="i:group-index(normalize-space(concat(primary/@sortas, primary[not(@sortas)])))" data-type="number"/>
    </xsl:apply-templates>
  </div>
</xsl:template>

<xsl:template match="indexterm" mode="index-div">
  <xsl:param name="scope" select="."/>
  <xsl:param name="role" select="''"/>
  <xsl:param name="type" select="''"/>

  <xsl:variable name="key" select="i:group-index(normalize-space(concat(primary/@sortas, primary[not(@sortas)])))"/>

  <xsl:variable name="lang">
    <xsl:call-template name="l10n.language"/>
  </xsl:variable>

  <xsl:if test="key('group-code', $key)[count(ancestor::node()|$scope) = count(ancestor::node())                 and ($role = @role or $type = @type or                 (string-length($role) = 0 and string-length($type) = 0))]                 [count(.|key('primary', normalize-space(concat(primary/@sortas, primary[not(@sortas)])))[count(ancestor::node()|$scope) = count(ancestor::node())                 and ($role = @role or $type = @type or                 (string-length($role) = 0 and string-length($type) = 0))][1]) = 1]">
    <div class="indexdiv">
      <h3>
        <xsl:value-of select="i:group-letter($key)"/>
      </h3>
      <dl>
        <xsl:apply-templates select="key('group-code', $key)[count(ancestor::node()|$scope) = count(ancestor::node())                 and ($role = @role or $type = @type or                 (string-length($role) = 0 and string-length($type) = 0))]                                      [count(.|key('primary', normalize-space(concat(primary/@sortas, primary[not(@sortas)])))[count(ancestor::node()|$scope) = count(ancestor::node())                 and ($role = @role or $type = @type or                 (string-length($role) = 0 and string-length($type) = 0))][1])=1]" mode="index-primary">
          <xsl:sort select="normalize-space(concat(primary/@sortas, primary[not(@sortas)]))" lang="{$lang}"/>
          <xsl:with-param name="scope" select="$scope"/>
          <xsl:with-param name="role" select="$role"/>
          <xsl:with-param name="type" select="$type"/>
        </xsl:apply-templates>
      </dl>
    </div>
  </xsl:if>
</xsl:template>

</xsl:stylesheet>
