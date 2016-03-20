# -- Generic tables.

# --- !Ups

create table language (
  id                        bigint not null,
  iso_name                  varchar(255),
  constraint pk_language primary key (id))
;

create sequence language_seq;



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists language;

SET REFERENTIAL_INTEGRITY TRUE;


drop sequence if exists language_seq;