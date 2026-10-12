<<<<<<< HEAD
# 乡村情感物流平台 MVP（乡村到村送）

农村包围城市打法的最小可运行原型：**子女城里下单 → 县域共配定时班线 → 到村入库派送 → 签收拍照回传子女（送达朋友圈） → 农产品上行收件**。

## 技术栈

| 层 | 选型 |
|----|------|
| 后端 | Java 8 + Spring Boot 2.7 + MyBatis-Plus 3.5 |
| 数据库 | MySQL 5.7 / 8.x（库名 `rural_logistics`） |
| 前端 | 原生 HTML/CSS/JS 三端演示页，由后端静态目录直出（零构建） |

## 目录结构

```
rural-logistics-mvp/
├── docs/
│   ├── 数据库设计.md      # ER 图、9 张表、订单状态机
│   └── API接口文档.md     # REST 接口清单与示例
├── sql/schema.sql         # 建库 DDL + 演示种子数据
└── backend/
    ├── pom.xml
    └── src/main/
        ├── java/com/runos/rurallog/   # entity / mapper / dto / service / controller
        └── resources/
            ├── application.yml        # 数据源、端口配置
            └── static/                # index.html(子女端) station.html(站点端) admin.html(运营端)
```

## 运行步骤（需要 JDK 8+、Maven 3.6+、MySQL）

```bash
# 1. 建库导数据
mysql -uroot -p < sql/schema.sql

# 2. 按本机修改 backend/src/main/resources/application.yml 的数据库账号密码（默认 root/root）

# 3. 启动
cd backend
mvn spring-boot:run
```

打开浏览器访问 `http://localhost:8080`。

## 演示脚本（3 分钟跑通闭环）

1. **子女端** `/index.html`：用 `13800000001`（张三）登录 → 提交代购订单 → 记下提示里的运单号（如 `WB2`）
   > 种子数据已带一条完整签收单 `RL20261010001`，登录即可看到时间线+签收照片效果
2. **站点端** `/station.html`：选「云岭县集散中心」→ 入库扫描 `WB2` → 选班线「草市北线」→ 装车发运；
   再选「大湾址村站点」→ 入库扫描 `WB2`（状态变为到村派送）→ **签收拍照回传**：填签收人、语音转述、勾选照片 → 上传
3. 回到**子女端**：订单详情出现完整轨迹时间线，「送达朋友圈」出现签收照片和老人的话
4. **上行支线**：站点端选村站 → 「农产品上行收件」登记土蜂蜜 → 到运营端 `/admin.html` 看今日上行单量、班线班次看板

## 状态机速查

```
下行: CREATED 已下单 → PICKED 已揽收 → AT_COUNTY 到县集散 → AT_TOWN 到乡镇 → AT_VILLAGE 到村派送 → SIGNED 已签收(生成POD)
上行: VILLAGE_RECEIVED 村站收寄 → AT_TOWN → AT_COUNTY → DISPATCHED 发往全国
```

## 下一步演进（离开 Demo 要做的事）

- 登录换成微信/手机号验证码；子女端迁到微信小程序，村民端做大字/语音模式
- 照片改走对象存储直传（OSS/COS）+ CDN；「送达朋友圈」拉取改订阅消息推送
- 接入通达系/极兔乡镇段共配 B2B 单，摊薄班线成本
- 排线从固定班线升级为 OR-Tools 路径优化；运费模板按重量/村庄分区计价
=======
#
乡村情感物流 + 县域生活服务平台
>>>>>>> 323aefdd15244febce096e25514a20d2c38ec01e
