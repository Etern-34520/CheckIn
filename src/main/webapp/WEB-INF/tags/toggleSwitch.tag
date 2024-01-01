<%@ tag pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="toggleSwitchId" required="true" %>

<%@ attribute name="toggleSwitchOn" %>
<c:set var="toggleSwitchOn" value="${(empty toggleSwitchOn) ? \"undefined\" : toggleSwitchOn}"/>

<%@ attribute name="toggleSwitchOff" %>
<c:set var="toggleSwitchOff" value="${(empty toggleSwitchOff) ? \"undefined\" : toggleSwitchOff}"/>

<%@ attribute name="toggleSwitchState" type="java.lang.Boolean" %>
<c:set var="toggleSwitchState" value="${(empty toggleSwitchState) ? true : toggleSwitchState}"/>

<%@ attribute name="toggleSwitchWidth" type="java.lang.Double" %>
<c:set var="toggleSwitchWidth" value="${(empty toggleSwitchWidth) ? 45 : toggleSwitchWidth}"/>

<%@ attribute name="toggleSwitchHeight" type="java.lang.Double" %>
<c:set var="toggleSwitchHeight" value="${(empty toggleSwitchHeight) ? 24 : toggleSwitchHeight}"/>

<%@ attribute name="toggleSwitchDotMargin" type="java.lang.Double" %>
<c:set var="toggleSwitchDotMargin" value="${(empty toggleSwitchDotMargin) ? 4 : toggleSwitchDotMargin}"/>

<%@ attribute name="toggleSwitchDisabled" type="java.lang.Boolean" %>
<c:set var="toggleSwitchDisabled" value="${(empty toggleSwitchDisabled) ? false : toggleSwitchDisabled}"/>

<c:set var="dotSize" value="${toggleSwitchHeight-2*toggleSwitchDotMargin}"/>
<c:set var="moveLeft" value="${toggleSwitchWidth-dotSize-toggleSwitchDotMargin*2}"/>
<span border id="${toggleSwitchId}" class="toggleSwitchBasic"
      style="width: ${toggleSwitchWidth}px;height: ${toggleSwitchHeight}px;
      <c:if test="${toggleSwitchState}">background: var(--highlight-component-background-color-hover)</c:if> " component_type="toggleSwitch" move_distance="${moveLeft}"
      <c:if test="${toggleSwitchDisabled}">disabled="disabled"</c:if>
      onclick="toggleSwitch($(this),${moveLeft},${toggleSwitchOn},${toggleSwitchOff})">
    <input type="hidden" class="toggleSwitchInput" name="${toggleSwitchId}" id="input_${toggleSwitchId}" value="${empty toggleSwitchState?"true":toggleSwitchState}">
    <span class="toggleSwitchDot" style="height: ${dotSize}px;width: ${dotSize}px;<c:if
            test="${toggleSwitchState}">left: ${moveLeft}px</c:if>"></span>
</span>