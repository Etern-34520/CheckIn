<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  Created by IntelliJ IDEA.
  User: o_345
  Date: 2023/10/31
  Time: 15:16
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:useBean id="partitionInfo" scope="request" class="indi.etern.checkIn.beans.PartitionInfo"/>
<jsp:useBean id="userInfo" scope="request" class="indi.etern.checkIn.beans.UserInfo"/>
<jsp:useBean id="question" scope="request"
             type="indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion"/>
<jsp:useBean id="isNew" scope="request" type="java.lang.Boolean"/>
<c:set var="md5" value="${question.md5}"/>
<form id="questionEditForm" onload="initQuestionForm()">
    <div id="formBox">
        <div id="formDiv" style="display: flex;flex-direction: column;">
            <input type="hidden" id="questionEdit" name="questionEdit" value="true">
            <input type="hidden" id="md5" name="md5" value="${md5}">
            <div style="display: flex;flex-direction: row;resize: vertical">
                <div style="display: flex;flex-direction: column;width: 100%;padding-right: 4px;height: 300px;">
                    <label for="questionContent" text>Content</label>
                    <textarea id="questionContent" name="questionContent"
                              style="height: 300px;" text>${question.content}</textarea>
                </div>
                <div style="display: flex;flex-direction: column;width: 200px;padding-right: 4px;height: 300px;">
                    <label text>Partition</label>
                    <div rounded style="background: var(--input-background-color);flex: 1" id="partitionSelectionsDiv">
                        <c:forEach var="partition" items="${partitionInfo.partitions}">
                            <c:set var="partitionInclude" value="${question.partitions.contains(partition)}"/>
                            <div rounded clickable onclick="selectQuestionPartition(this)" id="partition_select_${partition.id}" ${question.partitions.contains(partition)?"selected=\"pre\"":""}>
                                <input type="hidden" id="question_partition_${partition.name}"
                                       name="question_partition_${partition.name}">
                                    ${partition.name}
                            </div>
                        </c:forEach>
                    </div>
                    <div style="display: flex;flex-direction: column;width: 100%;flex: 0">
                        <label for="author" text>Author</label>
                        <select id="author" name="author">
                            <c:forEach var="author" items="${userInfo.users}">
                                <option value="${author.name}">${author.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
            </div>
            <label text>Images</label>
            <div rounded style="display: flex;flex-direction: row;background: var(--input-background-color)">
                <label class="fileUpload" for="imageInput" style="font-size: 24px" text>+</label>
                <input id="imageInput" type="file" onchange="addImages(this)" multiple accept="image/jpeg,image/png,image/gif">
                <div id="imagesDiv">
                </div>
            </div>
            <div style="display: flex;flex-direction: column;flex: 1">
                <div id="optionsDiv">
                    <c:forEach var="choice" items="${question.choices}" varStatus="stat">
                        <div class="optionDiv">
                            <input type="checkbox" name="correct${stat.index+1}" id="correct${stat.index+1}"
                                   value="true" <c:if test="${choice.isCorrect}">checked</c:if>>
                            <input type="text" style="margin-left: 8px" name="${stat.index+1}"
                                   id="choice${stat.index+1}" value="${choice.content}">
                            <button id="delete${stat.index+1}" class="deleteOptionButton" type="button"
                                    style="height: 32px;width: 32px;margin: 4px;font-size: 24px"
                                    onclick="deleteOption(this)">-
                            </button>
                        </div>
                    </c:forEach>
                </div>
                <button type="button" style="height:40px;margin-top: 8px;margin-left: 36px;font-size: 24px"
                        onclick="addOption()">+
                </button>
            </div>
        </div>
        <div id="formButtonsDiv" style="display: flex;flex-direction: row">
            <div class="blank"></div>
            <button type="button" style="margin-left: 8px" onclick="deleteQuestion('${md5}')">
                <c:choose>
                    <c:when test="${isNew}">
                        取消
                    </c:when>
                    <c:otherwise>
                        删除
                    </c:otherwise>
                </c:choose>
            </button>
            <button type="button" style="margin-left: 8px"
                    <c:choose>
                        <c:when test="${isNew}">
                            onclick="addQuestion('${md5}')">添加
                        </c:when>
                        <c:otherwise>
                            onclick="updateQuestion('${md5}')">更新
                        </c:otherwise>
                    </c:choose>
            </button>
        </div>
    </div>
</form>