<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Subscriber PCs"/>
</jsp:include>

<div class="ams-content">
  <h1>Subscriber PCs</h1>
  <jsp:include page="/WEB-INF/common/orderSteps.jsp">
    <jsp:param name="step" value="5"/>
  </jsp:include>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <%-- What was filled in automatically, and where each value came from. Shown rather than applied
       silently: every one of these is a starting point the operator is expected to check, and a
       prefilled field that nobody questions is worse than an empty one. --%>
  <s:if test="suggestionNotes != null && !suggestionNotes.isEmpty()">
    <div class="ams-suggested" role="note">
      <p class="ams-suggested-head">Filled in for you</p>
      <ul>
        <s:iterator value="suggestionNotes" var="note">
          <li><s:property value="#note" escapeHtml="true"/></li>
        </s:iterator>
      </ul>
    </div>
  </s:if>

  <p class="ams-hint">
    The machines that will sit behind the device. List at least one. Anything needing a fixed
    address has to be recorded here so that address can be reserved out of the DHCP pool before
    the device is staged - a static address discovered on the day is the commonest reason an
    install fails.
  </p>

  <s:form action="SaveSubscriberPcs" namespace="/order" method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>

    <div class="ams-table-scroll">
      <table class="ams-table ams-grid">
        <thead>
          <tr>
            <th scope="col">#</th>
            <th scope="col">Host name</th>
            <th scope="col">Type</th>
            <th scope="col">Operating system</th>
            <th scope="col">MAC address</th>
            <th scope="col">IP address</th>
            <th scope="col">Static</th>
            <th scope="col">Users</th>
            <th scope="col">Notes</th>
          </tr>
        </thead>
        <tbody>
          <s:iterator value="subscriberPcRows" status="row">
            <tr>
              <td class="ams-grid-index"><s:property value="#row.index + 1"/></td>
              <td>
                <s:textfield name="subscriberPcs[%{#row.index}].hostName" size="16" maxlength="60" placeholder="till-01" />
                <s:fielderror><s:param>subscriberPcs[%{#row.index}].hostName</s:param></s:fielderror>
              </td>
              <td>
                <s:select name="subscriberPcs[%{#row.index}].pcTypeCode"
                          list="subscriberPcTypeOptions" listKey="code" listValue="description"
                          headerKey="" headerValue="-"/>
                <s:fielderror><s:param>subscriberPcs[%{#row.index}].subscriberPcType</s:param></s:fielderror>
              </td>
              <td><s:textfield name="subscriberPcs[%{#row.index}].operatingSystem" size="14" maxlength="60" placeholder="Windows 11 Pro" /></td>
              <td>
                <s:textfield name="subscriberPcs[%{#row.index}].macAddress" size="18" maxlength="20" placeholder="B4:2E:99:10:00:01" />
                <s:fielderror><s:param>subscriberPcs[%{#row.index}].macAddress</s:param></s:fielderror>
              </td>
              <td>
                <s:textfield name="subscriberPcs[%{#row.index}].ipAddress" size="14" maxlength="15" placeholder="192.168.10.11" />
                <s:fielderror><s:param>subscriberPcs[%{#row.index}].ipAddress</s:param></s:fielderror>
              </td>
              <td class="ams-grid-check">
                <s:checkbox name="subscriberPcs[%{#row.index}].staticAddress"/>
              </td>
              <td>
                <s:textfield name="subscriberPcs[%{#row.index}].userCount" size="4" placeholder="2" />
                <s:fielderror><s:param>subscriberPcs[%{#row.index}].userCount</s:param></s:fielderror>
              </td>
              <td><s:textfield name="subscriberPcs[%{#row.index}].notes" size="20" maxlength="400" placeholder="Front counter till" /></td>
            </tr>
          </s:iterator>
        </tbody>
      </table>
    </div>

    <div class="ams-button-row">
      <s:submit value="Continue to despatch window" cssClass="ams-primary"/>
      <%--
        formaction, not <s:submit action="...">. That tag renders name="action:SaveForLater",
        which Struts only honours when struts.mapper.action.prefix.enabled is on - and it is off
        by default in Struts 6, for the same reason dynamic method invocation is off here. Left as
        it was, the button would silently submit the form's own action instead. formaction posts
        the whole form, CSRF token included, to a different URL.
      --%>
      <button type="submit"
              formaction="${pageContext.request.contextPath}/order/AddSubscriberPcRows.action">Add more rows</button>
      <button type="submit"
              formaction="${pageContext.request.contextPath}/order/SaveForLater.action">Save for later</button>
    </div>
  </s:form>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
