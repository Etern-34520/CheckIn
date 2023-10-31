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
<jsp:useBean id="question" scope="request" type="indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion"/>
<c:set var="md5" value="${question.md5}"/>
<form id="questionEditForm">
    <div id="formBox">
        <div id="formDiv">
            <div id="formMainDiv">
                <input type="hidden" id="questionEdit" name="questionEdit" value="true">
                <input type="hidden" id="md5" name="md5" value="${md5}">
                <div style="display: flex;flex-direction: row;resize: vertical">
                    <div style="display: flex;flex-direction: column;width: 100%;padding-right: 4px;height: 300px;">
                        <label for="questionContent">questionContent</label>
                        <textarea id="questionContent" name="questionContent"
                                  style="height: 300px;" text>${question.content}</textarea>
                    </div>
                    <div style="display: flex;flex-direction: column;width: 200px;padding-right: 4px;height: 300px;">
                        <label>Partition</label>
                        <div>
                            <c:forEach var="partitionName" items="${partitionInfo.partitionName}">
                                <div rounded clickable>
                                    ${partitionName}
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>
                <div style="display: flex;flex-direction: column;width: 100%">
                    <label for="authorName">authorName</label>
                    <input type="text" id="authorName" name="authorName" <%--value="${question.author}"--%>>
                </div>
                <div id="optionsDiv">
                    <c:forEach var="choice" items="${question.choices}" varStatus="stat">
                        <div class="optionDiv">
                            <label for="correct${stat.index+1}"></label>
                            <input type="checkbox" name="correct${stat.index+1}" id="correct${stat.index+1}"
                                   value="true" <c:if test="${choice.isCorrect}">checked</c:if>>
                            <label for="choice${stat.index+1}"></label>
                            <input type="text" style="margin-left: 8px" name="${stat.index+1}"
                                   id="choice${stat.index+1}" value="${choice.content}">
                            <button id="delete${stat.index+1}" class="deleteOptionButton" type="button"
                                    style="height: 32px;width: 32px;margin: 4px;font-size: 24px"
                                    onclick="deleteOption(this)">-
                            </button>
                        </div>
                    </c:forEach>
                </div>
                <button type="button" style="margin-top: 8px;margin-left: 30px;font-size: 24px" onclick="addOption()">+
                </button>
            </div>
        </div>
        <div id="formButtonsDiv">
            <button type="button" style="margin-left: 8px" onclick="deleteQuestion('${md5}')">
                <c:choose>
                    <c:when test="${md5==\"\"}">
                        取消
                    </c:when>
                    <c:otherwise>
                        删除
                    </c:otherwise>
                </c:choose>
            </button>
            <button type="button" style="margin-left: 8px"
            <c:choose>
                    <c:when test="${md5==\"\"}">
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