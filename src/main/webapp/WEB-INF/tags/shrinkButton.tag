<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ tag pageEncoding="utf-8" %>
<%@ attribute name="style"%>
<%@ attribute name="shrinkFuncName" required="true" %>
<%@ attribute name="expandFuncName" required="true" %>
<%@ attribute name="width"%>
<c:set var="width" value="${empty width ? '32px' : width}"/>
<%@ attribute name="height"%>
<c:set var="height" value="${empty height ? '32px' : height}"/>

<div style="width: ${width};height: ${height};display: flex;justify-content: center;align-items: center;flex: none;${style}" rounded component_type="shrinkButton" clickable onclick="
rotateShrinkButton(this,${shrinkFuncName},${expandFuncName})">
                <div style="rotate: 0deg;" component_type="shrinkButtonPointer">
                    <div></div>
                    <div></div>
                </div>
            </div>