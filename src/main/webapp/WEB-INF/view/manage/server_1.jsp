<%--
  Created by IntelliJ IDEA.
  User: o_345
  Date: 2023/10/16
  Time: 20:30
  To change this template use File | Settings | File Templates.
--%>
<%--for manage——server:home --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="trafficInfo" type="indi.etern.checkIn.beans.TrafficInfo" scope="request"/>
<div id="managePage" index="1" page-type="server" onload="initTraffic()">
    <div id="left" rounded border style="overflow-x: hidden">
        <div style="display: flex;flex-direction: row;max-height: 100%">
            <div class="noScrollBar" id="dateSelections" component_type="dateSelector"
                 style="overflow: auto;flex: none">
                <c:forEach items="${trafficInfo.dateTraffics}" var="dateTraffic">
                    <div style="width: 20px;writing-mode: vertical-rl;transform: rotate(180deg);display: flex;flex-direction: row-reverse"
                         rounded clickable border id="${dateTraffic.localDate}">
                        <div component_type="selectPointer"></div>
                        <label>${dateTraffic.localDate}</label>
                    </div>
                </c:forEach>
            </div>
            <div id="userTraffics" style="flex: 1">
                <div class="subContentRoot" style="max-height: calc(100% - 16px);"></div>
            </div>
        </div>
    </div>
    <div id="right" rounded border>
        <div class="subContentRoot"></div>
    </div>
</div>