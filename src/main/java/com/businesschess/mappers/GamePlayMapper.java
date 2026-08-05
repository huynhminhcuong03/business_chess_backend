package com.businesschess.mappers;

import com.businesschess.dto.response.BuyPropertyResponse;
import com.businesschess.dto.response.GoToJailResponse;
import com.businesschess.dto.response.JailActionResponse;
import com.businesschess.dto.response.LandCellResponse;
import com.businesschess.dto.response.LandedPropertyResponse;
import com.businesschess.dto.response.PayRentResponse;
import com.businesschess.dto.response.PayTaxResponse;
import com.businesschess.dto.response.RollDiceResponse;
import com.businesschess.entities.BoardCell;
import com.businesschess.entities.GamePlayer;
import com.businesschess.entities.GameProperty;
import com.businesschess.entities.PropertyDetail;
import com.businesschess.enums.BoardCellType;
import com.businesschess.enums.IncomeTaxOption;
import com.businesschess.enums.JailActionType;
import com.businesschess.enums.LandCellAction;
import org.springframework.stereotype.Component;

@Component
public class GamePlayMapper {

    // Mapper chỉ đóng gói dữ liệu response, không đặt luật gameplay ở đây.
    public RollDiceResponse toTestRoll(Integer dice1, Integer dice2) {
        RollDiceResponse response = new RollDiceResponse();
        response.setDice1(dice1);
        response.setDice2(dice2);
        response.setTotal(dice1 + dice2);
        response.setIsDouble(dice1.equals(dice2));
        return response;
    }

    public void fillMoveResult(
            RollDiceResponse response,
            GamePlayer gamePlayer,
            GamePlayer nextPlayer,
            Integer oldPosition,
            Integer newPosition,
            Boolean passedStart,
            Integer startReward
    ) {
        response.setOldPosition(oldPosition);
        response.setNewPosition(newPosition);
        response.setPassedStart(passedStart);
        response.setStartReward(startReward);
        response.setCurrentPlayerMoney(gamePlayer.getMoney());
        response.setCurrentPlayerId(gamePlayer.getId());
        response.setNextPlayerId(nextPlayer.getId());
        response.setSentToJail(false);
        response.setJailPosition(null);
        response.setInJail(gamePlayer.getInJail());
        response.setJailTurn(gamePlayer.getJailTurn());
        response.setJailFreeCard(gamePlayer.getJailFreeCard());
    }

    public void fillSentToJailResult(
            RollDiceResponse response,
            GamePlayer gamePlayer,
            GamePlayer nextPlayer,
            Integer oldPosition,
            Integer jailPosition
    ) {
        // Kết quả đi tù trực tiếp luôn báo không qua Start để FE không cộng nhầm $200.
        response.setOldPosition(oldPosition);
        response.setNewPosition(jailPosition);
        response.setPassedStart(false);
        response.setStartReward(0);
        response.setCurrentPlayerMoney(gamePlayer.getMoney());
        response.setCurrentPlayerId(gamePlayer.getId());
        response.setNextPlayerId(nextPlayer.getId());
        response.setSentToJail(true);
        response.setJailPosition(jailPosition);
        response.setInJail(gamePlayer.getInJail());
        response.setJailTurn(gamePlayer.getJailTurn());
        response.setJailFreeCard(gamePlayer.getJailFreeCard());
    }

    public LandCellResponse toLandCellResponse(
            BoardCell boardCell,
            LandCellAction action,
            LandedPropertyResponse property
    ) {
        LandCellResponse response = new LandCellResponse();
        response.setCellId(boardCell.getId());
        response.setCellPosition(boardCell.getPosition());
        response.setCellName(boardCell.getName());
        response.setCellType(boardCell.getType());
        response.setAction(action);
        response.setProperty(property);
        return response;
    }

    public LandedPropertyResponse toLandedPropertyResponse(
            PropertyDetail propertyDetail,
            GameProperty gameProperty,
            Integer rent
    ) {
        LandedPropertyResponse response = new LandedPropertyResponse();
        response.setBuyPrice(propertyDetail.getBuyPrice());
        response.setRent(rent);
        response.setHouseCount(gameProperty == null ? 0 : gameProperty.getHouseCount());
        response.setHasHotel(gameProperty != null && Boolean.TRUE.equals(gameProperty.getHasHotel()));
        response.setMortgaged(gameProperty != null && Boolean.TRUE.equals(gameProperty.getMortgaged()));

        if (gameProperty != null) {
            response.setGamePropertyId(gameProperty.getId());
            response.setOwnerGamePlayerId(gameProperty.getOwner().getId());
            response.setOwnerPlayerId(gameProperty.getOwner().getPlayer().getId());
        }

        return response;
    }

    public BuyPropertyResponse toBuyPropertyResponse(
            GameProperty gameProperty,
            PropertyDetail propertyDetail
    ) {
        BuyPropertyResponse response = new BuyPropertyResponse();
        response.setGamePropertyId(gameProperty.getId());
        response.setBoardCellId(gameProperty.getBoardCell().getId());
        response.setBoardCellPosition(gameProperty.getBoardCell().getPosition());
        response.setBoardCellName(gameProperty.getBoardCell().getName());
        response.setOwnerGamePlayerId(gameProperty.getOwner().getId());
        response.setOwnerMoney(gameProperty.getOwner().getMoney());
        response.setBuyPrice(propertyDetail.getBuyPrice());
        return response;
    }

    public PayRentResponse toPayRentResponse(
            BoardCell boardCell,
            GamePlayer payer,
            GamePlayer owner,
            Integer rentAmount
    ) {
        PayRentResponse response = new PayRentResponse();
        response.setBoardCellId(boardCell.getId());
        response.setBoardCellPosition(boardCell.getPosition());
        response.setBoardCellName(boardCell.getName());
        response.setPayerGamePlayerId(payer.getId());
        response.setOwnerGamePlayerId(owner.getId());
        response.setRentAmount(rentAmount);
        response.setPayerMoney(payer.getMoney());
        response.setOwnerMoney(owner.getMoney());
        return response;
    }

    public PayTaxResponse toPayTaxResponse(
            BoardCell boardCell,
            GamePlayer gamePlayer,
            IncomeTaxOption incomeTaxOption,
            Integer taxAmount,
            Integer netWorth
    ) {
        PayTaxResponse response = new PayTaxResponse();
        response.setBoardCellId(boardCell.getId());
        response.setBoardCellPosition(boardCell.getPosition());
        response.setBoardCellName(boardCell.getName());
        response.setTaxType(boardCell.getType());
        response.setIncomeTaxOption(
                boardCell.getType() == BoardCellType.INCOME_TAX
                        ? incomeTaxOption
                        : null
        );
        response.setTaxAmount(taxAmount);
        response.setPlayerMoney(gamePlayer.getMoney());
        response.setNetWorth(netWorth);
        return response;
    }

    public GoToJailResponse toGoToJailResponse(
            GamePlayer gamePlayer,
            Integer fromPosition,
            Integer jailPosition
    ) {
        GoToJailResponse response = new GoToJailResponse();
        response.setGamePlayerId(gamePlayer.getId());
        response.setFromPosition(fromPosition);
        response.setJailPosition(jailPosition);
        response.setInJail(gamePlayer.getInJail());
        response.setJailTurn(gamePlayer.getJailTurn());
        response.setJailFreeCard(gamePlayer.getJailFreeCard());
        return response;
    }

    public JailActionResponse toJailActionResponse(
            JailActionType actionType,
            RollDiceResponse roll,
            GamePlayer gamePlayer,
            GamePlayer nextPlayer,
            Boolean moved,
            Integer oldPosition,
            Integer newPosition,
            Boolean passedStart,
            Integer startReward,
            Integer finePaid
    ) {
        JailActionResponse response = new JailActionResponse();
        response.setActionType(actionType);
        response.setDice1(roll.getDice1());
        response.setDice2(roll.getDice2());
        response.setTotal(roll.getTotal());
        response.setIsDouble(roll.getIsDouble());
        response.setMoved(moved);
        response.setOldPosition(oldPosition);
        response.setNewPosition(newPosition);
        response.setPassedStart(passedStart);
        response.setStartReward(startReward);
        response.setCurrentPlayerMoney(gamePlayer.getMoney());
        response.setCurrentPlayerId(gamePlayer.getId());
        response.setNextPlayerId(nextPlayer.getId());
        response.setInJail(gamePlayer.getInJail());
        response.setJailTurn(gamePlayer.getJailTurn());
        response.setJailFreeCard(gamePlayer.getJailFreeCard());
        response.setFinePaid(finePaid);
        return response;
    }
}
