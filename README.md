# Web URL Copy

IntelliJ IDEA插件，帮助开发者快速生成当前Java方法的HTTP请求并复制到剪贴板。

## 2024.3.5 版本适配说明

针对 IntelliJ IDEA 2024.3.5 版本中出现的内存泄漏问题，本次更新做了如下修改：

1. 新增 `DisposerUtil` 工具类，用于正确处理 Disposable 对象
2. 修改 `ConfigToolWindowPanel` 类，使其实现 Disposable 接口并正确处理资源释放

## 功能特点

- 支持从右键菜单复制 Bash Curl 命令
- 支持从右键菜单复制 Python Request 代码
- 支持配置主机地址和请求头
- 支持 Spring MVC 和 JAX-RS 风格的 API

## 使用方法

1. 在编辑器中打开一个Spring MVC或JAX-RS控制器类
2. 右键点击方法名或类名
3. 在上下文菜单中选择"Web Copy URL" > "Copy Bash Curl"或"Copy Python Request"
4. 请求代码会被复制到剪贴板
5. 通过IDE右侧的"Web URL Config"工具窗口可以配置请求地址和头信息

## 配置

通过IDE右侧的"Web URL Config"工具窗口可以：

- 设置默认主机地址
- 添加/删除自定义请求头

## 支持的框架

- Spring MVC (@RestController, @Controller, @RequestMapping, @GetMapping等)
- JAX-RS (javax.ws.rs.Path, @GET, @POST等)

## 构建项目

```bash
./gradlew buildPlugin
```

## 安装插件

- 通过本地安装: 下载最新的发布版本或自行构建，然后在IntelliJ IDEA中从磁盘安装插件
- 通过插件市场搜索"Web URL Copy"进行安装

## 开发环境要求

- Java 17+
- IntelliJ IDEA (2022.2 - 2024.3.5)

## 许可证

[MIT License](LICENSE)