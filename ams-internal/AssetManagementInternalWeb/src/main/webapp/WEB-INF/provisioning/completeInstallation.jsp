<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Record installation"/>
</jsp:include>

<div class="ams-content">
  <h1>Record installation</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <s:if test="order != null">
    <p class="ams-hint">
      This brings the asset into service: its status becomes active and the stored configuration is
      marked as applied, which is what the compare screen checks the device against from then on.
    </p>

    <div class="ams-field-grid">
      <div class="ams-field">
        <label>Order</label>
        <p><s:property value="order.orderNumber" escapeHtml="true"/></p>
      </div>
      <div class="ams-field">
        <label>Status</label>
        <p><s:property value="order.orderStatusType.description" escapeHtml="true"/></p>
      </div>
      <div class="ams-field">
        <label>Asset</label>
        <p><s:property value="order.asset.assetTag" escapeHtml="true"/></p>
      </div>
    </div>

    <s:form action="SubmitCompleteInstallation" namespace="/provisioning" method="post">
      <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
      <s:hidden name="orderId"/>
      <div class="ams-field-grid">
        <div class="ams-field">
          <label for="tech">Engineer</label>
          <s:textfield name="technicianName" id="tech" size="24" maxlength="120"
                       placeholder="R. Alvarez"/>
        </div>
        <div class="ams-field">
          <label for="notes">Notes</label>
          <s:textfield name="notes" id="notes" size="40" maxlength="400"
                       placeholder="Mounted above the roaster control panel"/>
        </div>
      </div>
      <div class="ams-button-row">
        <s:submit value="Record as completed" cssClass="ams-primary"/>
      </div>
    </s:form>
  </s:if>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
