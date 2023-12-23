<%@ tag pageEncoding="utf-8" %>
<%@ attribute name="style"%>
<%@ attribute name="shrinkFuncName" required="true" %>
<%@ attribute name="expandFuncName" required="true" %>

<div style="width: 32px;height: 32px;display: flex;justify-content: center;align-items: center;flex: none;${style}" rounded
                 clickable onclick="
rotateShrinkButton(this,${shrinkFuncName},${expandFuncName})">
                <div style="rotate: 0deg;" component_type="shrinkButtonPointer">
                    <div></div>
                    <div></div>
                </div>
            </div>