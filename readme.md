# CheckIn

#### 更易用的入群测试系统
![Static Badge](https://img.shields.io/badge/Author-Etern-cyan)
![Static Badge](https://img.shields.io/badge/Language-Java-red)
![Static Badge](https://img.shields.io/badge/JavaRequire-21-red)
![Static Badge](https://img.shields.io/badge/Version-1.0.0-blue)
![Static Badge](https://img.shields.io/badge/Status-Developing-green)
---

### 简介

`CheckIn` 是一个入群测试系统，用于对有门槛的群聊进行测试，以保证群内成员的素质。

### 功能

- 完善的答题日志
> 答题日志将会记录每一次的答题情况，以便管理员进行查看。
>
> #### 可查看
> - QQ号码 `由答题者输入，不会被校验是否存在及是否为所有者本人`
> - 头像 `根据QQ号获取`
> - 访问时间
> - IP地址 `支持IPV4和IPV6`
> - 抽取到的题目
> - 分数及是否通过
> - 请求头及Attributes

- 分区答题
> 可自建分区，支持一题多分区。
> 
> 答题者可自由选择分区作答。
> 
> 提供设置控制最小选择数和最大选择数。
> 
> 目前仅提供单选和多选题。 `必须全对才给分`
> > #### 未来计划加入
> > - 题组
> > - 填空题

- 自动校验
> 根据答题者的答案折算至百分自动校验，无需人工干预，可自定义通过分数以通过和未通过的消息。

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

- 精美的UI
> 动画全覆盖，简洁易用

### 技术栈
- SpringBoot 和 Spring系列框架 `后端`
- Thymeleaf `前端`
- ECharts `前端图表`
- JQuery `前端`
- Lombok `简化代码`
- MYSQL `数据库`

---
<details>
<summary>如何更换数据库名即数据库服务器(for dev)</summary>

更改application.properties中的
```properties
spring.datasource.url
spring.datasource.username
spring.datasource.password
```

eg.
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/checkIn?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=114514
```

并手工修改源码中的pom.xml并添加jdbc驱动后构建打包
参考SpringData更换数据库的方式
</details>

---

### 截图

<details>
<summary>详情</summary>

![1.png](/screenshots/1.png "1")
![2.png](/screenshots/2.png "2")
![3.png](/screenshots/3.png "3")
![4.png](/screenshots/4.png "4")
![5.png](/screenshots/5.png "5")
![6.png](/screenshots/6.png "6")
![7.png](/screenshots/7.png "7")
![8.png](/screenshots/8.png "8")
![9.png](/screenshots/9.png "9")

</details>

### 搭建教程
1. 连接MYSQL并创建`checkIn`数据库
2. 执行initialSQL文件夹内的sql语句
3. 在jar包所在位置下创建application.properties并填入以下内容
```properties
spring.datasource.username=[your username]
spring.datasource.password=[your password]
```
4. 在jar包下新建文件夹`data`并授予读写权限
5. 运行jar包
> 推荐Java21版本 理论上17以上皆可

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
> 初始用户为admin 密码为00000000

### API及端点
- 答题 `8080:/checkIn/exam/`
- 管理 `8080:/checkIn/manage/` `8080:/checkIn/login/`
- QBot API `8080:/checkIn/api/check/`

post
```json
{
  "qq": 123456789,
  "token": "[your token]"
}
```
---
<details>
<summary>如何修改默认的8080端口</summary>

在application.properties中添加
```properties
server.port=[your port]
```
</details>

---