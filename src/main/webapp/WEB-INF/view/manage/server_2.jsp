<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  Created by IntelliJ IDEA.
  User: o_345
  Date: 2023/10/16
  Time: 20:30
  To change this template use File | Settings | File Templates.
--%>
<%--for manage——server:home --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:useBean id="partitionInfo" class="indi.etern.checkIn.beans.PartitionInfo" scope="request"/>
<link rel="stylesheet" href="./css/server_2.css">
<div id="managePage">
    <div id="left" rounded>
        <label class="titleLabel" text>分区</label>
        <div id="partitionButtons">
            <c:forEach var="partition" items="${partitionInfo.partitions}">
                <button type="button" class="partitionButton" onclick="switchToPartition(this)" preText>${fn:escapeXml(partition.name)}</button>
            </c:forEach>
            <button type="button" id="addPartitionButton" class="partitionButton" style="font-size: 20px" onclick="newPartition()">+</button>
        </div>
    </div>
    <div id="right" rounded>
        <div class="subContentRoot">
        </div>
    </div>
</div>