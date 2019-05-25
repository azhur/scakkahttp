CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR NOT NULL UNIQUE,
    address VARCHAR,
    email VARCHAR NOT NULL
);
/*INSERT INTO users(username, address, email) VALUES('admin', 'address', 'admin@mail.com');*/
