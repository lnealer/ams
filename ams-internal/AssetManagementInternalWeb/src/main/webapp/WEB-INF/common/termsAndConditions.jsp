<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Terms and conditions"/>
</jsp:include>

<div class="ams-content">
  <h1>Terms and conditions</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <p>Version <s:property value="version" escapeHtml="true"/>.</p>

  <s:if test="acceptanceRequired">
    <p class="ams-warning">You have not accepted the current version.</p>
    <s:form action="AcceptTerms" namespace="/termsAndConditions" method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
      <s:submit value="Accept" cssClass="ams-primary"/>
    </s:form>
  </s:if>
  <s:else>
    <p class="ams-messages">You have accepted the current version.</p>
  </s:else>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
