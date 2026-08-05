package com.businesschess.services.gameplay;

import java.util.List;

import com.businesschess.dto.response.CardPlayerMoneyChangeResponse;
import com.businesschess.dto.response.DrawCardResponse;
import com.businesschess.entities.BoardCell;
import com.businesschess.entities.Game;
import com.businesschess.entities.GamePlayer;
import com.businesschess.entities.GameProperty;
import com.businesschess.enums.BoardCellType;
import com.businesschess.enums.CardActionType;
import com.businesschess.enums.ErrorCode;
import com.businesschess.exceptions.AppException;
import com.businesschess.repositories.GamePlayerRepository;
import com.businesschess.repositories.GamePropertyRepository;
import com.businesschess.repositories.GameRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class GameCardEffectService {

    // Service này gom toàn bộ effect của Chance/Community card, không xử lý thứ tự deck.
    private static final int START_REWARD_AMOUNT = 200;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final GameRepository gameRepository;

    private final GamePlayerRepository gamePlayerRepository;

    private final GamePropertyRepository gamePropertyRepository;

    private final GameTurnService gameTurnService;

    public GameCardEffectService(
            GameRepository gameRepository,
            GamePlayerRepository gamePlayerRepository,
            GamePropertyRepository gamePropertyRepository,
            GameTurnService gameTurnService
    ) {
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.gamePropertyRepository = gamePropertyRepository;
        this.gameTurnService = gameTurnService;
    }

    public void applyCardEffect(
            Game game,
            GamePlayer gamePlayer,
            DrawnCard card,
            DrawCardResponse response
    ) {
        // Mỗi actionType của card chỉ đổi trạng thái game/player, còn việc rút deck nằm ở service khác.
        switch (card.actionType()) {
            case RECEIVE_FROM_BANK -> receiveFromBank(gamePlayer, requiredAmount(card), response);
            case PAY_TO_BANK -> payToBank(gamePlayer, requiredAmount(card), response);
            case COLLECT_FROM_EACH_PLAYER -> collectFromEachPlayer(game, gamePlayer, requiredAmount(card), response);
            case PAY_EACH_PLAYER -> payEachPlayer(game, gamePlayer, requiredAmount(card), response);
            case GET_OUT_OF_JAIL -> receiveJailCard(gamePlayer, response);
            case GO_TO_JAIL -> sendToJail(game, gamePlayer, response);
            case MOVE_TO_POSITION -> moveToPosition(game, gamePlayer, requiredTargetPosition(card), true, response);
            case MOVE_BACK -> moveBack(game, gamePlayer, requiredAmount(card), response);
            case MOVE_TO_NEAREST_STATION -> moveToNearest(game, gamePlayer, BoardCellType.STATION, response);
            case MOVE_TO_NEAREST_UTILITY -> moveToNearest(game, gamePlayer, BoardCellType.UTILITY, response);
            case REPAIR_PROPERTIES -> repairProperties(game, gamePlayer, card, response);
        }

        response.setCurrentPlayerMoney(gamePlayer.getMoney());
    }

    private void receiveFromBank(
            GamePlayer gamePlayer,
            int amount,
            DrawCardResponse response
    ) {
        gamePlayer.setMoney(gamePlayer.getMoney() + amount);
        gamePlayerRepository.save(gamePlayer);
        addMoneyChange(response, gamePlayer, amount);
    }

    private void payToBank(
            GamePlayer gamePlayer,
            int amount,
            DrawCardResponse response
    ) {
        ensureEnoughMoney(gamePlayer, amount);
        gamePlayer.setMoney(gamePlayer.getMoney() - amount);
        gamePlayerRepository.save(gamePlayer);
        addMoneyChange(response, gamePlayer, -amount);
    }

    private void collectFromEachPlayer(
            Game game,
            GamePlayer receiver,
            int amount,
            DrawCardResponse response
    ) {
        List<GamePlayer> otherPlayers = getOtherPlayers(game, receiver);

        // Hiện tại chưa có flow phá sản, nên nếu ai thiếu tiền thì chặn rõ ràng trước khi trừ.
        for (GamePlayer player : otherPlayers) {
            ensureEnoughMoney(player, amount);
        }

        for (GamePlayer player : otherPlayers) {
            player.setMoney(player.getMoney() - amount);
            receiver.setMoney(receiver.getMoney() + amount);
            addMoneyChange(response, player, -amount);
        }

        gamePlayerRepository.saveAll(otherPlayers);
        gamePlayerRepository.save(receiver);
        addMoneyChange(response, receiver, amount * otherPlayers.size());
    }

    private void payEachPlayer(
            Game game,
            GamePlayer payer,
            int amount,
            DrawCardResponse response
    ) {
        List<GamePlayer> otherPlayers = getOtherPlayers(game, payer);
        int totalAmount = amount * otherPlayers.size();
        // Người rút card phải đủ tiền để trả cho tất cả người chơi khác trong một transaction.
        ensureEnoughMoney(payer, totalAmount);

        payer.setMoney(payer.getMoney() - totalAmount);
        addMoneyChange(response, payer, -totalAmount);

        for (GamePlayer player : otherPlayers) {
            player.setMoney(player.getMoney() + amount);
            addMoneyChange(response, player, amount);
        }

        gamePlayerRepository.save(payer);
        gamePlayerRepository.saveAll(otherPlayers);
    }

    private void receiveJailCard(GamePlayer gamePlayer, DrawCardResponse response) {
        // Chỉ tăng số lượng thẻ trên player; GameCardDrawService sẽ đánh dấu deck card đang được giữ.
        gamePlayer.setJailFreeCard(gamePlayer.getJailFreeCard() + 1);
        gamePlayerRepository.save(gamePlayer);
        addMoneyChange(response, gamePlayer, 0);
    }

    private void sendToJail(Game game, GamePlayer gamePlayer, DrawCardResponse response) {
        int oldPosition = gamePlayer.getPosition();
        BoardCell jailCell = gameTurnService.getJailCell(game);

        // Card đi tù luôn đưa thẳng tới ô JAIL, không đi từng bước và không cộng thưởng qua Start.
        gamePlayer.setPosition(jailCell.getPosition());
        gamePlayer.setInJail(true);
        gamePlayer.setJailTurn(0);
        game.setConsecutiveDoubles(0);

        // Nếu người chơi còn đang giữ lượt do đổ đôi thì vào tù sẽ kết thúc lượt đó ngay.
        if (game.getCurrentPlayer() != null && game.getCurrentPlayer().getId().equals(gamePlayer.getId())) {
            game.setCurrentPlayer(gameTurnService.resolveNextPlayer(game.getId(), gamePlayer, false));
        }

        gamePlayerRepository.save(gamePlayer);
        gameRepository.save(game);

        response.setMoved(true);
        response.setOldPosition(oldPosition);
        response.setNewPosition(jailCell.getPosition());
        response.setSentToJail(true);
        response.setJailPosition(jailCell.getPosition());
        response.setPassedStart(false);
        response.setStartReward(0);
        response.setNextPlayerId(game.getCurrentPlayer() == null ? null : game.getCurrentPlayer().getId());
    }

    private void moveToPosition(
            Game game,
            GamePlayer gamePlayer,
            int targetPosition,
            boolean allowStartReward,
            DrawCardResponse response
    ) {
        int boardSize = gameTurnService.getBoardSize(game);
        if (targetPosition < 0 || targetPosition >= boardSize) {
            throw new AppException(ErrorCode.BOARD_CELL_NOT_FOUND);
        }

        int oldPosition = gamePlayer.getPosition();
        boolean passedStart = allowStartReward && targetPosition < oldPosition;

        // Card di chuyển tới vị trí phía sau được hiểu là đã đi vòng qua Start.
        gamePlayer.setPosition(targetPosition);
        if (passedStart) {
            gamePlayer.setMoney(gamePlayer.getMoney() + START_REWARD_AMOUNT);
            addMoneyChange(response, gamePlayer, START_REWARD_AMOUNT);
        }

        gamePlayerRepository.save(gamePlayer);
        fillMoveResponse(response, oldPosition, targetPosition, passedStart);
    }

    private void moveBack(
            Game game,
            GamePlayer gamePlayer,
            int stepCount,
            DrawCardResponse response
    ) {
        int boardSize = gameTurnService.getBoardSize(game);
        int oldPosition = gamePlayer.getPosition();
        // MOVE_BACK đi ngược lại nên không có thưởng qua Start.
        int newPosition = Math.floorMod(oldPosition - stepCount, boardSize);

        gamePlayer.setPosition(newPosition);
        gamePlayerRepository.save(gamePlayer);
        fillMoveResponse(response, oldPosition, newPosition, false);
    }

    private void moveToNearest(
            Game game,
            GamePlayer gamePlayer,
            BoardCellType targetType,
            DrawCardResponse response
    ) {
        int oldPosition = gamePlayer.getPosition();
        BoardCell targetCell = gameTurnService.getNextBoardCellByType(game, oldPosition, targetType);
        moveToPosition(game, gamePlayer, targetCell.getPosition(), true, response);
    }

    private void repairProperties(
            Game game,
            GamePlayer gamePlayer,
            DrawnCard card,
            DrawCardResponse response
    ) {
        RepairCost repairCost = getRepairCost(card);
        // Chỉ tính tiền sửa trên tài sản của chính người rút card trong game hiện tại.
        int totalAmount = gamePropertyRepository.findAllByOwnerId(gamePlayer.getId()).stream()
                .filter(gameProperty -> gameProperty.getGame().getId().equals(game.getId()))
                .mapToInt(gameProperty -> getPropertyRepairAmount(gameProperty, repairCost))
                .sum();

        if (totalAmount == 0) {
            addMoneyChange(response, gamePlayer, 0);
            return;
        }

        payToBank(gamePlayer, totalAmount, response);
    }

    private int getPropertyRepairAmount(GameProperty gameProperty, RepairCost repairCost) {
        if (Boolean.TRUE.equals(gameProperty.getHasHotel())) {
            return repairCost.hotelAmount();
        }

        int houseCount = gameProperty.getHouseCount() == null ? 0 : gameProperty.getHouseCount();
        return houseCount * repairCost.houseAmount();
    }

    private RepairCost getRepairCost(DrawnCard card) {
        // Seed có thể lưu chi phí sửa trong actionData JSON; nếu không có thì dùng amount cho cả nhà/khách sạn.
        if (card.actionData() == null || card.actionData().isBlank()) {
            int amount = requiredAmount(card);
            return new RepairCost(amount, amount);
        }

        try {
            JsonNode root = objectMapper.readTree(card.actionData());
            int houseAmount = firstInt(root, "houseAmount", "amountPerHouse", "house", "perHouse");
            int hotelAmount = firstInt(root, "hotelAmount", "amountPerHotel", "hotel", "perHotel");
            return new RepairCost(houseAmount, hotelAmount);
        } catch (Exception exception) {
            throw new AppException(ErrorCode.CARD_ACTION_DATA_INVALID);
        }
    }

    private int firstInt(JsonNode root, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode value = root.get(fieldName);
            if (value != null && value.canConvertToInt()) {
                return value.asInt();
            }
        }

        throw new AppException(ErrorCode.CARD_ACTION_DATA_INVALID);
    }

    private List<GamePlayer> getOtherPlayers(Game game, GamePlayer currentPlayer) {
        return gamePlayerRepository.findByGameIdOrderByTurnOrderAsc(game.getId()).stream()
                .filter(gamePlayer -> !gamePlayer.getId().equals(currentPlayer.getId()))
                .toList();
    }

    private int requiredAmount(DrawnCard card) {
        if (card.amount() != null) {
            return card.amount();
        }

        Integer amountFromActionData = firstActionDataInt(
                card,
                "amount",
                "amountPerPlayer",
                "steps",
                "amountPerHouse"
        );
        if (amountFromActionData == null) {
            throw new AppException(ErrorCode.CARD_AMOUNT_REQUIRED);
        }

        return amountFromActionData;
    }

    private int requiredTargetPosition(DrawnCard card) {
        if (card.targetPosition() != null) {
            return card.targetPosition();
        }

        Integer targetPositionFromActionData = firstActionDataInt(card, "targetPosition");
        if (targetPositionFromActionData == null) {
            throw new AppException(ErrorCode.CARD_TARGET_POSITION_REQUIRED);
        }

        return targetPositionFromActionData;
    }

    private Integer firstActionDataInt(DrawnCard card, String... fieldNames) {
        // Dữ liệu card hiện tại đang để phần lớn tham số trong actionData JSON.
        // Vẫn giữ ưu tiên cột riêng ở trên để sau này seed có thể chuyển dần sang schema chuẩn.
        if (card.actionData() == null || card.actionData().isBlank()) {
            return null;
        }

        try {
            JsonNode root = objectMapper.readTree(card.actionData());
            for (String fieldName : fieldNames) {
                JsonNode value = root.get(fieldName);
                if (value != null && value.canConvertToInt()) {
                    return value.asInt();
                }
            }
            return null;
        } catch (Exception exception) {
            throw new AppException(ErrorCode.CARD_ACTION_DATA_INVALID);
        }
    }

    private void ensureEnoughMoney(GamePlayer gamePlayer, int amount) {
        if (gamePlayer.getMoney() < amount) {
            throw new AppException(ErrorCode.PLAYER_NOT_ENOUGH_MONEY);
        }
    }

    private void fillMoveResponse(
            DrawCardResponse response,
            int oldPosition,
            int newPosition,
            boolean passedStart
    ) {
        response.setMoved(true);
        response.setOldPosition(oldPosition);
        response.setNewPosition(newPosition);
        response.setPassedStart(passedStart);
        response.setStartReward(passedStart ? START_REWARD_AMOUNT : 0);
    }

    private void addMoneyChange(
            DrawCardResponse response,
            GamePlayer gamePlayer,
            int moneyDelta
    ) {
        // Ghi lại từng biến động tiền để FE có thể hiện thông báo và cập nhật player card chính xác.
        CardPlayerMoneyChangeResponse moneyChange = new CardPlayerMoneyChangeResponse();
        moneyChange.setGamePlayerId(gamePlayer.getId());
        moneyChange.setMoneyDelta(moneyDelta);
        moneyChange.setMoneyAfter(gamePlayer.getMoney());
        response.getMoneyChanges().add(moneyChange);
    }

    private record RepairCost(int houseAmount, int hotelAmount) {
    }
}
