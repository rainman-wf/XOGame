import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.ChosenInlineResult;
import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.InlineQueryResultArticle;
import com.pengrad.telegrambot.request.AnswerInlineQuery;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class XOGame {

    private final TelegramBot bot = new TelegramBot("5220200952:AAHZWYPCEDx44idXsgPhZl6KNXQrQyr7EcY");

    public Map<Long, Player> playerMap = new HashMap<>();
    public Map<String, Game> gameMap = new HashMap<>();

    public void listen() {

        this.bot.setUpdatesListener(updates -> {
            updates.forEach(this::process);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    @SuppressWarnings("unchecked")
    private void process(@NotNull Update update) {

        CallbackQuery callbackQuery = update.callbackQuery();
        InlineQuery inlineQuery = update.inlineQuery();
        ChosenInlineResult chosenInlineResult = update.chosenInlineResult();

        BaseRequest request = null;

        if (inlineQuery != null) {
            boolean isPlayerInGame = playerMap.containsKey(inlineQuery.from().id()); // если игрок в игре
            if (!isPlayerInGame) {
                request = answerInlineQuery(inlineQuery).cacheTime(1);
            } else {
                System.out.println("Player " + inlineQuery.from().id() + " in game");
            }
        }

        if (chosenInlineResult != null) { // если пришло приглашение в игру

            String senderName = chosenInlineResult.from().firstName(); // имя отправителя
            String gameId = chosenInlineResult.inlineMessageId(); // айдишник игры
            long senderId = chosenInlineResult.from().id(); // айдишник отправителя

            gameMap.put(gameId, new Game()); // кладем игру новую в таблицу игр
            playerMap.put(senderId, new Player(senderName, senderId,'X')); // кладем отправителя в таблицу игроков

            gameMap.get(gameId).setSender(playerMap.get(senderId)); // добавляем отправителя в игру
            playerMap.get(senderId).setGameID(gameId); // добавляем айдишник игры отправителю

            request = new EditMessageReplyMarkup(gameId).replyMarkup(acceptBtn("accept " + gameId));
        }

        if (callbackQuery != null) {

            String data = callbackQuery.data();

            if (data.startsWith("accept")) { // если оппонент принял игру

                String gameId = data.split(" ")[1]; // получили айди игры
                long opponentID = callbackQuery.from().id(); // получили айди оппонента
                String opponentName = callbackQuery.from().firstName(); // получили имя оппонента

                Game game = gameMap.get(gameId); // игра
                Player sender = game.getSender(); // отправитель

                if (!playerMap.containsKey(callbackQuery.from().id())) { // если оппонента нет в словаре
                    Player opponent = new Player(opponentName, opponentID,'0'); // создали оппонента

                    playerMap.put(opponentID, opponent); // положили оппонента в карту игроков

                    game.setOpponent(opponent); // добавили оппонента в игру
                    opponent.setGameID(gameId); // добавили айдишник игры оппоненту

                    game.setCurrentPlayer(sender); // указали текущего играка в игре

                    char[][] charMatrix = game.getCharsMatrix(); // получили пустую матрицу из игры

                    request = new EditMessageText(gameId, sender.getName() + " vs " + opponentName)
                            .replyMarkup(new InlineKeyboardMarkup(buttonsField(charMatrix)));
                } else {
                    System.out.println(playerMap.remove(sender.getPlayerIdForRemove()));
                    gameMap.remove(gameId);
                    request = new EditMessageText(gameId, "Canceled");
                }
            } else {

                if (playerMap.containsKey(callbackQuery.from().id())) {

                    Player currentPlayer = playerMap.get(callbackQuery.from().id());
                    Game game = gameMap.get(currentPlayer.getGameID());
                    String gameID = currentPlayer.getGameID();

                    if (currentPlayer == game.getCurrentPlayer()) {
                        String[] point = data.split(" ");
                        int x = Integer.parseInt(point[0]);
                        int y = Integer.parseInt(point[1]);

                        if (game.getCharsMatrix()[x][y] == 0) {
                            if (!game.setPoint(x, y)) {
                                if (game.getSteps() == 9) {
                                    request = gameOverResult(game, gameID, "Nobody wins!");
                                } else  {
                                    request = new EditMessageReplyMarkup(gameID)
                                            .replyMarkup(new InlineKeyboardMarkup(buttonsField(game.getCharsMatrix())));
                                }
                            } else {
                                request = gameOverResult(game, gameID, currentPlayer.getName() + " is Winner");
                            }
                        } else {
                            System.out.println("ячейка занята");
                        }
                    }
                }
            }
        }
        if (request != null) bot.execute(request);
    }

    private EditMessageText gameOverResult(Game game, String gameID, String msg) {
        gameMap.remove(gameID);
        playerMap.remove(game.getSender().getPlayerIdForRemove());
        playerMap.remove(game.getOpponent().getPlayerIdForRemove());
        return new EditMessageText(gameID, msg)
                .replyMarkup(new InlineKeyboardMarkup(buttonsField(game.getCharsMatrix())));

    }

    @NotNull
    @Contract("_ -> new")
    private AnswerInlineQuery answerInlineQuery(@NotNull InlineQuery inlineQuery) {
        return new AnswerInlineQuery(inlineQuery.id(), resultArticle(inlineQuery));
    }

    private InlineQueryResultArticle resultArticle(@NotNull InlineQuery inlineQuery) {

        String imageUrl = "https://e7.pngegg.com/pngimages/50/629/png-" +
                "clipart-challenge-your-friends-2player-tic-tac-toe-best-tic-tac-toe-tic" +
                "-tac-toe-games-android-game-angle.png";
        return new InlineQueryResultArticle(
                "xoGame",
                "\uD83D\uDD79 Start the Game",
                "Game started by " + inlineQuery.from().firstName())
                .description("Developed by RainMan")
                .thumbUrl(imageUrl)
                .replyMarkup(acceptBtn(" "));
    }

    @NotNull
    @Contract("_ -> new")
    private InlineKeyboardMarkup acceptBtn(String callbackData) {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton("Accept the challenge")
                        .callbackData(callbackData));
    }

    @NotNull
    private InlineKeyboardButton[][] buttonsField(@NotNull char[][] charMatrix) {
        InlineKeyboardButton[][] field = new InlineKeyboardButton[charMatrix.length][charMatrix.length];
        for (int i = 0; i < charMatrix.length; i++) {
            for (int j = 0; j < charMatrix.length; j++) {
                field[i][j] = new InlineKeyboardButton(Character.toString(charMatrix[i][j])).callbackData(i + " " + j);
            }
        }
        return field;
    }
}





