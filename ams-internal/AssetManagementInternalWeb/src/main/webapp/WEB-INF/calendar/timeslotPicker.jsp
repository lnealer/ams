<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Choose a time"/>
</jsp:include>

<div class="ams-content">
  <h1>Choose a time</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <%--
    The slots themselves are fetched as JSON as the user moves through the calendar; this is the
    container the picker renders into.
  --%>
  <div class="ams-timeslot-picker"
       data-slots-url="${pageContext.request.contextPath}/calendar/InstallationTimeSlots.action"
       data-ajax-token="<c:out value='${ajaxToken}'/>">
    Loading available times&hellip;
  </div>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
