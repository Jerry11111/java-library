CREATE ROLE test LOGIN
  PASSWORD 'test'
  NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOREPLICATION;

CREATE DATABASE test_db
  WITH OWNER = test
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'Chinese (Simplified)_China.936'
       LC_CTYPE = 'Chinese (Simplified)_China.936'
       CONNECTION LIMIT = -1;

CREATE TABLE t_user
(
  user_id serial,
  username text NOT NULL,
  password text,
  age integer,
  CONSTRAINT  t_user_pkey PRIMARY KEY (user_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE t_user
  OWNER TO test;
  
  
CREATE OR REPLACE FUNCTION public.insert_user(
    _username text,
    _password text)
  RETURNS void AS
$BODY$
       DECLARE
          t_user_record RECORD;
          result text := '';
       BEGIN
	   INSERT INTO t_user(username, password) VALUES(_username, _password);
       END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.insert_user(text, text)
  OWNER TO postgres;