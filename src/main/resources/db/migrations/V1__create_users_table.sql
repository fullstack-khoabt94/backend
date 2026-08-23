CREATE TABLE users (
	id UUID primary key,
	name VARCHAR(30) NOT NULL,
	email varchar(50) NOT NULL UNIQUE,
	password varchar(200) NOT NULL,
	created_at TIMESTAMP NOT NULL DEFAULT now(),
	updated_at TIMESTAMP NOT NULL DEFAULT now()
)