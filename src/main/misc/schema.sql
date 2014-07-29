CREATE DATABASE avyeyes_db
  WITH OWNER = jamie
  ENCODING = 'UTF8'
  TABLESPACE = pg_default
  LC_COLLATE = 'en_US.UTF-8'
  LC_CTYPE = 'en_US.UTF-8'
  CONNECTION LIMIT = -1;

CREATE TABLE "avalanche" (
    "id" bigint primary key,
    "createTime" timestamp not null,
    "extId" varchar(8) not null,
    "viewable" boolean not null,
    "submitterEmail" varchar(256) not null,
    "submitterExp" integer not null,
    "lat" double precision not null,
    "lng" double precision not null,
    "areaName" varchar(256) not null,
    "avyDate" date not null,
    "sky" integer not null,
    "precip" integer not null,
    "elevation" integer not null,
    "aspect" integer not null,
    "angle" integer not null,    
    "avyType" integer not null,
    "trigger" integer not null,
    "bedSurface" integer not null,
    "rSize" double precision not null,
    "dSize" double precision not null,
    "caught" integer not null,
    "partiallyBuried" integer not null,
    "fullyBuried" integer not null,
    "injured" integer not null,
    "killed" integer not null,
    "modeOfTravel" integer not null,
    "comments" text,
    "kmlCoords" text not null
  );

CREATE SEQUENCE s_avalanche_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE UNIQUE INDEX idx2f5d05cf
  ON avalanche
  USING btree
  ("extId");
  
CREATE TABLE "avalanche_img" (
    "id" bigint primary key,
    "createTime" timestamp not null,
    "avyExtId" varchar(8) not null,
    "filename" varchar(255) not null,
    "mimeType" varchar(20) not null,
    "bytes" bytea not null
);

CREATE SEQUENCE s_avalanche_img_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE INDEX idx644c089b 
  ON "avalanche_img"
  USING btree
  ("avyExtId");
  
CREATE TABLE "avalanche_img_dropbox" (
    "id" bigint primary key,
    "createTime" timestamp not null,
    "avyExtId" varchar(8) not null,
    "filename" varchar(255) not null,
    "mimeType" varchar(20) not null,
    "bytes" bytea not null
);

CREATE SEQUENCE s_avalanche_img_dropbox_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;