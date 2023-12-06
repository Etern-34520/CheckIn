<%--
  Created by IntelliJ IDEA.
  User: o_345
  Date: 2023/10/16
  Time: 20:30
  To change this template use File | Settings | File Templates.
--%>
<%--for manage——server:home --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div id="managePage" index="0" page-type="server" onload="initChart()">
    <div id="left">
        <div id="leftTop" rounded border>

        </div>
        <div id="leftBottom" rounded border>
            <div id="chart"></div>
            <div id="todayTraffics"></div>
        </div>
    </div>
    <div id="right" rounded border>
        <div class="subContentRoot">

        </div>
    </div>
</div>