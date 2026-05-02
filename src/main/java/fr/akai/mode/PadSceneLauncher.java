package fr.akai.mode;

import fr.akai.MPK249Extension;
import fr.akai.common.MPK2Common;
import fr.akai.common.MPK2Common.PadColor;
import fr.akai.common.MPK2Hardware;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.SceneBank;
import com.bitwig.extension.controller.api.Scene;

public class PadSceneLauncher extends PadMode {
    private final SceneBank sceneBank;
    private final ControllerHost host;
    private final MPK249Extension extension;
    private final com.bitwig.extension.controller.api.HardwareActionBindable[] sceneActions = new com.bitwig.extension.controller.api.HardwareActionBindable[64];

    public PadSceneLauncher(MPK2Common common, MPK249Extension extension, SceneBank sceneBank, ControllerHost host) {
        super(common, host);
        this.extension = extension;
        this.sceneBank = sceneBank;
        this.host = host;

        // --- PHASE INITIALISATION (Obligatoire ici !) ---
        for (int i = 0; i < 64; i++) {
            Scene scene = sceneBank.getItemAt(i);
            // On pré-crée l'action ICI
            sceneActions[i] = scene.launchAction();

            scene.exists().markInterested();
            scene.color().markInterested(); // Indispensable pour lire la couleur

            final int padIdx = i;

            // Observateur d'existence
            scene.exists().addValueObserver(exists -> updateSceneLED(padIdx));

            // Observateur de couleur
            scene.color().addValueObserver((r, g, b) -> updateSceneLED(padIdx));
        }
    }

    @Override
    public void init() {
        updateAllSceneLEDs();
    }

    public void initBindings(MPK2Hardware hardware) {
        for (int i = 0; i < 64; i++) {
            hardware.getPad(i).pressedAction().setBinding(sceneActions[i]);
        }
    }

    public void updateAllSceneLEDs() {
        for (int i = 0; i < 64; i++) {
            updateSceneLED(i);
        }
    }

    private void updateSceneLED(int index) {
        Scene scene = sceneBank.getItemAt(index);

        if (scene.exists().get()) {

            float r = scene.color().red();
            float g = scene.color().green();
            float b = scene.color().blue();

            // On convertit en PadColor (gris/noir retournera PadColor.Off)
            PadColor bitwigColor = fr.akai.common.PadColorMapper.fromBitwigRGB((int)(r*255), (int)(g*255), (int)(b*255));

            // Logique : Si c'est éteint/gris (0,0,0), on met Rouge par défaut, sinon couleur Bitwig
            if (bitwigColor == PadColor.Off) {
                lightPad(index, PadColor.Red);
            } else {
                lightPad(index, bitwigColor);
            }
        } else {
            lightPad(index, PadColor.Off);
        }
    }
}