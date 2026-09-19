<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Order despatched"/>
</jsp:include>

<div class="ams-content">
  <h1>Order despatched</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <div class="ams-field-grid">
    <div class="ams-field">
      <label>Asset tag</label>
      <p><strong><s:property value="asset.assetTag" escapeHtml="true"/></strong></p>
    </div>
    <div class="ams-field">
      <label>Serial number</label>
      <p><s:property value="asset.serialNumber" escapeHtml="true"/></p>
    </div>
    <div class="ams-field">
      <label>Status</label>
      <p><s:property value="asset.assetStatusType.description" escapeHtml="true"/></p>
    </div>
  </div>

  <p class="ams-hint">
    The installation has been raised unscheduled. Book a visit on the calendar, then record it as
    completed to bring the asset into service.
  </p>

  <div class="ams-button-row">
    <s:a namespace="/provisioning" action="CompleteInstallation" cssClass="ams-primary">
      <s:param name="orderId" value="orderId"/>Record the installation</s:a>
  </div>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
