<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Ordering"/>
</jsp:include>

<div class="ams-content">
  <h1>Ordering</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <form class="ams-inline-form" id="ams-ordering-form"
        action="${pageContext.request.contextPath}/customer/UpdateCanSubmitOrders.action"
        method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
    <input type="hidden" name="customerId" value="<s:property value='customerId'/>"/>
    <%-- The per-session AJAX token; AjaxJsonTokenInterceptor rejects the post without it. --%>
    <input type="hidden" name="ajaxToken" value="<c:out value='${ajaxToken}'/>"/>
    <label>
      <input type="checkbox" name="canSubmitOrders" value="true"
             <s:if test="canSubmitOrders">checked="checked"</s:if>/>
      This customer may submit orders
    </label>
    <button type="submit" class="ams-primary">Save</button>
  </form>
  <s:if test="confirmationMessage != null">
    <p class="ams-messages"><s:property value="confirmationMessage" escapeHtml="true"/></p>
  </s:if>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
