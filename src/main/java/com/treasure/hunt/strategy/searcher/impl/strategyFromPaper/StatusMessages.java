package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.jts.geom.Line;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.geom.StatusMessageItem;
import com.treasure.hunt.strategy.geom.StatusMessageType;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;

/**
 * @author Rank
 */
public class StatusMessages {
    static StatusMessageItem visualisationMessage = new StatusMessageItem(StatusMessageType.EXPLANATION_VISUALISATION_SEARCHER,
            "The previous hint is visualized by a red area which covers the area the treasure is not in.\n" +
                    "The hint before the previous hint is visualized by a lighter red area which covers the area the treasure is not in.\n" +
                    "\n" +
                    "The phase rectangle is visualized in a dark green color.\n" +
                    "The current rectangle is visualized in a lighter green color.\n" +
                    "\n" +
                    "When L1'' is needed (it will be explained when it is), this line is visualized in blue.");

    static StatusMessageItem explainingStrategyMessage = new StatusMessageItem(StatusMessageType.EXPLANATION_STRATEGY,
            "This strategy implements the strategy from the paper \"Deterministic Treasure Hunt in the Plane with Angular Hints\"\n" +
                    "from Bouchard et al..\n" +
                    "\n" +
                    "Generally this strategy works in phases i=1, 2, ... in which it searchers the treasure in rectangles of the side \n" +
                    "length 2^i.\n" +
                    "The rectangles are centered in the start position of the searcher and are axis parallel.\n" +
                    "We will call the rectangle of the current phase the phase rectangle.\n" +
                    "The strategy uses a second rectangle, called the current rectangle, which equals the phase rectangle at the beginning\n" +
                    "of each phase. \n" +
                    "In some draws (most draws) the searcher can exclude a part of the current rectangle by using areas seen and the current hint\n" +
                    "gotten and lower its area.\n" +
                    "The previous rectangle is the current rectangle from the previous draw.\n" +
                    "When the current rectangle is small enough, the rectangle gets scanned which means the player walks a route in such a way \n" +
                    "that it sees all points of the current rectangle. \n" +
                    "When this happens the phase is incremented and the current rectangle is again set to the phase rectangle.\n" +
                    "\n" +
                    "For more information please look in the paper."
    );

    static void addCaseDescriptionToStatus(SearchPath move, int basicTransformation, int caseIndex,
                                           LastHintBadSubroutine lastHintBadSubroutine) {
        String[] transformedRectangle = getLettersOfTransformedRectangle(basicTransformation);
        String statusMessage = "Let ABCD be the previous rectangle's corners, with\n" +
                "A being the corner on the top left,\n" +
                "B being the corner on the top right,\n" +
                "C being the corner on the bottom right and\n" +
                "D being the corner on the bottom left.\n" +
                "\n" +
                "Let H1 be the current hint (which was not yet analyzed)\n" +
                "Let H2 be the hint before H1\n" +
                "Let H3 be the hint before H2 (the bad hint due to which the player went 2 Steps orthogonal to the " +
                "hintline)\n\n";

        // the defining Strings for the variables:
        String L1DoubleAposDef = "Let L1'' be the line parallel to the line of H3, and going through the point where " +
                "the player was when he received H2.\n" +
                "This line gets visualized in blue.\n" +
                "(The name is taken out of the paper)\n";

        String pDef = "Let p be the point where H3 was received.\n";
        String pAposDef = "Let p' be the point where H2 was received.\n";

        String mDef = "Let m be the orthogonal projection of p onto segment " + transformedRectangle[0] +
                transformedRectangle[3] + "\n";
        String kDef = "Let k be the orthogonal projection of p oonto segment " + transformedRectangle[1] +
                transformedRectangle[2] + "\n";
        String mAposDef = "Let m' be the orthogonal projection of p' onto segment " + transformedRectangle[0] +
                transformedRectangle[3] + "\n";
        String kAposDef = "Let m' be the orthogonal projection of p' onto segment " + transformedRectangle[0] +
                transformedRectangle[3] + "\n";

        String gDef = "Let g be the orthogonal projection of p onto segment " + transformedRectangle[0] +
                transformedRectangle[1] + ".\n";
        String hDef = "Let h be the orthogonal projection of p onto segment " + transformedRectangle[3] +
                transformedRectangle[2] + ".\n";
        String gAposDef = "Let g' be the orthogonal projection of p' onto segment " + transformedRectangle[0] +
                transformedRectangle[1] + "\n";
        String hAposDef = "Let h' be the orthogonal projection of p' onto segment " + transformedRectangle[3] +
                transformedRectangle[2] + ".\n";

        String sDef = "Let s be the orthogonal projection of " + transformedRectangle[0] + " onto the hintline of H3.\n";
        String sAposDef = "Let s' be the orthogonal projection of " + transformedRectangle[0] + " onto L1''.\n";
        String dDef = "Let d be the intersection between the hintline of H3 and " + transformedRectangle[1] +
                transformedRectangle[2] + ".\n";
        String dAposDef = "Let d' be the orthogonal projection of d onto line L1''.\n";

        String jDef = "Let j be the intersection of L1'' and segment  " + transformedRectangle[1] +
                transformedRectangle[2] + ".\n";
        String jAposDef = "Let j' be the orthogonal projeciton of j onto segment " + transformedRectangle[0] +
                transformedRectangle[3] + ".\n";

        String fDef = "Let f be the intersection of L1'' and the segment " + transformedRectangle[0]
                + transformedRectangle[1] + ".\n";
        String tDef = "Let t be the orthogonal projection of f on the opposite side of the previous rectangle.\n";

        // now the rectangles that are scanned, the rectangle the search rectangle is reduced to and the
        // definitions of the used points get joint so that one gets a coherent status message
        switch (caseIndex) {
            case 1:
                addL1DoubleApos(move, lastHintBadSubroutine);
                statusMessage = statusMessage.concat(L1DoubleAposDef + fDef + tDef +
                        "\n The search rectangle is reduced to f" + transformedRectangle[1] + transformedRectangle[2]
                        + "t.");
                break;
            case 2:
                statusMessage = statusMessage.concat(pDef + pAposDef + "\n" + mDef + kDef + mAposDef + kAposDef + "\n"
                        + gDef + hDef + "\n The rectangle m'k'km gets scanned and the search rectangle "
                        + "is reduced to g" + transformedRectangle[1] + transformedRectangle[2] + "h.");
                break;
            case 3:
                addL1DoubleApos(move, lastHintBadSubroutine);
                statusMessage = statusMessage.concat(L1DoubleAposDef + "\n" + sDef + sAposDef + dDef + dAposDef + "\n"
                        + pDef + "\n" + mDef + kDef + mAposDef + kAposDef + hDef + "\n The rectangles ss'd'd and m'k'km"
                        + " get scanned and the search rectangle is reduced to pk" + transformedRectangle[2] + "h."
                );
                break;
            case 4:
                addL1DoubleApos(move, lastHintBadSubroutine);
                statusMessage = statusMessage.concat(L1DoubleAposDef + "\n" + sDef + sAposDef + dDef + dAposDef + "\n"
                        + pDef + "\n" + gDef + hDef + gAposDef + hAposDef + "\n" + mDef
                        + "\n The rectangles ss'd'd and gg'h'h get scanned and the search rectangle is reduced to "
                        + transformedRectangle[0] + "gpm.");
                break;
            case 5:
                statusMessage = statusMessage.concat(pDef + "\n" + gDef + hDef + gAposDef + hAposDef + "\n" + mDef +
                        kDef + "\n The rectangle gg'h'h gets scanned and the search rectangle is reduced to " +
                        transformedRectangle[0] + transformedRectangle[1] + "km.");
                break;
            case 6:
                addL1DoubleApos(move, lastHintBadSubroutine);
                statusMessage = statusMessage.concat(L1DoubleAposDef + "\n" + jDef + jAposDef +
                        "\n The search rectangle is reduced to " + transformedRectangle[0] + transformedRectangle[1] +
                        "jj'.");
                break;
            default:
                throw new IllegalArgumentException("caseIndex must be in [1,...,6] but equals " + caseIndex);
        }
        StatusMessageItem explanation = new StatusMessageItem(StatusMessageType.EXPLANATION_MOVEMENT, statusMessage);
        move.getStatusMessageItemsToBeAdded().add(explanation);
        lastHintBadSubroutine.getStrategy().statusMessageItemsToBeRemovedNextMove.add(explanation);
    }

    private static void addL1DoubleApos(SearchPath move, LastHintBadSubroutine lastHintBadSubroutine) {
        move.addAdditionalItem(
                new GeometryItem<>(new Line(
                        RoutinesFromPaper.phiPointInverse(lastHintBadSubroutine.getBasicTransformation(),
                                lastHintBadSubroutine.getCurrentRectangle(), lastHintBadSubroutine.getL1DoubleApos().p0),
                        RoutinesFromPaper.phiPointInverse(lastHintBadSubroutine.getBasicTransformation(),
                                lastHintBadSubroutine.getCurrentRectangle(), lastHintBadSubroutine.getL1DoubleApos().p1)
                ), GeometryType.L1_DOUBLE_APOS));
        lastHintBadSubroutine.getStrategy().geometryItemsToBeAddedNextMove.add(new GeometryItem<>(JTSUtils.GEOMETRY_FACTORY.createPolygon(),
                GeometryType.L1_DOUBLE_APOS));
    }

    private static String[] getLettersOfTransformedRectangle(int basicTransformation) {
        /**
         * The rectangle got transformed by phi (with basicTransformation as index)
         * The old rectangle gets called ABCD in the following, with A being the corner-point on the top left,
         * B being the point on the top right, etc.
         * In the following transformedRectangle[0] is the top left corner from the transformed rectangle
         * (this could be A,B,C or D dependant on the basicTransformation's value)
         * transformedRectangle[0] is the top left corner,
         * transformedRectangle[1] is the top right corner,
         * transformedRectangle[2] is the bottom right corner and
         * transformedRectangle[3] is the bottom left corner.
         */
        String transformedRectangleString;
        String[] transformedRectangle;
        switch (basicTransformation) {
            case 0:
                transformedRectangleString = "ABCD";
                break;
            case 1:
                transformedRectangleString = "BCDA";//"DABC";
                break;
            case 2:
                transformedRectangleString = "CDAB";
                break;
            case 3:
                transformedRectangleString = "DABC";//"BCDA";
                break;
            case 4:
                transformedRectangleString = "BADC";
                break;
            case 5:
                transformedRectangleString = "CBAD";//"ADCB";
                break;
            case 6:
                transformedRectangleString = "DCBA";
                break;
            case 7:
                transformedRectangleString = "ADCB";
                break;
            default:
                throw new AssertionError("basicTransformation should be in {0 ,1 ,2 ,3 ,4 ,5 ,6 ,7} " +
                        "but equals " + basicTransformation);
        }
        transformedRectangle = transformedRectangleString.split("");
        return transformedRectangle;
    }
}
