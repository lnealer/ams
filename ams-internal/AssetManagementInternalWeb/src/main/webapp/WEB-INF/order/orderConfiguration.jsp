<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="External configuration"/>
</jsp:include>

<div class="ams-content">
  <h1>External configuration</h1>
  <jsp:include page="/WEB-INF/common/orderSteps.jsp">
    <jsp:param name="step" value="4"/>
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
    The device is staged in the warehouse from this configuration and arrives on site already
    addressed, so both sides are keyed now rather than by the engineer on the day. Everything here
    is validated before you can continue: a transposed digit found on site is a wasted visit.
  </p>

  <s:form action="SaveConfiguration" namespace="/order" method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>

    <fieldset>
      <legend>WAN &mdash; the carrier facing side</legend>
      <div class="ams-field">
        <label for="netcfg">Addressing</label>
        <s:select name="networkConfigurationCode" id="netcfg"
                  list="networkConfigurationOptions" listKey="code" listValue="description"/>
      </div>
      <p class="ams-hint">
        With DHCP or PPPoE the carrier assigns the address at connection time, so the WAN fields
        below are left blank and are not checked.
      </p>
      <div class="ams-field-grid">
        <div class="ams-field">
          <label for="wan-ip">WAN IP address</label>
          <s:textfield name="assetConfiguration.wanIpAddress" id="wan-ip" placeholder="203.0.113.10" size="18" maxlength="15"/>
        </div>
        <div class="ams-field">
          <label for="wan-mask">WAN subnet mask</label>
          <s:textfield name="assetConfiguration.wanSubnetMask" id="wan-mask" placeholder="255.255.255.248" size="18" maxlength="15"/>
        </div>
        <div class="ams-field">
          <label for="wan-gw">Default gateway</label>
          <s:textfield name="assetConfiguration.defaultGateway" id="wan-gw" placeholder="203.0.113.9" size="18" maxlength="15"/>
        </div>
        <div class="ams-field">
          <label for="dns1">Primary DNS</label>
          <s:textfield name="assetConfiguration.primaryDnsAddress" id="dns1" placeholder="9.9.9.9" size="18" maxlength="15"/>
        </div>
        <div class="ams-field">
          <label for="dns2">Secondary DNS</label>
          <s:textfield name="assetConfiguration.secondaryDnsAddress" id="dns2" placeholder="149.112.112.112" size="18" maxlength="15"/>
        </div>
        <div class="ams-field">
          <label for="circuit">Circuit ID</label>
          <s:textfield name="assetConfiguration.circuitId" id="circuit" placeholder="CKT-00099" size="24" maxlength="60"/>
        </div>
        <div class="ams-field">
          <label for="bandwidth">Bandwidth (Kbps)</label>
          <s:textfield name="assetConfiguration.bandwidthKbps" id="bandwidth" placeholder="100000" size="10"/>
          <s:fielderror><s:param>assetConfiguration.bandwidthKbps</s:param></s:fielderror>
        </div>
      </div>
      <s:fielderror><s:param>assetConfiguration.wanIpAddress</s:param></s:fielderror>
    </fieldset>

    <fieldset>
      <legend>LAN &mdash; the site side</legend>
      <div class="ams-field-grid">
        <div class="ams-field">
          <label for="lan-type">LAN type</label>
          <s:select name="configurationTypeCode" id="lan-type"
                    list="configurationTypeOptions" listKey="code" listValue="description"/>
          <s:fielderror><s:param>assetConfiguration.assetConfigurationType</s:param></s:fielderror>
        </div>
        <div class="ams-field">
          <label for="lan-ip">LAN IP address</label>
          <s:textfield name="assetConfiguration.lanIpAddress" id="lan-ip" placeholder="192.168.10.1" size="18" maxlength="15"/>
        </div>
        <div class="ams-field">
          <label for="lan-mask">LAN subnet mask</label>
          <s:textfield name="assetConfiguration.lanSubnetMask" id="lan-mask" placeholder="255.255.255.0" size="18" maxlength="15"/>
        </div>
        <div class="ams-field">
          <label for="lan-gw">LAN gateway</label>
          <s:textfield name="assetConfiguration.lanGateway" id="lan-gw" placeholder="192.168.10.254" size="18" maxlength="15"/>
        </div>
      </div>
      <p class="ams-hint">
        The LAN IP address is the device's own address on the site network; the LAN gateway is the
        customer's own router, a different box, and the two may not be the same address. LAN type A
        expects the device at the first usable address of the subnet and the customer's router at
        the last. The subnet also has to be big enough for the machines listed on the next step.
      </p>
      <s:fielderror><s:param>assetConfiguration.lanIpAddress</s:param></s:fielderror>
    </fieldset>

    <div class="ams-button-row">
      <s:submit value="Continue to subscriber PCs" cssClass="ams-primary"/>
      <%--
        formaction, not <s:submit action="...">. That tag renders name="action:SaveForLater",
        which Struts only honours when struts.mapper.action.prefix.enabled is on - and it is off
        by default in Struts 6, for the same reason dynamic method invocation is off here. Left as
        it was, the button would silently submit the form's own action instead. formaction posts
        the whole form, CSRF token included, to a different URL.
      --%>
      <button type="submit"
              formaction="${pageContext.request.contextPath}/order/SaveForLater.action">Save for later</button>
    </div>
  </s:form>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
