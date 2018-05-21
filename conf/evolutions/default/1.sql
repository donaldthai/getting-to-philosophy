# Philosophy schema

# --- !Ups

CREATE TABLE INITIAL_PATH (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    url varchar(2083) NOT NULL,
    title varchar(255) NOT NULL,
    hops int NOT NULL,
    PRIMARY KEY (id, url)
);

CREATE TABLE PHIL_PATH (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    initpathid bigint(20) NOT NULL,
    title varchar(255) NOT NULL,
    url varchar(2083) NOT NULL,
    PRIMARY KEY (id, initpathid),
    FOREIGN KEY (initpathid) references INITIAL_PATH(id)
);

# --- !Downs

DROP TABLE INITIAL_PATH;
DROP TABLE PHIL_PATH;