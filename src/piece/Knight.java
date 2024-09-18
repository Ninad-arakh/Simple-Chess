package piece;

import main.GamePanel;

public class Knight extends Piece {

	public Knight(int col, int row, int color) {
		super(col, row, color);
		
		if(color == GamePanel.WHITE) {
			image = getImage("/piece/white-nightrd");
		}
		else {
			image = getImage("/piece/black-nightrd");
		}
	}

}
