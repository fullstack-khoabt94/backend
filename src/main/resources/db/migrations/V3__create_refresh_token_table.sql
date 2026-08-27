CREATE TABLE refresh_tokens
(
	id            UUID primary key,
	user_id       UUID references users (id) on delete cascade,
	refresh_token TEXT      not null unique,
	is_revoked    BOOLEAN   not null default false,
	created_at    timestamp not null default now(),
	updated_at    TIMESTAMP NOT NULL DEFAULT now(),
	expired_at    timestamp NOT NULL,
	revoked_at    timestamp          default null
)