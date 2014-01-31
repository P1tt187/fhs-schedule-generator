# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table TBLLECTURE (
  ID                        bigint not null,
  fk_lecture                bigint not null,
  NAME                      varchar(255),
  constraint pk_TBLLECTURE primary key (ID))
;

create table TBLNODE (
  dtype                     varchar(10) not null,
  ID                        integer not null,
  parent_ID                 integer,
  STARTHOUR                 integer,
  STARTMINUTE               integer,
  STOPHOUR                  integer,
  STOPMINUTE                integer,
  TOLERANCE                 boolean,
  NAME                      varchar(255),
  INDEX                     integer,
  constraint pk_TBLNODE primary key (ID))
;

create table TBLPARALLELLECTURE (
  ID                        bigint not null,
  constraint pk_TBLPARALLELLECTURE primary key (ID))
;

create table TBLParticipants (
  dtype                     varchar(10) not null,
  id                        bigint not null,
  SIZE                      integer,
  lecture_ID                bigint,
  parent_id                 bigint,
  course_id                 bigint,
  NAME                      varchar(255),
  constraint pk_TBLParticipants primary key (id))
;

create sequence TBLLECTURE_seq;

create sequence TBLNODE_seq;

create sequence TBLPARALLELLECTURE_seq;

create sequence TBLParticipants_seq;

alter table TBLLECTURE add constraint fk_TBLLECTURE_TBLPARALLELLECTU_1 foreign key (fk_lecture) references TBLPARALLELLECTURE (ID) on delete restrict on update restrict;
create index ix_TBLLECTURE_TBLPARALLELLECTU_1 on TBLLECTURE (fk_lecture);
alter table TBLNODE add constraint fk_TBLNODE_parent_2 foreign key (parent_ID) references TBLNODE (ID) on delete restrict on update restrict;
create index ix_TBLNODE_parent_2 on TBLNODE (parent_ID);
alter table TBLParticipants add constraint fk_TBLParticipants_lecture_3 foreign key (lecture_ID) references TBLLECTURE (ID) on delete restrict on update restrict;
create index ix_TBLParticipants_lecture_3 on TBLParticipants (lecture_ID);
alter table TBLParticipants add constraint fk_TBLParticipants_parent_4 foreign key (parent_id) references TBLParticipants (id) on delete restrict on update restrict;
create index ix_TBLParticipants_parent_4 on TBLParticipants (parent_id);
alter table TBLParticipants add constraint fk_TBLParticipants_course_5 foreign key (course_id) references TBLParticipants (id) on delete restrict on update restrict;
create index ix_TBLParticipants_course_5 on TBLParticipants (course_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists TBLLECTURE;

drop table if exists TBLNODE;

drop table if exists TBLPARALLELLECTURE;

drop table if exists TBLParticipants;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists TBLLECTURE_seq;

drop sequence if exists TBLNODE_seq;

drop sequence if exists TBLPARALLELLECTURE_seq;

drop sequence if exists TBLParticipants_seq;

