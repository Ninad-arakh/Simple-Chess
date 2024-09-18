package piece;

import main.GamePanel;

public class Pawn extends Piece {

	public Pawn(int col, int row, int color) {
		super(col, row, color);
		
		if(color == GamePanel.WHITE) {
			image = getImage("/piece/white-pawn");
		}
		else {
			image = getImage("/piece/black-pawn");
		}
	}

}
