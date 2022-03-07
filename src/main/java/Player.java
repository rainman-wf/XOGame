public class Player {
    private final String name;
    private final char side;
    private String gameID;
    private final long playerIdForRemove;

    public Player(String name, long playerIdForRemove, char side) {
        this.name = name;
        this.side = side;
        this.playerIdForRemove = playerIdForRemove;
    }

    public char getSide() {
        return side;
    }

    public String getGameID() {
        return gameID;
    }

    public long getPlayerIdForRemove() {
        return playerIdForRemove;
    }

    public String getName() {
        return name;
    }

    public void setGameID(String gameID) {
        this.gameID = gameID;
    }
}
