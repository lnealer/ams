<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
  <dl class="ams-detail">
    <dt>Order type</dt><dd><s:property value="orderType.description" escapeHtml="true"/></dd>
    <dt>Asset</dt><dd><s:property value="selectedAsset.displayTag" escapeHtml="true"/></dd>
    <dt>Shipping to</dt>
    <dd><pre class="ams-address"><s:property value="shippingAddress" escapeHtml="true"/></pre></dd>
    <dt>Carrier</dt><dd><s:property value="shippingCarrier.description" escapeHtml="true"/></dd>
  </dl>