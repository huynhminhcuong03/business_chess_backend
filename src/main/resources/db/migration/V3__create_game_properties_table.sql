CREATE TABLE IF NOT EXISTS players (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_players_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS games (
    id BIGINT NOT NULL AUTO_INCREMENT,
    board_id BIGINT NOT NULL,
    status ENUM('WAITING', 'PLAYING', 'FINISHED', 'CANCELLED') NOT NULL DEFAULT 'WAITING',
    game_mode ENUM('NORMAL', 'QUICK') NOT NULL DEFAULT 'NORMAL',
    current_player_id BIGINT,
    winner_id BIGINT,
    consecutive_doubles INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP NULL DEFAULT NULL,
    finished_at TIMESTAMP NULL DEFAULT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_games_board (board_id),
    INDEX idx_games_current_player (current_player_id),
    INDEX idx_games_winner (winner_id),
    CONSTRAINT fk_games_board
        FOREIGN KEY (board_id) REFERENCES boards (id),
    CONSTRAINT chk_games_consecutive_doubles CHECK (
        consecutive_doubles BETWEEN 0 AND 3
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS game_players (
    id BIGINT NOT NULL AUTO_INCREMENT,
    game_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    turn_order INT NOT NULL,
    token_color ENUM('RED', 'BLUE', 'GREEN', 'YELLOW') NOT NULL,
    money INT NOT NULL DEFAULT 1500,
    position INT NOT NULL DEFAULT 0,
    in_jail TINYINT(1) NOT NULL DEFAULT 0,
    jail_turn INT NOT NULL DEFAULT 0,
    bankrupt TINYINT(1) NOT NULL DEFAULT 0,
    jail_free_card INT NOT NULL DEFAULT 0,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_game_players_player UNIQUE (game_id, player_id),
    CONSTRAINT uk_game_players_turn_order UNIQUE (game_id, turn_order),
    CONSTRAINT uk_game_players_token_color UNIQUE (game_id, token_color),
    INDEX idx_game_players_game (game_id),
    INDEX idx_game_players_player (player_id),
    CONSTRAINT fk_game_players_game
        FOREIGN KEY (game_id) REFERENCES games (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_game_players_player
        FOREIGN KEY (player_id) REFERENCES players (id),
    CONSTRAINT chk_game_players_turn_order CHECK (turn_order >= 0),
    CONSTRAINT chk_game_players_money CHECK (money >= 0),
    CONSTRAINT chk_game_players_position CHECK (position >= 0),
    CONSTRAINT chk_game_players_jail_turn CHECK (jail_turn BETWEEN 0 AND 3),
    CONSTRAINT chk_game_players_jail_free_card CHECK (jail_free_card >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS game_properties (
    id BIGINT NOT NULL AUTO_INCREMENT,
    game_id BIGINT NOT NULL,
    board_cell_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    house_count INT NOT NULL DEFAULT 0,
    has_hotel TINYINT(1) NOT NULL DEFAULT 0,
    mortgaged TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_game_properties_cell UNIQUE (game_id, board_cell_id),
    INDEX idx_game_properties_owner (owner_id),
    CONSTRAINT fk_game_properties_game
        FOREIGN KEY (game_id) REFERENCES games (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_game_properties_board_cell
        FOREIGN KEY (board_cell_id) REFERENCES board_cells (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_game_properties_owner
        FOREIGN KEY (owner_id) REFERENCES game_players (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_game_properties_house_count CHECK (
        house_count BETWEEN 0 AND 4
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
