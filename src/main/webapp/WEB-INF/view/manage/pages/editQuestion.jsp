<%@ taglib prefix="components" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  Created by IntelliJ IDEA.
  User: o_345
  Date: 2023/10/31
  Time: 15:16
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:useBean id="partitionInfo" scope="request" class="indi.etern.checkIn.beans.PartitionInfo"/>
<jsp:useBean id="userInfo" scope="request" class="indi.etern.checkIn.beans.UserInfo"/>
<jsp:useBean id="question" scope="request"
             type="indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleChoiceQuestion"/>
<jsp:useBean id="ignorePartitionSelection" scope="request" type="java.lang.Boolean"/>
<jsp:useBean id="currentUser" type="indi.etern.checkIn.entities.user.User" scope="request"/>

<jsp:useBean id="permission_edit_others_question" type="java.lang.Boolean" scope="request"/>
<jsp:useBean id="permission_create_and_edit_owns_question" type="java.lang.Boolean" scope="request"/>
<jsp:useBean id="permission_enable_and_disable_question" type="java.lang.Boolean" scope="request"/>
<jsp:useBean id="permission_change_author" type="java.lang.Boolean" scope="request"/>

<%--<jsp:useBean id="newQuestion" scope="request" type="java.lang.Boolean"/>--%>
<c:set var="md5" value="${question.md5}"/>
<c:set var="canEditQuestion"
       value="${permission_edit_others_question || question.author.equals(currentUser)&&permission_create_and_edit_owns_question}"/>
<form id="questionEditForm" onload="initQuestionForm()">
    <div id="formBox">
        <div id="formDiv" style="display: flex;flex-direction: row">
            <input type="hidden" id="questionEdit" name="questionEdit" value="true">
            <input type="hidden" id="md5" name="md5" value="${md5}">
            <div style="display: flex;flex-direction: column;resize: vertical;flex: 1">
                <label for="questionContent" text>内容</label>
                <textarea id="questionContent" name="questionContent"
                          style="height: 300px;resize: none"
                          text ${canEditQuestion?"":"disabled"}>${question.content}</textarea>
                <label text>图片</label>
                <div rounded style="display: flex;flex-direction: row;background: var(--input-background-color)">
                    <c:if test="${canEditQuestion}">
                        <label class="fileUpload" for="imageInput" style="font-size: 24px" text>+</label>
                        <input id="imageInput" type="file" onchange="addImages(this)" multiple
                               accept="image/jpeg,image/png,image/gif">
                    </c:if>
                    <div id="imagesDiv">
                    </div>
                </div>
                <label text>选项</label>
                <div style="display: flex;flex-direction: column;flex: 1">
                    <div id="optionsDiv">
                        <c:forEach var="choice" items="${question.choices}" varStatus="stat">
                            <div class="optionDiv">
                                <label for="correct${stat.index+1}" style="display:none;"></label>
                                <input type="checkbox" name="correct${stat.index+1}" id="correct${stat.index+1}"
                                       value="true"
                                       <c:if test="${choice.isCorrect}">checked</c:if> ${canEditQuestion?"":"disabled"}>
                                <label for="choice${stat.index+1}" style="display:none;"></label>
                                <input type="text" style="margin-left: 8px" name="${stat.index+1}"
                                       id="choice${stat.index+1}"
                                       value="${choice.content}" ${canEditQuestion?"":"disabled"}>
                                <c:if test="${canEditQuestion}">
                                    <button id="delete${stat.index+1}" class="deleteOptionButton" type="button"
                                            style="height: 32px;width: 32px;margin: 4px;font-size: 24px"
                                            onclick="deleteChoice(this)">-
                                    </button>
                                </c:if>
                            </div>
                        </c:forEach>
                    </div>
                    <c:if test="${canEditQuestion}">
                        <button type="button" style="height:40px;margin-top: 8px;margin-left: 36px;font-size: 24px"
                                onclick="addNewChoice()">+
                        </button>
                    </c:if>
                </div>
            </div>
            <div style="display: flex;flex-direction: column;width: 200px;padding-right: 4px">
                <div style="display: flex;flex-direction: column;width: 100%;flex: 0">
                    <label for="author" text>作者</label>
                    <select id="author" name="author" ${canEditQuestion && permission_change_author?"":"disabled"}>
                        <c:forEach var="user" items="${userInfo.users}">
                            <option id="userSelect${user.QQNumber}"
                                    value="${user.QQNumber}" ${question.author.equals(user)||(question.author==null&&user.equals(currentUser))?"selected":""}>${user.name}</option>
                        </c:forEach>
                    </select>
                </div>
                <div style="display: flex;align-items: center;margin-bottom: 8px">
                    <label style="margin-right: 4px;">启用</label>
                    <components:toggleSwitch toggleSwitchId="enabled"
                                             toggleSwitchState="${empty question.enabled?false:question.enabled}"
                                             toggleSwitchDisabled="${!(canEditQuestion && permission_enable_and_disable_question)}"/>
                </div>
                <label text>分区</label>
                <div rounded style="background: var(--input-background-color);flex: 1;overflow: auto;margin-bottom: 0;"
                     id="partitionSelectionsDiv">
                    <c:forEach var="partition" items="${partitionInfo.partitions}">
                        <c:set var="partitionInclude" value="${question.partitions.contains(partition)}"/>
                        <div rounded clickable
                            <c:if test="${!canEditQuestion}">
                                disabled
                            </c:if>
                             onclick="selectQuestionPartition(this)"
                             id="partition_select_${partition.id}" ${!ignorePartitionSelection && question.partitionIds.contains(partition.id)?"selected=\"pre\"":""}>
                            <input type="hidden" id="question_partition_${partition.id}"
                                   name="question_partition_${partition.id}" value="false">
                            <label>${fn:escapeXml(partition.name)}</label>
                        </div>
                    </c:forEach>
                </div>
            </div>

        </div>
    </div>
</form>
<c:if test="${canEditQuestion}">
    <button rounded clickable style="padding: 6px" onclick="updateQuestionBy('${md5}')">强制更新</button>
</c:if>
