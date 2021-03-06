<%--
property			value							comment
.datas				对象列表							列表数据
grid.<columnName>	[columnType]<sub items>			columnName为列名。
													columnType为列类型，可选值为：
													plain:　文本输出
													hidden: input type=hidden
													dict: dictionary表的输出
													button: 按钮输出。
													sub items为该列所包含的所有子项，由逗号分割，
													每一项都为.data项指定的列表中的对象的属性。
													如果sub items为空， 则sub item为<columnName>
<columnName>.label	string							列标题。如果没有使用此属性，则使用默认的key为
													<columnName>.label的国际化信息
<columnName>.value	html code		[String类型需加 空格 "${name} "]				列输出。如果没有使用此属性，则使用列表数据中
													对应key为<columnName>的值
<button>.onclick	javascript code					点击按钮触发事件
<dict>.options		列表								dict列的可选dictionary对象列表

example:
	<jsp:useBean id="gridMap" class="java.util.LinkedHashMap" scope="request" />
	<c:set target="${gridMap}" property="grid.username" value="[plain]"/>
	<c:set target="${gridMap}" property="grid.status" value="[dict]"/>
	<c:set target="${gridMap}" property="grid.operation" value="[button]test,delete"/>
	<c:set target="${gridMap}" property="status.options" value="/"/>
	<c:set target="${gridMap}" property="test.label" value="测试"/>
	<c:set target="${gridMap}" property="test.onclick" value="alert('test')"/>
	<c:set target="${gridMap}" property="delete.onclick" value="alert(${username})"/>
	<c:set target="${gridMap}" property=".datas" value="users"/>
	<jsp:include page="/WEB-INF/views/templates/grid.jsp" >
		<jsp:param name="grid.configuration" value="gridMap"></jsp:param>
	</jsp:include>
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="f" uri="/WEB-INF/tlds/functions.tld" %>

<c:set value="${requestScope[param['grid.configuration']]}" var="conf"></c:set>

<jsp:useBean id="map" class="java.util.LinkedHashMap"></jsp:useBean>
<c:forEach items="${conf}" var="p" >
	<c:if test="${fn:startsWith(p.key, 'grid.')}">
		<c:set value="${fn:substringAfter(p.key, 'grid.')}" var="item"></c:set>
		<c:set target="${map}" property="${item}" value="${p.value}"></c:set>
	</c:if>
</c:forEach>
<c:if test="${conf['.forPager'] != null && conf['.forPager'] == true}">
    <table style="width:100%">
</c:if>
<c:set var="colenth" value="${fn:length(map)}"/>
<thead class="headerRow"><tr>
    <c:forEach items="${map}" var="item" >
        <c:choose>
            <c:when test="${fn:startsWith(item.value, '[hidden]')}">
                <th style="display: none;"></th>
            </c:when>
            <c:otherwise>
                <th>
                    ${f:label(conf[f:append(item.key, '.label')], f:append(item.key, '.label'))}
                </th>
            </c:otherwise>
        </c:choose>
    </c:forEach>
</tr></thead>
<tbody>
    <c:choose>
    <c:when test="${conf['.datas']!= null && fn:length(conf['.datas'])>0}">
    <c:forEach items="${conf['.datas']}" var="d" varStatus="status">
        <c:if test="${status.index%2 == 0}">
            <tr class="dataRow rowOdd">
        </c:if>
        <c:if test="${status.index%2 != 0}">
            <tr class="dataRow rowEve">
        </c:if>
            <c:forEach items="${map}" var="item">
                    <c:if test="${fn:startsWith(item.value, '[plain]')}">
                        <td>
                            <c:set value="${fn:replace(item.value, '[plain]', '')}" var="subItems"></c:set>
                            <c:choose>
                                <c:when test="${fn:length(subItems) == 0}">
                                ${f:propStr(null, d) }
                                    <span name="${item.key}">${f:value(f:propStr(conf[f:append(item.key, '.value')], d), f:getProp(d, item.key))}</span>
                                </c:when>
                                <c:otherwise>
                                    <c:forTokens items="${subItems}" delims="," var="key">
                                        <span name="${key}">${f:value(f:propStr(conf[f:append(key, '.value')], d), f:getProp(d, key))}</span>
                                    </c:forTokens>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </c:if>
                    <c:if test="${fn:startsWith(item.value, '[money]')}">
                        <td>
                            <c:set value="${fn:replace(item.value, '[plain]', '')}" var="subItems"></c:set>
                            <c:choose>
                                <c:when test="${fn:length(subItems) == 0}">
                                    <c:set var="value" value="${f:value(f:propStr(conf[f:append(item.key, '.value')], d), f:getProp(d, item.key))}"></c:set>
                                    <span>${f:money(value)}</span>
                                </c:when>
                                <c:otherwise>
                                    <c:forTokens items="${subItems}" delims="," var="key">
                                        <c:set var="value" value="${f:value(f:propStr(conf[f:append(key, '.value')], d), f:getProp(d, key))}"></c:set>
                                        <span>${f:money(value)}</span>
                                    </c:forTokens>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </c:if>
                    <c:if test="${fn:startsWith(item.value, '[hidden]')}">
                        <td style="display: none;">
                            <c:set value="${fn:replace(item.value, '[hidden]', '')}" var="subItems"></c:set>
                            <c:choose>
                                <c:when test="${fn:length(subItems) == 0}">
                                    <input type="hidden" name="${item.key}" id="${item.key}" value="${f:value(f:propStr(conf[f:append(item.key, '.value')], d), f:getProp(d, item.key))}"/>
                                </c:when>
                                <c:otherwise>
                                    <c:forTokens items="${subItems}" delims="," var="key">
                                        <input type="hidden" name="${key}" id="${key}" value="${f:value(f:propStr(conf[f:append(key, '.value')], d), f:getProp(d, key))}"/>
                                    </c:forTokens>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </c:if>
                    <c:if test="${fn:startsWith(item.value, '[dict]')}">
                        <td>
                            <c:set value="${fn:replace(item.value, '[dict]', '')}" var="subItems"></c:set>
                            <c:choose>
                                <c:when test="${fn:length(subItems) == 0}">
                                    <c:choose>
                                        <c:when test="${conf[f:append(item.key, '.value')] != null}">
                                            ${f:propStr(conf[f:append(item.key, '.value')], d)}
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach items="${conf[f:append(item.key, '.options')]}" var="dict">
                                                <c:if test="${f:append(dict.code, '') == f:getProp(d, item.key)}">
                                                    ${dict.value}
                                                </c:if>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>
                                    <c:forTokens items="${subItems}" delims="," var="key">
                                        <c:choose>
                                            <c:when test="${conf[f:append(key, '.value')] != null}">
                                                ${f:propStr(conf[f:append(key, '.value')], d)}
                                            </c:when>
                                            <c:otherwise>
                                                <c:forEach items="${conf[f:append(key, '.options')]}" var="dict">
                                                    <c:if test="${dict.code == f:getProp(d, key)}">
                                                        ${dict.value}
                                                    </c:if>
                                                </c:forEach>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forTokens>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </c:if>
                    <c:if test="${fn:startsWith(item.value, '[button]')}">
                        <td class="operationBtns"><div class="btn-group">
                                        <a class="btn dropdown-toggle" data-toggle="dropdown" href="#">
                                           <spring:message code="common.operation" />
                                            <span class="caret"></span>
                                          </a>
                                          <ul class="dropdown-menu">
                                         
                            <c:set value="${fn:replace(item.value, '[button]', '')}" var="subItems"></c:set>
                            <c:choose>
                                <c:when test="${fn:length(subItems) == 0}">
                                    <c:choose>
                                        <c:when test="${conf[f:append(item.key, '.value')] != null}">
                                             <li>${f:propStr(conf[f:append(item.key, '.value')], d)}</li>
                                        </c:when>
                                        <c:otherwise>
                                        <li name="${item.key}">
                                            <a href="#" onclick="${f:propStr(conf[f:append(item.key, '.onclick')], d)};return false;" >
                                                    ${f:label(conf[f:append(item.key, '.label')], f:append(item.key, '.label'))}
                                            </a>
                                        </li>
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>
                                    <c:forTokens items="${subItems}" delims="," var="key">
                                        <c:choose>
                                            <c:when test="${conf[f:append(key, '.value')] != null}">
                                                 <li>${f:propStr(conf[f:append(key, '.value')], d)}</li>
                                            </c:when>
                                            <c:otherwise>
                                            <li name="${key}">
                                            <a href="#" onclick="${f:propStr(conf[f:append(key, '.onclick')], d)};return false;" >
                                                    ${f:label(conf[f:append(key, '.label')], f:append(key, '.label'))}
                                            </a>
                                            </li>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forTokens>
                                </c:otherwise>
                            </c:choose>
                              </ul>
                                        </div>
                        </td>
                    </c:if>
            </c:forEach>
        </tr>
    </c:forEach>
     </c:when>
     <c:otherwise>
        <tr><td colspan="${colenth}"><spring:message code="data.norecords"/></td></tr>
     </c:otherwise>
    </c:choose>
</tbody>
<c:if test="${conf['.forPager'] != null && conf['.forPager'] == true}">
</table>
</c:if>
