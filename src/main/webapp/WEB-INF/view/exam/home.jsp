<%--
  Created by IntelliJ IDEA.
  User: Etern
  Date: 2024/1/15
  Time: 13:36
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:useBean id="partitionInfo" type="indi.etern.checkIn.beans.PartitionInfo" scope="request"/>
<jsp:useBean id="settingInfo" type="indi.etern.checkIn.beans.SettingInfo" scope="request"/>
<%--<jsp:useBean id="Math" class="java.lang.Math" scope="request"/>--%>
<html>
<head>
    <title>Title</title>
    <link rel="stylesheet" href="./../css/global.css">
    <link rel="stylesheet" href="./../css/exam.css">
    <%--    <meta name="viewport" content="width=1080, initial-scale=1.0 user-scalable=no" />--%>
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=0.75, maximum-scale=0.75, minimum-scale=1.0">
        <script src="https://cdn.staticfile.org/jquery/3.7.1/jquery.min.js"></script>
        <script src="https://cdn.staticfile.org/jquery-cookie/1.4.1/jquery.cookie.min.js"></script>
        <script src="https://cdn.staticfile.org/jquery-easing/1.4.1/jquery.easing.min.js"></script>
        <script src="https://cdn.staticfile.org/jquery-color/2.1.2/jquery.color.min.js"></script>
<%--    <script src="./../lib/http_cdn.staticfile.org_jquery_3.7.1_jquery.js"></script>--%>
<%--    <script src="./../lib/http_cdn.staticfile.org_jquery-color_2.1.2_jquery.color.js"></script>--%>
<%--    <script src="./../lib/http_cdn.staticfile.org_jquery-cookie_1.4.1_jquery.cookie.js"></script>--%>
<%--    <script src="./../lib/http_cdn.staticfile.org_jquery-easing_1.4.1_jquery.easing.js"></script>--%>
<%--    <script src="./../lib/http_cdn.staticfile.org_jquery-mousewheel_3.1.10_jquery.mousewheel.min.js"></script>--%>

        <script src="https://cdn.staticfile.org/jquery-mousewheel/3.1.10/jquery.mousewheel.min.js"></script>

    <%--    <script src="https://cdn.staticfile.org/jquery-contextmenu/2.9.2/jquery.ui.position.min.js"></script>--%>
    <script src="./../js/exam.js"></script>

    <script>
        const partitionMinSelectCount = ${settingInfo.partitionCountMin};
        const partitionMaxSelectCount = ${settingInfo.partitionCountMax};
    </script>
</head>
<body style="background: #19191a">
<div id="content" class="noScrollBar"
     style="z-index: 2;scale: 1;overflow-x: hidden">
    <div class="page" id="homePage">
        <div id="title"><label>
            ${settingInfo.title}
        </label></div>
        <div id="detail" style="opacity: 0;display: flex;flex-direction: column">
        <pre style="font-size: 24px;text-align: center;white-space: pre-wrap;word-wrap: break-word;">
${settingInfo.description}

注意
本试题完全开卷，答题时间不限
可能需要查阅资料
请各位在开始答题前确保设备与环境合适
最大题目量：${settingInfo.questionCount}
分数线:${settingInfo.passScore}
满分:100
多选题必须选出全部正确答案才能得分
答题次数不限，答案顺序随机
100分进群可获得群内专属头衔
本题目检验的并不完全是个人水平
也包括查找搜寻资料的学习能力
        </pre>
            <div style="height: 20px;align-self: center">
                <div style="rotate: 0deg;align-self: center" class="down">
                    <div></div>
                    <div></div>
                </div>
            </div>
        </div>
    </div>
    <div class="page" id="partitionSelectPage">
        <div id="selectPartitionTitle" style="opacity: 0;display: flex;flex-direction: column;align-items: center">
            <label>选择答题分区</label>
            <label style="font-size: 16px;color: #aaaaaa">( ${settingInfo.partitionCountMin}
                —— ${settingInfo.partitionCountMax} 个)</label>
        </div>
        <div id="step2" style="opacity: 0;display: flex;flex-direction: column;align-items: center">
            <div id="partitions" style="margin-top: 40px">
                <c:forEach items="${partitionInfo.partitionNotEmpty}" var="partition">
                    <div class="partitions" id="${partition.id}" rounded clickable>
                            ${partition.name}
                    </div>
                </c:forEach>
            </div>
            <div style="min-height: 200px">
                <div id="qqNumberInputDiv" style="display: none;margin-top: 30px">
                    <div style="display: flex;flex-direction: column;align-items: center">
                        <label for="qqNumberInput" style="font-size: 20px;">你的 QQ 号码</label>
                        <label style="font-size: 12px;color: #aaaaaa">(仅用于入群验证)</label>
                        <input type="number" name="qqNumber" id="qqNumberInput">
                        <button highlight id="start" style="padding: 6px 24px;margin: 8px;opacity: 0;display: none">
                            开始答题
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="background">
</div>
</body>
</html>
