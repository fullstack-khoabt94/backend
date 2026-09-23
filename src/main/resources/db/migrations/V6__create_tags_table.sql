create table tags
(
	id          UUID primary key,
	user_id     UUID        not null references users (id) on delete cascade,
	title       varchar(50) not null,
	description text,
	color       varchar(50)          default 'default',
	created_at  TIMESTAMP   NOT NULL DEFAULT now(),
	updated_at  TIMESTAMP   NOT NULL DEFAULT now()
);

create table task_tags
(
	tag_id UUID not null references tags(id) on delete cascade,
	task_id UUID not null references tasks(id) on delete cascade,
	primary key (tag_id, task_id)
);

create index task_tag_index on task_tags(task_id, tag_id);
create index user_tag_index on tags(user_id);
create unique index user_title_index on tags(user_id, lower(title))