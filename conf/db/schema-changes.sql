-- Baseline schema is release 1.0.0

-- Release 1.0.3
alter table avalanche_image add sort_order integer not null default 0;

-- Release 1.0.5
alter table avalanche_classification add trigger_modifier text not null default 'empty';

-- Release 1.3.0
alter table app_user add last_activity_time timestamp not null default NOW();
update app_user set last_activity_time = create_time;
alter table app_user add facebook_user_id text;
alter table app_user add password_hash text;

-- Release 2.0.0
drop table avalanche_human;
