# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table TBLNODE (
  dtype                     varchar(10) not null,
  ID                        integer not null,
  parent_ID                 integer,
  STARTHOUR                 integer,
  STARTMINUTE               integer,
  STOPHOUR                  integer,
  STOPMINUTE                integer,
  name                      varchar(255),
  index                     integer,
  constraint pk_TBLNODE primary key (ID))
;

create sequence TBLNODE_seq;

alter table TBLNODE add constraint fk_TBLNODE_parent_1 foreign key (parent_ID) references TBLNODE (ID) on delete restrict on update restrict;
create index ix_TBLNODE_parent_1 on TBLNODE (parent_ID);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists TBLNODE;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists TBLNODE_seq;

