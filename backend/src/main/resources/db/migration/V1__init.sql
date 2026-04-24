-- MySQL 5.7 compatible (InnoDB, DATETIME(6) for Instant mapping)

CREATE DATABASE authz CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(200) NOT NULL,
  status VARCHAR(16) NOT NULL,
  last_login_at DATETIME(6) NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_role (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(64) NOT NULL,
  name VARCHAR(128) NOT NULL,
  description VARCHAR(500) NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  UNIQUE KEY uk_sys_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_permission (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(128) NOT NULL,
  name VARCHAR(128) NOT NULL,
  description VARCHAR(500) NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  UNIQUE KEY uk_sys_permission_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_role_permission (
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  CONSTRAINT fk_role_perm_role FOREIGN KEY (role_id) REFERENCES sys_role (id),
  CONSTRAINT fk_role_perm_perm FOREIGN KEY (permission_id) REFERENCES sys_permission (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_menu (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  parent_id BIGINT NULL,
  name VARCHAR(128) NOT NULL,
  path VARCHAR(200) NOT NULL,
  component VARCHAR(200) NULL,
  icon VARCHAR(64) NULL,
  `sort` INT NOT NULL,
  visible TINYINT(1) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  CONSTRAINT fk_menu_parent FOREIGN KEY (parent_id) REFERENCES sys_menu (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_menu_permission (
  menu_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (menu_id, permission_id),
  CONSTRAINT fk_menu_perm_menu FOREIGN KEY (menu_id) REFERENCES sys_menu (id),
  CONSTRAINT fk_menu_perm_perm FOREIGN KEY (permission_id) REFERENCES sys_permission (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_refresh_token (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  token_hash VARCHAR(200) NOT NULL,
  expires_at DATETIME(6) NOT NULL,
  revoked_at DATETIME(6) NULL,
  created_at DATETIME(6) NOT NULL,
  device VARCHAR(200) NULL,
  ip VARCHAR(64) NULL,
  ua VARCHAR(500) NULL,
  UNIQUE KEY uk_sys_refresh_token_hash (token_hash),
  CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_refresh_user ON sys_refresh_token (user_id);
CREATE INDEX idx_refresh_expires ON sys_refresh_token (expires_at);

CREATE TABLE sys_audit_log (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  actor_user_id BIGINT NULL,
  action VARCHAR(64) NOT NULL,
  target_type VARCHAR(64) NULL,
  target_id VARCHAR(64) NULL,
  result VARCHAR(16) NOT NULL,
  detail VARCHAR(2000) NULL,
  ip VARCHAR(64) NULL,
  ua VARCHAR(500) NULL,
  created_at DATETIME(6) NOT NULL,
  CONSTRAINT fk_audit_actor FOREIGN KEY (actor_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_audit_created ON sys_audit_log (created_at);
