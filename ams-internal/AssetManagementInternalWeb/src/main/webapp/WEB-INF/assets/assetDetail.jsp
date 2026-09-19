<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Asset"/>
</jsp:include>

<div class="ams-content">
  <h1>Asset</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <s:if test="asset != null">
    <s:if test="statusExplanation != null">
      <p class="ams-warning"><s:property value="statusExplanation" escapeHtml="true"/></p>
    </s:if>

    <dl class="ams-detail">
      <dt>Asset tag</dt><dd><s:property value="asset.displayTag" escapeHtml="true"/></dd>
      <dt>Serial number</dt><dd><s:property value="asset.serialNumber" escapeHtml="true"/></dd>
      <dt>Type</dt><dd><s:property value="asset.assetType.description" escapeHtml="true"/></dd>
      <dt>Status</dt><dd><s:property value="asset.assetStatusType.description" escapeHtml="true"/></dd>
      <dt>Customer</dt><dd><s:property value="asset.customer.displayName" escapeHtml="true"/></dd>
    </dl>

    <h2>Actions</h2>
    <ul class="ams-actions">
      <s:iterator value="availableActions" var="assetAction">
        <li><s:property value="#assetAction.description" escapeHtml="true"/></li>
      </s:iterator>
      <s:if test="availableActions.isEmpty()">
        <li class="ams-empty">No actions are available for this asset.</li>
      </s:if>
    </ul>
  </s:if>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
