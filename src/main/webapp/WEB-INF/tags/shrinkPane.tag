<%--suppress LombokSetterMayBeUsed --%>
<%@ tag pageEncoding="utf-8" %>
<%@ taglib prefix="components" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--<jsp:doBody var="body" scope="page"/>--%>
<%@ attribute name="title" required="true" %>
<%@ attribute name="useLabelTitle" type="java.lang.Boolean" %>
<c:set var="useLabel" value="${useLabelTitle == null ? false : useLabelTitle}"/>
<%@ attribute name="content" required="true" %>
<%@ attribute name="id" %>
<%@ attribute name="clazz" %>
<%@ attribute name="style" %>
<div
        <c:if test="${id!=null}">id="${id}"</c:if>
        <c:if test="${clazz!=null}">class="${clazz}"</c:if>
        style="display: flex;flex-direction: column;background: var(--input-background-color);padding: 4px;
        <c:if test="${style!=null}">${style}</c:if>" rounded>
    <div style="display: flex;flex-direction: row;align-items: center">
        <c:choose>
            <c:when test="${useLabel}">
                <label style="line-height: 52px;margin-left: 8px;">${title}</label>
                <div class="blank"></div>
            </c:when>
            <c:otherwise>
                ${title}
            </c:otherwise>
        </c:choose>
        <components:shrinkButton shrinkFuncName="shrinkDiv" expandFuncName="expandDiv" style="margin: 6px"/>
    </div>
    <div style="overflow: hidden;display: none;/*flex-direction: column;align-items: flex-start;*/">
        <div class="line"
        <%--style="height: 2px;margin: 4px;border-radius: 1px;background: rgba(128,128,128,0.2);flex: none"--%>></div>
        ${content}
    </div>
</div>