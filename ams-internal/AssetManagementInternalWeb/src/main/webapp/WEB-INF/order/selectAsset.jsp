<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/common/header.jsp">
  <jsp:param name="pageTitle" value="Select the asset to replace"/>
</jsp:include>

<div class="ams-content">
  <h1>Select the asset to replace</h1>
  <jsp:include page="/WEB-INF/common/messages.jsp"/>

  <p>Only assets with no other work in flight against them can be replaced.</p>

  <s:form action="SelectAsset" namespace="/order" method="post">
    <jsp:include page="/WEB-INF/common/csrfToken.jsp"/>
    <table class="ams-table">
      <thead>
        <tr>
          <th scope="col"></th>
          <th scope="col">Asset tag</th>
          <th scope="col">Serial number</th>
          <th scope="col">Type</th>
        </tr>
      </thead>
      <tbody>
        <s:iterator value="selectableAssets" var="candidate">
          <tr>
            <td>
              <input type="radio" name="selectedAssetId"
                     value="<s:property value='#candidate.assetId'/>"/>
            </td>
            <td><s:property value="#candidate.displayTag" escapeHtml="true"/></td>
            <td><s:property value="#candidate.serialNumber" escapeHtml="true"/></td>
            <td><s:property value="#candidate.assetType.description" escapeHtml="true"/></td>
          </tr>
        </s:iterator>
        <s:if test="selectableAssets.isEmpty()">
          <tr><td colspan="4" class="ams-empty">
            This customer has no assets eligible for replacement.
          </td></tr>
        </s:if>
      </tbody>
    </table>
    <s:submit value="Continue" cssClass="ams-primary"/>
  </s:form>
</div>

<jsp:include page="/WEB-INF/common/footer.jsp"/>
