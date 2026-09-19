<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Address not verified"/>
</jsp:include>

<div class="ams-content">
  <h1>Address not verified</h1>
  <jsp:include page="/WEB-INF/common/orderSteps.jsp">
    <jsp:param name="step" value="2"/>
  </jsp:include>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <div class="ams-warning" role="status">
    <p>
      The address validation service could not be reached, so this address has not been checked.
      The order can still go ahead - it will be marked as unverified so the warehouse knows nobody
      confirmed it.
    </p>
  </div>

  <div class="ams-panel">
    <h2>The address as you typed it</h2>
    <p class="ams-address">
      <s:if test="shippingAddress.attentionTo != null">
        ATTN: <s:property value="shippingAddress.attentionTo" escapeHtml="true"/><br/>
      </s:if>
      <s:property value="shippingAddress.addressLine1" escapeHtml="true"/><br/>
      <s:if test="shippingAddress.addressLine2 != null">
        <s:property value="shippingAddress.addressLine2" escapeHtml="true"/><br/>
      </s:if>
      <s:property value="shippingAddress.city" escapeHtml="true"/>,
      <s:property value="shippingAddress.state.code" escapeHtml="true"/>
      <s:property value="shippingAddress.zipCode" escapeHtml="true"/>
    </p>
  </div>

  <div class="ams-button-row">
    <s:form action="KeepAddressAsKeyed" namespace="/order" method="post">
      <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
      <s:submit value="Continue with this address" cssClass="ams-primary"/>
    </s:form>
    <a class="ams-actions" href="${pageContext.request.contextPath}/order/OrderAddress.action">
      Edit the address
    </a>
  </div>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
