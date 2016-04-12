/* With condition on name. */
set referential_integrity false;

delete from ingredient where id in (select ingredient_id from ingredient_name where name like '%todoname%');

delete from ingredient_name where name like '%todoname%';

set referential_integrity true;