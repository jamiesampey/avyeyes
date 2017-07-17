create schema public;
grant usage on schema public to public;
grant create on schema public to public;

create table "avalanche" (
    "create_time" timestamp not null,
    "update_time" timestamp not null,
    "external_id" text primary key,
    "viewable" boolean not null,
    "submitter_email" text not null,
    "submitter_experience" text not null,
    "area_name" text not null,
    "date" date not null,
    "longitude" double precision not null,
    "latitude" double precision not null,
    "elevation" integer not null,
    "aspect" text not null,
    "angle" integer not null,
    "perimeter" text not null,
    "comments" text
);

create table "avalanche_weather" (
    "avalanche" text unique,
    "recent_snow" integer not null,
    "recent_wind_speed" text not null,
    "recent_wind_direction" text not null
);

create table "avalanche_classification" (
    "avalanche" text unique,
    "avalanche_type" text not null,
    "trigger" text not null,
    "trigger_modifier" text not null,
    "interface" text not null,
    "r_size" double precision not null,
    "d_size" double precision not null
);

create table "avalanche_human" (
    "avalanche" text unique,
    "mode_of_travel" text not null,
    "caught" integer not null,
    "partially_buried" integer not null,
    "fully_buried" integer not null,
    "injured" integer not null,
    "killed" integer not null
);

create table "avalanche_image" (
    "create_time" timestamp not null,
    "avalanche" text not null,
    "filename" text not null,
    "original_filename" text not null,
    "mime_type" text not null,
    "size" integer not null,
    "sort_order" integer not null,
    "caption" text,
    primary key(avalanche, filename)
);

create table "app_user" (
    "create_time" timestamp not null,
    "last_activity_time" timestamp not null,
    "email" text primary key,
    "facebook_user_id" text,
    "password_hash" text
);

create table "app_role" (
    "role_name" text primary key
);

create table "app_user_role_assignment" (
    "app_user" text not null,
    "app_role" text not null,
    primary key(app_user, app_role)
);

-- indices
create index "avalanche_image_extid_idx" on "avalanche_image" ("avalanche");

-- foreign key constraints
alter table "avalanche" add constraint "avalanche_submitter_email_fk" foreign key ("submitter_email") references "app_user"("email");
alter table "avalanche_weather" add constraint "avalanche_weather_extid_fk" foreign key ("avalanche") references "avalanche"("external_id");
alter table "avalanche_classification" add constraint "avalanche_classification_extid_fk" foreign key ("avalanche") references "avalanche"("external_id");
alter table "avalanche_human" add constraint "avalanche_human_extid_fk" foreign key ("avalanche") references "avalanche"("external_id");
alter table "app_user_role_assignment" add constraint "app_user_role_assignment_user_fk" foreign key ("app_user") references "app_user"("email");
alter table "app_user_role_assignment" add constraint "app_user_role_assignment_role_fk" foreign key ("app_role") references "app_role"("role_name");

-- initial inserts
insert into app_user (create_time, email) values (now(), 'jamie.sampey@gmail.com');
insert into app_user (create_time, email) values (now(), 'avyeyes@gmail.com');
insert into app_role (role_name) values ('site_owner');
insert into app_role (role_name) values ('admin');
insert into app_user_role_assignment(app_user, app_role) values ('jamie.sampey@gmail.com', 'site_owner');
insert into app_user_role_assignment(app_user, app_role) values ('avyeyes@gmail.com', 'admin');
