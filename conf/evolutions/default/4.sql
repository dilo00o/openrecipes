# --- !Ups

create table ingredient_alias (
  id                        bigint not null,
  name                      varchar(255),
  language_id               bigint,
  ingredient_id             bigint,
  constraint pk_ingredient_alias primary key (id))
;

create sequence ingredient_alias_seq;

alter table ingredient_alias add constraint fk_ingredient_alias_ingredient foreign key (ingredient_id) references ingredient (id) on delete restrict on update restrict;
create index ix_ingredient_alias_ingredient on ingredient_alias (ingredient_id);
alter table ingredient_alias add constraint fk_ingredient_alias_language foreign key (language_id) references language (id) on delete restrict on update restrict;
create index ix_ingredient_alias_language on ingredient_alias (language_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists ingredient_alias;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists ingredient_alias_seq;