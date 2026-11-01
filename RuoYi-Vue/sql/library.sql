-- ----------------------------
-- 图书管理系统 建表脚本
-- 注意：会先删除同名表，表里已有数据时不要直接执行
-- ----------------------------

-- ----------------------------
-- 1、图书分类表
-- ----------------------------
drop table if exists lib_category;
create table lib_category (
  category_id    bigint(20)     not null auto_increment  comment '分类ID',
  parent_id      bigint(20)     default 0                comment '父分类ID',
  category_name  varchar(50)    not null                 comment '分类名称',
  order_num      int(4)         default 0                comment '显示顺序',
  status         char(1)        default '0'              comment '状态（0正常 1停用）',
  create_by      varchar(64)    default ''               comment '创建者',
  create_time    datetime                                comment '创建时间',
  update_by      varchar(64)    default ''               comment '更新者',
  update_time    datetime                                comment '更新时间',
  remark         varchar(500)   default null             comment '备注',
  primary key (category_id)
) engine=innodb comment = '图书分类表';

-- ----------------------------
-- 2、图书信息表
-- ----------------------------
drop table if exists lib_book;
create table lib_book (
  book_id        bigint(20)     not null auto_increment  comment '图书ID',
  category_id    bigint(20)     default null             comment '分类ID',
  isbn           varchar(20)    default ''               comment 'ISBN',
  book_name      varchar(100)   not null                 comment '书名',
  author         varchar(50)    default ''               comment '作者',
  publisher      varchar(100)   default ''               comment '出版社',
  cover_image    varchar(255)   default ''               comment '封面',
  total_count    int(11)        default 0                comment '馆藏总数',
  stock_count    int(11)        default 0                comment '可借数量',
  status         char(1)        default '0'              comment '状态（0上架 1下架）',
  create_by      varchar(64)    default ''               comment '创建者',
  create_time    datetime                                comment '创建时间',
  update_by      varchar(64)    default ''               comment '更新者',
  update_time    datetime                                comment '更新时间',
  remark         varchar(500)   default null             comment '备注',
  primary key (book_id)
) engine=innodb comment = '图书信息表';

-- ----------------------------
-- 3、借阅记录表
-- ----------------------------
drop table if exists lib_borrow;
create table lib_borrow (
  borrow_id      bigint(20)     not null auto_increment  comment '借阅ID',
  book_id        bigint(20)     not null                 comment '图书ID',
  user_id        bigint(20)     not null                 comment '读者ID',
  borrow_time    datetime                                comment '借出时间',
  due_time       datetime                                comment '应还时间',
  return_time    datetime                                comment '归还时间',
  status         char(1)        default '0'              comment '状态（0借阅中 1已归还 2已逾期）',
  create_by      varchar(64)    default ''               comment '创建者',
  create_time    datetime                                comment '创建时间',
  update_by      varchar(64)    default ''               comment '更新者',
  update_time    datetime                                comment '更新时间',
  remark         varchar(500)   default null             comment '备注',
  primary key (borrow_id)
) engine=innodb comment = '借阅记录表';
