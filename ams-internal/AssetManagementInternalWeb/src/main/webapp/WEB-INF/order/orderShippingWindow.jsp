<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Despatch window"/>
</jsp:include>

<div class="ams-content">
  <h1>Despatch window</h1>
  <jsp:include page="/WEB-INF/common/orderSteps.jsp">
    <jsp:param name="step" value="6"/>
  </jsp:include>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <s:if test="regionUnmapped">
    <div class="ams-warning" role="status">
      <p>
        No warehouse region covers the destination ZIP code
        <strong><s:property value="shippingAddress.zipCode" escapeHtml="true"/></strong>, so no
        despatch window can be offered. The order can still be placed and operations will assign a
        window by hand.
      </p>
    </div>
  </s:if>
  <s:elseif test="shippingWindows == null || shippingWindows.isEmpty()">
    <div class="ams-warning" role="status">
      <p>
        The <s:property value="shippingRegion" escapeHtml="true"/> warehouse has no capacity left
        in the current booking horizon. The order can still be placed and operations will assign a
        window when one opens up.
      </p>
    </div>
  </s:elseif>

  <s:if test="shippingWindows != null && !shippingWindows.isEmpty()">
    <p class="ams-hint">
      Windows served by the <s:property value="shippingRegion" escapeHtml="true"/> warehouse.
      The window is not held while you look at it - it is reserved when the order is placed, so
      if it fills in the meantime the order still goes through and operations assign another.
    </p>
  </s:if>

  <s:form action="SaveShippingWindow" namespace="/order" method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>

    <s:if test="shippingWindows != null && !shippingWindows.isEmpty()">
      <fieldset>
        <legend>Choose a window</legend>
        <div class="ams-table-scroll">
          <table class="ams-table">
            <thead>
              <tr>
                <th scope="col"></th>
                <th scope="col">Date</th>
                <th scope="col">Window</th>
                <th scope="col">Places left</th>
              </tr>
            </thead>
            <tbody>
              <s:iterator value="shippingWindows" var="window">
                <tr>
                  <td>
                    <input type="radio" name="shippingWindowTimeslotId"
                           id="win-<s:property value='#window.timeslotId'/>"
                           value="<s:property value='#window.timeslotId'/>"
                           <s:if test="shippingWindowTimeslotId == #window.timeslotId">checked="checked"</s:if>/>
                  </td>
                  <td>
                    <label for="win-<s:property value='#window.timeslotId'/>">
                      <s:date name="#window.startTime" format="EEEE d MMMM yyyy"/>
                    </label>
                  </td>
                  <td><s:property value="#window.displayLabel" escapeHtml="true"/></td>
                  <td><s:property value="#window.remainingCapacity"/></td>
                </tr>
              </s:iterator>
            </tbody>
          </table>
        </div>
        <s:fielderror><s:param>shippingWindowTimeslotId</s:param></s:fielderror>
      </fieldset>
    </s:if>

    <fieldset>
      <legend>Due diligence</legend>
      <p class="ams-check">
        <s:checkbox name="dueDiligenceCompleted" id="dd"/>
        <label for="dd">Due diligence has been completed</label>
      </p>
      <s:fielderror><s:param>dueDiligenceCompleted</s:param></s:fielderror>
      <c:if test="${pageContext.request.isUserInRole('INT_OVERRIDE_DUE_DILIGENCE')}">
        <div class="ams-field">
          <label for="waiver">Or give a reason for waiving it</label>
          <s:textfield name="dueDiligenceWaiverReason" id="waiver" size="60" maxlength="400"/>
          <s:fielderror><s:param>dueDiligenceWaiverReason</s:param></s:fielderror>
        </div>
      </c:if>
    </fieldset>

    <fieldset>
      <legend>Anything else</legend>
      <div class="ams-field">
        <label for="comments">Comments</label>
        <s:textarea name="comments" id="comments" rows="3" cols="60"/>
      </div>
    </fieldset>

    <%--
      This is the last screen: there is no review step, so the button that says "Place order"
      places it. The form posts to SaveShippingWindow, which records the choice and chains
      straight into PlaceOrder inside the same request.
    --%>
    <%--
      The secondary buttons use formaction rather than <s:submit action="...">. That tag renders
      name="action:SkipShippingWindow", which Struts only honours when
      struts.mapper.action.prefix.enabled is on - and it is off by default in Struts 6, for the
      same reason dynamic method invocation is off here. Left as it was, the button would silently
      submit the form's own action instead. formaction posts the whole form, CSRF token included,
      to a different URL.
    --%>
    <div class="ams-button-row">
      <s:if test="shippingWindows != null && !shippingWindows.isEmpty()">
        <s:submit value="Place order" cssClass="ams-primary"/>
      </s:if>
      <s:else>
        <button type="submit" class="ams-primary"
                formaction="${pageContext.request.contextPath}/order/SkipShippingWindow.action">
          Place order without a window
        </button>
      </s:else>
      <button type="submit"
              formaction="${pageContext.request.contextPath}/order/SaveForLater.action">Save for later</button>
    </div>
  </s:form>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
