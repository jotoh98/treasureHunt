package com.treasure.hunt.view.main;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.utils.SwingUtils;
import com.treasure.hunt.view.in_game.impl.CanvasView;
import com.treasure.hunt.view.swing.CanvasMouseListener;
import lombok.Getter;
import org.locationtech.jts.math.Vector2D;

import javax.swing.*;
import java.awt.*;

import static javax.swing.BorderFactory.createEmptyBorder;

public class CanvasController extends JFrame {

    @Getter
    private final CanvasView canvasView;
    private final GameManager gameManager;

    @Getter
    private Vector2D offset = new Vector2D();

    @Getter
    private double scale = 1.0;

    CanvasController(CanvasView canvasView, GameManager gameManager) {
        this.canvasView = canvasView;
        this.gameManager = gameManager;
        CanvasMouseListener canvasMouseListener = new CanvasMouseListener(this);
        canvasView.addMouseMotionListener(canvasMouseListener);
        canvasView.addMouseListener(canvasMouseListener);
        canvasView.addMouseWheelListener(canvasMouseListener);
        setVisible(true);
        setSize(1080, 600);
        initCanvasWrapper(canvasView);
    }

    private void initCanvasWrapper(CanvasView canvasView) {
        JPanel borderPanel = new JPanel();
        JPanel bottomControlPanel = new JPanel();
        borderPanel.setLayout(new BorderLayout());
        bottomControlPanel.setLayout(new BoxLayout(bottomControlPanel, BoxLayout.X_AXIS));
        setContentPane(borderPanel);
        borderPanel.add(canvasView, BorderLayout.CENTER);
        borderPanel.add(bottomControlPanel, BorderLayout.SOUTH);
        bottomControlPanel.setBackground(Color.gray);
        bottomControlPanel.setBorder(createEmptyBorder(10, 10, 10, 10));

        Button prevButton = new Button();
        Button nextButton = new Button();
        prevButton.setLabel("Previous");
        prevButton.addActionListener(e -> {
            gameManager.previous();
            if (gameManager.isFirstStepShown()) {
                prevButton.setEnabled(false);
            }
            nextButton.setEnabled(true);
        });
        bottomControlPanel.add(prevButton);

        bottomControlPanel.add(new Box.Filler(new Dimension(2, 0), new Dimension(2, 0), new Dimension(2, 0)));

        nextButton.setLabel("Next");
        nextButton.addActionListener(e -> {
            gameManager.next();
            if (gameManager.isGameFinished() && gameManager.isSimStepLatest()) {
                nextButton.setEnabled(false);
                SwingUtils.infoPopUp("The game ended", "Info");
            }
            prevButton.setEnabled(true);
        });
        bottomControlPanel.add(nextButton);

        bottomControlPanel.add(new Box.Filler(new Dimension(0, 0), new Dimension(Integer.MAX_VALUE, 0), new Dimension(Integer.MAX_VALUE, 0)));
    }

    public void setOffset(Vector2D vector2D) {
        offset = vector2D;
        canvasView.getPointTransformation().setOffset(offset);
    }

    public void addToScale(double addend) {
        this.scale += addend;
        this.getCanvasView().getPointTransformation().setScale(this.scale);
    }
}
