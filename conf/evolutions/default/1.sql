# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table TBLCRITERIA (
  dtype                     varchar(10) not null,
  ID                        bigint auto_increment not null,
  fk_criteria               bigint not null,
  TOLERANCE                 tinyint(1) default 0 not null,
  PRIORITY                  integer,
  CAPACITY                  integer not null,
  HOUSE                     varchar(255),
  NUMBER                    integer not null,
  PCPOOL                    tinyint(1) default 0 not null,
  BEAMER                    tinyint(1) default 0 not null,
  STARTHOUR                 integer not null,
  STARTMINUTE               integer not null,
  STOPHOUR                  integer not null,
  STOPMINUTE                integer not null,
  fk_weekday                bigint not null,
  constraint pk_TBLCRITERIA primary key (ID))
;

create table TBLCRITERIACONTAINER (
  ID                        bigint auto_increment not null,
  constraint pk_TBLCRITERIACONTAINER primary key (ID))
;

create table TBLDOCENT (
  ID                        bigint auto_increment not null,
  FIRSTNAME                 varchar(255),
  LASTNAME                  varchar(255),
  criteria_container_ID     bigint,
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
  criteria_container_ID     bigint,
  constraint pk_TBLROOM primary key (ID))
;

alter table TBLCRITERIA add constraint fk_TBLCRITERIA_TBLCRITERIACONTAINER_1 foreign key (fk_criteria) references TBLCRITERIACONTAINER (ID) on delete restrict on update restrict;
create index ix_TBLCRITERIA_TBLCRITERIACONTAINER_1 on TBLCRITERIA (fk_criteria);
alter table TBLCRITERIA add constraint fk_TBLCRITERIA_weekday_2 foreign key (fk_weekday) references TBLNODE (ID) on delete restrict on update restrict;
create index ix_TBLCRITERIA_weekday_2 on TBLCRITERIA (fk_weekday);
alter table TBLDOCENT add constraint fk_TBLDOCENT_criteriaContainer_3 foreign key (criteria_container_ID) references TBLCRITERIACONTAINER (ID) on delete restrict on update restrict;
create index ix_TBLDOCENT_criteriaContainer_3 on TBLDOCENT (criteria_container_ID);
alter table TBLLECTURE add constraint fk_TBLLECTURE_TBLPARALLELLECTURE_4 foreign key (fk_lecture) references TBLPARALLELLECTURE (ID) on delete restrict on update restrict;
create index ix_TBLLECTURE_TBLPARALLELLECTURE_4 on TBLLECTURE (fk_lecture);
alter table TBLLECTURE add constraint fk_TBLLECTURE_docent_5 foreign key (fk_docent) references TBLDOCENT (ID) on delete restrict on update restrict;
create index ix_TBLLECTURE_docent_5 on TBLLECTURE (fk_docent);
alter table TBLNODE add constraint fk_TBLNODE_parent_6 foreign key (parent_ID) references TBLNODE (ID) on delete restrict on update restrict;
create index ix_TBLNODE_parent_6 on TBLNODE (parent_ID);
alter table TBLParticipants add constraint fk_TBLParticipants_lecture_7 foreign key (lecture_ID) references TBLLECTURE (ID) on delete restrict on update restrict;
create index ix_TBLParticipants_lecture_7 on TBLParticipants (lecture_ID);
alter table TBLParticipants add constraint fk_TBLParticipants_parent_8 foreign key (parent_ID) references TBLParticipants (ID) on delete restrict on update restrict;
create index ix_TBLParticipants_parent_8 on TBLParticipants (parent_ID);
alter table TBLParticipants add constraint fk_TBLParticipants_course_9 foreign key (course_ID) references TBLParticipants (ID) on delete restrict on update restrict;
create index ix_TBLParticipants_course_9 on TBLParticipants (course_ID);
alter table TBLROOM add constraint fk_TBLROOM_criteriaContainer_10 foreign key (criteria_container_ID) references TBLCRITERIACONTAINER (ID) on delete restrict on update restrict;
create index ix_TBLROOM_criteriaContainer_10 on TBLROOM (criteria_container_ID);



# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table TBLCRITERIA;

drop table TBLCRITERIACONTAINER;

drop table TBLDOCENT;

drop table TBLLECTURE;

drop table TBLNODE;

drop table TBLPARALLELLECTURE;

drop table TBLParticipants;

drop table TBLROOM;

SET FOREIGN_KEY_CHECKS=1;

