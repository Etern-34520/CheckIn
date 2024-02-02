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
<jsp:useBean id="settingInfo" type="indi.etern.checkIn.beans.SettingInfo" scope="request"/>
<jsp:useBean id="partitionInfo" type="indi.etern.checkIn.beans.PartitionInfo" scope="request"/>
<div id="managePage" index="3" page-type="server" style="display: flex;flex-direction: column">
    <div id="settingRoot" class="forScroll" rounded style="display: flex;flex: 1;flex-direction: column" border>
        <components:shrinkPane title="抽题设置" useLabelTitle="true">
            <jsp:attribute name="content">
                <div id="examSettingBase" class="settingBase">
                    <form name="examSetting">
                        <div id="examSetting" class="settingDiv">
                            <div><label for="examTitle">测试标题</label><input id="examTitle" name="exam.title"
                                                                               type="text"
                                                                               value="${settingInfo.title}"></div>
                            <div textareaInput><label for="examDescription">测试描述</label><textarea
                                    id="examDescription"
                                    name="exam.description">${fn:escapeXml(settingInfo.description)}</textarea>
                            </div>
                            <div><label>测试题量</label><components:slider
                                    min="1"
                                    max="${partitionInfo.questionCount}"
                                    name="exam.questionCount"
                                    value="${settingInfo.questionCount}"/>
                            </div>
                            <div><label>选择分区数</label><components:slider useDoubleSlider="true" min="1"
                                                                             max="${partitionInfo.partitionNotEmpty.size()}"
                                                                             value="${settingInfo.partitionCountMin}"
                                                                             value1="${settingInfo.partitionCountMax}"
                                                                             name="exam.partitionCountMin"
                                                                             name1="exam.partitionCountMax"/></div>
                        </div>
                    </form>
                    <button highlight rounded onclick="saveSetting(this)">保存</button>
                </div>
            </jsp:attribute>
        </components:shrinkPane>
        <components:shrinkPane title="校对设置" useLabelTitle="true">
            <jsp:attribute name="content">
                <div id="checkSettingBase" class="settingBase">
                    <form name="checkingSetting">
                        <div id="checkSetting" class="settingDiv">
                            <div><label>及格线</label><components:slider min="0" max="100"
                                                                         value="${settingInfo.passScore}"
                                                                         name="checking.passScore"/></div>
                            <div textareaInput>
                                <label for="passMessage">通过消息</label>
                                <textarea id="passMessage"
                                          name="checking.passMessage">${fn:escapeXml(settingInfo.passMessage)}</textarea>
                            </div>
                            <div textareaInput>
                                <label for="notPassMessage">未通过消息</label>
                                <textarea id="notPassMessage"
                                          name="checking.notPassMessage">${fn:escapeXml(settingInfo.notPassMessage)}</textarea>
                            </div>
                            <div>
                                <label>启用入群申请后自动添加用户</label>
                                <components:toggleSwitch switchID="checking.enableAutoCreateUser"
                                                         state="${settingInfo.enableAutoCreateUser}"/>
                                <div class="blank"></div>
                            </div>
                        </div>
                    </form>
                    <button highlight rounded onclick="saveSetting(this)">保存</button>
                </div>
            </jsp:attribute>
        </components:shrinkPane>
    </div>
</div>
<script>
    initSlider($("div[component_type='slider']"));
</script>