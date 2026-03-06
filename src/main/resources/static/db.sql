SELECT * FROM BD_USR;
SELECT * FROM BD_ROLES;
SELECT * FROM USER_ROLES;
SELECT * FROM BD_PLAN;
SELECT * FROM BD_REPORT;

SELECT * FROM reports.posts;



create table BD_USR(
                       F_ID SERIAL PRIMARY KEY ,
                       USERNAME varchar(100) NOT NULL ,
                       PASSWORD varchar NOT NULL,
                       AUTH_TYPE VARCHAR(10) NOT NULL CHECK (AUTH_TYPE IN ('NTLM', 'STANDARD'))
);

create table BD_ROLES(
                         F_ID SERIAL PRIMARY KEY,
                         NAME VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE USER_ROLES (
                            USER_ID INTEGER REFERENCES BD_USR(F_ID) ON DELETE CASCADE,
                            ROLE_ID INTEGER REFERENCES BD_ROLES(F_ID) ON DELETE CASCADE,
                            PRIMARY KEY (USER_ID, ROLE_ID)
);



create table BD_PLAN(
                        ID SERIAL PRIMARY KEY ,
                        PLAN_ID varchar(100) NOT NULL ,
                        DATA varchar NOT NULL
);

create table BD_REPORT(
                          ID SERIAL PRIMARY KEY ,
                          REPORT_NAME varchar(30),
                          REPORT_CATEGORY varchar(30),
                          DB_URL varchar,
                          DB_USERNAME varchar(30),
                          DB_PASSWORD varchar(30),
                          DB_DRIVER varchar,
                          SQL varchar,
                          CONTENT varchar,
                          STYLES varchar,
                          PARAMETERS VARCHAR,
                          SCRIPT VARCHAR,
                          IS_SQL boolean,
                          DATA_BANDS varchar,
                          IS_BOOK_ORIENTATION boolean,
                          LAYOUT_SETTINGS_PARAMS varchar,
                          LAYOUT_PARAMS varchar
);

create table BD_REPORT_GLOBALS(
                                  ID SERIAL PRIMARY KEY ,
                                  KEY varchar(50) NOT NULL UNIQUE,
                                  VALUE varchar DEFAULT '',
                                  DESCRIPTION varchar DEFAULT ''
);