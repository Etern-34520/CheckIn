<%--
  Created by IntelliJ IDEA.
  User: o_345
  Date: 2023/10/16
  Time: 20:30
  To change this template use File | Settings | File Templates.
--%>
<%--for manage——server:home --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="components" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="settingInfo" type="indi.etern.checkIn.beans.SettingInfo" scope="request"/>
<jsp:useBean id="partitionInfo" type="indi.etern.checkIn.beans.PartitionInfo" scope="request"/>
<div id="managePage" index="4" page-type="server" style="display: flex;flex-direction: column">
    <div id="infoRoot" class="forScroll" rounded style="display: flex;flex: 1;flex-direction: column;align-content: center;align-items: center" border>
        <label titleLabel style="font-size: 32px">CheckIn</label>
        <a href="https://github.com/Etern-34520/CheckIn">GitHub</a>
        <div style="display: flex;flex-direction: row;flex-wrap: wrap">
            <div rounded clickable style="display: flex;flex-direction: column;align-items: center" onclick="window.open('https://github.com/Etern-34520')">
                <div class="userAvatar" style="background-image: url('https://avatars.githubusercontent.com/u/93083287?v=4')"></div>
                <label titleLabel>Etern_</label>
                <label style="color: var(--highlight-color)">作者</label>
            </div>
            <div rounded clickable style="display: flex;flex-direction: column;align-items: center" onclick="window.open('https://github.com/Etern-34520')">
                <div class="userAvatar" style="background-image: url('https://avatars.githubusercontent.com/u/103395523?v=4')"></div>
                <label titleLabel>Harkerbest</label>
                <label style="color: var(--highlight-color)">发起者 QBot接入</label>
            </div>
        </div>
        <div style="display: flex;flex-direction: column;align-items: center">
            <label titleLabel>本项目的诞生离不开下列项目</label>
            <c:forTokens items="
Spring Boot:https://spring.io/projects/spring-boot,
Jjwt:https://github.com/jwtk/jjwt,
H2database:https://h2database.com/html/main.html,
Gson:https://github.com/google/gson,
Jackson:
Glassfish:https://glassfish.org/,
Tomcat:https://tomcat.apache.org/,
Lombok:https://projectlombok.org/，
Testng:https://testng.org/,
Slf4j:https://slf4j.org/,
junit:https://junit.org/junit5/
            " delims="," var="link">
                <c:set var="projectName" value="${link.split(':',2)[0]}"/>
                <c:set var="linkUrl" value="${link.split(':',2)[1]}"/>
                <a href="${linkUrl}">${projectName}</a>
            </c:forTokens>
        </div>
    </div>
</div>