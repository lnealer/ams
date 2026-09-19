<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Address updated"/>
</jsp:include>

<div class="ams-content">
  <h1>Address updated</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <p>The installation address has been updated.</p>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
