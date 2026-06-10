-- ================================================================
-- 社区隐患排查系统 - 完整建表脚本
-- 版本: 1.0
-- 说明: 基于 RuoYi 框架, 3张业务表 + 字典 + 角色 + 菜单
-- ================================================================

-- ----------------------------
-- 1. 院落表（网格地理单元）
-- ----------------------------
drop table if exists ins_courtyard;
create table ins_courtyard (
  courtyard_id    bigint(20)      not null auto_increment    comment '院落ID',
  courtyard_name  varchar(50)     not null                   comment '院落名称',
  sort_order      int(4)          default 0                  comment '显示顺序',
  status          char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  remark          varchar(500)    default null               comment '备注',
  primary key (courtyard_id)
) engine=innodb auto_increment=100 comment = '院落/网格单元表';

-- 初始化13个院落
insert into ins_courtyard values(1,  '北一街',       1,  '0', 'admin', sysdate(), '', null, '');
insert into ins_courtyard values(2,  '北二街',       2,  '0', 'admin', sysdate(), '', null, '');
insert into ins_courtyard values(3,  '北新苑',       3,  '0', 'admin', sysdate(), '', null, '');
insert into ins_courtyard values(4,  '聚乐苑',       4,  '0', 'admin', sysdate(), '', null, '');
insert into ins_courtyard values(5,  '双林南支路',   5,  '0', 'admin', sysdate(), '', null, '');
insert into ins_courtyard values(6,  '136号院',      6,  '0', 'admin', sysdate(), '', null, '');
insert into ins_courtyard values(7,  '14号院',       7,  '0', 'admin', sysdate(), '', null, '');
insert into ins_courtyard values(8,  '44号院',       8,  '0', 'admin', sysdate(), '', null, '');
insert into ins_courtyard values(9,  '49号院',       9,  '0', 'admin', sysdate(), '', null, '');
insert into ins_courtyard values(10, '51号院',       10, '0', 'admin', sysdate(), '', null, '');
insert into ins_courtyard values(11, '8号院',        11, '0', 'admin', sysdate(), '', null, '');
insert into ins_courtyard values(12, '149号院',      12, '0', 'admin', sysdate(), '', null, '');
insert into ins_courtyard values(13, '名都楼',       13, '0', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 2. 地址库表
-- ----------------------------
drop table if exists ins_address;
create table ins_address (
  address_id      bigint(20)      not null auto_increment    comment '地址ID',
  courtyard_id    bigint(20)      not null                   comment '所属院落ID',
  address_name    varchar(200)    not null                   comment '地址全称',
  place_type      varchar(20)     default 'building'         comment '巡查类型（building/street/public/biz/site）',
  tenant_name     varchar(50)     default null               comment '预存承租人',
  tenant_phone    varchar(20)     default null               comment '预存联系电话',
  status          char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  remark          varchar(500)    default null               comment '备注',
  primary key (address_id),
  key idx_addr_courtyard (courtyard_id),
  key idx_addr_name (address_name)
) engine=innodb auto_increment=100 comment = '巡查地址库表';

-- 示例地址数据（北新苑）
insert into ins_address values(1,  3, '北新苑1号楼101',  'building', '王爱华', '13812340021', '0', 'admin', sysdate(), '', null, '');
insert into ins_address values(2,  3, '北新苑1号楼201',  'building', '张大勇', '15577888877', '0', 'admin', sysdate(), '', null, '');
insert into ins_address values(3,  3, '北新苑1号楼301',  'building', null,       null,          '0', 'admin', sysdate(), '', null, '');
insert into ins_address values(4,  3, '北新苑2号楼101',  'building', '李明华',  '13987654321', '0', 'admin', sysdate(), '', null, '');
insert into ins_address values(5,  3, '北新苑3号楼201',  'building', null,       null,          '0', 'admin', sysdate(), '', null, '');
insert into ins_address values(6,  3, '北新苑西门岗亭',  'public',   null,       null,          '0', 'admin', sysdate(), '', null, '');
-- 北一街
insert into ins_address values(7,  1, '北一街28号1栋',   'building', '赵国庆',  '13812341001', '0', 'admin', sysdate(), '', null, '');
insert into ins_address values(8,  1, '北一街28号2栋',   'building', null,       null,          '0', 'admin', sysdate(), '', null, '');
insert into ins_address values(9,  1, '北一街东段路口',  'street',   null,       null,          '0', 'admin', sysdate(), '', null, '');
-- 聚乐苑
insert into ins_address values(10, 4, '聚乐苑1栋1单元',  'building', '钱爱军',  '13812341002', '0', 'admin', sysdate(), '', null, '');
insert into ins_address values(11, 4, '聚乐苑中心广场',  'public',   null,       null,          '0', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 3. 巡查日志表
-- ----------------------------
drop table if exists ins_log;
create table ins_log (
  log_id           bigint(20)      not null auto_increment    comment '日志ID',
  log_code         varchar(50)     not null                   comment '单据流水号',
  courtyard_id     bigint(20)      not null                   comment '所属院落ID（提交时快照）',
  address_id       bigint(20)      default null               comment '地址库ID',
  address_name     varchar(200)    not null                   comment '巡查地址',
  main_type        varchar(20)     not null                   comment '巡查类型（building/street/public/biz/site）',
  sub_type         varchar(20)     default null               comment '巡查项目（gas/fire/tenant/solo/guard/other）',
  inspect_result   varchar(20)     not null                   comment '巡查结果（pass/hazard/rectified）',
  hazard_level     varchar(20)     default null               comment '隐患级别（general/major/critical）',
  description      text                                       comment '巡查文字描述',
  tenant_name      varchar(50)     default null               comment '承租人',
  tenant_phone     varchar(20)     default null               comment '联系电话',
  inspect_time     datetime        not null                   comment '巡查时间',
  create_by        varchar(64)     default ''                 comment '巡查人（微网格员用户名）',
  create_time      datetime                                   comment '创建时间',
  update_by        varchar(64)     default ''                 comment '更新者',
  update_time      datetime                                   comment '更新时间',
  remark           varchar(500)    default null               comment '备注',
  primary key (log_id),
  key idx_log_courtyard (courtyard_id),
  key idx_log_time (inspect_time),
  key idx_log_result (inspect_result),
  key idx_log_maintype (main_type),
  key idx_log_subtype (sub_type),
  key idx_log_user (create_by)
) engine=innodb auto_increment=100 comment = '巡查日志表';

-- ----------------------------
-- 4. 巡查照片表
-- ----------------------------
drop table if exists ins_log_photo;
create table ins_log_photo (
  photo_id     bigint(20)      not null auto_increment    comment '照片ID',
  log_id       bigint(20)      not null                   comment '日志ID',
  photo_url    varchar(255)    not null                   comment '照片路径',
  sort_order   int(4)          default 0                  comment '排序',
  primary key (photo_id),
  key idx_photo_log (log_id)
) engine=innodb auto_increment=100 comment = '巡查照片表';

-- ----------------------------
-- 5. 修改 sys_user 表，增加院落关联
-- ----------------------------
-- 检测并添加字段（MySQL 没有 IF NOT EXISTS for columns，用存储过程）
drop procedure if exists add_courtyard_column;
delimiter $$
create procedure add_courtyard_column()
begin
  if not exists (select * from information_schema.columns
    where table_schema = database() and table_name = 'sys_user' and column_name = 'courtyard_id') then
    alter table sys_user add column courtyard_id bigint(20) default null comment '所属院落ID';
  end if;
end$$
delimiter ;
call add_courtyard_column();
drop procedure if exists add_courtyard_column;

-- ----------------------------
-- 6. 字典类型
-- ----------------------------
insert into sys_dict_type values(20, '巡查类型',   'ins_place_type',     '0', 'admin', sysdate(), '', null, '巡查类型：楼栋/街巷/公共场所/商企/工地');
insert into sys_dict_type values(21, '巡查项目',   'ins_inspect_item',   '0', 'admin', sysdate(), '', null, '巡查项目：燃气/消防/租户/独居/门卫/其他');
insert into sys_dict_type values(22, '巡查结果',   'ins_inspect_result', '0', 'admin', sysdate(), '', null, '巡查结果：合格/存在隐患/已整改');
insert into sys_dict_type values(23, '隐患级别',   'ins_hazard_level',   '0', 'admin', sysdate(), '', null, '隐患级别：一般/较大/重大');

-- ----------------------------
-- 7. 字典数据
-- ----------------------------
-- 巡查类型
insert into sys_dict_data values(200, 1, '楼栋',     'building', 'ins_place_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(201, 2, '街巷',     'street',   'ins_place_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(202, 3, '公共场所', 'public',   'ins_place_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(203, 4, '商企',     'biz',      'ins_place_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(204, 5, '工地',     'site',     'ins_place_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- 巡查项目
insert into sys_dict_data values(210, 1, '燃气', 'gas',    'ins_inspect_item', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(211, 2, '消防', 'fire',   'ins_inspect_item', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(212, 3, '租户', 'tenant', 'ins_inspect_item', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(213, 4, '独居', 'solo',   'ins_inspect_item', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(214, 5, '门卫', 'guard',  'ins_inspect_item', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(215, 6, '其他', 'other',  'ins_inspect_item', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- 巡查结果
insert into sys_dict_data values(220, 1, '合格',     'pass',      'ins_inspect_result', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(221, 2, '存在隐患', 'hazard',    'ins_inspect_result', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(222, 3, '已整改',   'rectified', 'ins_inspect_result', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '');

-- 隐患级别
insert into sys_dict_data values(230, 1, '一般', 'general',  'ins_hazard_level', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(231, 2, '较大', 'major',    'ins_hazard_level', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(232, 3, '重大', 'critical', 'ins_hazard_level', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 8. 角色数据
-- ----------------------------
insert into sys_role values(3, '网格员',   'grid_manager', 3, 1, 1, 1, '0', '0', 'admin', sysdate(), '', null, '网格员（PC端管理员，管理院落和微网格员）');
insert into sys_role values(4, '微网格员', 'micro_grid',   4, 5, 1, 1, '0', '0', 'admin', sysdate(), '', null, '微网格员（H5端，执行巡查任务）');

-- ----------------------------
-- 9. 菜单权限
-- ----------------------------
-- 一级菜单
insert into sys_menu values(200, '巡查管理', '0',  '4', 'inspect',  null, '', '', 1, 0, 'M', '0', '0', '',        'monitor',  'admin', sysdate(), '', null, '巡查管理目录');
-- 二级菜单
insert into sys_menu values(201, '巡查总览', '200', '1', 'dashboard', 'inspect/dashboard/index',  '', '', 1, 0, 'C', '0', '0', 'inspect:dashboard:list',  'dashboard', 'admin', sysdate(), '', null, '巡查总览');
insert into sys_menu values(202, '地址库管理', '200', '2', 'address',  'inspect/address/index',   '', '', 1, 0, 'C', '0', '0', 'inspect:address:list',    'list',      'admin', sysdate(), '', null, '巡查地址库管理');
insert into sys_menu values(203, '租户档案', '200',  '3', 'tenant',   'inspect/tenant/index',    '', '', 1, 0, 'C', '0', '0', 'inspect:tenant:list',     'peoples',   'admin', sysdate(), '', null, '租户信息档案');

-- 地址库按钮
insert into sys_menu values(204, '地址查询', '202', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'inspect:address:query',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(205, '地址新增', '202', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'inspect:address:add',     '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(206, '地址修改', '202', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'inspect:address:edit',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(207, '地址删除', '202', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'inspect:address:remove',  '#', 'admin', sysdate(), '', null, '');
-- 日志操作按钮
insert into sys_menu values(208, '日志订正', '201', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'inspect:log:edit',       '#', 'admin', sysdate(), '', null, '管理员订正巡查日志');
insert into sys_menu values(209, '日志导出', '201', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'inspect:log:export',     '#', 'admin', sysdate(), '', null, '导出巡查日志Excel');
-- 租户档案按钮
insert into sys_menu values(210, '租户查询', '203', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'inspect:tenant:query',   '#', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 10. 角色菜单关联
-- ----------------------------
-- 网格员角色：分配巡查管理所有菜单
insert into sys_role_menu values(3, 200);
insert into sys_role_menu values(3, 201);
insert into sys_role_menu values(3, 202);
insert into sys_role_menu values(3, 203);
insert into sys_role_menu values(3, 204);
insert into sys_role_menu values(3, 205);
insert into sys_role_menu values(3, 206);
insert into sys_role_menu values(3, 207);
insert into sys_role_menu values(3, 208);
insert into sys_role_menu values(3, 209);
insert into sys_role_menu values(3, 210);
-- 微网格员角色：不分配PC菜单（H5端通过API鉴权）
