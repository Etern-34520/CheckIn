<img width="64" height="64" src="icon.svg" alt="icon">

# CheckIn
#### 更易用的入群测试系统
![Static Badge](https://img.shields.io/badge/Language-Java-red)
![Static Badge](https://img.shields.io/badge/JavaRequire-21-red)
![Static Badge](https://img.shields.io/badge/Stable-2.1.0-blue)
![Static Badge](https://img.shields.io/badge/Status-Developing-green)
---

### 简介

`CheckIn` 是一个Web入群测试系统，用于为需要入群门槛的群聊提供入群测试，以保证群内成员的水平合格。

---
### 功能

- 记录
- - 答题记录
> 将会记录每一次的答题情况
> #### 可查看
> - QQ号码 `由答题者输入，不会被校验是否存在及是否为所有者本人`
> - 头像 `根据QQ号获取`
> - 生成时间
> - 过期时间
> - 提交时间
> - 抽取到的题目
> - 提交的数据
> - 分数
> - 结果

- - 请求记录
> 将记录每一个请求
> #### 可查看
> - Request headers
> - Request attributes
> - Response headers
> - SessionID
> - IP地址 `支持IPV4和IPV6`
> - 潜在的异常信息和堆栈

- 自定义首页及结果内容
> 支持Markdown

- 分区题库
> 简单易用，功能强大
> 
> 支持自定义题目内容限制（字数、图片、选项等）
> 
> 题目内容支持Markdown
> 
> 支持单个拖拽、批量操作
> 
> 题目分区为多对多关系
>
> 目前提供选择题、题组。
> > #### 未来计划加入
> > - 填空题

- 动态生成试题
> 根据用户选择分区、必选分区（可配置选择范围）动态生成试题
> 
> 可配置抽取策略以及补全策略
> 
> 可配置必选分区、可选分区
> 
> 可配置某些分区的特殊限制
> 
> 可配置用户注册策略 ( 2.0.0 pre-release )

- 自动校验
> 根据答题者的答案自动校验，无需人工干预，可自定义任意多个等级配置不同返回信息。

- 多用户和权限控制
> 支持多用户，可自由添加和删除用户。供群友共同贡献题库
> 
> 可通过用户组控制用户权限
> 
> 包括但不限于
> - 删改自己或他人的题目
> - 添加分区
> - 修改设置
> - 用户管理

- QBot API
> 可使用QBot接入RestAPI以实现自动进群审核
---

### 特色

- 动态生成答题，隐藏题目id，防止刷题，爆破题库
- 分区答题，自由度高
- 多用户支持，权限控制细化，更改冲突提醒，共建题库
- 多分数级别支持，划分层次
- 设置多样，后台管理页面配置简易，可自定义程度高
- 后台管理页面通信采用 WebSocket ，实时数据更新
- 响应式前端设计，适配各种尺寸的设备

---

<details>

<summary>
截图
</summary>

![1.png](/screenshots/2.0.1/screenshot(1).jpeg "1")
![2.png](/screenshots/2.0.1/screenshot(2).jpeg "2")
![3.png](/screenshots/2.0.1/screenshot(3).jpeg "3")
![4.png](/screenshots/2.0.1/screenshot(4).jpeg "4")
![5.png](/screenshots/2.0.1/screenshot(5).jpeg "5")
![6.png](/screenshots/2.0.1/screenshot(6).jpeg "6")
![7.png](/screenshots/2.0.1/screenshot(7).jpeg "7")
![8.png](/screenshots/2.0.1/screenshot(8).jpeg "8")
![9.png](/screenshots/2.0.1/screenshot(9).jpeg "9")
![10.png](/screenshots/2.0.1/screenshot(10).jpeg "10")
![11.png](/screenshots/2.0.1/screenshot(11).jpeg "11")
![12.png](/screenshots/2.0.1/screenshot(12).jpeg "12")
![13.png](/screenshots/2.0.1/screenshot(13).jpeg "13")
![14.png](/screenshots/2.0.1/screenshot(14).jpeg "14")
![15.png](/screenshots/2.0.1/screenshot(15).jpeg "15")
![16.png](/screenshots/2.0.1/screenshot(16).jpeg "16")
![17.png](/screenshots/2.0.1/screenshot(17).jpeg "17")
![18.png](/screenshots/2.0.1/screenshot(18).jpeg "18")
![19.png](/screenshots/2.0.1/screenshot(19).jpeg "19")
![20.png](/screenshots/2.0.1/screenshot(20).jpeg "20")
![21.png](/screenshots/2.0.1/screenshot(21).jpeg "21")
![22.png](/screenshots/2.0.1/screenshot(22).jpeg "22")
![23.png](/screenshots/2.0.1/screenshot(23).jpeg "23")
![24.png](/screenshots/2.0.1/screenshot(24).jpeg "24")
![25.png](/screenshots/2.0.1/screenshot(25).jpeg "25")

</details>

---

### 技术栈
- SpringBoot 和 Spring系列框架等 `后端`
- VUE 等 `前端`

---

### 试运行 ( DEMO )
无需任何操作，直接使用如下命令运行
```shell
java -jar checkIn-x.x.x.jar
```
预期行为：将会在内存中创建临时数据库，关闭服务器后销毁

> 若需体验常规存储模式（文件模式）请参考下文搭建教程中的更换数据库步骤

可在下方 Api 及端点处找到管理入口
> `初始用户` super admin 或 10000
> 
> `密码` 114514

---

### 搭建教程
1. 连接MYSQL并创建`check_in`数据库
2. 在jar包所在位置下创建application.properties并填入以下内容
> 由于添加了 demo 支持，需要手动指定数据库提供方及url
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/check_in?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=[your username]
spring.datasource.password=[your password]
```
---

<details>
<summary>如何更换 数据库名 或 数据库服务器</summary>

更改application.properties中的
```properties
spring.datasource.url
```

eg.
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/checkIn?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
```

> 内嵌支持的数据库
>
> MySQL
> ```properties
> spring.datasource.url=jdbc:mysql://localhost:3306/check_in?characterEncoding=utf8&allowPublicKeyRetrieval=true&useSSL=false
> ```
> H2Database (文件模式)
> ```properties
> spring.datasource.url=jdbc:h2:file:./check_in
> spring.datasource.username=root
> spring.datasource.password=root
> #===open web console(http://localhost:8080/h2-console)===
> #spring.h2.console.enabled=true
> #spring.h2.console.path=/h2-console
> ```
</details>

---

3. 运行jar包，将会自动生成数据库及初始数据
> 需要Java21

> 注意：此时需要命令行主目录在jar包所在目录下
> 
> 以cmd为例，若jar包在C:\checkIn\checkIn-x.x.x.jar下，则需要在cmd中输入
> ```shell
> cd C:\checkIn\
> ```
> 或者参考网上其他修改springboot properties配置目录的方法

```shell
java -jar checkIn-x.x.x.jar
```
> `初始用户` super admin
>
> `密码` 114514


### API及端点
- 答题 `8080:/checkIn/exam/`
- 管理 `8080:/checkIn/manage/` `8080:/checkIn/login/`
- QBot API `8080:/checkIn/api/qualify/`

> QBot API `POST`
> 
> Headers
> 
> > Token: 在高级设置中生成的json web token
> 
> Body
> ```json
> {
>  "qq": 123456789
> }
> ```
> response
>
> > <details> 
> > <summary>失败示例（用户未答题）</summary>
> >
> > ```json 
> > {
> > "result": "examData not found",
> > "type": "error"
> > }
> > ```
> > 
> > </details>
>
> > <details> 
> > <summary>成功示例1（答题通过）</summary>
> >
> > ```json
> > {
> > "examData": {
> > "id": "4fb9ec45-bffa-402e-92ff-078b993eb303",
> > "qqNumber": 10001,
> > "questionAmount": 10,
> > "status": "SUBMITTED",
> > "examResult": {
> > "qq": 10001,
> > "score": 100.0,
> > "correctCount": 10,
> > "halfCorrectCount": 0,
> > "wrongCount": 0,
> > "questionCount": 0,
> > "message": "[markdown text1]",
> > "level": "通过",
> > "colorHex": "#67C23A"
> > },
> > "generateTime": "2025-02-11 23:03:45",
> > "submitTime": "2025-02-11 23:03:58",
> > "expireTime": "2025-02-18 23:03:45"
> > },
> > "level": "通过",
> > "type": "success"
> > }
> > ```
> > 
> > </details>
>
> > <details> 
> > <summary>成功示例2（答题未通过）</summary>
> > 
> >  ```json
> >  {
> >  "examData": {
> >  "id": "9e2b4566-33c5-4279-9b76-0140926f5cab",
> >  "qqNumber": 10002,
> >  "questionAmount": 10,
> >  "status": "SUBMITTED",
> >  "examResult": {
> >  "qq": 10002,
> >  "score": 0.0,
> >  "correctCount": 0,
> >  "halfCorrectCount": 0,
> >  "wrongCount": 10,
> >  "questionCount": 0,
> >  "message": "[markdown text1]",
> >  "level": "未通过",
> >  "colorHex": "#F56C6C"
> >  },
> >  "generateTime": "2025-02-11 23:05:54",
> >  "submitTime": "2025-02-11 23:06:03",
> >  "expireTime": "2025-02-18 23:05:54"
> >  },
> >  "level": "未通过",
> >  "type": "success"
> >  }
> >  ```
> > 
> > </details>
---
<details>
<summary>如何修改默认的8080端口</summary>

在application.properties中添加
```properties
server.port=[your port]
```
</details>

---
