<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="component" tagdir="/WEB-INF/tags" %>
<%--
  Created by IntelliJ IDEA.
  User: o_345
  Date: 2023/10/16
  Time: 20:30
  To change this template use File | Settings | File Templates.
--%>
<%--for manage——server:home --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:useBean id="partitionInfo" type="indi.etern.checkIn.beans.PartitionInfo" scope="request"/>
<jsp:useBean id="permission_create_partition" type="java.lang.Boolean" scope="request"/>
<div id="managePage" index="2" page-type="server">
    <div id="left" rounded border>
        <div class="subContentRoot">
            <component:shrinkPane titlePadding="2" titleMinHeight="30" style="margin:0">
                <jsp:attribute name="title">
                    <label style="margin-left: 4px">排序</label>
                    <div class="blank"></div>
                </jsp:attribute>
                <jsp:attribute name="content">
                    <div style="display: flex;flex-direction: row">
                        <div class="blank"></div>
                        <label>降序</label>
                        <component:toggleSwitch switchID="sortTypeSwitch" style="margin: 0 6px" onFuncName="sortQuestionAsc" offFuncName="sortQuestionDesc"/>
                        <label>升序</label>
                        <div class="blank"></div>
                    </div>
                    <div class="selections">
                        <div rounded clickable type="content" onclick="selectSortOption(this)" selected="selected">内容</div>
                        <div rounded clickable type="lastEditTime" onclick="selectSortOption(this)">修改时间</div>
                        <div rounded clickable type="author" onclick="selectSortOption(this)">作者</div>
                    </div>
                </jsp:attribute>
            </component:shrinkPane>
            <label titleLabel text>分区</label>
            <div id="partitionButtons">
                <c:forEach var="partition" items="${partitionInfo.partitions}">
                    <button type="button" id="partitionButton${partition.id}" class="partitionButton"
                            onclick="switchToPartition(this)" preText
                            editing="false">${fn:escapeXml(partition.name)}</button>
                </c:forEach>
                <c:if test="${permission_create_partition}">
                    <button type="button" id="addPartitionButton" style="font-size: 20px" onclick="newPartition()">+
                    </button>
                </c:if>
            </div>
        </div>
    </div>
    <div id="right" rounded border>
        <div class="subContentRoot">
        </div>
    </div>
</div>