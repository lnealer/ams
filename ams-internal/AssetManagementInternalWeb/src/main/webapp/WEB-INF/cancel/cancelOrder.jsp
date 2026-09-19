<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Cancel order"/>
</jsp:include>

<div class="ams-content">
  <h1>Cancel order</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <dl class="ams-detail">
    <dt>Order</dt><dd><s:property value="orderNumber" escapeHtml="true"/></dd>
    <dt>Scheduled installation</dt><dd><s:property value="scheduledInstallationDate"/></dd>
  </dl>

  <s:if test="penaltyIncurred">
    <p class="ams-warning" role="alert">
      <s:property value="penaltyWarning" escapeHtml="true"/>
    </p>
  </s:if>

  <s:form action="SubmitCancelOrder" namespace="/cancel" method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
    <s:hidden name="orderId"/>
    <s:hidden name="customerId"/>
    <label for="reason">Reason for cancelling</label>
    <s:textfield name="reason" id="reason" size="60"/>
    <s:submit value="Cancel this order" cssClass="ams-danger"/>
  </s:form>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
