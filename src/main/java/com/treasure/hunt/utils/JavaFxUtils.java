package com.treasure.hunt.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.io.File;


@Slf4j
public class JavaFxUtils {

    @NotNull
    public static String getVersionString() {
        String implementationVersion = JavaFxUtils.class.getPackage().getImplementationVersion();
        return implementationVersion == null ? "snapshot" : "v" + implementationVersion;
    }

    public static void savePngFromStage(Window stage) {
        WritableImage snapshot = stage.getScene().snapshot(null);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("plot.png");
        File file = fileChooser.showSaveDialog(stage);

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
        } catch (Exception e) {
            log.error("Something went wrong when creating a png file (of the screenshot) for plot!", e);
            return;
        }
    }
}
