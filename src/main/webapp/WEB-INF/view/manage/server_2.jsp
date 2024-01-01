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
<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:useBean id="partitionInfo" class="indi.etern.checkIn.beans.PartitionInfo" scope="request"/>
<jsp:useBean id="permission_create_partition" type="java.lang.Boolean" scope="request"/>
<div id="managePage" index="2" page-type="server">
    <div id="left" rounded border>
        <div class="subContentRoot">
            <label titleLabel text>分区</label>
            <div id="partitionButtons">
                <c:forEach var="partition" items="${partitionInfo.partitions}">
                    <button type="button" id="partitionButton${partition.id}" class="partitionButton" onclick="switchToPartition(this)" preText editing="false">${fn:escapeXml(partition.name)}</button>
                </c:forEach>
                <c:if test="${permission_create_partition}">
                    <button type="button" id="addPartitionButton" style="font-size: 20px" onclick="newPartition()">+</button>
                </c:if>
            </div>
        </div>
    </div>
    <div id="right" rounded border>
        <div class="subContentRoot">
        </div>
    </div>
</div>