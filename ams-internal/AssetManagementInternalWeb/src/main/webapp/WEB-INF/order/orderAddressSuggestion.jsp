<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Confirm the address"/>
</jsp:include>

<div class="ams-content">
  <h1>Confirm the address</h1>
  <jsp:include page="/WEB-INF/common/orderSteps.jsp">
    <jsp:param name="step" value="2"/>
  </jsp:include>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <p class="ams-hint">
    The address validation service returned a slightly different address. Its version is usually
    the deliverable one, but it is wrong often enough on new builds and renumbered streets that
    keeping what you typed is a supported answer.
  </p>

  <div class="ams-address-compare">
    <div class="ams-panel">
      <h2>As you typed it</h2>
      <p class="ams-address">
        <s:property value="shippingAddress.addressLine1" escapeHtml="true"/><br/>
        <s:if test="shippingAddress.addressLine2 != null">
          <s:property value="shippingAddress.addressLine2" escapeHtml="true"/><br/>
        </s:if>
        <s:property value="shippingAddress.city" escapeHtml="true"/>,
        <s:property value="shippingAddress.state.code" escapeHtml="true"/>
        <s:property value="shippingAddress.zipCode" escapeHtml="true"/>
      </p>
      <s:form action="KeepAddressAsKeyed" namespace="/order" method="post">
        <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
        <s:submit value="Keep what I typed"/>
      </s:form>
    </div>

    <div class="ams-panel">
      <h2>Suggested</h2>
      <p class="ams-address">
        <s:property value="suggestedAddress.addressLine1" escapeHtml="true"/><br/>
        <s:if test="suggestedAddress.addressLine2 != null">
          <s:property value="suggestedAddress.addressLine2" escapeHtml="true"/><br/>
        </s:if>
        <s:property value="suggestedAddress.city" escapeHtml="true"/>,
        <s:property value="suggestedAddress.state.code" escapeHtml="true"/>
        <s:property value="suggestedAddress.zipCode" escapeHtml="true"/>
      </p>
      <s:form action="AcceptAddressSuggestion" namespace="/order" method="post">
        <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
        <s:submit value="Use the suggested address" cssClass="ams-primary"/>
      </s:form>
    </div>
  </div>

  <p class="ams-actions">
    <a href="${pageContext.request.contextPath}/order/OrderAddress.action">Edit the address</a>
  </p>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
