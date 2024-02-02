<%@ tag pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="switchID" required="true" %>

<%@ attribute name="onFuncName" %>
<c:set var="onFuncName" value="${(empty onFuncName) ? \"undefined\" : onFuncName}"/>

<%@ attribute name="offFuncName" %>
<c:set var="offFuncName" value="${(empty offFuncName) ? \"undefined\" : offFuncName}"/>

<%@ attribute name="state" type="java.lang.Boolean" %>
<c:set var="state" value="${(empty state) ? true : state}"/>

<%@ attribute name="width" type="java.lang.Double" %>
<c:set var="width" value="${(empty width) ? 45 : width}"/>

<%@ attribute name="height" type="java.lang.Double" %>
<c:set var="height" value="${(empty height) ? 24 : height}"/>

<%@ attribute name="dotMargin" type="java.lang.Double" %>
<c:set var="dotMargin" value="${(empty dotMargin) ? 4 : dotMargin}"/>

<%@ attribute name="disabled" type="java.lang.Boolean" %>
<c:set var="disabled" value="${(empty disabled) ? false : disabled}"/>
<%@ attribute name="style" %>

<c:set var="dotSize" value="${height-2*dotMargin}"/>
<c:set var="moveLeft" value="${width-dotSize-dotMargin*2}"/>
<span border id="${switchID}" class="toggleSwitchBasic"
      style="width: ${width}px;height: ${height}px;align-self: center;flex: none;
      <c:if test="${state}">background: var(--highlight-component-background-color-hover)</c:if>;${empty style?null:style}" component_type="toggleSwitch" move_distance="${moveLeft}"
      <c:if test="${disabled}">disabled="disabled"</c:if>
      onclick="toggleSwitch($(this),${moveLeft},${onFuncName},${offFuncName})">
    <input type="hidden" class="toggleSwitchInput" name="${switchID}" id="input_${switchID}" value="${empty state?"true":state}">
    <span class="toggleSwitchDot" style="height: ${dotSize}px;width: ${dotSize}px;<c:if
            test="${state}">left: ${moveLeft}px</c:if>"></span>
</span>