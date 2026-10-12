# 乡村情感物流平台 — REST API 文档 (v0.1 MVP)

Base URL: `http://localhost:8080`　统一响应：`{ "code": 0, "msg": "ok", "data": ... }`（`code!=0` 为业务错误）

## 1. 认证（MVP mock）

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | `/api/auth/login` | 手机号登录（内置演示账号） | body: `{phone}` → 返回 user |

演示账号：`13800000001` 张三(在深圳的子女) / `13900000002` 李大爷(村民) / `13700000003` 王婶(大湾址村站长) / `13600000004` 运营

## 2. 子女端（下单 & 跟踪）

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | `/api/orders` | 创建订单：SEND 代寄 / AGENT 代购（自动建运单+首条轨迹） | body 见下 |
| GET | `/api/orders?userId=` | 我的订单列表 | query |
| GET | `/api/orders/{id}` | 订单详情 = `{order, waybill, traces[], pods[], items[]}` | |
| GET | `/api/pods?userId=` | 送达朋友圈：该用户订单的签收回执流（照片/视频/语音转述） | |
| GET | `/api/stations?type=VILLAGE` | 收件村站点列表 | type 可选 COUNTY/TOWN/VILLAGE |

**POST /api/orders 请求体**

```json
{
  "type": "AGENT",              // SEND | AGENT
  "userId": 1,
  "senderName": "张三", "senderPhone": "13800000001",
  "receiverName": "李大爷", "receiverPhone": "13900000002",
  "receiverStationId": 4,       // 村级站点 id
  "itemDesc": "面粉10斤+钙片2瓶",
  "weight": 6.5,
  "goodsAmount": 89.00,         // 代购货款（AGENT 时）
  "freight": 6.00,
  "remark": "周末上午送",
  "items": [{"name":"高筋面粉","qty":1,"price":49.0}]
}
```

## 3. 站点端（到村闭环）

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | `/api/stations/{stationId}/inbound` | 入库扫描：按站点类型自动推进状态（县→AT_COUNTY，镇→AT_TOWN，村→AT_VILLAGE 派送中） | body: `{waybillNo}` |
| POST | `/api/stations/{stationId}/agri-receive` | 上行收件：村民交寄农产品，创建 AGRI 单 + 运单，状态 VILLAGE_RECEIVED | body: 同订单结构（sender=村民, receiverName=县城/目的地） |
| POST | `/api/orders/{waybillNo}/pod` | **签收拍照回传**：置 SIGNED + 生成回执（推送子女） | body: `{photos:[url], videoUrl, signedName, relayText}` |
| POST | `/api/waybills/{waybillNo}/dispatch` | 班线装车发运（CREATED 时置 PICKED，否则仅追加轨迹）并更新 current_station | body: `{routeId, stationId}` |

## 4. 班线 & 排班（运营端）

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | `/api/routes` | 班线列表（含站点链名称） | |
| GET | `/api/schedules?date=2026-10-12` | 某日班次（含线路名） | date 缺省=今天 |
| POST | `/api/schedules` | 新增班次 | body: `{routeId, busNo, departTime:"2026-10-12 08:00:00", capacity}` |
| GET | `/api/stats` | 运营大屏数据：今日单量/上行单/待签收/回执数/班线数 | |

## 5. 前端演示页（由后端静态目录直出）

| 页面 | URL | 角色 |
|------|-----|------|
| 子女端 | `/index.html` | 下单、订单轨迹时间线、送达朋友圈 |
| 站点端 | `/station.html` | 入库扫描、签收拍照回传、农产品收件 |
| 运营端 | `/admin.html` | 数据总览、班线班次 |

## 6. 错误码约定

| code | 含义 |
|------|------|
| 0 | 成功 |
| 1001 | 参数缺失 |
| 2001 | 运单/订单不存在 |
| 2002 | 状态不允许该操作（如已签收再签收） |
