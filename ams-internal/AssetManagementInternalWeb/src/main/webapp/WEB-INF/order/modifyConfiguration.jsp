<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Modify configuration"/>
</jsp:include>

<div class="ams-content">
  <h1>Modify configuration</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <s:if test="configuration != null">
    <dl class="ams-detail">
      <dt>LAN address</dt><dd><s:property value="configuration.lanIpAddress" escapeHtml="true"/></dd>
      <dt>LAN mask</dt><dd><s:property value="configuration.lanSubnetMask" escapeHtml="true"/></dd>
      <dt>WAN address</dt><dd><s:property value="configuration.wanIpAddress" escapeHtml="true"/></dd>
      <dt>Gateway</dt><dd><s:property value="configuration.defaultGateway" escapeHtml="true"/></dd>
      <dt>Revision</dt><dd><s:property value="configuration.revision"/></dd>
    </dl>

    <h2>Ports</h2>
    <table class="ams-table">
      <thead>
        <tr><th scope="col">Port</th><th scope="col">Speed</th><th scope="col">Active</th></tr>
      </thead>
      <tbody>
        <s:iterator value="ports">
          <tr>
            <td><s:property value="portName" escapeHtml="true"/></td>
            <td><s:property value="portConfigurationType.description" escapeHtml="true"/></td>
            <td><s:property value="active"/></td>
          </tr>
        </s:iterator>
      </tbody>
    </table>
  </s:if>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
