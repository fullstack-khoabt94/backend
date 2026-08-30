create table boards
(
	id          UUID primary key,
	user_id     UUID        not null references users (id) on delete cascade,
	title       varchar(50) not null,
	description text,
	color       varchar(50)          default 'default',
	icon        varchar(50)          default 'default',
	is_archived boolean              default false,
	created_at  TIMESTAMP   NOT NULL DEFAULT now(),
	updated_at  TIMESTAMP   NOT NULL DEFAULT now()
);


-- remove user id in tasks table
-- add board id in tasks table
ALTER TABLE tasks
	DROP COLUMN user_id;

ALTER TABLE tasks
	ADD board_id UUID not null references boards (id) on delete no action;