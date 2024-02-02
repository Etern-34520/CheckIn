<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ tag pageEncoding="utf-8" %>

<%@ attribute name="max" type="java.lang.Long" %>
<c:set var="max" value="${empty max?100:max}" scope="page"/>
<%@ attribute name="min" type="java.lang.Long" %>
<c:set var="min" value="${empty min?0:min}" scope="page"/>
<%@ attribute name="value" type="java.lang.Long" %>
<c:set var="value" value="${empty value?min:value}" scope="page"/>
<%@ attribute name="value1" type="java.lang.Long" %>
<c:choose>
    <c:when test="${useDoubleSlider}">
        <c:set var="value1" value="${empty value1?min:value1}" scope="page"/>
    </c:when>
    <c:otherwise>
        <c:set var="value1" value="${value}" scope="page"/>
        <c:set var="value" value="${min}" scope="page"/>
    </c:otherwise>
</c:choose>
<%@ attribute name="name" %>
<c:set var="name" value="${empty name?'min_value':name}" scope="page"/>
<%@ attribute name="name1" %>
<c:set var="name1" value="${empty name1?'max_value':name1}" scope="page"/>

<%@ attribute name="useDoubleSlider" type="java.lang.Boolean" %>
<c:set var="useDoubleSlider" value="${empty useDoubleSlider?false:useDoubleSlider}" scope="page"/>
<%@ attribute name="id" type="java.lang.String" %>
<div component_type="slider" max_value="${max}" min_value="${min}" round_calc="true">
    <c:if test="${useDoubleSlider}">
        <label>
            <input style="width: 60px" class="sliderMinValue" name="${name}" type="number" value="${value}" max_value="${max}">
        </label>
    </c:if>
    <div component_type="sliderBase">
        <c:if test="${useDoubleSlider}">
            <span component_type="sliderLine" style="width: ${100*(value-min)/(max-min)}%"></span>
            <div component_type="sliderPoint" value="${(value-min)/(max-min)}">
                <div></div>
                <input type="hidden" value="${(value-min)/(max-min)}"/>
            </div>
        </c:if>
        <span component_type="sliderLine" style="width: ${100*(value1-value)/(max-min)}%"></span>
        <div component_type="sliderPoint" value="${(value1-min)/(max-min)}">
            <div></div>
            <input type="hidden" value="${(value1-min)/(max-min)}"/>
        </div>
    </div>
    <label>
        <input style="width: 60px" class="sliderMaxValue" name="${useDoubleSlider?name1:name}" type="number" value="${value1}" max_value="${max}">
    </label>
</div>