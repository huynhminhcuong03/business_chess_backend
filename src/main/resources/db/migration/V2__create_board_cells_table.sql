CREATE TABLE IF NOT EXISTS board_cells (
    id BIGINT NOT NULL AUTO_INCREMENT,
    board_id BIGINT NOT NULL,
    position INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    type ENUM(
        'START',
        'PROPERTY',
        'CHANCE',
        'COMMUNITY',
        'JAIL',
        'GO_TO_JAIL',
        'FREE_PARKING',
        'INCOME_TAX',
        'LUXURY_TAX',
        'STATION',
        'UTILITY'
    ) NOT NULL,
    color VARCHAR(20),
    image VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_board_cells_board
        FOREIGN KEY (board_id) REFERENCES boards (id)
        ON DELETE CASCADE,
    CONSTRAINT uk_board_cells_position
        UNIQUE (board_id, position),
    INDEX idx_board_cells_board_type (board_id, type),
    CONSTRAINT chk_board_cells_position CHECK (position >= 0)
);
