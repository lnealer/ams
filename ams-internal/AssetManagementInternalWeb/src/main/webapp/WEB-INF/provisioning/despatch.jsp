<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Despatch order"/>
</jsp:include>

<div class="ams-content">
  <h1>Despatch order</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <s:if test="order != null">
    <p class="ams-hint">
      Creating the asset is what this screen does. The tag and serial are generated here and cannot
      be changed afterwards, so check the hardware against the order before despatching.
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
        <label>Device nickname</label>
        <p><s:property value="order.deviceNickname" escapeHtml="true"/></p>
      </div>
    </div>

    <s:form action="SubmitDespatch" namespace="/provisioning" method="post">
      <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
      <s:hidden name="orderId"/>

      <div class="ams-field-grid">
        <div class="ams-field">
          <label for="asset-type">Hardware despatched</label>
          <s:select name="assetTypeCode" id="asset-type"
                    list="assetTypeOptions" listKey="code" listValue="description"
                    headerKey="" headerValue="Choose the model"/>
          <s:fielderror><s:param>assetTypeCode</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="tracking">Tracking number</label>
          <s:textfield name="trackingNumber" id="tracking" size="24" maxlength="60"
                       placeholder="1Z-AAA-111"/>
        </div>
      </div>

      <div class="ams-button-row">
        <s:submit value="Despatch" cssClass="ams-primary"/>
      </div>
    </s:form>
  </s:if>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
