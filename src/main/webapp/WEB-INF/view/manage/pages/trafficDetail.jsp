<%--
  Created by IntelliJ IDEA.
  User: Etern
  Date: 2024/1/9
  Time: 22:04
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="components" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="userTraffic" type="indi.etern.checkIn.entities.traffic.UserTraffic" scope="request"/>
<jsp:useBean id="multiPartitionableQuestionService" type="indi.etern.checkIn.service.dao.MultiPartitionableQuestionService" scope="request"/>
<div style="display: flex;flex-direction: column">
    <div style="display: flex;flex-direction: row">
        <c:if test="${userTraffic.QQNumber >= 10000}">
            <div style="
                    width: 100px;
                    aspect-ratio: 1;
                    max-width: 90%;
                    border-radius: 50%;
                    background-size: 100% 100%;
                    background-image: url('https://q1.qlogo.cn/g?b=qq&nk=${userTraffic.QQNumber}&s=640');"></div>
        </c:if>
        <div style="display: flex;flex-direction: column">
            <label titleLabel>${userTraffic.QQNumber}</label>
            <label titleLabel>${userTraffic.IP}</label>
            <label>${userTraffic.localDateTime}</label>
        </div>
        <c:choose>
            <c:when test="${userTraffic.attributesMap.get(\"passed\").equals(\"true\")}">
                <label titleLabel style="color: var(--correct-choice-font-color)">已通过</label>
            </c:when>
            <c:when test="${userTraffic.attributesMap.get(\"passed\").equals(\"false\")}">
                <label titleLabel style="color: var(--incorrect-choice-font-color)">未通过</label>
            </c:when>
        </c:choose>
    </div>
    <components:shrinkPane title="Attributes" useLabelTitle="true">
        <jsp:attribute name="content">
            <c:forEach var="entry" items="${userTraffic.attributesMap}">
                <div class="KeyValueItem" style="display: flex;flex-direction: row">
                    <div style="margin-right: 4px;" rounded>${entry.key}</div>
                    <div rounded>${entry.value}</div>
                </div>
            </c:forEach>
        </jsp:attribute>
    </components:shrinkPane>
    <components:shrinkPane title="Headers" useLabelTitle="true">
        <jsp:attribute name="content">
            <c:forEach var="entry" items="${userTraffic.headerMap}">
                <div class="keyValueItem" style="display: flex;flex-direction: row">
                    <div style="margin-right: 4px;" rounded>${entry.key}</div>
                    <div rounded>${entry.value}</div>
                </div>
            </c:forEach>
        </jsp:attribute>
    </components:shrinkPane>
    <c:set var="action" value="${userTraffic.attributesMap.get(\"action\")}"/>
    <c:if test="${action.equals(\"submitExam\")||action.equals(\"generateExam\")}">
        <components:shrinkPane title="试题" useLabelTitle="true">
            <jsp:attribute name="content">
                <c:set var="questionIdsString" value="${userTraffic.attributesMap.get(\"examQuestionIds\")}"/>
                <c:forTokens delims="\",\"" var="examQuestionId" items="${questionIdsString.substring(1,questionIdsString.length()-1)}">
<%--suppress ELValidationInspection --%>
                    <c:if test="${multiPartitionableQuestionService.findById(examQuestionId).isPresent()}">
                        <components:questionOverview question="${multiPartitionableQuestionService.findById(examQuestionId).orElseThrow()}" inUser_2="true"/>
                    </c:if>
                </c:forTokens>
            </jsp:attribute>
        </components:shrinkPane>
    </c:if>
    <div></div>
</div>