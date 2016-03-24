# --- !Ups

create table ingredient_tag (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_ingredient_tag primary key (id))
;

create table recipe (
  id                        bigint not null,
  difficulty                integer,
  name                      clob,
  url                       clob,
  time                      bigint,
  constraint pk_recipe primary key (id))
;

create table recipe_ingredient (
  id                        bigint not null,
  ingredient_id             bigint,
  recipe_id                 bigint,
  constraint pk_recipe_ingredient primary key (id))
;

create table recipe_tag (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_recipe_tag primary key (id))
;

create table ingredient_tag_ingredient (
  ingredient_tag_id              bigint not null,
  ingredient_id                  bigint not null,
  constraint pk_ingredient_tag_ingredient primary key (ingredient_tag_id, ingredient_id))
;

create table recipe_tag_recipe (
  recipe_tag_id                  bigint not null,
  recipe_id                      bigint not null,
  constraint pk_recipe_tag_recipe primary key (recipe_tag_id, recipe_id))
;

create sequence ingredient_tag_seq;

create sequence recipe_seq;

create sequence recipe_ingredient_seq;

create sequence recipe_tag_seq;

alter table recipe_ingredient add constraint fk_recipe_ingredient_ingredient foreign key (ingredient_id) references ingredient (id) on delete restrict on update restrict;
create index ix_recipe_ingredient_ingredient on recipe_ingredient (ingredient_id);
alter table recipe_ingredient add constraint fk_recipe_ingredient_recipe foreign key (recipe_id) references recipe (id) on delete restrict on update restrict;
create index ix_recipe_ingredient_recipe on recipe_ingredient (recipe_id);

alter table ingredient_tag_ingredient add constraint fk_ingredient_tag_ingredient_t foreign key (ingredient_tag_id) references ingredient_tag (id) on delete restrict on update restrict;

alter table ingredient_tag_ingredient add constraint fk_ingredient_tag_ingredient_i foreign key (ingredient_id) references ingredient (id) on delete restrict on update restrict;

alter table recipe_tag_recipe add constraint fk_recipe_tag_recipe_recipe_rt foreign key (recipe_tag_id) references recipe_tag (id) on delete restrict on update restrict;

alter table recipe_tag_recipe add constraint fk_recipe_tag_recipe_recipe_r foreign key (recipe_id) references recipe (id) on delete restrict on update restrict;


# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists ingredient_tag;

drop table if exists recipe;

drop table if exists recipe_ingredient;

drop table if exists recipe_tag;

drop table if exists ingredient_tag_ingredient;

drop table if exists recipe_tag_recipe;

SET REFERENTIAL_INTEGRITY TRUE;


drop sequence if exists ingredient_tag_seq;

drop sequence if exists recipe_seq;

drop sequence if exists recipe_ingredient_seq;

drop sequence if exists recipe_tag_seq;