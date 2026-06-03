package common;

import java.io.Serializable;

//NE PAS OUBLIER SERIALIZABLE SINON KAPUT LE RMI
public class Point implements Serializable {
    public int x;
    public int y;

    public Point(int xPos, int yPos) {
        x = xPos;
        y = yPos;
    }
}
