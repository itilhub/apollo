## Apollo 个人总结


### 项目模块
```
apollo
    |___ apollo-adminservice
    |                           [核心模块] Apllo管理服务 
    |                           [服务能力] 配置管理接口、配置修改、发布接口、服务Portal
    |
    |___ apollo-assembly 
    |                           [聚合模块] Apllo核心模块装配服
    |                           [服务能力] 将Apllo管理服务和Apllo配置服务聚合装配统一启动
    |                           [启动参考] 配置启动参数
    |                                       设置VM options 为github 并传入数据库连接信息 也可直接修改common模块下的*-github.properties配置信息
    |                                       设置Program arguments 为--configservice --adminservice 此设置将此参数传入main方法args参数中             
    |                   
    |___ apollo-biz
    |
    |___ apollo-buildtools
    |
    |___ apollo-client
    |                           [服务模块] Apollo客户端
    |                           [服务能力] 应用获取配置，实时更新，
    |
    |___ apollo-common
    |
    |___ apollo-configservice 
    |                           [核心模块] Apollo配置服务 
    |                           [服务能力] 配置获取接口、配置推送接口、服务Apollo客户端
    |                                     通过注册中心可获取ConfigService服务列表，客户端软负载
    |
    |___ apollo-core
    |
    |___ apollo-demo
    |                           [演示模块] Apollo客户端使用演示
    |                           [服务能力] 依赖于apollo-client模块
    |
    |___ apollo-mockserver
    |
    |___ apollo-openapi
    |                           [内部模块] Apollo核心服务对内提供API封装模块，类似有请求客户端 
    |                           [服务能力] 服务Apollo-portal模块
    |
    |___ apollo-portal
    |                           [核心模块] Apollo配置管理门户 
    |                           [服务能力] 依赖于apollo-adminservice模块，提供配置管理交互web页面
    |                                     通过注册中心可获取AdminService服务列表，客户端软负载
    |
    |___ scripts  项目脚本管理
    |       |_apollo-on-kubernetes                     
    |       |_db flyway管理  Mavne运行命令 mvn -N -Pportaldb flyway:migrate || mvn -N -Pportaldb flyway:migrate


```
