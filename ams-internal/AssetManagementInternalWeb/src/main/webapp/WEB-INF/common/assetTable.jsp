<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%--
  A reusable asset table. Expects a request-scoped "assetRows" collection of AssetGridRow.
  Every cell goes through <c:out>, so a tag or serial containing markup is shown as text.
--%>
<table class="ams-table">
  <thead>
    <tr>
      <th scope="col">Asset tag</th>
      <th scope="col">Serial number</th>
      <th scope="col">Type</th>
      <th scope="col">Status</th>
      <th scope="col">Customer</th>
      <th scope="col">Actions</th>
    </tr>
  </thead>
  <tbody>
    <c:forEach var="row" items="${assetRows}">
      <tr class="${row.needsAttention ? 'ams-row-attention' : 'ams-row-normal'}"
          <c:if test="${row.needsAttention}">title="<c:out value='${row.attentionReason}'/>"</c:if>>
        <td><c:out value="${row.assetTag}"/></td>
        <td><c:out value="${row.serialNumber}"/></td>
        <td><c:out value="${row.assetTypeDescription}"/></td>
        <td><c:out value="${row.statusDescription}"/></td>
        <td><c:out value="${row.customerName}"/></td>
        <td>
          <%-- Built here from the identifiers rather than sent down as markup from the action. --%>
          <a href="${pageContext.request.contextPath}/assets/ViewAsset.action?assetId=${row.assetId}&amp;customerId=${row.customerId}">View</a>
        </td>
      </tr>
    </c:forEach>
    <c:if test="${empty assetRows}">
      <tr><td colspan="6" class="ams-empty">No assets to show.</td></tr>
    </c:if>
  </tbody>
</table>
