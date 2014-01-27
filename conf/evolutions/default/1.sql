# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table TBLTIMESLOT (
  ID                        bigint not null,
  STARTHOUR                 integer,
  STARTMINUTE               integer,
  STOPHOUR                  integer,
  STOPMINUTE                integer,
  constraint pk_TBLTIMESLOT primary key (ID))
;

create sequence TBLTIMESLOT_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists TBLTIMESLOT;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists TBLTIMESLOT_seq;

