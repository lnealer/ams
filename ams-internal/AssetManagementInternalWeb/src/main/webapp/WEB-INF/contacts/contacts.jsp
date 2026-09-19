<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Customer contacts"/>
</jsp:include>

<div class="ams-content">
  <h1>Customer contacts</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <table class="ams-table">
    <thead>
      <tr>
        <th scope="col">Name</th>
        <th scope="col">Role</th>
        <th scope="col">Email</th>
        <th scope="col">Telephone</th>
      </tr>
    </thead>
    <tbody>
      <s:iterator value="services">
        <tr>
          <td><s:property value="description" escapeHtml="true"/></td>
          <td><s:property value="serviceType.description" escapeHtml="true"/></td>
          <td></td>
          <td></td>
        </tr>
      </s:iterator>
    </tbody>
  </table>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
