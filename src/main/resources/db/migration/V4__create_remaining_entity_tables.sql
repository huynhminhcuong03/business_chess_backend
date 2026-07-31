CREATE TABLE IF NOT EXISTS property_details (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cell_id BIGINT NOT NULL,
    buy_price INT NOT NULL,
    mortgage_price INT NOT NULL,
    house_price INT NOT NULL,
    hotel_price INT NOT NULL,
    rent_level0 INT NOT NULL,
    rent_level1 INT NOT NULL,
    rent_level2 INT NOT NULL,
    rent_level3 INT NOT NULL,
    rent_level4 INT NOT NULL,
    rent_hotel INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_property_details_cell UNIQUE (cell_id),
    CONSTRAINT fk_property_details_cell
        FOREIGN KEY (cell_id) REFERENCES board_cells (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_property_details_prices CHECK (
        buy_price >= 0
        AND mortgage_price >= 0
        AND house_price >= 0
        AND hotel_price >= 0
        AND rent_level0 >= 0
        AND rent_level1 >= 0
        AND rent_level2 >= 0
        AND rent_level3 >= 0
        AND rent_level4 >= 0
        AND rent_hotel >= 0
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS chance_cards (
    id BIGINT NOT NULL AUTO_INCREMENT,
    board_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    action_data JSON,
    amount INT,
    target_position INT,
    PRIMARY KEY (id),
    INDEX idx_chance_cards_board (board_id),
    CONSTRAINT fk_chance_cards_board
        FOREIGN KEY (board_id) REFERENCES boards (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_chance_cards_target_position CHECK (
        target_position IS NULL OR target_position >= 0
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS community_cards (
    id BIGINT NOT NULL AUTO_INCREMENT,
    board_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    action_data JSON,
    amount INT,
    target_position INT,
    PRIMARY KEY (id),
    INDEX idx_community_cards_board (board_id),
    CONSTRAINT fk_community_cards_board
        FOREIGN KEY (board_id) REFERENCES boards (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_community_cards_target_position CHECK (
        target_position IS NULL OR target_position >= 0
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

CREATE TABLE IF NOT EXISTS game_card_decks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    game_id BIGINT NOT NULL,
    card_type ENUM('CHANCE', 'COMMUNITY') NOT NULL,
    card_id BIGINT NOT NULL,
    deck_order INT NOT NULL,
    held_by_player_id BIGINT,
    is_used TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_game_card_decks_order UNIQUE (game_id, card_type, deck_order),
    CONSTRAINT uk_game_card_decks_card UNIQUE (game_id, card_type, card_id),
    INDEX idx_game_card_decks_holder (held_by_player_id),
    CONSTRAINT fk_game_card_decks_game
        FOREIGN KEY (game_id) REFERENCES games (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_game_card_decks_holder
        FOREIGN KEY (held_by_player_id) REFERENCES game_players (id)
        ON DELETE SET NULL,
    CONSTRAINT chk_game_card_decks_order CHECK (deck_order >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @fk_games_current_player_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE games ADD CONSTRAINT fk_games_current_player FOREIGN KEY (current_player_id) REFERENCES game_players (id) ON DELETE SET NULL',
        'SELECT 1'
    )
    FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'games'
      AND constraint_name = 'fk_games_current_player'
);

PREPARE fk_games_current_player_stmt FROM @fk_games_current_player_sql;
EXECUTE fk_games_current_player_stmt;
DEALLOCATE PREPARE fk_games_current_player_stmt;

SET @fk_games_winner_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE games ADD CONSTRAINT fk_games_winner FOREIGN KEY (winner_id) REFERENCES game_players (id) ON DELETE SET NULL',
        'SELECT 1'
    )
    FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'games'
      AND constraint_name = 'fk_games_winner'
);

PREPARE fk_games_winner_stmt FROM @fk_games_winner_sql;
EXECUTE fk_games_winner_stmt;
DEALLOCATE PREPARE fk_games_winner_stmt;
