package com.treasure.hunt.game.mods.insecurehints;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.seeker.Seeker;
import org.locationtech.jts.geom.Point;

/**
 * Nothing to implement here, BUT
 * you should ensure, your {@link Seeker} can handle with insecure hints.
 */
public interface InsecureSeeker extends Seeker {

    /**
     * Use this to initialize your Seeker.
     *
     * @param startPosition the prosition, the seeker starts on
     * @param insecurity    the probability, the hint of the {@link InsecureTipster} is correct.
     * @param gameHistory   the {@link com.treasure.hunt.game.GameManager}, the list of {@link com.treasure.hunt.strategy.geom.GeometryItem}
     *                      will be dumped in.
     */
    // TODO not sure, whether the seeker may know this
    void init(Point startPosition, double insecurity, GameHistory gameHistory);
}
