create table "avalanche" (
    "id" bigint primary key,
    "createTime" timestamp not null,
    "extId" varchar(8),
    "viewable" boolean not null,
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
    "submitterEmail" varchar(256),
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