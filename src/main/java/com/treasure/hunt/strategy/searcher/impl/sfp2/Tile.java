package com.treasure.hunt.strategy.searcher.impl.sfp2;

import com.treasure.hunt.geom.GeometryAngle;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.utils.JTSUtils;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

class Tile {
    enum Color {black, white};

    /*Upper left corner of the tile*/
    @Getter
    private Coordinate location;
    @Getter
    private int size;
    @Getter
    private Color color;

    /* The 4 subtiles of the tile, with
    subtiles[0] being in the upper left position,
    subtiles[1] being in the upper right position,
    subtiles[2] being in the lower left position,
    subtiles[3] being in the lower right position.
    If the tile is tiled no further, subtiles is null.
     */
    Tile[] subtiles;

    
    Tile(Coordinate location, int size, Movement nextMoves){
        this.location = location;
        this.size = size;
        this.color = Color.white;
        subtiles = null;
        drawTile(nextMoves);
    }

    private void drawTile(Movement nextMoves){
        LinearRing ring = new LinearRing(new CoordinateArraySequence(
                new Coordinate[]{getA(), getB(), getC(), getD(), getA()}),
                JTSUtils.GEOMETRY_FACTORY);

        GeometryType type = this.color == Color.white ? GeometryType.WHITE_TILE : GeometryType.BLACK_TILE;

        nextMoves.addAdditionalItem(new GeometryItem<>(
                new Polygon(ring, null, JTSUtils.GEOMETRY_FACTORY),
                type));
    }


    void drawBlack(Movement nextMoves){
        this.color = Color.black;
        if (subtiles != null){
            subtiles = null;
        }
        drawTile(nextMoves);
    }

    /* Recursively creating the Tiling
     * As in the paper, a Tiling is the partition of a Tile with side length x into 4^i tiles, each side length x/(2^i)*/
    void createTiling(int i, Movement nextMoves){
        if (i == 0)
        {
            if (subtiles != null){
                if (subtiles[0].getColor() == Color.black &&
                        subtiles[1].getColor() == Color.black &&
                        subtiles[2].getColor() == Color.black &&
                        subtiles[3].getColor() == Color.black){
                    this.drawBlack(nextMoves);
                }
                subtiles = null;
            }
            return;
        }

        if (size == 1){
            //System.out.println("Maximally tiled but Algorithm would want an even finer tiling, which makes no sense.");
            return;
        }

        if (subtiles == null){
            subtiles = new Tile[4];
            subtiles[0] = new Tile(this.getLocation(), size/2, nextMoves);
            subtiles[1] = new Tile(new Coordinate(this.getLocation().getX() + size/2., this.getLocation().getY()), size/2, nextMoves);
            subtiles[2] = new Tile(new Coordinate(this.getLocation().getX(), this.getLocation().getY() - size/2.), size/2, nextMoves);
            subtiles[3] = new Tile(new Coordinate(this.getLocation().getX() + size/2., this.getLocation().getY() - size/2.), size/2, nextMoves);
        }
        for (int j = 0; j < 4; j++){
            subtiles[j].createTiling(i-1, nextMoves);
        }
    }

    /*Upper left corner of the tile*/
    public Coordinate getA(){
        return location;
    }
    
    /*Upper right corner of the tile*/
    public Coordinate getB(){
        return new Coordinate(location.getX() + size, location.getY());
    }
    
    /*lower right corner of the tile*/
    public Coordinate getC(){
        return new Coordinate(location.getX() + size, location.getY() - size);
    }
    
    /*lower left corner of the tile*/
    public Coordinate getD(){
        return new Coordinate(location.getX(), location.getY() - size);
    }
    public Coordinate getCenter(){
        return new Coordinate(location.getX() + size/2., location.getY() - size/2.);
    }

    boolean isInsideOfAngle(GeometryAngle geometryAngle){
        if (!JTSUtils.pointInAngle(geometryAngle, getA())
                || !JTSUtils.pointInAngle(geometryAngle, getB())
                || !JTSUtils.pointInAngle(geometryAngle, getC())
                || !JTSUtils.pointInAngle(geometryAngle, getD()))
            return false;

        return true;
    }
}
