public class Calculate {

    public boolean isWin(char[][] field, int lastRow, int lastCol) {

        char player = field[lastRow][lastCol];

        int size = field.length;
        int uBound = size - 1;
        boolean win = false;

        if (lastRow == lastCol) {
            for (int d = 0; d < size; d++) {
                if (field[d][d] != player) break;
                win = (d == uBound);
            }
            if (win) return true;
        }

        if (lastRow == uBound - lastCol) {
            for (int d = 0; d < size; d++) {
                if (field[d][uBound - d] != player) break;
                win = (d == uBound);
            }
            if (win) return true;
        }

        for (int l = 0; l < size; l++) {
            if (field[l][lastCol] != player) break;
            win = (l == uBound);
        }
        if (win) return true;

        for (int l = 0; l < size; l++) {
            if (field[lastRow][l] != player) break;
            win = (l == uBound);
        }
        return win;
    }
}
