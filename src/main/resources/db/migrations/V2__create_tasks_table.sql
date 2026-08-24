CREATE TABLE tasks (
	id UUID PRIMARY KEY,
	user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
	title varchar(120) NOT NULL,
	description text,
	status varchar(20) NOT NULL DEFAULT 'TODO',
	priority varchar(20) NOT NULL DEFAULT 'MEDIUM',
	due_date timestamp,
	created_at TIMESTAMP NOT NULL DEFAULT now(),
	updated_at TIMESTAMP NOT NULL DEFAULT now()
)