<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page trimDirectiveWhitespaces="true" %>
<c:choose>
<c:when test='${not empty dataList}'>
<c:forEach items="${dataList}" var="item" varStatus="status">
    <c:if test="${status.index%2==0}">
                <tr class="dataRow rowOdd">
    </c:if>
    <c:if test="${status.index%2!=0}">
    <tr class="dataRow  rowEve">
    </c:if>
    <td class="categoryName"><!-- to be compatible with ie6,hidden input should be place in td element  -->
    <input type="hidden" id="pageTotal" value="${pageTotal}"/>
    <input isos="id" type="hidden" value="${item.id}" />
    <input isos="enable" type="hidden" value="${item.enable}" />
    <input isos="system" type="hidden" value="${item.system}" />
    <c:forEach items="${item.name}" var="i18Name">
    <input class="langId" lang_isos_id="${i18Name.languageId}" type="hidden" value="${i18Name.content}" />
    ${i18Name.content}<br/>
    </c:forEach>
    </td>
    <td class="categoryStatus">${item.enabledDesc}</td>
    <td class="categoryOperation moduleOperation">
    <div class="btn-group">
         <a class="btn dropdown-toggle" data-toggle="dropdown" href="#">
            <spring:message code="common.operation" />
             <span class="caret"></span>
         </a>
         <ul class="dropdown-menu">
            <li> <a href="#" onclick="showEditCategory(this);return false;"><spring:message code="edit.button" /></a></li>
            <li> <c:if test='${item.system==false}'><span ><a href="#" onclick="showRemoveCategory(this);return false;"><spring:message code="remove.button" /></a></span></c:if></li>
         </ul>
    </div>
    </td>
</tr>
</c:forEach>
</c:when>
<c:otherwise>
    <tr><td colspan="3"><spring:message code="data.norecords"/></td></tr>
</c:otherwise>
</c:choose>