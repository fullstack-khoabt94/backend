CREATE TABLE resetpw_tokens
(
	id            UUID primary key,
	user_id       UUID      not null references users (id) on delete cascade,
	resetpw_token TEXT      not null unique,
	is_revoked    BOOLEAN   not null default false,
	created_at    timestamp not null default now(),
	expired_at    timestamp NOT NULL,
	revoked_at    timestamp          default null,
	updated_at    timestamp not null default now()
)