# --- !Ups

create table ingredient (
  id                        bigint not null,
  constraint pk_ingredient primary key (id))
;

create table ingredient_name (
  id                        bigint not null,
  name                      varchar(255),
  ingredient_id             bigint,
  language_id               bigint,
  constraint pk_ingredient_name primary key (id))
;

create sequence ingredient_seq;

create sequence ingredient_name_seq;

alter table ingredient_name add constraint fk_ingredient_name_ingredient foreign key (ingredient_id) references ingredient (id) on delete restrict on update restrict;
create index ix_ingredient_name_ingredient on ingredient_name (ingredient_id);
alter table ingredient_name add constraint fk_ingredient_name_language foreign key (language_id) references language (id) on delete restrict on update restrict;
create index ix_ingredient_name_language on ingredient_name (language_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists ingredient_name;

drop table if exists ingredient;

SET REFERENTIAL_INTEGRITY TRUE;


drop sequence if exists ingredient_seq;

drop sequence if exists ingredient_name_seq;