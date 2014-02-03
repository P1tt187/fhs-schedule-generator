# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table TBLDOCENT (
  ID                        bigint auto_increment not null,
  FIRSTNAME                 varchar(255),
  LASTNAME                  varchar(255),
  constraint pk_TBLDOCENT primary key (ID))
;

create table TBLLECTURE (
  ID                        bigint auto_increment not null,
  fk_lecture                bigint not null,
  NAME                      varchar(255) not null,
  fk_docent                 bigint not null,
  constraint pk_TBLLECTURE primary key (ID))
;

create table TBLNODE (
  dtype                     varchar(10) not null,
  ID                        bigint auto_increment not null,
  parent_ID                 bigint,
  NAME                      varchar(255),
  SORTINDEX                 integer,
  STARTHOUR                 integer,
  STARTMINUTE               integer,
  STOPHOUR                  integer,
  STOPMINUTE                integer,
  constraint pk_TBLNODE primary key (ID))
;

create table TBLPARALLELLECTURE (
  ID                        bigint auto_increment not null,
  constraint pk_TBLPARALLELLECTURE primary key (ID))
;

create table TBLParticipants (
  dtype                     varchar(10) not null,
  ID                        bigint auto_increment not null,
  SIZE                      integer not null,
  lecture_ID                bigint,
  NAME                      varchar(255) not null,
  parent_ID                 bigint,
  course_ID                 bigint,
  constraint pk_TBLParticipants primary key (ID))
;

create table TBLROOM (
  ID                        bigint auto_increment not null,
  CAPACITY                  integer not null,
  HOUSE                     varchar(255),
  NUMBER                    integer not null,
  PCPOOL                    tinyint(1) default 0 not null,
  BEAMER                    tinyint(1) default 0 not null,
  constraint pk_TBLROOM primary key (ID))
;

create table TBLTIMESLOTCRITERIA (
  ID                        bigint auto_increment not null,
  TOLERANCE                 tinyint(1) default 0 not null,
  STARTHOUR                 integer not null,
  STARTMINUTE               integer not null,
  STOPHOUR                  integer not null,
  STOPMINUTE                integer not null,
  fk_weekday                bigint not null,
  constraint pk_TBLTIMESLOTCRITERIA primary key (ID))
;

alter table TBLLECTURE add constraint fk_TBLLECTURE_TBLPARALLELLECTURE_1 foreign key (fk_lecture) references TBLPARALLELLECTURE (ID) on delete restrict on update restrict;
create index ix_TBLLECTURE_TBLPARALLELLECTURE_1 on TBLLECTURE (fk_lecture);
alter table TBLLECTURE add constraint fk_TBLLECTURE_docent_2 foreign key (fk_docent) references TBLDOCENT (ID) on delete restrict on update restrict;
create index ix_TBLLECTURE_docent_2 on TBLLECTURE (fk_docent);
alter table TBLNODE add constraint fk_TBLNODE_parent_3 foreign key (parent_ID) references TBLNODE (ID) on delete restrict on update restrict;
create index ix_TBLNODE_parent_3 on TBLNODE (parent_ID);
alter table TBLParticipants add constraint fk_TBLParticipants_lecture_4 foreign key (lecture_ID) references TBLLECTURE (ID) on delete restrict on update restrict;
create index ix_TBLParticipants_lecture_4 on TBLParticipants (lecture_ID);
alter table TBLParticipants add constraint fk_TBLParticipants_parent_5 foreign key (parent_ID) references TBLParticipants (ID) on delete restrict on update restrict;
create index ix_TBLParticipants_parent_5 on TBLParticipants (parent_ID);
alter table TBLParticipants add constraint fk_TBLParticipants_course_6 foreign key (course_ID) references TBLParticipants (ID) on delete restrict on update restrict;
create index ix_TBLParticipants_course_6 on TBLParticipants (course_ID);
alter table TBLTIMESLOTCRITERIA add constraint fk_TBLTIMESLOTCRITERIA_weekday_7 foreign key (fk_weekday) references TBLNODE (ID) on delete restrict on update restrict;
create index ix_TBLTIMESLOTCRITERIA_weekday_7 on TBLTIMESLOTCRITERIA (fk_weekday);



# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table TBLDOCENT;

drop table TBLLECTURE;

drop table TBLNODE;

drop table TBLPARALLELLECTURE;

drop table TBLParticipants;

drop table TBLROOM;

drop table TBLTIMESLOTCRITERIA;

SET FOREIGN_KEY_CHECKS=1;

