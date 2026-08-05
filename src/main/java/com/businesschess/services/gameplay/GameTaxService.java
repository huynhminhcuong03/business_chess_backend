package com.businesschess.services.gameplay;

import com.businesschess.dto.response.PayTaxResponse;
import com.businesschess.entities.BoardCell;
import com.businesschess.entities.Game;
import com.businesschess.entities.GamePlayer;
import com.businesschess.enums.BoardCellType;
import com.businesschess.enums.ErrorCode;
import com.businesschess.enums.IncomeTaxOption;
import com.businesschess.exceptions.AppException;
import com.businesschess.mappers.GamePlayMapper;
import com.businesschess.repositories.GamePlayerRepository;
import org.springframework.stereotype.Service;

@Service
public class GameTaxService {

    // Xử lý thuế thu nhập và thuế xa xỉ theo ô người chơi đang đứng.
    private static final int FIXED_INCOME_TAX_AMOUNT = 200;

    private static final int LUXURY_TAX_AMOUNT = 100;

    private final GamePlayerRepository gamePlayerRepository;

    private final GameTurnService gameTurnService;

    private final GameRentCalculator gameRentCalculator;

    private final GamePlayMapper gamePlayMapper;

    public GameTaxService(
            GamePlayerRepository gamePlayerRepository,
            GameTurnService gameTurnService,
            GameRentCalculator gameRentCalculator,
            GamePlayMapper gamePlayMapper
    ) {
        this.gamePlayerRepository = gamePlayerRepository;
        this.gameTurnService = gameTurnService;
        this.gameRentCalculator = gameRentCalculator;
        this.gamePlayMapper = gamePlayMapper;
    }

    public PayTaxResponse payTax(GamePlayContext context, IncomeTaxOption incomeTaxOption) {
        Game game = context.game();
        GamePlayer gamePlayer = context.gamePlayer();
        BoardCell boardCell = gameTurnService.getPlayerBoardCell(game, gamePlayer);

        if (boardCell.getType() != BoardCellType.INCOME_TAX
                && boardCell.getType() != BoardCellType.LUXURY_TAX) {
            throw new AppException(ErrorCode.PLAYER_NOT_ON_TAX_CELL);
        }

        Integer netWorth = null;
        int taxAmount;

        if (boardCell.getType() == BoardCellType.INCOME_TAX) {
            // Income tax cho phép chọn trả cố định hoặc 10% tổng tài sản.
            if (incomeTaxOption == null) {
                throw new AppException(ErrorCode.TAX_OPTION_REQUIRED);
            }

            if (incomeTaxOption == IncomeTaxOption.FIXED) {
                taxAmount = FIXED_INCOME_TAX_AMOUNT;
            } else {
                netWorth = gameRentCalculator.getNetWorth(game.getId(), gamePlayer);
                taxAmount = netWorth / 10;
            }
        } else {
            // Luxury tax luôn là số cố định, không cần option từ FE.
            taxAmount = LUXURY_TAX_AMOUNT;
        }

        if (gamePlayer.getMoney() < taxAmount) {
            throw new AppException(ErrorCode.PLAYER_NOT_ENOUGH_MONEY);
        }

        gamePlayer.setMoney(gamePlayer.getMoney() - taxAmount);
        gamePlayerRepository.save(gamePlayer);

        return gamePlayMapper.toPayTaxResponse(
                boardCell,
                gamePlayer,
                incomeTaxOption,
                taxAmount,
                netWorth
        );
    }
}
