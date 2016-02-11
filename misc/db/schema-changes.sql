-- Baseline schema is release 1.0.0

-- Release 1.0.3
alter table avalanche_image add sort_order integer not null default 0;

-- Release 1.0.5
alter table avalanche_classification add trigger_modifier text not null default 'U';
