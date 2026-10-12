-- ============================================================
-- 乡村情感物流平台 MVP — MySQL 8.x / 5.7 建库脚本
-- 执行: mysql -uroot -p < sql/schema.sql
-- ============================================================
CREATE DATABASE IF NOT EXISTS rural_logistics DEFAULT CHARSET utf8mb4;
USE rural_logistics;

DROP TABLE IF EXISTS t_user, t_station, t_order, t_order_item,
  t_waybill, t_trace, t_pod, t_route, t_schedule;

-- 站点三级网络：县域集散 / 乡镇hub / 村级站点
CREATE TABLE t_station (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(64)  NOT NULL,
  type        VARCHAR(16)  NOT NULL COMMENT 'COUNTY/TOWN/VILLAGE',
  parent_id   BIGINT       NULL     COMMENT '上级站点',
  contact     VARCHAR(32)  NULL,
  phone       VARCHAR(20)  NULL,
  address     VARCHAR(128) NULL
) COMMENT '站点';

-- 用户：SENDER子女 / VILLAGER村民 / STATION_MASTER站长 / OPS运营
CREATE TABLE t_user (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  name       VARCHAR(32) NOT NULL,
  phone      VARCHAR(20) NOT NULL UNIQUE,
  role       VARCHAR(20) NOT NULL,
  village_id BIGINT      NULL COMMENT '关联村站点',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '用户';

-- 订单主表
CREATE TABLE t_order (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_no           VARCHAR(32) NOT NULL UNIQUE,
  type               VARCHAR(16) NOT NULL COMMENT 'SEND代寄/AGENT代购/AGRI农产品上行',
  direction          VARCHAR(8)  NOT NULL COMMENT 'DOWN/UP',
  status             VARCHAR(24) NOT NULL COMMENT '见状态机',
  user_id            BIGINT      NULL COMMENT '下单人',
  sender_name        VARCHAR(32),
  sender_phone       VARCHAR(20),
  receiver_name      VARCHAR(32),
  receiver_phone     VARCHAR(20),
  receiver_station_id BIGINT     NULL COMMENT '目的村站(下行) / 收件村站(上行)',
  item_desc          VARCHAR(256),
  weight             DECIMAL(8,2) DEFAULT 0,
  goods_amount       DECIMAL(10,2) DEFAULT 0 COMMENT '代购货款/农产品收购值',
  freight            DECIMAL(10,2) DEFAULT 0 COMMENT '运费',
  remark             VARCHAR(256),
  created_at         DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_user (user_id), KEY idx_status (status)
) COMMENT '订单';

CREATE TABLE t_order_item (
  id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  name     VARCHAR(64),
  spec     VARCHAR(64),
  qty      INT DEFAULT 1,
  price    DECIMAL(10,2) DEFAULT 0,
  KEY idx_order (order_id)
) COMMENT '订单明细';

-- 运单（与订单 1:1）
CREATE TABLE t_waybill (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  waybill_no        VARCHAR(32) NOT NULL UNIQUE,
  order_id          BIGINT NOT NULL,
  route_id          BIGINT NULL COMMENT '承运班线',
  current_station_id BIGINT NULL,
  status            VARCHAR(24),
  created_at        DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_order (order_id)
) COMMENT '运单';

-- 轨迹事件
CREATE TABLE t_trace (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  waybill_id  BIGINT NOT NULL,
  station_id  BIGINT NULL,
  action      VARCHAR(32) NOT NULL COMMENT '与订单status同枚举',
  description VARCHAR(256),
  operator_id BIGINT NULL,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_waybill (waybill_id)
) COMMENT '物流轨迹';

-- 签收回执（核心差异化：照片/视频回传给城里子女）
CREATE TABLE t_pod (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id    BIGINT NOT NULL,
  waybill_id  BIGINT NOT NULL,
  photos      TEXT COMMENT 'JSON数组: ["url1","url2"]',
  video_url   VARCHAR(256),
  signed_name VARCHAR(32),
  relay_text  VARCHAR(256) COMMENT '语音转述, 如"爹收到啦, 东西都齐"',
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_order (order_id)
) COMMENT '签收回执';

-- 定时班线
CREATE TABLE t_route (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(64) NOT NULL,
  description VARCHAR(256),
  stations    VARCHAR(256) NOT NULL COMMENT '站点id链, 逗号分隔: 1,2,4,5'
) COMMENT '班线';

-- 班次
CREATE TABLE t_schedule (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  route_id    BIGINT NOT NULL,
  bus_no      VARCHAR(16),
  depart_time DATETIME NOT NULL,
  capacity    INT DEFAULT 200,
  status      VARCHAR(16) DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/FINISHED',
  KEY idx_route (route_id)
) COMMENT '班次';

-- ============================================================
-- 演示种子数据
-- ============================================================
INSERT INTO t_station (id, name, type, parent_id, contact, phone, address) VALUES
 (1, '云岭县集散中心',   'COUNTY',  NULL, '赵经理', '13600000011', '云岭县物流园区3号库'),
 (2, '草市镇hub',        'TOWN',    1,    '刘掌柜', '13600000012', '草市镇老街客运站旁'),
 (3, '青溪镇hub',        'TOWN',    1,    '陈哥',   '13600000013', '青溪镇集市路口'),
 (4, '大湾址村站点',     'VILLAGE', 2,    '王婶',   '13700000003', '大湾址村村头小卖部'),
 (5, '石桥村站点',       'VILLAGE', 2,    '周木匠', '13700000005', '石桥村桥东第一户'),
 (6, '云上村站点',       'VILLAGE', 2,    '吴婶',   '13700000006', '云上村村委对面'),
 (7, '青溪村站点',       'VILLAGE', 3,    '徐会计', '13700000007', '青溪村小学旁'),
 (8, '柳湾村站点',       'VILLAGE', 3,    '孙婶',   '13700000008', '柳湾村湾口老槐树');

INSERT INTO t_user (id, name, phone, role, village_id) VALUES
 (1, '张三', '13800000001', 'SENDER',          NULL),
 (2, '李大爷', '13900000002', 'VILLAGER',      4),
 (3, '王婶', '13700000003', 'STATION_MASTER',  4),
 (4, '运营管理员', '13600000004', 'OPS',       NULL);

INSERT INTO t_route (id, name, description, stations) VALUES
 (1, '草市北线', '县集散→草市镇→大湾址→石桥→云上', '1,2,4,5,6'),
 (2, '青溪南线', '县集散→青溪镇→青溪村→柳湾村',     '1,3,7,8');

INSERT INTO t_schedule (route_id, bus_no, depart_time, capacity) VALUES
 (1, '云L·86ha', '2026-10-12 07:30:00', 300),
 (1, '云L·22oh', '2026-10-12 13:30:00', 200),
 (2, '云L·55fa', '2026-10-12 08:00:00', 150);

-- 一条已完成演示订单（含运单/轨迹/回执），打开子女端即可看到完整闭环效果
INSERT INTO t_order (id, order_no, type, direction, status, user_id,
  sender_name, sender_phone, receiver_name, receiver_phone, receiver_station_id,
  item_desc, weight, goods_amount, freight, remark) VALUES
 (1, 'RL20261010001', 'AGENT', 'DOWN', 'SIGNED', 1,
  '张三', '13800000001', '李大爷', '13900000002', 4,
  '高筋面粉10斤 + 血压计1台', 6.5, 168.00, 6.00, '老人耳背, 到村大声说');

INSERT INTO t_order_item (order_id, name, qty, price) VALUES
 (1, '高筋面粉10斤', 1, 49.00), (1, '电子血压计', 1, 119.00);

INSERT INTO t_waybill (id, waybill_no, order_id, route_id, current_station_id, status) VALUES
 (1, 'WB1', 1, 1, 4, 'SIGNED');

INSERT INTO t_trace (waybill_id, station_id, action, description) VALUES
 (1, NULL, 'CREATED',       '子女已下单, 县城代购点备货'),
 (1, 1,    'AT_COUNTY',     '集散中心验货入库'),
 (1, 2,    'AT_TOWN',       '草市镇hub分拣, 搭乘北线班车'),
 (1, 4,    'AT_VILLAGE',    '大湾址村站点入库, 派送中'),
 (1, 4,    'SIGNED',        '李大爷本人签收, 照片已回传');

INSERT INTO t_pod (order_id, waybill_id, photos, video_url, signed_name, relay_text) VALUES
 (1, 1, '["img/pod1.svg","img/pod2.svg"]', NULL, '李大爷', '爹收到啦, 面粉够用一冬天, 血压计也教会了');
