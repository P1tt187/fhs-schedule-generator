# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table TBLDOCENT (
  ID                        bigint not null,
  FIRSTNAME                 varchar(255),
  LASTNAME                  varchar(255),
  constraint pk_TBLDOCENT primary key (ID))
;

create table TBLLECTURE (
  ID                        bigint not null,
  fk_lecture                bigint not null,
  NAME                      varchar(255) not null,
  fk_docent                 bigint not null,
  constraint pk_TBLLECTURE primary key (ID))
;

create table TBLNODE (
  dtype                     varchar(10) not null,
  ID                        integer not null,
  parent_ID                 integer,
  NAME                      varchar(255),
  INDEX                     integer,
  STARTHOUR                 integer,
  STARTMINUTE               integer,
  STOPHOUR                  integer,
  STOPMINUTE                integer,
  constraint pk_TBLNODE primary key (ID))
;

create table TBLPARALLELLECTURE (
  ID                        bigint not null,
  constraint pk_TBLPARALLELLECTURE primary key (ID))
;

create table TBLParticipants (
  dtype                     varchar(10) not null,
  id                        bigint not null,
  SIZE                      integer not null,
  lecture_ID                bigint,
  NAME                      varchar(255) not null,
  parent_id                 bigint,
  course_id                 bigint,
  constraint pk_TBLParticipants primary key (id))
;

create table TBLROOM (
  ID                        bigint not null,
  CAPACITY                  integer not null,
  HOUSE                     varchar(255),
  NUMBER                    integer not null,
  TOLERANCE                 boolean not null,
  PCPOOL                    boolean not null,
  BEAMER                    boolean not null,
  constraint pk_TBLROOM primary key (ID))
;

create table TBLTIMESLOTCRITERIA (
  ID                        bigint not null,
  TOLERANCE                 boolean not null,
  STARTHOUR                 integer not null,
  STARTMINUTE               integer not null,
  STOPHOUR                  integer not null,
  STOPMINUTE                integer not null,
  fk_weekday                integer not null,
  constraint pk_TBLTIMESLOTCRITERIA primary key (ID))
;

create sequence TBLDOCENT_seq;

create sequence TBLLECTURE_seq;

create sequence TBLNODE_seq;

create sequence TBLPARALLELLECTURE_seq;

create sequence TBLParticipants_seq;

create sequence TBLROOM_seq;

create sequence TBLTIMESLOTCRITERIA_seq;

alter table TBLLECTURE add constraint fk_TBLLECTURE_TBLPARALLELLECTU_1 foreign key (fk_lecture) references TBLPARALLELLECTURE (ID) on delete restrict on update restrict;
create index ix_TBLLECTURE_TBLPARALLELLECTU_1 on TBLLECTURE (fk_lecture);
alter table TBLLECTURE add constraint fk_TBLLECTURE_docent_2 foreign key (fk_docent) references TBLDOCENT (ID) on delete restrict on update restrict;
create index ix_TBLLECTURE_docent_2 on TBLLECTURE (fk_docent);
alter table TBLNODE add constraint fk_TBLNODE_parent_3 foreign key (parent_ID) references TBLNODE (ID) on delete restrict on update restrict;
create index ix_TBLNODE_parent_3 on TBLNODE (parent_ID);
alter table TBLParticipants add constraint fk_TBLParticipants_lecture_4 foreign key (lecture_ID) references TBLLECTURE (ID) on delete restrict on update restrict;
create index ix_TBLParticipants_lecture_4 on TBLParticipants (lecture_ID);
alter table TBLParticipants add constraint fk_TBLParticipants_parent_5 foreign key (parent_id) references TBLParticipants (id) on delete restrict on update restrict;
create index ix_TBLParticipants_parent_5 on TBLParticipants (parent_id);
alter table TBLParticipants add constraint fk_TBLParticipants_course_6 foreign key (course_id) references TBLParticipants (id) on delete restrict on update restrict;
create index ix_TBLParticipants_course_6 on TBLParticipants (course_id);
alter table TBLTIMESLOTCRITERIA add constraint fk_TBLTIMESLOTCRITERIA_weekday_7 foreign key (fk_weekday) references TBLNODE (ID) on delete restrict on update restrict;
create index ix_TBLTIMESLOTCRITERIA_weekday_7 on TBLTIMESLOTCRITERIA (fk_weekday);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists TBLDOCENT;

drop table if exists TBLLECTURE;

drop table if exists TBLNODE;

drop table if exists TBLPARALLELLECTURE;

drop table if exists TBLParticipants;

drop table if exists TBLROOM;

drop table if exists TBLTIMESLOTCRITERIA;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists TBLDOCENT_seq;

drop sequence if exists TBLLECTURE_seq;

drop sequence if exists TBLNODE_seq;

drop sequence if exists TBLPARALLELLECTURE_seq;

drop sequence if exists TBLParticipants_seq;

drop sequence if exists TBLROOM_seq;

drop sequence if exists TBLTIMESLOTCRITERIA_seq;

